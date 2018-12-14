package com.vaadin.wolfgang.urlparameters;

import com.google.common.annotations.VisibleForTesting;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.Transformer;
import org.apache.commons.collections4.map.LazyMap;

import java.beans.PropertyDescriptor;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

// instanciate this bean in the scope where the actual parameter values are supposed to be valid
public class ViewStateParameterFactory {
	@Getter(AccessLevel.PACKAGE)
	@VisibleForTesting
	private final ParameterFactory parameterFactory;
	private final Map<String, Collection<ViewStateParameter>> actualParameters = LazyMap.lazyMap(new HashMap<>(), new Transformer<String, Collection<ViewStateParameter>>() {
		@Override
		public Collection<ViewStateParameter> transform(String input) {
			return parameterFactory.createViewStateParameters(input);
		}
	});

	public ViewStateParameterFactory(@NonNull ParameterFactory parameterFactory) {
		this.parameterFactory = parameterFactory;
	}

	public Collection<ViewStateParameter> getViewStateParameters(String viewName) {
		return actualParameters.get(viewName);
	}

	public interface ViewDescriptor {
		String getName();

		Class getType();
	}

	/**
	 * Whenever a configuration is not perfect an according subclass of ConfigurationException is thrown
	 */
	static class ConfigurationException extends RuntimeException {
		private ConfigurationException(String message) {
			super(message);
		}

		static class GetterSetterConfigurationException extends ConfigurationException {
			GetterSetterConfigurationException(String viewName, String parameterName, PropertyDescriptor pd, Class parameterType) {
				super("You declared a " + ViewParameter.class.getSimpleName() + " in '" + viewName + "' for '" + parameterName + "' with property of type " + pd.getPropertyType() + " and configured type " + parameterType);
			}
		}
	}

	static class AnnotationTypeConfigurationException extends ConfigurationException {
		AnnotationTypeConfigurationException(String viewName, String parameterName) {
			super("You declared a " + ViewParameter.class.getSimpleName() + " in '" + viewName + "' for '" + parameterName + "' but there is neither a getter or a setter nor a configured type - unable to determine the type");
		}
	}

	static class GetterSetterConverterMissingException extends ConfigurationException {
		GetterSetterConverterMissingException(PropertyDescriptor pd) {
			super("no converter found for class: " + pd.getPropertyType());
		}
	}

	static class AnnotationConverterMissingException extends ConfigurationException {
		AnnotationConverterMissingException(ViewParameter viewParameter) {
			super("no converter found for configuration: " + viewParameter);
		}
	}

	static class NonConvertibleDefaultValueException extends ConfigurationException {
		NonConvertibleDefaultValueException(ViewParameter viewParameter, Converter converter, Exception e) {
			super("Default value '" + viewParameter.defaultValue() + "' cannot be converted by " + converter + ": " + e);
		}
	}

	@RequiredArgsConstructor
	@Getter
	static class ViewParameterDefinition {
		private final ViewParameter viewParameter;
		private final String beanName;
	}
}