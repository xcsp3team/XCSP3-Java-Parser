package org.xcsp.common.structures;

import static org.xcsp.common.Constants.STAR_INT;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.xcsp.common.Constants;
import org.xcsp.common.Utilities;
import org.xcsp.common.enumerations.EnumerationCartesian;

/**
 * This class allows us to represent integer tables that are useful objects when defining {@code extension} constraints.
 */
public class Table extends TableAbstract {

	public static int[][] toOrdinaryTable(int[][] shortTable, int[][] values) {
		List<int[]> tuples = new ArrayList<>();
		for (int[] t : shortTable) {
			int[] pos = IntStream.range(0, t.length).filter(i -> t[i] == Constants.STAR_INT).toArray();
			if (pos.length == 0)
				tuples.add(t.clone());
			else {
				EnumerationCartesian ec = new EnumerationCartesian(
						IntStream.range(0, t.length).mapToObj(i -> t[i] == Constants.STAR_INT ? values[i] : new int[] { t[i] }).toArray(int[][]::new));
				while (ec.hasNext())
					tuples.add(ec.next().clone());
			}
		}
		return tuples.stream().sorted(Utilities.lexComparatorInt).distinct().toArray(int[][]::new);
		// return Kit.intArray2D(tuples);
	}

	public static int[][] toOrdinaryTable(int[][] shortTable, int... nValues) {
		return toOrdinaryTable(shortTable, IntStream.of(nValues).mapToObj(i -> IntStream.range(0, i).toArray()).toArray(int[][]::new));
	}

	@Override
	public Table positive(Boolean positive) {
		this.positive = positive;
		return this;
	}

	private List<int[]> list = new ArrayList<>();

	@Override
	public int size() {
		return list.size();
	}

	/**
	 * Adds an integer tuple to the table.
	 * 
	 * @param tuple
	 *            an integer tuple
	 * @return this integer table
	 */
	public Table add(int... tuple) {
		Utilities.control(tuple.length > 0, "A tuple of length 0 has been encoutered during parsing.\n" + TABLE_SYNTAX_PB);
		Utilities.control(list.size() == 0 || list.get(0).length == tuple.length, "The tuple has a different length from those already recorded");
		list.add(tuple);
		return this;
	}

	/**
	 * Adds an integer tuple to the table if the condition evaluates to {@code true}
	 * 
	 * @param condition
	 *            a Boolean condition
	 * @param tuple
	 *            an integer tuple
	 * @return this integer table
	 */
	public Table addIf(boolean condition, int... tuple) {
		if (condition)
			add(tuple);
		return this;
	}

	/**
	 * Adds the specified integer tuples to the table.
	 * 
	 * @param tuples
	 *            a sequence of other tuples
	 * @return this integer table
	 */
	public Table add(int[]... tuples) {
		Stream.of(tuples).forEach(t -> add(t));
		return this;
	}

	/**
	 * Adds all tuples of the specified stream to the table.
	 * 
	 * @param stream
	 *            a stream of tuples to be added to the table
	 * @return this integer table
	 */
	public Table add(Stream<int[]> stream) {
		stream.forEach(t -> add(t));
		return this;
	}

	/**
	 * Adds all tuples of the specified table to this table.
	 * 
	 * @param table
	 *            another table
	 * @return this integer table
	 */
	public Table add(Table table) {
		Stream.of(table.toArray()).forEach(t -> add(t));
		return this;
	}

	/**
	 * Adds the tuples obtained after parsing the specified string. The string must represent a sequence of tuples as defined in XCSP3. For example,
	 * it could be {@code "(0,0,1)(0,2,0)(1,0,1)(1,1,2)"}.
	 * 
	 * @param s
	 *            a string representing a sequence of integer tuples
	 * @return this integer table
	 */
	public Table add(String s) {
		boolean b = controlStringRepresentationOfTuples(s);
		Utilities.control(b, "The specified string is not correct, as it does not correspond to a sequence of integer tuples");
		int[][] tuples = Stream.of(s.split(Constants.DELIMITER_LISTS)).skip(1).map(
				tok -> Stream.of(tok.split("\\s*,\\s*")).mapToInt(v -> v.equals(Constants.STAR_SYMBOL) ? Constants.STAR_INT : Integer.parseInt(v)).toArray())
				.toArray(int[][]::new);
		Stream.of(tuples).forEach(tuple -> add(tuple));
		return this;
	}

	private int[] intersectionOf(int[] t1, int[] t2, int[] out) {
		assert t1.length == t2.length;
		for (int i = 0; i < t1.length; i++) {
			if (t1[i] == t2[i])
				out[i] = t1[i];
			else if (t1[i] == STAR_INT)
				out[i] = t2[i];
			else if (t2[i] == STAR_INT)
				out[i] = t1[i];
			else
				return null;
		}
		return out;
	}

	public Table intersectionWith(Table other) {
		Utilities.control(positive && other.positive, "Tables must be both positive");
		int[][] m1 = this.toArray();
		int[][] m2 = other.toArray();
		Utilities.control(m1[0].length == m2[0].length, "Not the same arity");
		if (m1.length > m2.length) {
			int[][] m = m1;
			m1 = m2;
			m2 = m;
		}
		int[] tmp = new int[m1[0].length];
		Set<int[]> setOfSupports = new TreeSet<>(Utilities.lexComparatorInt);
		for (int[] t1 : m1) {
			IntStream.range(0, tmp.length).forEach(i -> tmp[i] = t1[i] == STAR_INT ? Integer.MIN_VALUE : t1[i]); // we compute tmp from t1
			int index = Arrays.binarySearch(m2, tmp, Utilities.lexComparatorInt);
			if (index >= 0)
				setOfSupports.add(t1.clone());
			else {
				for (int i = -index - 1; i < m2.length; i++) {
					// if (Utilities.lexicographicInt.compare(lowerTuple(m2[i], tmp), t1) > 0) break; else
					if (intersectionOf(t1, m2[i], tmp) != null)
						setOfSupports.add(tmp.clone());
				}
			}
		}
		return new Table().add(setOfSupports.stream()); // .toArray(new int[setOfSupports.size()][]);
	}

	public Table addColumnWithValue(int position, int value) {
		int[][] m = this.toArray();
		Utilities.control(0 <= position && position <= m[0].length, "bad value of column position");
		return new Table().add(IntStream.range(0, m.length)
				.mapToObj(i -> IntStream.range(0, m[0].length + 1).map(j -> j < position ? m[i][j] : j == position ? STAR_INT : m[i][j - 1]).toArray()));
	}

	/**
	 * Returns a 2-dimensional array corresponding to the collected tuples. Tuples are sorted and made distinct.
	 * 
	 * @return a 2-dimensional array corresponding to the collected tuples
	 */
	public int[][] toArray() {
		return list.stream().sorted(Utilities.lexComparatorInt).distinct().toArray(int[][]::new);
	}

	public int[][] toOrdinaryTableArray(int[][] values) {
		return Table.toOrdinaryTable(toArray(), values);
	}

	public int[][] toOrdinaryTableArray(int... nValues) {
		return Table.toOrdinaryTable(toArray(), nValues);
	}

	@Override
	public String toString() {
		return list.stream().map(t -> Utilities.join(t)).collect(Collectors.joining("\n"));
	}

}