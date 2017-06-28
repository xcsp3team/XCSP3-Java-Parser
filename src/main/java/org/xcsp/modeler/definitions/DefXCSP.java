package org.xcsp.modeler.definitions;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.xcsp.common.Utilities;

public class DefXCSP {

	/**
	 * Name of the element (constraint or objective). For example, this may be {@code allDifferent}, {@code regular}, or {@code extension}.
	 */
	public String name;

	/**
	 * Attributes associated with the constraint, that may be useful for defining the semantics.
	 */
	public List<SimpleEntry<String, Object>> attributes = new ArrayList<>();

	/**
	 * Parameters of the constraint, seen as children of the constraint.
	 */
	public List<Parameter> childs = new ArrayList<>();

	public boolean possibleSimplification;

	public Map<String, Object> map;

	private String print(List<SimpleEntry<String, Object>> list) {
		return list.size() == 0 ? "" : " (" + list.stream().map(a -> a.getKey() + "='" + a.getValue() + "'").collect(Collectors.joining(" ")) + ")";
	}

	private boolean same(List<SimpleEntry<String, Object>> list1, List<SimpleEntry<String, Object>> list2) {
		return list1.size() == list2.size() && IntStream.range(0, list1.size())
				.allMatch(i -> list1.get(i).getKey().equals(list2.get(i).getKey()) && list1.get(i).getValue().equals(list2.get(i).getValue()));
	}

	public class Parameter {
		public String name;
		public Object content;
		public List<SimpleEntry<String, Object>> attributes = new ArrayList<>();

		public Parameter(String name, Object content) {
			this.name = name;
			this.content = content;
		}

		public Parameter(String name, Object content, String attName, Object attValue) {
			this(name, content);
			addAttribute(attName, attValue);
		}

		public void addAttribute(String attName, Object attValue) {
			attributes.add(new SimpleEntry<>(attName, attValue));
		}

		@Override
		public String toString() {
			return name + " : " + content.toString() + print(attributes);
		}
	}

	public DefXCSP(String name, boolean possibleSimplification, Map<String, Object> map) {
		this.name = name;
		this.possibleSimplification = possibleSimplification;
		this.map = map;
	}

	public DefXCSP(String name, Map<String, Object> map) {
		this(name, true, map);
	}

	public DefXCSP(String name) {
		this(name, true, null);
	}

	public DefXCSP addChild(String name, Object content) {
		Utilities.control(content != null, "Pb");
		childs.add(new Parameter(name, content));
		return this;
	}

	public DefXCSP addChild(String name, Object content, String attName, Object attValue) {
		Utilities.control(content != null && attValue != null, "Pb");
		childs.add(new Parameter(name, content, attName, attValue));
		return this;
	}

	public DefXCSP add(String... params) {
		Stream.of(params).filter(param -> map.get(param) != null).forEach(param -> childs.add(new Parameter(param, map.get(param))));
		return this;
	}

	public DefXCSP addConditional(String param) {
		return map.containsKey(param) ? add(param) : this;
	}

	public DefXCSP addListOrLifted() {
		if (map.containsKey(ICtr.MATRIX)) {
			possibleSimplification = false;
			return add(ICtr.MATRIX);
		}
		String lifted = map.containsKey(ICtr.LISTS) ? ICtr.LIST : map.containsKey(ICtr.SETS) ? ICtr.SET : map.containsKey(ICtr.MSETS) ? ICtr.MSET : null;
		if (lifted == null)
			return add(ICtr.LIST); // basic variant
		Stream.of((Object[]) map.get(lifted + "s")).forEach(o -> addChild(lifted, o));
		return this;
	}

	public int[] differencesWith(DefXCSP def) {
		if (!name.equals(def.name) || attributes.size() != def.attributes.size() || childs.size() != def.childs.size()
				|| possibleSimplification != def.possibleSimplification)
			return null;
		if (!same(attributes, def.attributes))
			return null;
		if (IntStream.range(0, childs.size())
				.anyMatch(i -> !childs.get(i).name.equals(def.childs.get(i).name) || !same(childs.get(i).attributes, def.childs.get(i).attributes)))
			return null;
		return IntStream.range(0, childs.size()).filter(i -> !childs.get(i).content.toString().equals(def.childs.get(i).content.toString())).toArray();
	}

	@Override
	public String toString() {
		return name + " : " + childs.stream().map(c -> c.toString()).collect(Collectors.joining(" ")) + print(attributes);
	}
}
