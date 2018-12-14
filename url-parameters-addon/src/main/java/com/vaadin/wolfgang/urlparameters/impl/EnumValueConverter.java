package com.vaadin.wolfgang.urlparameters.impl;

import com.vaadin.wolfgang.urlparameters.Converter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

@Slf4j
public class EnumValueConverter<T extends Enum<T>> implements Converter<T> {
	private Class<T> enumeratorClass;

	public EnumValueConverter(Class<T> enumeratorClass) {
		this.enumeratorClass = enumeratorClass;
	}

	@Override
	public Class<T> getInternalClass() {
		return enumeratorClass;
	}

	@Override
	public T getInternalObject(String stringRepresentation) {
		if (StringUtils.isEmpty(stringRepresentation)) {
			return null;
		}
		return Enum.valueOf(enumeratorClass, stringRepresentation);
	}

	@Override
	public String getStringRepresentation(T o) {
		return o != null ? o.name() : StringUtils.EMPTY;
	}
}
