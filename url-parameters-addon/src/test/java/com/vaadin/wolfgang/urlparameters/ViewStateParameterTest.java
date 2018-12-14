package com.vaadin.wolfgang.urlparameters;

import com.vaadin.spring.annotation.SpringView;
import com.vaadin.wolfgang.urlparameters.impl.ClasspathParameterFactory;
import com.vaadin.wolfgang.urlparameters.impl.ConfigurationbasedViewStateParameter;
import com.vaadin.wolfgang.urlparameters.impl.EnumValueConverter;
import com.vaadin.wolfgang.urlparameters.impl.PropertybasedViewStateParameter;
import org.apache.commons.collections4.MultiValuedMap;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.support.StaticListableBeanFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.time.Month;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ViewStateParameterTest {
	private StaticListableBeanFactory beanFactory = new StaticListableBeanFactory();
	private static final Map<String, Converter> CONVERTERS = new HashMap() {
		{
			put("month", new EnumValueConverter(Month.class));
			put("longho", new LongConverter());
			put("inti", new IntConverter());
		}
	};

	public static class LongConverter implements Converter<Long> {

		@Override
		public Class<Long> getInternalClass() {
			return Long.class;
		}

		@Override
		public Long getInternalObject(String stringRepresentation) {
			return Long.parseLong(stringRepresentation);
		}

		@Override
		public String getStringRepresentation(Long o) {
			return o.toString();
		}
	}

	public static class IntConverter implements Converter<Integer> {

		@Override
		public Class<Integer> getInternalClass() {
			return Integer.class;
		}

		@Override
		public Integer getInternalObject(String stringRepresentation) {
			return Integer.parseInt(stringRepresentation);
		}

		@Override
		public String getStringRepresentation(Integer o) {
			return o.toString();
		}
	}

	@BeforeClass
	private void initMocks() throws IllegalAccessException, InstantiationException {
		for (Object[] row : okConfigs()) {
			Class c = (Class) row[0];
			beanFactory.addBean(c.getSimpleName(), c.newInstance());
		}
		for (Map.Entry<String, Converter> converterEntry : CONVERTERS.entrySet()) {
			beanFactory.addBean(converterEntry.getKey(), converterEntry.getValue());
		}
	}

	@DataProvider
	private Object[][] okConfigs() {
		return new Object[][]{
				{WithGetProperty.class, PropertybasedViewStateParameter.class, Long.class, LongConverter.class},
				{WithSetProperty.class, PropertybasedViewStateParameter.class, Long.class, LongConverter.class},
				{WithProperty.class, PropertybasedViewStateParameter.class, Integer.class, IntConverter.class},
				{WithBoth.class, PropertybasedViewStateParameter.class, Long.class, LongConverter.class},
				{WithConfiguration.class, ConfigurationbasedViewStateParameter.class, Integer.class, IntConverter.class},
				{SingleParameterAnnotation.class, ConfigurationbasedViewStateParameter.class, Integer.class, IntConverter.class},
				{DefaultValueAnnotation.class, ConfigurationbasedViewStateParameter.class, Integer.class, IntConverter.class},
				{MethodAnnotationOkView.class, PropertybasedViewStateParameter.class, Integer.class, IntConverter.class},

		};
	}

	@DataProvider
	private Object[][] nokConfigs() {
		return new Object[][]{
				{TypeMissing.class},
				{Ambiguous1.class},
				{Ambiguous2.class},
				{Ambiguous3.class},
				{WrongSetterApi.class},
				{WrongDefaultValueAnnotation.class},
				{WrongDefaultValueProperty.class}
		};
	}

	@Test(dataProvider = "okConfigs")
	public void testCreateInstanceSuccess(Class clazz, Class<ViewStateParameter> resultClass, Class parameterType, Class converterClass) throws Exception {
//		ParameterFactory parameterFactory = new SpringParameterFactory(beanFactory);
		boolean givenClassIdentified = false;
		ClasspathParameterFactory parameterFactory = new ClasspathParameterFactory("com.vaadin.wolfgang.urlparameters", OkViewName.class, "value", CONVERTERS);
		ViewStateParameterFactory vspf = new ViewStateParameterFactory(parameterFactory);
		MultiValuedMap<String, ViewStateParameterFactory.ViewParameterDefinition> definitions = parameterFactory.getViewParameterDefinitionMap();
		for (String key : definitions.keySet()) {
			Collection<ViewStateParameter> viewStateParameters = vspf.getViewStateParameters(key);
			for (ViewStateParameter viewStateParameter : viewStateParameters) {
				if (clazz.equals(parameterFactory.getClassForViewName(key))) {//viewStateParameter.getViewName())) {
					Assert.assertNotNull(viewStateParameter, "ViewStateParameter must not be null");
					Assert.assertEquals(viewStateParameter.getClass(), resultClass, "wrong resultClass for " + viewStateParameter.getViewName());
					Assert.assertEquals(viewStateParameter.getParameterType(), parameterType, "wrong parameterType for " + viewStateParameter.getViewName());
					Assert.assertEquals(viewStateParameter.getConverter().getClass(), converterClass, "wrong converterClass for " + viewStateParameter.getViewName());
					givenClassIdentified = true;
					break;
				}
			}
		}
		Assert.assertTrue(givenClassIdentified, "given class " + clazz + " not identified");
	}

	@Test(dataProvider = "nokConfigs", expectedExceptions = ViewStateParameterFactory.ConfigurationException.class)
	public void testCreateInstanceFail(Class clazz) throws Exception {
		StaticListableBeanFactory beanFactory = new StaticListableBeanFactory();
		for (Map.Entry<String, Converter> converterEntry : CONVERTERS.entrySet()) {
			beanFactory.addBean(converterEntry.getKey(), converterEntry.getValue());
		}
		beanFactory.addBean(clazz.getSimpleName(), clazz.newInstance());
		ObjectFactory<Map<String, Collection<ViewStateParameter>>> scopedParameterFactory = new ObjectFactory<Map<String, Collection<ViewStateParameter>>>() {
			Map<String, Collection<ViewStateParameter>> map = new HashMap<>();

			@Override
			public Map<String, Collection<ViewStateParameter>> getObject() throws BeansException {
				return map;
			}
		};
//		ParameterFactory parameterFactory = new SpringParameterFactory(beanFactory, scopedParameterFactory);
		ParameterFactory parameterFactory = new ClasspathParameterFactory("jodel", clazz);

		for (Map.Entry<String, ViewStateParameterFactory.ViewParameterDefinition> entry : parameterFactory.getViewParameterDefinitionMap().entries()) {
			parameterFactory.createViewStateParameters(entry.getKey());
		}
	}

	// OK-configurations
	@OkViewName("one")
	@ViewParameters({
			@ViewParameter("longValueGetter")
	})
	public static class WithGetProperty {
		public Long getLongValueGetter() {
			return Long.MIN_VALUE;
		}
	}

	@OkViewName("two")
	@ViewParameters({
			@ViewParameter("longValueSetter")
	})
	public static class WithSetProperty {
		public void setLongValueSetter(Long value) {
		}
	}

	@OkViewName("three")
	@ViewParameters({
			@ViewParameter("intValue")
	})
	public static class WithProperty {
		public Integer getIntValue() {
			return Integer.MIN_VALUE;
		}

		public void setIntValue(Integer value) {
		}
	}

	@OkViewName("four")
	@ViewParameters({
			@ViewParameter(value = "longValue", valueClass = Long.class)
	})
	public static class WithBoth {
		public Long getLongValue() {
			return Long.MIN_VALUE;
		}

		public void setLongValue(Long value) {
		}
	}

	@OkViewName("five")
	@ViewParameters({
			@ViewParameter(value = "intValue", valueClass = Integer.class)
	})
	public static class WithConfiguration {
	}

	@OkViewName("six")
	@ViewParameter(value = "intValue", valueClass = Integer.class)
	public static class SingleParameterAnnotation {
	}

	@OkViewName("seven")
	@ViewParameter(value = "intValue", valueClass = Integer.class, defaultValue = "1640")
	public static class DefaultValueAnnotation {
	}

	// N-OK-configurations
	@SpringView(name = "eight")
	@ViewParameters({
			@ViewParameter("longValueGetter")
	})
	public static class TypeMissing {
	}

	@SpringView(name = "nine")
	@ViewParameters({
			@ViewParameter(value = "longValue", valueClass = Integer.class)
	})
	public static class Ambiguous1 {
		public Long getLongValue() {
			return Long.MIN_VALUE;
		}

		public void setLongValue(Long value) {
		}
	}

	@SpringView(name = "ten")
	@ViewParameters({
			@ViewParameter(value = "longValue", valueClass = Integer.class)
	})
	public static class Ambiguous2 {
		public void setLongValue(Long value) {
		}
	}


	@SpringView(name = "eleven")
	@ViewParameters({
			@ViewParameter(value = "longValue", valueClass = Integer.class)
	})
	public static class Ambiguous3 {
		public Long getLongValue() {
			return Long.MIN_VALUE;
		}
	}


	@SpringView(name = "twelve")
	@ViewParameters({
			@ViewParameter("longValue")
	})
	public static class WrongSetterApi {
		public void setLongValue() {
		}
	}

	@SpringView(name = "thirteen")
	@ViewParameter(value = "intValue", valueClass = Integer.class, defaultValue = "Not numeric")
	public static class WrongDefaultValueAnnotation {
	}

	@SpringView(name = "fourteen")
	@ViewParameter(value = "intValue", defaultValue = "Not numeric")
	public static class WrongDefaultValueProperty {
		public Integer getIntValue() {
			return null;
		}
	}

	@OkViewName("fifteen")
	public static class MethodAnnotationOkView {
		@ViewMethodParameter
		public void setIntValue(Integer value) {
		}
	}

	public static class MethodAnnotationNoViewName {
		@ViewMethodParameter
		public void setLongValue(Long value) {
		}
	}


}