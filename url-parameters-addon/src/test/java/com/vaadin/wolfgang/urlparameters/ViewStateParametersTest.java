package com.vaadin.wolfgang.urlparameters;

import com.vaadin.navigator.NavigationStateManager;
import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewProvider;
import com.vaadin.spring.navigator.SpringNavigator;
import com.vaadin.wolfgang.urlparameters.impl.ClasspathParameterFactory;
import com.vaadin.wolfgang.urlparameters.impl.CollectionValueConverter;
import com.vaadin.wolfgang.urlparameters.impl.EnumValueConverter;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.support.StaticListableBeanFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.*;
import java.util.function.Function;

//import static org.mockito.Mockito.mock;
//import static org.mockito.Mockito.when;

public class ViewStateParametersTest {
	private Navigator navigator;
	private ViewStateParameters candidate;
	private static final Property STATIC_PROPERTY = new Property();
	private static final String STATIC_PROPERTY_STRING = "PROPERTY";
	private static final String PROPERTY_PARAM = "property=" + STATIC_PROPERTY_STRING;
	private static final String HUBERT_PARAM = "hubert=" + Hubert.class.getName();
	private static final Map<String, View> VIEWS;

	static {
		VIEWS = new HashMap();
		VIEWS.put("naked", new Naked());
		VIEWS.put("simple", new Simple());
		VIEWS.put("setter", new PSetter());
		VIEWS.put("getter", new PGetter());
		VIEWS.put("doublegetter", new DGetter());
		VIEWS.put("multisetter", new MultiSetter());
		VIEWS.put("multisetter2", new MultiSetter2());
	}

	public enum MyEnum {
		ONE, TWO, THREE
	}

	public enum MyOtherEnum {
		FOUR, FIVE, SIX
	}

	@BeforeClass
	private void setupCandidate() {
		StaticListableBeanFactory beanFactory = new StaticListableBeanFactory();
		for (Map.Entry<String, View> viewEntry : VIEWS.entrySet()) {
			beanFactory.addBean(viewEntry.getKey(), viewEntry.getValue());
		}
//		beanFactory.addBean("propertyconverter", new PropertyConverter());
//		beanFactory.addBean("hubertconverter", new HubertConverter());
//		beanFactory.addBean("multiEnumConverter", new CollectionValueConverter(new EnumValueConverter(MyEnum.class), ArrayList.class));
//		beanFactory.addBean("otherMultiEnumConverter", new CollectionValueConverter(new EnumValueConverter(MyOtherEnum.class), ArrayList.class));

		ObjectFactory<Map<String, Collection<ViewStateParameter>>> parameterFactory = new ObjectFactory<Map<String, Collection<ViewStateParameter>>>() {
			Map<String, Collection<ViewStateParameter>> map = new HashMap<>();

			@Override
			public Map<String, Collection<ViewStateParameter>> getObject() throws BeansException {
				return map;
			}
		};
		Map<String, Converter> converters = new HashMap<String, Converter>() {
			{
				put("propertyconverter", new PropertyConverter());
				put("hubertconverter", new HubertConverter());
				put("multiEnumConverter", new CollectionValueConverter(new EnumValueConverter(MyEnum.class), ArrayList.class));
				put("otherMultiEnumConverter", new CollectionValueConverter(new EnumValueConverter(MyOtherEnum.class), ArrayList.class));
			}
		};
		ParameterFactory parameterFactory1 = new ClasspathParameterFactory("com.vaadin.wolfgang.urlparameters", ViewName.class, "value", converters);
//		ViewStateParameterFactory.ParameterFactory parameterFactory1 = new SpringParameterFactory(beanFactory, parameterFactory);

		ViewStateParameterFactory viewStateParameterFactory = new ViewStateParameterFactory(parameterFactory1);

		NavigationStateManager navigationStateManager = new NavigationStateManager() {
			@Getter
			@Setter
			private String state = "";
			@Setter
			private Navigator navigator;
		};

		navigator = new SpringNavigator(/*welcomeViewSelector, uiEventBus, eventPublisher*/) {

			@Override
			protected NavigationStateManager getStateManager() {
				return navigationStateManager;
			}
		};

		navigator.addProvider(new ViewProvider() {
			@Override
			public String getViewName(String s) {
				return ViewStateParameters.getViewName(s);
			}

			@Override
			public View getView(String s) {
				return VIEWS.get(s);
			}
		});
		candidate = new ViewStateParameters(viewStateParameterFactory, navigator);
	}

	@DataProvider
	private Object[][] navigateToData() {
		return new Object[][]{
				{"naked", IS_NULL, false},
				{"naked/" + PROPERTY_PARAM, IS_NULL, false},
				{"simple", IS_NULL, true},
				{"simple/" + PROPERTY_PARAM, IS_STATIC, true},
				{"setter", IS_NULL, true},
				{"setter/" + PROPERTY_PARAM, IS_STATIC, true},
				{"getter", IS_STATIC, false},
				{"getter/" + PROPERTY_PARAM, IS_STATIC, false},
		};
	}

	private static final Function<Object, Boolean> IS_NULL = new Function<Object, Boolean>() {
		@Override
		public Boolean apply(Object o) {
			return o == null;
		}
	};
	private static final Function<Object, Boolean> IS_NOT_NULL = new Function<Object, Boolean>() {
		@Override
		public Boolean apply(Object o) {
			return o != null;
		}
	};
	private static final Function<Object, Boolean> IS_STATIC = new Function<Object, Boolean>() {
		@Override
		public Boolean apply(Object o) {
			return o == STATIC_PROPERTY;
		}
	};

	@Test(dataProvider = "navigateToData")
	public void testNavigateTo(String url, Function<Object, Boolean> resultEvaluator, boolean expectToGetSameAsSet) {
		navigator.navigateTo(url);
		Property property = candidate.getElement(Property.class);
		Assert.assertTrue(resultEvaluator.apply(property), "wrong answer for " + url);
		property = new Property();
		candidate.putElement(Property.class, property);
		Property candidateProperty = candidate.getElement(Property.class);
		if (expectToGetSameAsSet) {
			Assert.assertEquals(property, candidateProperty, "wrong answer for " + url);
		} else {
			Assert.assertNotEquals(property, candidateProperty, "wrong answer for " + url);
		}
	}

	@DataProvider
	private Object[][] navigateWithParametersData() {
		return new Object[][]{
				{"naked", new Object[]{}, null},
				{"naked", new Object[]{STATIC_PROPERTY}, null},
				{"simple", new Object[]{}, null},
				{"simple", new Object[]{STATIC_PROPERTY}, PROPERTY_PARAM},
				{"setter", new Object[]{}, null},
				{"setter", new Object[]{STATIC_PROPERTY}, PROPERTY_PARAM},
				{"getter", new Object[]{}, null},
				{"getter", new Object[]{STATIC_PROPERTY}, PROPERTY_PARAM},
				{"doublegetter", new Object[]{}, null},
				{"doublegetter", new Object[]{STATIC_PROPERTY}, PROPERTY_PARAM},
				{"doublegetter", new Object[]{new Hubert()}, HUBERT_PARAM},
				{"doublegetter", new Object[]{STATIC_PROPERTY, new Hubert()}, HUBERT_PARAM + "&" + PROPERTY_PARAM},
				{"multisetter", new Object[]{MyEnum.TWO}, "multisetter=TWO"},
				{"multisetter", new Object[]{Arrays.asList(MyEnum.ONE, MyEnum.THREE)}, "multisetter=(ONE,THREE)"},
				{"multisetter2", new Object[]{MyOtherEnum.FIVE}, "othermultisetter=FIVE"},
				{"multisetter2", new Object[]{Arrays.asList(MyOtherEnum.FOUR, MyOtherEnum.SIX)}, "othermultisetter=(FOUR,SIX)"},
		};
	}

	@Test(dataProvider = "navigateWithParametersData")
	public void testNavigateWithParameters(String viewName, Object[] parameters, String expectedParamString) {
		candidate.navigateWithParameters(viewName, parameters);
		String state = navigator.getState();
		String expectedState = (expectedParamString != null ? viewName + "/" + expectedParamString : viewName);
		Assert.assertEquals(state, expectedState);
		String candidateString = candidate.toString();
		if (expectedParamString == null) {
			expectedParamString = "";
		}
//		Assert.assertEquals(candidateString, expectedParamString);
	}

	@ViewName("naked")
	public static final class Naked implements View {
	}

	@ViewName("simple")
	@ViewParameter(value = "property", valueClass = Property.class)
	public static final class Simple implements View {
	}

	@ViewName("setter")
	@ViewParameter(value = "property")
	public static final class PSetter implements View {
		public void setProperty(Property property) {
		}
	}

	@ViewName("multisetter")
	@ViewParameter(value = "multisetter")
	public static final class MultiSetter implements View {
		public void setMultisetter(Collection<MyEnum> property) {
		}
	}

	@ViewName("multisetter2")
	@ViewParameter(value = "othermultisetter")
	public static final class MultiSetter2 implements View {
		public void setOthermultisetter(Collection<MyOtherEnum> property) {
		}
	}

	@ViewName("getter")
	@ViewParameter(value = "property")
	public static final class PGetter implements View {
		public Property getProperty() {
			return STATIC_PROPERTY;
		}
	}

	@ViewName("doublegetter")
	@ViewParameters({
			@ViewParameter("property"),
			@ViewParameter("hubert")
	})
	public static final class DGetter implements View {
		public Property getProperty() {
			return STATIC_PROPERTY;
		}

		public Hubert getHubert() {
			return new Hubert();
		}
	}

	public static final class Property {
	}

	public static final class Hubert {
	}

	public static final class PropertyConverter implements Converter<Property> {

		@Override
		public Class<Property> getInternalClass() {
			return Property.class;
		}

		@Override
		public Property getInternalObject(String stringRepresentation) {
			if (STATIC_PROPERTY_STRING.equals(stringRepresentation)) {
				return STATIC_PROPERTY;
			}
			if (StringUtils.isEmpty(stringRepresentation)) {
				return null;
			}
			return new Property();
		}

		@Override
		public String getStringRepresentation(Property o) {
			if (o == STATIC_PROPERTY) {
				return STATIC_PROPERTY_STRING;
			}
			return String.valueOf(o);
		}
	}

	public static final class HubertConverter implements Converter<Hubert> {

		@Override
		public Class<Hubert> getInternalClass() {
			return Hubert.class;
		}

		@Override
		public Hubert getInternalObject(String stringRepresentation) {
			if (StringUtils.isEmpty(stringRepresentation)) {
				return null;
			}
			return new Hubert();
		}

		@Override
		public String getStringRepresentation(Hubert o) {
			if (o == null) {
				return "";
			}
			return o.getClass().getName();
		}
	}

}