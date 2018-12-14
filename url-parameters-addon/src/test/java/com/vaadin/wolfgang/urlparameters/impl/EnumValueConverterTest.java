package com.vaadin.wolfgang.urlparameters.impl;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class EnumValueConverterTest {
	@DataProvider
	private Object[][] data() {
		TestEnum[] values = TestEnum.values();
		Object[][] result = new Object[values.length][1];
		for (int i = 0; i < values.length; i++) {
			result[i][0] = values[i];
		}
		return result;
	}

	@Test(dataProvider = "data")
	public void identifyTest(TestEnum testEnum) {
		EnumValueConverter<TestEnum> candidate = new EnumValueConverter(TestEnum.class);

		String s = candidate.getStringRepresentation(testEnum);
		Assert.assertEquals(s, testEnum.name());
		TestEnum internal = candidate.getInternalObject(s);
		Assert.assertEquals(internal, testEnum);
	}

	public enum TestEnum {
		ONE, TWO, THREE
	}
}