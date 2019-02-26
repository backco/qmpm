package net.dcrgraphs.core;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

public class LabelledDCRGraph extends DCRGraph {
	private HashMap<String, HashSet<String>> labels = new HashMap<String, HashSet<String>>();

	@Override
	public HashSet<String> getActivities() {
		return new HashSet<String>(labels.keySet());
	}

	public void addLabel(String event, String label) {
		if (!labels.containsKey(label))
			labels.put(label, new HashSet<String>());
		labels.get(label).add(event);
	}

	public void removeLabel(String event, String label) {
		if (labels.containsKey(label))
			labels.get(label).remove(event);
	}

	@Override
	public void addEvent(String event) {
		super.addEvent(event);
		addLabel(event, event);
	}

	public void addEvent(String event, String label) {
		super.addEvent(event);
		addLabel(event, label);
	}

	public Boolean enabledLabel(final DCRMarking marking, final String label) {
		for (String event : labels.get(label))
			if (super.enabled(marking, event))
				return true;
		return false;
	}

	public void executeLabel(final String label) {
		marking = this.execute(marking, label);
	}

	public DCRMarking executelabel(final DCRMarking marking, final String label) {
		if (!labels.containsKey(label)) {
			return marking;
		}
		if (!enabledLabel(marking, label)) {
			return marking;
		}

		for (String event : labels.get(label)) {
			if (super.enabled(marking, event)) {
				return super.execute(marking, event);
			}
		}

		// shouldn't get here anyway...
		System.out.println("Invalid execution path.");
		return marking;
	}

	@Override
	public String name(String e) {
		for (String l : labels.keySet())
			if (labels.get(l).contains(e))
				return l;
		return null;
	}

	@Override
	public Set<String> names() {
		return labels.keySet();
	}

	@Override
	public void run(final List<String> trace) {
		marking = this.run(marking, trace);
	}

	@Override
	public DCRMarking run(final DCRMarking marking, List<String> trace) {
		DCRMarking m = marking.clone();
		for (String l : trace) {
			if (!labels.containsKey(l)) {
				// skip
			} else if (!enabledLabel(m, l))
				return null;
			else
				m = executelabel(m, l);
		}
		return m;
	}

	@Override
	public String toString() {
		final StringBuilder result = new StringBuilder();
		final String NEW_LINE = System.getProperty("line.separator");

		result.append(this.getClass().getName() + " Labelled DCR Graph {" + NEW_LINE);

		result.append(" Core DCR Graph: ");
		result.append(NEW_LINE);
		result.append(super.toString());
		result.append(NEW_LINE);

		result.append(" Labels: ");
		result.append(NEW_LINE);
		for (Entry<String, HashSet<String>> r : labels.entrySet()) {
			String trg = r.getKey();
			for (String src : r.getValue())
				result.append("    l(" + src + ") = " + trg + ";" + NEW_LINE);
		}
		result.append(NEW_LINE);

		result.append("}");

		return result.toString();
	}

}
