package com.vaadin.wolfgang.urlparameters;

import lombok.Getter;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractParameterFactory implements ParameterFactory {
	@Getter
	private MultiValuedMap<String, ViewStateParameterFactory.ViewParameterDefinition> viewParameterDefinitionMap = new ArrayListValuedHashMap<>();
	@Getter
	private Map<Type, Converter> converterByValueClass = new HashMap();
	private final Map<String, Converter> converters = new HashMap<>();

	@Override
	public Map<String, Converter> getConverters() {
		return new HashMap<>(converters);
	}

	protected void registerConverters(Map<String, Converter> toRegister) {
		converters.putAll(toRegister);
	}

	public void initialise() {
		for (Map.Entry<String, Converter> converterEntry : getConverters().entrySet()) {
			Converter converter = converterEntry.getValue();
			Type converterInternalClass = converter.getInternalClass();
			Converter replacedConverter = converterByValueClass.put(converterInternalClass, converter);
			if (replacedConverter != null) {
				throw new IllegalStateException("You have more than one converter configured for class " + converterInternalClass + ": " + replacedConverter + ", and " + converterEntry.getValue() + " Bean name: " + converterEntry.getKey());
			}
		}
		for (Map.Entry<String, ViewParameters> multi : getMultiParameters().entrySet()) {
			String beanName = multi.getKey();
			String viewName = getViewName(beanName);
			if (viewName != null) {
				ViewParameters viewParameters = multi.getValue();
				for (ViewParameter viewParameter : viewParameters.value()) {
					viewParameterDefinitionMap.put(viewName, new ViewStateParameterFactory.ViewParameterDefinition(viewParameter, beanName));
				}
			}
		}
		for (Map.Entry<String, ViewParameter> single : getSingleParameters().entrySet()) {
			String beanName = single.getKey();
			String viewName = getViewName(beanName);
			if (viewName != null) {
				ViewParameter annotation = single.getValue();
				viewParameterDefinitionMap.put(viewName, new ViewStateParameterFactory.ViewParameterDefinition(annotation, beanName));
			}
		}
		for (Map.Entry<String, MethodDescriptor> entry : getViewMethodDescriptors().entries()) {
			String beanName = entry.getKey();
			String viewName = getViewName(beanName);
			if (viewName != null) {
				MethodDescriptor methodDescriptor = entry.getValue();
				ViewParameter viewParameter = new ViewParameter() {

					@Override
					public Class<? extends Annotation> annotationType() {
						return ViewParameter.class;
					}

					@Override
					public String value() {
						return methodDescriptor.getName();
					}

					@Override
					public String defaultValue() {
						return methodDescriptor.getParameter().defaultValue();
					}

					@Override
					public Class<?> valueClass() {
						return methodDescriptor.getParameterType();
					}

					@Override
					public Class<? extends Converter> convertedBy() {
						return methodDescriptor.getParameter().convertedBy();
					}
				};
				viewParameterDefinitionMap.put(viewName, new ViewStateParameterFactory.ViewParameterDefinition(viewParameter, beanName));
			}
		}

	}

}
