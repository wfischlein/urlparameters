package com.vaadin.wolfgang.demo;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewDisplay;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;

public class AppViewDisplay extends CssLayout implements ViewDisplay {

	@Override
	public void showView(View view) {
		removeAllComponents();
		addComponent((Component) view);
	}
}
