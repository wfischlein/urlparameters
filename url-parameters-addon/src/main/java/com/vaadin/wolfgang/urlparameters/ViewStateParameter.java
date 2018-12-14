package com.vaadin.wolfgang.urlparameters;

import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Type;
import java.util.Objects;

@ToString
@Slf4j
public abstract class ViewStateParameter<T> {
	@Getter
	private String viewName;
	@Getter
	private ViewParameter viewParameter;
	@Getter
	private Converter<T> converter;
	private boolean temp;
	private T tempValue;
	private T value;

	protected ViewStateParameter(@NonNull String viewName, @NonNull ViewParameter viewParameter, @NonNull Converter<T> converter) {
		this.viewName = viewName;
		this.viewParameter = viewParameter;
		this.converter = converter;
	}

	public String getParameterName() {
		return viewParameter.value();
	}

	public String getDefaultValue() {
		return viewParameter.defaultValue();
	}

	public abstract Class<T> getParameterType();

	public boolean setValue(T newValue, Object valueBean, boolean notify) {
		boolean changed = !Objects.equals(value, newValue);
		value = newValue;
		return changed;
	}

	void setTempValue(T value, Object valueBean) {
		tempValue = value;
		temp = true;
	}

	void fireTempValue(Object valueBean) {
		if (temp) {
			try {
				setValue(tempValue, valueBean, true);
			} finally {
				temp = false;
			}
		}
	}

	void flushTempValue() {
		tempValue = null;
		temp = false;

	}
	public T getValue(Object valueBean) {
		if (temp) {
			return tempValue;
		}
		return value;
	}

	public void setStringValue(String stringValue, Object valueBean, boolean notify) {
		T value = getConverter().getInternalObject(stringValue);
		setValue(value, valueBean, notify);
	}

	public String getStringValue(Object valueBean) {
		T value = getValue(valueBean);
		if (value != null) {
			return getConverter().getStringRepresentation(value);
		} else {
			return "";
		}
	}

	public boolean convertsClass(@NonNull Type valueClass) {
		return getConverter().converts(valueClass);
	}
}
