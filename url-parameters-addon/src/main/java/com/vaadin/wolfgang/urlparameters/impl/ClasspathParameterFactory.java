package com.vaadin.wolfgang.urlparameters.impl;

import com.google.common.annotations.VisibleForTesting;
import com.vaadin.wolfgang.urlparameters.*;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.reflections.Configuration;
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.util.ConfigurationBuilder;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class ClasspathParameterFactory extends AbstractParameterFactory {
	private final Map<String, ViewParameters> viewStateParameterss = new HashMap<>();
	private final Map<String, ViewParameter> viewStateParameters = new HashMap<>();
	@Getter
	private final MultiValuedMap<String, MethodDescriptor> viewMethodDescriptors = new ArrayListValuedHashMap<>();
	private final Map<String, String> viewNames = new HashMap<>();
	private final Map<String, Class<?>> annotatedClasses = new HashMap<>();

	@FunctionalInterface
	interface ClassAnnotationMatchProcessor<A extends Annotation> {
		void processMatch(Class<?> matchingClass);
	}

	class Matcher<A extends Annotation> implements ClassAnnotationMatchProcessor<A> {
		@Getter
		private final Class<A> annotation;

		private Matcher(Class<A> annotation) {
			this.annotation = annotation;
		}

		public void processMatch(Class<?> matchingClass) {
			A anno = matchingClass.getAnnotation(annotation);
			String key = matchingClass.getName();
			if (anno instanceof ViewParameters) {
				viewStateParameterss.put(key, (ViewParameters) anno);
			} else {
				viewStateParameters.put(key, (ViewParameter) anno);
			}
			annotatedClasses.put(key, matchingClass);
		}
	}

	public ClasspathParameterFactory(String packageHint, Class<? extends Annotation> nameAnnotationClass, String nameKey, Map<String, Converter> converters) {
		registerConverters(converters);
		Configuration conf = new ConfigurationBuilder().forPackages(packageHint).setExpandSuperTypes(false).addScanners(new MethodAnnotationsScanner());
		Reflections reflections = new Reflections(conf);
		Matcher parametersMatcher = new Matcher<ViewParameters>(ViewParameters.class);
		reflections.getTypesAnnotatedWith(ViewParameters.class).forEach(aClass -> parametersMatcher.processMatch(aClass));

		Matcher parameterMatcher = new Matcher<ViewParameter>(ViewParameter.class);
		reflections.getTypesAnnotatedWith(ViewParameter.class).forEach(aClass -> parameterMatcher.processMatch(aClass));

		reflections.getMethodsAnnotatedWith(ViewMethodParameter.class).forEach(method -> {
			Class declaringClass = method.getDeclaringClass();
			String declaringClassName = declaringClass.getName();
			MethodDescriptor methodDescriptor = new MethodDescriptor(method);
			viewMethodDescriptors.put(declaringClassName, methodDescriptor);
			annotatedClasses.put(declaringClassName, declaringClass);
		});

		reflections.getTypesAnnotatedWith(nameAnnotationClass).forEach(classWithAnnotation -> {
			Annotation anno = classWithAnnotation.getAnnotation(nameAnnotationClass);
			try {
				Method m = anno.getClass().getMethod(nameKey, new Class[]{});
				String name = (String) m.invoke(anno, null);
				viewNames.put(classWithAnnotation.getName(), name);
			} catch (NoSuchMethodException e) {
				throw new IllegalArgumentException("given annotation " + nameAnnotationClass + " must have a '" + nameKey + "' attribute");
			} catch (IllegalAccessException e) {
				throw new RuntimeException(e);
			} catch (InvocationTargetException e) {
				throw new RuntimeException(e.getTargetException());
			} catch (ClassCastException e) {
				throw new RuntimeException("The given method '" + nameKey + "' must return a string value");
			}
		});
		initialise();
	}

	@VisibleForTesting
	public ClasspathParameterFactory(String name, Class beanClass) {
		viewNames.put(beanClass.getName(), name);
		new Matcher(ViewParameters.class).processMatch(beanClass);
		new Matcher(ViewParameter.class).processMatch(beanClass);
		initialise();
	}

	@VisibleForTesting
	public Class getClassForViewName(String viewName) {
		for (Map.Entry<String, String> entry : viewNames.entrySet()) {
			if (entry.getValue().equals(viewName)) {
				try {
					return Class.forName(entry.getKey());
				} catch (ClassNotFoundException e) {
					throw new RuntimeException(e);
				}
			}
		}
		return null;
	}

	@Override
	public Map<String, ViewParameters> getMultiParameters() {
		return new HashMap<>(viewStateParameterss);
	}

	@Override
	public Map<String, ViewParameter> getSingleParameters() {
		return new HashMap<>(viewStateParameters);
	}

	public ViewStateParameterFactory.ViewDescriptor getViewDescriptor(String name) {
		if (viewNames.containsKey(name)) {
			return new ViewStateParameterFactory.ViewDescriptor() {
				@Override
				public String getName() {
					return viewNames.get(name);
				}

				@Override
				public Class getType() {
					return annotatedClasses.get(name);
				}
			};
		} else {
			log.warn("no view found for name " + name);
			return null;
		}
	}

}
