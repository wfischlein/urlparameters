package com.vaadin.wolfgang.demo;

import com.vaadin.navigator.View;
import com.vaadin.ui.Label;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;
import com.vaadin.wolfgang.urlparameters.ViewMethodParameter;
import com.vaadin.wolfgang.urlparameters.ViewName;
import com.vaadin.wolfgang.urlparameters.ViewStateParameters;

import java.util.HashMap;
import java.util.Map;

@ViewName("one")
public class ViewOne extends VerticalLayout implements View {
	private Map<TabSheet.Tab, TabEnum> tabMap = new HashMap<>();
	private final TabSheet tabSheet;

	public enum TabEnum {
		TAB_ONE("Tab One"), TAB_TWO("Tab Two"), TAB_THREE("Tab Three");

		private final String displayname;

		TabEnum(String displayname) {
			this.displayname = displayname;
		}
	}

	public ViewOne(ViewStateParameters viewStateParameters) {
		tabSheet = new TabSheet();
		tabSheet.setHeight(100.0f, Unit.PERCENTAGE);
		tabSheet.addStyleName(ValoTheme.TABSHEET_FRAMED);
		tabSheet.addStyleName(ValoTheme.TABSHEET_PADDED_TABBAR);
		for (TabEnum tabEnum : TabEnum.values()) {
			final Label label = new Label("This is the content of " + tabEnum.displayname);
			label.setWidth(100.0f, Unit.PERCENTAGE);

			final VerticalLayout layout = new VerticalLayout(label);
			layout.setMargin(true);

			TabSheet.Tab tab = tabSheet.addTab(layout, tabEnum.displayname);
			tabMap.put(tab, tabEnum);
		}
		addComponent(tabSheet);

		tabSheet.addSelectedTabChangeListener(new TabSheet.SelectedTabChangeListener() {
			@Override
			public void selectedTabChange(TabSheet.SelectedTabChangeEvent event) {
				TabSheet.Tab tab = tabSheet.getTab(tabSheet.getSelectedTab());
				viewStateParameters.putElement(TabEnum.class, tabMap.get(tab));
			}
		});
	}

	@ViewMethodParameter(defaultValue = "TAB_TWO")
	public void setViewTab(TabEnum tabToSet) {
		tabMap.forEach((tab, tabEnum) -> {
			if (tabEnum == tabToSet) {
				tabSheet.setSelectedTab(tab);
				return;
			}
		});
	}
}
