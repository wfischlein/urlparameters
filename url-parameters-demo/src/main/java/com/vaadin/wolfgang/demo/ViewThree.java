package com.vaadin.wolfgang.demo;

import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.navigator.View;
import com.vaadin.ui.Grid;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.components.grid.MultiSelectionModel;
import com.vaadin.wolfgang.demo.data.ValueObject;
import com.vaadin.wolfgang.urlparameters.ViewMethodParameter;
import com.vaadin.wolfgang.urlparameters.ViewName;
import com.vaadin.wolfgang.urlparameters.ViewStateParameters;
import lombok.NonNull;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@ViewName("three")
public class ViewThree extends VerticalLayout implements View {
	private final Grid<ValueObject> grid = new Grid<>(ValueObject.class);

	ViewThree(ViewStateParameters viewStateParameters, List<ValueObject> gridExampleValues) {
		setWidth("100%");
		grid.setWidth("100%");
		grid.setDataProvider(new ListDataProvider<>(gridExampleValues));

		MultiSelectionModel selectionModel = (MultiSelectionModel) grid.setSelectionMode(Grid.SelectionMode.MULTI);
		selectionModel.setSelectAllCheckBoxVisibility(MultiSelectionModel.SelectAllCheckBoxVisibility.VISIBLE);

		addComponent(grid);
		grid.addSelectionListener(event -> {
			viewStateParameters.putElements(ValueObject.class, event.getAllSelectedItems());
		});
	}

	@ViewMethodParameter
	public void setSelectedItems(@NonNull Set<ValueObject> selectedItems) {
		MultiSelectionModel<ValueObject> multiSelectionModel = (MultiSelectionModel<ValueObject>) grid.getSelectionModel();
		multiSelectionModel.updateSelection(new HashSet<>(selectedItems), new HashSet<>(multiSelectionModel.getSelectedItems()));
	}
}
