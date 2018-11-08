package net.dcrgraphs.core;

import java.util.HashSet;

public class DCRMarking {
	public HashSet<String> executed = new HashSet<String>();
	public HashSet<String> included = new HashSet<String>();
	public HashSet<String> pending = new HashSet<String>();

	public boolean IsAccepting()
	{
		for (String e: included)
			if (pending.contains(e)) return false;
		
		return true;
	}
	
	@Override
	public String toString() {
		final StringBuilder result = new StringBuilder();
		final String NEW_LINE = System.getProperty("line.separator");

		result.append(this.getClass().getName() + " DCR marking {" + NEW_LINE);
		result.append(" Executed: ");
		for (final String e : executed) {
			result.append(e + "; ");
		}
		result.append(NEW_LINE);

		result.append(" Pending: ");
		for (final String e : pending) {
			result.append(e + "; ");
		}
		result.append(NEW_LINE);

		result.append(" Included: ");
		for (final String e : included) {
			result.append(e + "; ");
		}
		result.append(NEW_LINE);

		return result.toString();
	}
	
	@Override
    public int hashCode() {        
        return this.toString().hashCode();
    }
	
	@Override
	public DCRMarking clone()
	{
		DCRMarking newMarking = new DCRMarking();
		
		newMarking.executed = new HashSet<String>(this.executed);
		newMarking.included = new HashSet<String>(this.included);
		newMarking.pending = new HashSet<String>(this.pending);
		/*
		newMarking.executed = (HashSet<String>) this.executed.clone();
		newMarking.included = (HashSet<String>) this.included.clone();
		newMarking.pending = (HashSet<String>) this.pending.clone();*/
		return newMarking;
	}		
	
}
