package com.vaadin.wolfgang.urlparameters;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The ViewParameter annotation is used to make a View aware of parameters in the URL.
 * There are several ways to do that:
 * '@ViewParameter("myparam")' is not sufficient because then the type of the parameter is not known.
 * When starting the container a AnnotationTypeConfigurationException will be thrown.
 *
 * '@ViewParameter(value = "myparam", valueClass = Parameter.class)' would work when there is a Converter bean there 'internalClass' is Parameter.class.
 * '@ViewParameter(value = "myparam", valueClass = Parameter.class, convertedBy = ParamConverter.class)' is fully specified: the value type is known, the converter as well
 *
 * '@ViewParameter("myparam")' can be sufficient as soon as the annotated class has either a method
 * <code>public Parameter getMyparam()</code> or a method
 * <code>public void setMyparam(Parameter parameter)</code> or both. Then the type of parameter is known as well.
 * The name 'myparam' is considered as a bean property. AND: Whenever a view is entered that has the setter method declared
 * the setter will be called with the converted value of the parameter.
 *
 *
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ViewParameter {
	/**
	 * Value string: The name of the parameter. It will show up in the URL, and be used as a property to be found in the
	 * annotated class to set or get an actual value.
	 * @return the key for the parameter in the url (like: "key=value")
	 */
	String value();

	/**
	 * Default value string: Set this when you want to define a value for situations where no value is present in the URL
	 * For example: enter a view that shows tabs like 'PENDING', 'APPROVED', 'DENIED' the first time. Without a default
	 * value it is not determined what tab will be selected initially. Configure the value to change that
	 * @return a string to represent the default walue if no value is provided in the url (typically the initial situation
	 * when a view is addressed the first time)
	 */
	String defaultValue() default "";

	/**
	 * Value class: Configure that value when you do not need to have getters or setters to determine the class of the
	 * url parameter value.
	 * @return the parameter class in that case
	 */
	Class<?> valueClass() default void.class;

	/**
	 * Converted by class: Configure a converter class here when you want to overrule the converter that is configured
	 * in the spring container
	 * @return the converter class when you prefer to configure the converter this way
	 */
	Class<? extends Converter> convertedBy() default Converter.class;
}
