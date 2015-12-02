/**
 * Combinatorics Library
 * Copyright 2012 Dmytro Paukov d.paukov@gmail.com
 */
package org.paukov.combinatorics;

import java.util.List;

/**
 * This interface is implemented by all generic combinatorics vectors of the
 * library
 * 
 * @author Dmytro Paukov
 * @see Factory
 * @version 2.0
 * @param <T>
 *            Type of the elements
 */
public interface ICombinatoricsVector<T> extends Iterable<T> {

	/**
	 * Sets value to position <code>index</code>. If the index is out of bounds
	 * the value will be added at the end of the vector
	 * 
	 * @param index
	 *            Position of element
	 * @param value
	 *            Value of the element to be set
	 */
	public abstract void setValue(int index, T value);

	/**
	 * Adds value to the vector.
	 * 
	 * @param value
	 *            Value of the element to be added
	 */
	public abstract boolean addValue(T value);

	/**
	 * Returns value of the <code>index</code>-element
	 * 
	 * @param index
	 *            The position of the element (Index of the element)
	 * @return Value of the element
	 */
	public abstract T getValue(int index);

	/**
	 * Returns size of the vector
	 * 
	 * @return Current size of the vector
	 */
	public abstract int getSize();

	/**
	 * This method detects duplicates in the vector
	 * 
	 * @return <code>true</code> if the vector has duplicates, otherwise
	 *         <code>false</code>
	 */
	public abstract boolean hasDuplicates();

	/**
	 * This method calculates the count of duplicates of a given value
	 * 
	 * @return Number of the duplicates within the vector
	 */
	public abstract int countElements(T value);

	/**
	 * This method detects if the victor contains a given value
	 * 
	 * @return <code>true</code> if the vector contains the value, otherwise
	 *         <code>false</code>
	 */
	public abstract boolean contains(T value);

	/**
	 * This method detects if the vector's elements are equal. For example, this
	 * method returns true for a vector (a, a, a) and false for a vector (a, b,
	 * a)
	 * 
	 * @return <code>True</code> if the vector's elements are equal
	 */
	public abstract boolean isAllElementsEqual();

	/**
	 * Returns vector as a list of elements
	 * 
	 * @return List of all elements
	 */
	public abstract List<T> getVector();

	/**
	 * Clear the elements
	 */
	public abstract void clearVector();
}