package com.vaadin.wolfgang.urlparameters;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The interface ViewParameters' only purpose is to group annotations of type ViewParameter in case you want to configure
 * more than one ViewParameter against one View class
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ViewParameters {
	ViewParameter[] value();
}
