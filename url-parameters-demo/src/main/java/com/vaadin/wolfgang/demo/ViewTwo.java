package com.vaadin.wolfgang.demo;

import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.navigator.View;
import com.vaadin.ui.Grid;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.wolfgang.demo.data.ValueObject;
import com.vaadin.wolfgang.urlparameters.ViewName;
import com.vaadin.wolfgang.urlparameters.ViewParameter;
import com.vaadin.wolfgang.urlparameters.ViewStateParameters;

import java.util.List;

@ViewName("two")
@ViewParameter("selectedValue")
public class ViewTwo extends VerticalLayout implements View {
	private final Grid<ValueObject> grid = new Grid(ValueObject.class);

	public ViewTwo(ViewStateParameters viewStateParameters, List<ValueObject> gridExampleValues) {
		setWidth("100%");
		grid.setWidth("100%");
		grid.setDataProvider(new ListDataProvider<ValueObject>(gridExampleValues));
		addComponent(grid);
		grid.addSelectionListener(event -> {
			ValueObject valueObject = event.getFirstSelectedItem().orElse(null);
			viewStateParameters.putElement(ValueObject.class, valueObject);
		});
	}

	public void setSelectedValue(ValueObject valueObject) {
		if (valueObject != null) {
			grid.select(valueObject);
		}
	}
}
