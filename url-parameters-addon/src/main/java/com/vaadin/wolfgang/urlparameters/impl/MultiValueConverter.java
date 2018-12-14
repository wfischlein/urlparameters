package com.vaadin.wolfgang.urlparameters.impl;

import com.google.inject.internal.MoreTypes;
import com.vaadin.wolfgang.urlparameters.Converter;
import lombok.Getter;
import lombok.NonNull;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.StringWriter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.stream.Collectors;

public class MultiValueConverter<T, COLL extends Collection> implements Converter<Collection<T>> {
	private static final String BEGIN = "(";
	private static final String SEPARATOR = ",";
	private static final String END = ")";
	@Getter
	private Converter<T> singleValueConverter;
	private Class<COLL> collectionClass;

	public MultiValueConverter(@NonNull Converter<T> singleValueConverter, Class<COLL> collectionClass) {
		this.singleValueConverter = singleValueConverter;
		this.collectionClass = collectionClass;
	}

	@Override
	public final Type getInternalClass() {
		return new MoreTypes.ParameterizedTypeImpl(null, collectionClass, singleValueConverter.getInternalClass());
	}

	public Type getSingleValueClass() {
		return singleValueConverter.getInternalClass();
	}

	@Override
	public Collection<T> getInternalObject(String stringRepresentation) {
		Collection<T> result;
		try {
			result = collectionClass.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}

		if (StringUtils.isNotEmpty(stringRepresentation)) {
			if (stringRepresentation.startsWith(BEGIN)) {
				int endIndex = stringRepresentation.lastIndexOf(END);
				if (endIndex > 0) {
					for (String elementString : stringRepresentation.substring(1, endIndex).split(SEPARATOR)) {
						result.add(singleValueConverter.getInternalObject(elementString));
					}
				}
			} else {
				result.add(singleValueConverter.getInternalObject(stringRepresentation));
			}
		}
		return result;
	}

	@Override
	public String getStringRepresentation(Collection<T> o) {
		int size = (o != null ? o.size() : 0);
		switch (size) {
			case 0:
				return StringUtils.EMPTY;
			case 1:
				return singleValueConverter.getStringRepresentation((T) CollectionUtils.get((Object) o, 0));
			default:
				StringWriter result = new StringWriter();
				result.append(BEGIN);
				result.append(o.stream().map(t -> singleValueConverter.getStringRepresentation(t)).collect(Collectors.joining(SEPARATOR)));
				result.append(END);
				return result.toString();
		}
	}

	@Override
	public boolean converts(Type valueClass) {
		// legal as long as getInternalClass remains final and unchanged
		ParameterizedType testType = (ParameterizedType) getInternalClass();

		while (true) {
			if (valueClass.equals(testType)) {
				return true;
			}
			Type rawType = testType.getRawType();
			if (!(rawType instanceof Class)) {
				return false;
			}
			Class rawClass = (Class) rawType;
			Type interfaceType = findMatchingInterface(valueClass, testType);
			if (interfaceType != null) {
				return true;
			}
			testType = new MoreTypes.ParameterizedTypeImpl(null, rawClass.getSuperclass(), singleValueConverter.getInternalClass());
		}
	}

	private Type findMatchingInterface(Type valueClass, ParameterizedType type) {
		Type rawType = type.getRawType();
		if (!(rawType instanceof Class)) {
			return null;
		}
		for (Class inferfaceClass : ((Class) rawType).getInterfaces()) {
			ParameterizedType testType = new MoreTypes.ParameterizedTypeImpl(null, inferfaceClass, singleValueConverter.getInternalClass());
			if (valueClass.equals(testType)) {
				return testType;
			}
			Type result = findMatchingInterface(valueClass, testType);
			if (result != null) {
				return type;
			}
		}
		return null;
	}
}
