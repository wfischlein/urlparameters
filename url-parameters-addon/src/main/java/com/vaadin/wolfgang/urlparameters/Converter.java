package com.vaadin.wolfgang.urlparameters;

import java.lang.reflect.Type;

/**
 * The interface Converter is primarily used to convert between string values out of an URL and internal Objets represented
 * by these strings. They get identified via their internalClass property. There are implementations for repository access
 * and enumerated values available.
 *
 * @param <T> the type parameter
 */
public interface Converter<T> {
	default boolean converts(Type valueClass) {
		return valueClass.equals(getInternalClass());
	}

	Type getInternalClass();

	/**
	 * Create or find an instance of internalClass according to the passed stringRepresentation. For an ICO the string
	 * could be like "4711", the internal class InitialCoinOffer.class, the result an InitialCoinOffer instance with
	 * the id 4711.
	 * For an enumerated tab value the String could be "PENDING", the internalClass IdentityVerificationState.UserVerificationState.class,
	 * the internal value IdentityVerificationState.UserVerificationState.PENDING
	 * @param stringRepresentation How the parameter is represented in the url
	 * @return the actual instance represented py the stringRepresentation
	 */
	T getInternalObject(String stringRepresentation);

	/**
	 * Produce the string representation of the given object that you are able to identify via 'getInternalObject.
	 * For an ICO with the id 4711 this could be "4711" when the the internal class is InitialCoinOffer.class,
	 * for an enumerated value instance like IdentityVerificationState.UserVerificationState.PENDING it could be
	 * "PENDING" (or the ordinal value.toString like "3" when your 'getInternalObject' implementation understands
	 * the given string as a numeric string that encrypts the ordinal value of the enumerated value)
	 * @param o the internal representation of the url parameter value
	 * @return the string repesentation to be shown in the url
	 */
	String getStringRepresentation(T o);

}
