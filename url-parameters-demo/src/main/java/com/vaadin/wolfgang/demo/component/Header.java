package com.vaadin.wolfgang.demo.component;

import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.UI;

public class Header extends HorizontalLayout {
	public Header() {
		Button one = new Button("Tab Example");
		one.addClickListener(event -> UI.getCurrent().getNavigator().navigateTo(""));
		Button two = new Button("Grid Example");
		two.addClickListener(event -> UI.getCurrent().getNavigator().navigateTo("two"));
		Button three = new Button("Grid multi Example");
		three.addClickListener(event -> UI.getCurrent().getNavigator().navigateTo("three"));
		addComponents(one, two, three);
	}
}
