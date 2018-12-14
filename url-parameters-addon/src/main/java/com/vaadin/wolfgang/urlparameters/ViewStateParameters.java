package com.vaadin.wolfgang.urlparameters;

import com.google.inject.internal.MoreTypes;
import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.Page;
import com.vaadin.wolfgang.urlparameters.impl.CollectionValueConverter;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Type;
import java.util.*;

/**
 * The type ViewStateParameters is designed for managing parameters that specify details of what to display inside
 * a view.
 * It gets constructed with a <code>ViewStateParameterFactory</code> that scans for all <code>ViewParameter[s]</code>
 * annotated views to produce a list of <code>ViewStateParameter</code> elements.
 * It listens to Navigator's events to identify and apply parameters.
 * To change the value of a parameter you inject this bean and call <code>putElement</code>
 * @see  <code>ViewParameter</code> or <code>ViewParameters</code>for more details of how to configure ViewParameters
 */
@Slf4j
public class ViewStateParameters {
	private final ViewStateParameterFactory viewStateParameterFactory;
	private final Navigator navigator;
	@Getter(AccessLevel.PROTECTED)
	private String currentViewName = null;
	@Getter(AccessLevel.PROTECTED)
	private View currentView = null;

	/**
	 * Instantiates a new View state parameters.
	 *
	 * @param viewStateParameterFactory the factory for viewstate parameters configured via @ViewParameters
	 * @param navigator the vaadin navigator to listen to and address
	 */
	public ViewStateParameters(ViewStateParameterFactory viewStateParameterFactory,
							   Navigator navigator) {
		this.viewStateParameterFactory = viewStateParameterFactory;
		this.navigator = navigator;
		dependOn(navigator);
		/*
		else we are running e.g. in DummyDataCreation. no UI -> no handling of ViewStateParameters
		 */
	}

	private void dependOn(@NonNull Navigator navigator) {
		navigator.addViewChangeListener(new ViewChangeListener() {
			@Override
			public boolean beforeViewChange(ViewChangeEvent event) {
				/*
				before the view is entered the parameters are parsed and assigned to the fully initialised view instance
				 */
				currentViewName = event.getViewName();
				currentView = event.getNewView();
				Map<String, String> map = event.getParameterMap();
				internalize(map);
				return true;
			}

			@Override
			public void afterViewChange(ViewChangeListener.ViewChangeEvent event) {
				/*
				after the view has successfully been entered the url gets updated
				 */
				updateUriFragment();
			}
		});
	}

	private ViewStateParameter getParameterConfigByValue(@NonNull Object value, String viewName) {
		return getParameterConfigByValueClass(value.getClass(), viewName);
	}

	private ViewStateParameter getParameterConfigByValueClass(@NonNull Type valueClass, String viewName) {
		Collection<ViewStateParameter> configs = viewStateParameterFactory.getViewStateParameters(viewName);
		for (ViewStateParameter parameterConfig : configs) {
			if (parameterConfig.convertsClass(valueClass)) {
				return parameterConfig;
			}
			Converter converter = parameterConfig.getConverter();
			if (converter instanceof CollectionValueConverter) {
				CollectionValueConverter collectionValueConverter = (CollectionValueConverter) converter;
				if (collectionValueConverter.getSingleValueClass() == valueClass) {
					return parameterConfig;
				}
			}
		}
		return null;
	}

	public void navigateWithParameters(String viewName, Object... parameters) {
		Map<String, String> stringParameters = new HashMap<>();
		for (Object parameter : parameters) {
			if (parameter != null) {
				ViewStateParameter parameterConfig;
				if (parameter instanceof Collection) {
					Collection l = (Collection) parameter;
					if (l.size() == 0) {
						throw new IllegalArgumentException("cannot determine element type of empty list");
					}
					Class paramType = CollectionUtils.get((Object) l, 0).getClass();
					Type type = new MoreTypes.ParameterizedTypeImpl(null, List.class, paramType);
					parameterConfig = getParameterConfigByValueClass(type, viewName);
				} else {
					parameterConfig = getParameterConfigByValue(parameter, viewName);
				}
				if (parameterConfig != null) {
					Converter converter = parameterConfig.getConverter();
					String value;
					if (converter instanceof CollectionValueConverter && !(parameter instanceof Collection)) {
						Converter singleConverter = ((CollectionValueConverter) converter).getSingleValueConverter();
						value = singleConverter.getStringRepresentation(parameter);
					} else {
						value = converter.getStringRepresentation(parameter);
					}
					stringParameters.put(parameterConfig.getParameterName(), value);
				}
			}
		}
		String state = buildState(viewName, stringParameters);
		navigator.navigateTo(state);
	}

	/**
	 * Gets element.
	 *
	 * @param <T>          the type parameter
	 * @param elementClass the element class
	 * @return the element
	 */

	public <T> T getElement(@NonNull Class<T> elementClass) {
		return getElement(elementClass, getCurrentViewName());
	}

	private <T> T getElement(@NonNull Class<T> elementClass, String viewName) {
		ViewStateParameter<T> viewStateParameter = getParameterConfigByValueClass(elementClass, viewName);
		if (viewStateParameter != null) {
			return viewStateParameter.getValue(getCurrentView());
		}
		return null;
	}

	public <E> void putElements(@NonNull Class<E> elementClass, Collection<E> elements) {
		Type t = new MoreTypes.ParameterizedTypeImpl(null, Collection.class, elementClass);
		putElement(t, elements);
	}

	/**
	 * Put element puts a ViewState-element to be reflected in the url. Passing a null value will make the url parameter
	 * disappear.
	 *
	 * @param <T>          the type parameter
	 * @param elementClass the element class
	 * @param element      the element
	 * @return the replaced value for the given element if any
	 */
	public <T> T putElement(@NonNull Type elementClass, T element) {
		return putElement(elementClass, element, false);
	}

	private <T> T putElement(@NonNull Type elementClass, T element, boolean initial) {
		T result = null;
		String currentViewName = getCurrentViewName();
		if (StringUtils.isNotEmpty(currentViewName)) {
			boolean found = false;
			ViewStateParameter<T> viewStateParameter = getParameterConfigByValueClass(elementClass, currentViewName);
			if (viewStateParameter != null) {
				View currentView = getCurrentView();
				if (element == null) {
					String defaultValue = viewStateParameter.getDefaultValue();
					if (StringUtils.isNotEmpty(defaultValue)) {
						element = viewStateParameter.getConverter().getInternalObject(defaultValue);
					}
				}
				boolean valueChanged = viewStateParameter.setValue(element, currentView, initial);

				if (valueChanged) {
					updateUriFragment();
				}
				found = true;
			}
			if (!found) {
				log.warn("You have put an unknown class to be used as a viewStateParameter: {} (currentView: {})",
						elementClass, getCurrentView());
			}
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	private void updateUriFragment() {
		Page currentPage = Page.getCurrent();
		if (currentPage != null) {
			String state = StringUtils.defaultIfEmpty(currentPage.getUriFragment(), "");
			String viewName = getViewName(state);
			currentPage.setUriFragment(buildState(viewName, externalize()), false);
		}
	}

	private String buildState(String viewName, Map<String, String> parameters) {
		String myInfo = parametersToString(parameters);

		if (StringUtils.isNotEmpty(myInfo)) {
			return viewName + '/' + myInfo;
		} else {
			return viewName;
		}
	}

	/**
	 * Gets view name.
	 *
	 * @param location the location
	 * @return the view name
	 */
	public static String getViewName(@NonNull String location) {
		String result = location;
		int slashIndex = location.indexOf('/');
		if (slashIndex > -1) {
			result = location.substring(0, slashIndex);
		}
		return result;
	}

	/**
	 * Call before entering a view- will NOT update URL
	 *
	 * @param parameters the map of parameters from the vaadin navigation event
	 */
	private void internalize(@NonNull Map<String, String> parameters) {
		Collection<ViewStateParameter> params = viewStateParameterFactory.getViewStateParameters(getCurrentViewName());

		Collection<ViewStateParameter> newValues = new HashSet<>();

		Collection<Class> sharedParameterTypes = new HashSet<>();
		for (ViewStateParameter viewStateParameter : params) {
			String name = viewStateParameter.getParameterName();


			if (parameters.containsKey(name)) {
				String value = parameters.get(name);
				try {
					Object internalValue = viewStateParameter.getConverter().getInternalObject(value);
					viewStateParameter.setTempValue(internalValue, getCurrentView());
				} catch (RuntimeException e) {
					Object internalValue = getDefaultValue(viewStateParameter);
					viewStateParameter.setTempValue(internalValue, getCurrentView());
				}
				newValues.add(viewStateParameter);

			} else {
				Object internalValue = getDefaultValue(viewStateParameter);
				viewStateParameter.setTempValue(internalValue, getCurrentView());
				newValues.add(viewStateParameter);
			}
		}
		try {
			View currentView = getCurrentView();
			newValues.forEach(viewStateParameter -> viewStateParameter.fireTempValue(currentView));
		} finally {
			newValues.forEach(viewStateParameter -> viewStateParameter.flushTempValue());
		}
	}

	private Object getDefaultValue(@NonNull ViewStateParameter viewStateParameter) {
		String value = viewStateParameter.getDefaultValue();
		Converter converter = viewStateParameter.getConverter();
		if (StringUtils.isEmpty(value)) {
			return converter.getInternalObject("");
		} else {
			return converter.getInternalObject(value);
		}
	}

	private Map<String, String> externalize() {
		Map<String, String> result = new HashMap<>();
		Collection<ViewStateParameter> params = viewStateParameterFactory.getViewStateParameters(getCurrentViewName());
		if (params != null) {
			for (ViewStateParameter viewStateParameter : params) {
				String name = viewStateParameter.getParameterName();
				String value = viewStateParameter.getStringValue(getCurrentView());
				if (StringUtils.isNotEmpty(value)) {
					result.put(name, value);
				}
			}
		}
		return result;
	}

	public void refresh() {
		// brute force internal references to disappear
		Map<String, String> parameterString = externalize();
		internalize(parameterString);
	}

	private static String parametersToString(@NonNull Map<String, String> parameters) {
		boolean first = true;
		StringBuilder buf = new StringBuilder();
		for (Map.Entry<String, String> entry : parameters.entrySet()) {
			if (first) {
				first = false;
			} else {
				buf.append('&');
			}
			buf.append(entry.getKey());
			buf.append('=');
			buf.append(entry.getValue());
		}
		return buf.toString();
	}

	@Override
	public String toString() {
		Map<String, String> parameters = externalize();
		return parametersToString(parameters);
	}
}
