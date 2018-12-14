package com.vaadin.wolfgang.urlparameters.impl;

import com.vaadin.wolfgang.urlparameters.Converter;
import com.vaadin.wolfgang.urlparameters.ViewParameter;
import com.vaadin.wolfgang.urlparameters.ViewStateParameter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;

@Slf4j
public class PropertybasedViewStateParameter<T> extends ViewStateParameter<T> {
	private PropertyDescriptor propertyDescriptor;

	public PropertybasedViewStateParameter(@NonNull String viewName, @NonNull ViewParameter viewParameter, @NonNull PropertyDescriptor propertyDescriptor, @NonNull Converter converter) {
		super(viewName, viewParameter, converter);
		this.propertyDescriptor = propertyDescriptor;
	}

	@Override
	public Class<T> getParameterType() {
		return (Class<T>) propertyDescriptor.getPropertyType();
	}

	@Override
	public boolean setValue(T value, Object valueBean, boolean notify) {
		boolean changed = super.setValue(value, valueBean, notify);
		if (changed || notify) {
			Method writeMethod = propertyDescriptor.getWriteMethod();
			try {
				if (writeMethod != null) {
					writeMethod.invoke(valueBean, value);
				}
			} catch (IllegalAccessException e) {
				throw new RuntimeException(e);
			} catch (InvocationTargetException e) {
				throw new RuntimeException(e.getTargetException());
			}
		}
		return changed;
	}

	@Override
	public T getValue(Object valueBean) {
		Method readMethod = propertyDescriptor.getReadMethod();
		try {
			if (readMethod != null) {
				T result = (T) readMethod.invoke(valueBean);
				if (!Objects.equals(result, super.getValue(valueBean))) {
					setValue(result, valueBean, false);
				}
				return result;
			} else {
				return super.getValue(valueBean);
			}
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e.getTargetException());
		}
	}
}
