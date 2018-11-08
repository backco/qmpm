package net.dcrgraphs.core; 

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

public class DCRGraph {
	protected HashSet<String> events = new HashSet<String>();	
	private HashMap<String, HashSet<String>> conditionsFor = new HashMap<String, HashSet<String>>();
	private HashMap<String, HashSet<String>> milestonesFor = new HashMap<String, HashSet<String>>();	
	private HashMap<String, HashSet<String>> responsesTo = new HashMap<String, HashSet<String>>();
	private HashMap<String, HashSet<String>> excludesTo = new HashMap<String, HashSet<String>>();
	private HashMap<String, HashSet<String>> includesTo = new HashMap<String, HashSet<String>>();
	public DCRMarking marking;
	
	private HashMap<String, HashSet<String>> conditionsTo = new HashMap<String, HashSet<String>>();
	
	private int relationCount = 0;
	
	public HashSet<String> getActivities(){
		return events;
	}
	
	public void addEvent(String e)
	{
		events.add(e);
		conditionsFor.put(e, new HashSet<String>());
		conditionsTo.put(e, new HashSet<String>());
		milestonesFor.put(e, new HashSet<String>());
		responsesTo.put(e, new HashSet<String>());
		excludesTo.put(e, new HashSet<String>());
		includesTo.put(e, new HashSet<String>());		
	}
	
	
	protected String randomAlphabeticString(int length)
	{
		char[] chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
		StringBuilder sb = new StringBuilder();
		Random random = new Random();
		for (int i = 0; i < length; i++) {
		    char c = chars[random.nextInt(chars.length)];
		    sb.append(c);
		}
		return sb.toString();	
	}
	
	
	public String newEvent()
	{
		String newName = "g" + randomAlphabeticString(10);
		while (events.contains(newName))
			newName = "g" + randomAlphabeticString(10);
		addEvent(newName);
		return newName;
	}
	
	public void addCondition(String src, String trg)
	{
		conditionsFor.get(trg).add(src);
		conditionsTo.get(src).add(trg);
		relationCount++;
	}
	
	public void addMilestone(String src, String trg)
	{
		milestonesFor.get(trg).add(src);
		relationCount++;
	}
	
	public void addResponse(String src, String trg)
	{
		responsesTo.get(src).add(trg);
		relationCount++;
	}	
	
	public void addExclude(String src, String trg)
	{
		excludesTo.get(src).add(trg);
		relationCount++;
	}	
	
	public void addInclude(String src, String trg)
	{
		includesTo.get(src).add(trg);
		relationCount++;
	}		
	
	public String name(String e)
	{
		return e;
	}	

	public Boolean enabled(final DCRMarking marking, final String event) {		
		if (!events.contains(event)) { return true; }
		// check included
		if (!marking.included.contains(event)) { return false;
		// check conditions
		}

		// if (!m.executed.containsAll(RelationsFor(conditions, e)))
		final Set<String> inccon = new HashSet<String>(conditionsFor.get(event));
		inccon.retainAll(marking.included);
		if (!marking.executed.containsAll(inccon)) { return false; }

		// check milestones
		final Set<String> incmil = new HashSet<String>(milestonesFor.get(event));
		incmil.retainAll(marking.included);

		for (final String p : marking.pending) {
			if (incmil.contains(p)) { return false; }
		}
		
		return true;
	}
	
	public Set<String> getAllEnabled(DCRMarking m)
	{
		HashSet<String> result = new HashSet<String>();
		
		for (String e : m.included)
			if (this.enabled(m, e))
				result.add(e);
		
		return result;
	}
	
	public Set<String> getIncludedPending()
	{
		HashSet<String> result = new HashSet<String>(marking.included);
		result.retainAll(marking.pending);		
		return result;
	}	
	
	public boolean isAccepting()
	{
		return getIncludedPending().isEmpty();
	}
	
	public void execute(final String event) {
		marking = execute(marking, event);		
	}	

	public DCRMarking execute(final DCRMarking marking, final String event) {
		if (!events.contains(event)) { return marking; }

		if (!this.enabled(marking, event)) { return marking; }
		
		DCRMarking result = marking.clone();
		
		if (!(conditionsTo.get(event).isEmpty()))
			result.executed.add(event);
		
		result.pending.remove(event);
		result.pending.addAll(responsesTo.get(event));
		result.included.removeAll(excludesTo.get(event));
		result.included.addAll(includesTo.get(event));
		
		return result;
	}
	
	public void run(final List<String> trace) {
		marking = run(marking, trace);
	}		
	
	public DCRMarking run(final DCRMarking marking, List<String> trace)
	{
		DCRMarking m = marking.clone();
		for (String e : trace)
		{
			if (!enabled(m, e))
				return null;
			else
				m = execute(m,e);
		}		
		return m;
	}

	public DCRMarking defaultInitialMarking() {
		final DCRMarking result = new DCRMarking();
		for (final String e : events) {
			result.included.add(e);
		}
		return result;
	}

	@Override
	public String toString() {
		final StringBuilder result = new StringBuilder();
		final String NEW_LINE = System.getProperty("line.separator");

		result.append(this.getClass().getName() + " DCR Graph {" + NEW_LINE);
		result.append(" Events: ");
		for (final String e : events) {
			result.append(e + "; ");
		}
		result.append(NEW_LINE);

		result.append(" Conditions: ");
		for (Entry<String, HashSet<String>> r : conditionsFor.entrySet()) {
			String trg = r.getKey();
			for (String src : r.getValue())
				result.append(src + " ->* " + trg + ";");
		}
		result.append(NEW_LINE);
		

		result.append(" Repsonses: ");
		for (Entry<String, HashSet<String>> r : responsesTo.entrySet()) {
			String src = r.getKey();
			for (String trg : r.getValue())
				result.append(src + " *-> " + trg + ";");
		}
		result.append(NEW_LINE);		
		
		result.append(" Exclusions: ");
		for (Entry<String, HashSet<String>> r : excludesTo.entrySet()) {
			String src = r.getKey();
			for (String trg : r.getValue())
				result.append(src + " ->% " + trg + ";");
		}
		result.append(NEW_LINE);
		
		result.append(" Inclusions: ");
		for (Entry<String, HashSet<String>> r : includesTo.entrySet()) {
			String src = r.getKey();
			for (String trg : r.getValue())
				result.append(src + " ->+ " + trg + ";");
		}
		result.append(NEW_LINE);		
		
				
		result.append(" Milestones: ");
		for (Entry<String, HashSet<String>> r : milestonesFor.entrySet()) {
			String trg = r.getKey();
			for (String src : r.getValue())
				result.append(src + " -><> " + trg + ";");
		}
		result.append(NEW_LINE);		
		
		if (marking != null)
		{
			result.append(" Marking: " + NEW_LINE);
			result.append(marking.toString());
		}
		
		// Note that Collections and Maps also override toString
		// result.append(" RelationID: " + relationID.toString() + NEW_LINE);
		result.append("}");

		return result.toString();
	}


	public Set<String> names() {
		return events;		
	}
	
	/*
	 * @author Christoffer
	 */
	public int getNumOfRelations() {
		return relationCount;
	}
}
