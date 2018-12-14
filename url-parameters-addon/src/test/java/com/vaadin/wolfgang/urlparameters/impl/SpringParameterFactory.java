package com.vaadin.wolfgang.urlparameters.impl;

import com.vaadin.spring.annotation.SpringView;
import com.vaadin.wolfgang.urlparameters.*;
import lombok.Getter;
import lombok.NonNull;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.beans.factory.ListableBeanFactory;

import java.util.HashMap;
import java.util.Map;

public class SpringParameterFactory extends AbstractParameterFactory {
	private final ListableBeanFactory beanFactory;

	public SpringParameterFactory(@NonNull ListableBeanFactory beanFactory) {
		this.beanFactory = beanFactory;
		registerConverters(beanFactory.getBeansOfType(Converter.class));
		initialise();
	}

	public Map<String, ViewParameters> getMultiParameters() {
		Map<String, ViewParameters> result = new HashMap<>();
		for (String beanName : beanFactory.getBeanNamesForAnnotation(ViewParameters.class)) {
			result.put(beanName, beanFactory.findAnnotationOnBean(beanName, ViewParameters.class));
		}
		return result;
	}

	public Map<String, ViewParameter> getSingleParameters() {
		Map<String, ViewParameter> result = new HashMap<>();
		for (String beanName : beanFactory.getBeanNamesForAnnotation(ViewParameter.class)) {
			result.put(beanName, beanFactory.findAnnotationOnBean(beanName, ViewParameter.class));
		}
		return result;
	}

	@Override
	public MultiValuedMap<String, MethodDescriptor> getViewMethodDescriptors() {
		throw new NotImplementedException("getViewMethodDescriptors");
	}

	public ViewStateParameterFactory.ViewDescriptor getViewDescriptor(String beanName) {
		return new SpringViewDescriptor(beanName, beanFactory);
	}

	private class SpringViewDescriptor implements ViewStateParameterFactory.ViewDescriptor {
		@Getter
		private final String name;
		@Getter
		private final Class type;

		public SpringViewDescriptor(String beanName, ListableBeanFactory beanFactory) {
			type = beanFactory.getType(beanName);
			String viewName = beanName;
			SpringView viewAnnotation = beanFactory.findAnnotationOnBean(beanName, SpringView.class);
			if (viewAnnotation != null) {
				viewName = viewAnnotation.name();
			}
			this.name = viewName;
		}
	}
}
