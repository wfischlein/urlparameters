package com.vaadin.wolfgang.urlparameters;

import com.vaadin.wolfgang.urlparameters.impl.ConfigurationbasedViewStateParameter;
import com.vaadin.wolfgang.urlparameters.impl.PropertybasedViewStateParameter;
import lombok.Getter;
import lombok.NonNull;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.lang3.StringUtils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

// instanciate an implementation of this bean as a singleton
public interface ParameterFactory {

	ViewStateParameterFactory.ViewDescriptor getViewDescriptor(String beanName);

	@NonNull
	Map<String, ViewParameters> getMultiParameters();

	@NonNull
	Map<String, ViewParameter> getSingleParameters();

	@NonNull
	MultiValuedMap<String, MethodDescriptor> getViewMethodDescriptors();

	@NonNull
	Map<String, Converter> getConverters();

	@Getter
	class MethodDescriptor {
		private final ViewMethodParameter parameter;
		private final String name;
		private final Class<?> parameterType;

		public MethodDescriptor(@NonNull Method method) {
			for (PropertyDescriptor pd : PropertyUtils.getPropertyDescriptors(method.getDeclaringClass())) {
				if (method.equals(pd.getReadMethod())) {
					parameter = method.getDeclaredAnnotation(ViewMethodParameter.class);
					name = pd.getName();
					parameterType = method.getReturnType();
					return;
				} else if (method.equals(pd.getWriteMethod())) {
					if (method.getParameterCount() != 1) {
						throw new IllegalArgumentException("Given setter method has wrong argument count - should have one");
					}
					parameter = method.getDeclaredAnnotation(ViewMethodParameter.class);
					name = pd.getName();
					parameterType = method.getParameterTypes()[0];
					return;
				}
			}
			throw new IllegalArgumentException("Cannot get property name for method: " + method);
		}
	}

	void initialise();

	MultiValuedMap<String, ViewStateParameterFactory.ViewParameterDefinition> getViewParameterDefinitionMap();

	default String getViewName(String beanName) {
		ViewStateParameterFactory.ViewDescriptor viewDescriptor = getViewDescriptor(beanName);
		if (viewDescriptor != null) {
			return viewDescriptor.getName();
		} else {
			return null;
		}
	}

	default Collection<ViewStateParameter> createViewStateParameters(String viewName) {
		MultiValuedMap<String, ViewStateParameterFactory.ViewParameterDefinition> viewParameterDefinitionMap = getViewParameterDefinitionMap();
		if (!viewParameterDefinitionMap.containsKey(viewName)) {
			return Collections.emptySet();
		}
		return viewParameterDefinitionMap.get(viewName).stream().
				map(viewParameterDefinition -> createInstance(viewName, viewParameterDefinition)).
				collect(Collectors.toSet());
	}

	default ViewStateParameter createInstance(String viewName, ViewStateParameterFactory.ViewParameterDefinition viewParameterDefinition) {
		String beanName = viewParameterDefinition.getBeanName();
		ViewParameter viewParameter = viewParameterDefinition.getViewParameter();
		String name = viewParameter.value();
		Class type = getViewDescriptor(beanName).getType();

		for (PropertyDescriptor pd : PropertyUtils.getPropertyDescriptors(type)) {
			if (pd.getName().equals(name)) {
				Class<?> parameterType = viewParameter.valueClass();
				if (parameterType != void.class) {
					if (parameterType != pd.getPropertyType()) {
						throw new ViewStateParameterFactory.ConfigurationException.GetterSetterConfigurationException(viewName, name, pd, parameterType);
					}
				}
				Converter converter = getConverterByType(pd.getPropertyType());
				if (converter == null) {
					Type genericPropertyType = getGenericType(pd);
					if (genericPropertyType == null) {
						throw new ViewStateParameterFactory.GetterSetterConverterMissingException(pd);
					}
					converter = getConverterByType(genericPropertyType);
					if (converter == null) {
						throw new ViewStateParameterFactory.GetterSetterConverterMissingException(pd);
					}
				}
				checkDefaultValue(viewParameter, converter);
				return new PropertybasedViewStateParameter(viewName, viewParameter, pd, converter);
			}
		}
		Class<?> parameterType = viewParameter.valueClass();
		if (parameterType != void.class) {
			Converter converter = getConverterByAnno(viewParameter);
			if (converter == null) {
				throw new ViewStateParameterFactory.AnnotationConverterMissingException(viewParameter);
			}
			checkDefaultValue(viewParameter, converter);
			return new ConfigurationbasedViewStateParameter(viewName, viewParameter, converter);
		} else {
			throw new ViewStateParameterFactory.AnnotationTypeConfigurationException(viewName, name);
		}
	}

	default Converter getConverterByType(Type valueClass) {
		Converter result = getConverterByValueClass().get(valueClass);
		if (result != null) {
			return result;
		}
		return findConverter(valueClass);
	}

	Map<Type, Converter> getConverterByValueClass();

	default Converter findConverter(Type valueClass) {
		for (Map.Entry<Type, Converter> converterEntry : getConverterByValueClass().entrySet()) {
			if (converterEntry.getValue().converts(valueClass)) {
				return converterEntry.getValue();
			}
		}
		return null;
	}

	default Type getGenericType(PropertyDescriptor pd) {
		Method writeMethod = pd.getWriteMethod();
		if (writeMethod != null) {
			return writeMethod.getGenericParameterTypes()[0];
		}
		Method readMethod = pd.getReadMethod();
		if (readMethod != null) {
			return readMethod.getGenericReturnType();
		}
		return null;
	}

	default void checkDefaultValue(ViewParameter viewParameter, Converter converter) {
		try {
			String defaultValue = viewParameter.defaultValue();
			if (StringUtils.isNotEmpty(defaultValue)) {
				converter.getInternalObject(defaultValue);
			}
		} catch (RuntimeException e) {
			throw new ViewStateParameterFactory.NonConvertibleDefaultValueException(viewParameter, converter, e);
		}
	}

	default Converter getConverterByAnno(ViewParameter viewParameter) {
		if (viewParameter.convertedBy() != Converter.class) {
			try {
				return viewParameter.convertedBy().newInstance();
			} catch (InstantiationException | IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		} else {
			return getConverterByType(viewParameter.valueClass());
		}
	}

}
