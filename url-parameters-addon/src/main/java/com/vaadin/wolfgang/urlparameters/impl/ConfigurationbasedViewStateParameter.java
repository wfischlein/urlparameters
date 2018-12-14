package com.vaadin.wolfgang.urlparameters.impl;

import com.vaadin.wolfgang.urlparameters.Converter;
import com.vaadin.wolfgang.urlparameters.ViewParameter;
import com.vaadin.wolfgang.urlparameters.ViewStateParameter;
import lombok.Getter;
import lombok.NonNull;

public class ConfigurationbasedViewStateParameter<T> extends ViewStateParameter<T> {
	@Getter
	private Class<T> parameterType;

	public ConfigurationbasedViewStateParameter(@NonNull String viewName, @NonNull ViewParameter viewParameter, @NonNull Converter converter) {
		super(viewName, viewParameter, converter);
		this.parameterType = (Class<T>) viewParameter.valueClass();
	}

}
