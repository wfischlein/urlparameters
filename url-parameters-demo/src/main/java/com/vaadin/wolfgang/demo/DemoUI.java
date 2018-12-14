package com.vaadin.wolfgang.demo;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.navigator.Navigator;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.UI;
import com.vaadin.wolfgang.demo.component.Header;
import com.vaadin.wolfgang.demo.data.ValueObject;
import com.vaadin.wolfgang.urlparameters.*;
import com.vaadin.wolfgang.urlparameters.impl.ClasspathParameterFactory;
import com.vaadin.wolfgang.urlparameters.impl.CollectionValueConverter;
import com.vaadin.wolfgang.urlparameters.impl.EnumValueConverter;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.annotation.WebServlet;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Theme("demo")
@Title("MyComponent Add-on Demo")
@SuppressWarnings("serial")
public class DemoUI extends UI {

	@WebServlet(value = "/*", asyncSupported = true)
	@VaadinServletConfiguration(productionMode = false, ui = DemoUI.class)
	public static class Servlet extends VaadinServlet {
	}

	private Navigator navigator;
	private final Map<String, Converter> converters = new HashMap<>();
	private final AppViewDisplay appViewDisplay;
	private final ComponentContainer content;
	private final List<ValueObject> gridExampleValues;

	public DemoUI() {
		this.appViewDisplay = new AppViewDisplay();
		appViewDisplay.setWidth("100%");
		Header header = new Header();
		content = new GridLayout(1, 2, header, appViewDisplay);
		content.setSizeFull();
		((GridLayout) content).setRowExpandRatio(1, 1f);

		converters.put("tabparam", new EnumValueConverter(ViewOne.TabEnum.class));
		final int[] i = {1};
		gridExampleValues = Stream.generate(() -> new ValueObject(i[0]++, UUID.randomUUID().toString())).limit(100).collect(Collectors.toList());

		ValueConverter singleValueConverter = new ValueConverter(gridExampleValues);
		converters.put("singleValueConverter", singleValueConverter);

		CollectionValueConverter<ValueObject, Set> multiValueConverter = new CollectionValueConverter(singleValueConverter, HashSet.class);
		converters.put("multiValueConverter", multiValueConverter);
	}

	@Override
	protected void init(VaadinRequest request) {
		this.navigator = new Navigator(this, null, appViewDisplay) {
			@Override
			public void navigateTo(String viewName) {
				String realView = viewName;
				if (StringUtils.isEmpty(viewName)) {
					realView = "one";
				}
				super.navigateTo(realView);
			}
		};

		ParameterFactory parameterFactory = new ClasspathParameterFactory("com.vaadin.wolfgang.demo", ViewName.class, "value", converters);
		ViewStateParameterFactory viewStateParameterFactory = new ViewStateParameterFactory(parameterFactory);
		ViewStateParameters vsp = new ViewStateParameters(viewStateParameterFactory, navigator);
		navigator.addView("one", new ViewOne(vsp));
		navigator.addView("two", new ViewTwo(vsp, gridExampleValues));
		navigator.addView("three", new ViewThree(vsp, gridExampleValues));
		// Initialize our new UI component
		setContent(content);
	}
}
