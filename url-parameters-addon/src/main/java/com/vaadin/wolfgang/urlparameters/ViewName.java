package com.vaadin.wolfgang.urlparameters;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The ViewName annotation is used to assign a name to an element that can be navigated to when you are not in a spring environment.
 * In Spring you use @SpringView instead - and configure the ClasspathParameterFactory accordingly
 * There are several ways to do that:
 * '@ViewParameter("myparam")' is not sufficient because then the type of the parameter is not known.
 * When starting the container a AnnotationTypeConfigurationException will be thrown.
 * <p>
 * '@ViewParameter(value = "myparam", valueClass = Parameter.class)' would work when there is a Converter bean there 'internalClass' is Parameter.class.
 * '@ViewParameter(value = "myparam", valueClass = Parameter.class, convertedBy = ParamConverter.class)' is fully specified: the value type is known, the converter as well
 * <p>
 * '@ViewParameter("myparam")' can be sufficient as soon as the annotated class has either a method
 * <code>public Parameter getMyparam()</code> or a method
 * <code>public void setMyparam(Parameter parameter)</code> or both. Then the type of parameter is known as well.
 * The name 'myparam' is considered as a bean property. AND: Whenever a view is entered that has the setter method declared
 * the setter will be called with the converted value of the parameter.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ViewName {
	/**
	 * Value string: The name of the parameter. It will show up in the URL, and be used as a property to be found in the
	 * annotated class to set or get an actual value.
	 * @return the name by that the view is supposed to be identified
	 */
	String value();

}
