package com.vaadin.wolfgang.urlparameters.impl;

import com.vaadin.wolfgang.urlparameters.Converter;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

public class MultiValueConverterTest {
	@DataProvider
	private Object[][] data() {
		return new Object[][]{
				new Object[]{"(ONE,TWO,THREE)", Arrays.asList(TestEnum.ONE, TestEnum.TWO, TestEnum.THREE)},
				new Object[]{"(THREE,TWO,ONE)", Arrays.asList(TestEnum.THREE, TestEnum.TWO, TestEnum.ONE)},
				new Object[]{"ONE", Arrays.asList(TestEnum.ONE)},
				new Object[]{"TWO", Arrays.asList(TestEnum.TWO)},
				new Object[]{"", Collections.emptySet()},
		};
	}

	@Test(dataProvider = "data")
	public void identifyTest(String stringRepresentation, Collection<TestEnum> elements) {
		Converter<TestEnum> valueConverter = new EnumValueConverter<>(TestEnum.class);
		MultiValueConverter<TestEnum, ArrayList> candidate = new MultiValueConverter<>(valueConverter, ArrayList.class);
		String s = candidate.getStringRepresentation(elements);
		Assert.assertEquals(stringRepresentation, s);
		Collection<TestEnum> internalByString = candidate.getInternalObject(stringRepresentation);
		Assert.assertEquals(elements, internalByString);
	}

	public enum TestEnum {
		ONE, TWO, THREE
	}

}