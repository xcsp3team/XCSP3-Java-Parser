/**
 * AbsCon - Copyright (c) 2017, CRIL-CNRS - lecoutre@cril.fr
 * 
 * All rights reserved.
 * 
 * This program and the accompanying materials are made available under the terms of the CONTRAT DE LICENCE DE LOGICIEL LIBRE CeCILL which accompanies this
 * distribution, and is available at http://www.cecill.info
 */
package org.xcsp.modeler.problems;

import org.xcsp.common.IVar.Var;
import org.xcsp.modeler.ProblemAPI;

public class Blackhole implements ProblemAPI {

	int nCardsPerSuit, nCardsPerPile;
	int[][] piles;

	@Override
	public void model() {
		int nCards = 4 * nCardsPerSuit, nPiles = (nCards - 1) / nCardsPerPile;

		Var[] x = array("x", size(nCards), dom(range(nCards)), "x[i] is the value j of the card at the ith position of the built stack.");
		Var[] y = array("y", size(nCards), dom(range(nCards)), "y[j] is the position i of the card whose value is j");

		channel(x, y);
		equal(y[0], 0).note("The Ace of Spades is initially put on the stack");

		forall(range(nPiles), i -> ordered(range(nCardsPerPile).provideVars(j -> y[piles[i][j]]), STRICTLY_INCREASING))
				.note("Cards must be played in the order of the piles");

		int[][] tuples = range(nCards).range(nCards)
				.select((i, j) -> i % nCardsPerSuit == (j + 1) % nCardsPerSuit || j % nCardsPerSuit == (i + 1) % nCardsPerSuit);
		slide(x, range(nCards - 1), i -> extension(vars(x[i], x[i + 1]), tuples))
				.note("Each new card put on the stack must be a rank higher or lower than the previous one.");
	}
}
