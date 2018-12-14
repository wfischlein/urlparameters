package com.vaadin.wolfgang.demo;

import com.vaadin.wolfgang.demo.data.ValueObject;
import com.vaadin.wolfgang.urlparameters.Converter;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Objects;

public class ValueConverter implements Converter<ValueObject> {
	private final List<ValueObject> values;

	public ValueConverter(List<ValueObject> values) {
		this.values = values;
	}

	@Override
	public Type getInternalClass() {
		return ValueObject.class;
	}

	@Override
	public ValueObject getInternalObject(String stringRepresentation) {
		return values.stream().filter(valueObject -> Objects.equals(valueObject.getId().toString(), stringRepresentation)).findFirst().orElse(null);
	}

	@Override
	public String getStringRepresentation(ValueObject o) {
		return o.getId().toString();
	}
}
