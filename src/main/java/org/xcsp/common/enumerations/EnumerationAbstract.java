/**
 * AbsCon - Copyright (c) 2017, CRIL-CNRS - lecoutre@cril.fr
 * 
 * All rights reserved.
 * 
 * This program and the accompanying materials are made available under the terms of the CONTRAT DE LICENCE DE LOGICIEL
 * LIBRE CeCILL which accompanies this distribution, and is available at http://www.cecill.info
 */
package org.xcsp.common.enumerations;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;

import org.xcsp.common.Utilities;

public abstract class EnumerationAbstract implements Iterator<int[]> {

	/**
	 * Indicates if the method {@code hasNext()} has already been called. This is not the case if the value of this field is {@code null}.
	 * Otherwise, the value of this field indicates the presence ({@code Boolean.TRUE}) or the absence ({@code Boolean.FALSE}) of another
	 * tuple already computed.
	 */
	protected Boolean nextTuple;

	/**
	 * The current tuple composed of indexes (of values)
	 */
	protected final int[] currTupleOfIdxs;

	/**
	 * The current tuple composed of values
	 */
	protected final int[] currTupleOfVals;

	/**
	 * Indicates if indexes match values (i.e., if the value at index {@code i} is always {@code i}).
	 */
	protected final boolean areIdxsEqualToVals;

	/**
	 * Builds an object that can be used for enumerating tuples, each of them with the specified length.
	 * 
	 * @param tupleLength
	 *            the length of each tuple in the enumeration
	 */
	public EnumerationAbstract(int tupleLength, boolean areIdxsEqualToVals) {
		this.currTupleOfIdxs = new int[tupleLength];
		this.currTupleOfVals = new int[tupleLength];
		this.areIdxsEqualToVals = areIdxsEqualToVals;
	}

	/**
	 * Computes the first tuple (called at reset time).
	 */
	protected abstract void computeFirstTuple();

	/**
	 * Resets the object, so as to be able to iterate again over all tuples of the enumeration.
	 */
	public void reset() {
		nextTuple = Boolean.TRUE; // true because the first tuple is computed below
		computeFirstTuple();
	}

	/**
	 * Returns the value of the current tuple at the specified position.
	 * 
	 * @param pos
	 *            the position of an integer in the tuple
	 * @return the value of the current tuple at the specified position
	 */
	protected abstract int valAt(int pos);

	private int[] vals() {
		if (areIdxsEqualToVals)
			return currTupleOfIdxs;
		for (int i = 0; i < currTupleOfVals.length; i++)
			currTupleOfVals[i] = valAt(i);
		return currTupleOfVals;
	}

	// /**
	// * Determines if there is another element in the enumeration (strictly greater than the current one).
	// *
	// * @return {@code true} if there is another tuple
	// */

	@Override
	public abstract boolean hasNext();

	// /**
	// * Returns the next combination. DO NOT MODIFY the tuple that is returned.
	// */

	@Override
	public int[] next() {
		if (nextTuple == null)
			hasNext(); // determine if there is another tuple by computing it
		int[] t = nextTuple == Boolean.FALSE ? null : vals();
		nextTuple = nextTuple == Boolean.FALSE ? Boolean.FALSE : null;
		return t;
	}

	public int[][] allTuples(Predicate<int[]> p) {
		reset();
		List<int[]> list = new ArrayList<>();
		while (hasNext()) {
			int[] x = next();
			if (p.test(x))
				list.add(x.clone());
		}
		return list.stream().toArray(int[][]::new);
	}

	public int[][] allTuples() {
		reset();
		List<int[]> list = new ArrayList<>();
		while (hasNext())
			list.add(next().clone());
		return list.stream().toArray(int[][]::new);
	}

	/**
	 * Displays all tuples of this enumeration.
	 */
	public void displayAllTuples() {
		reset();
		int cnt = 0;
		while (hasNext()) {
			System.out.println("(" + Utilities.join(next(), ",") + ") ");
			cnt++;
		}
		System.out.println("\nThere are " + cnt + " tuples");
	}
}
