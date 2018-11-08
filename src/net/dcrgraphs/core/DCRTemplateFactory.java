package net.dcrgraphs.core;
import java.util.HashSet;

//import net.dcrgraphs.core.*;

public class DCRTemplateFactory {
	
	// TODO: Make sure that eventA is added first.
	public static DCRGraph init(HashSet<String> events, String eventA)
	{		
		DCRGraph g = new DCRGraph();
		g.addEvent(eventA);
		for(String e : events)
		{
			g.addEvent(e);
			if (!e.equals(eventA))
				g.addCondition(eventA, e);
		}
		g.marking = g.defaultInitialMarking();		
		return g;
	}
	
	
	public static DCRGraph existence(HashSet<String> events, String eventA)
	{
		DCRGraph g = new DCRGraph();
		for(String e : events)
			g.addEvent(e);
		g.marking = g.defaultInitialMarking();
		g.marking.pending.add(eventA);
		return g;
	}
	
	
	public static DCRGraph absence(HashSet<String> events, String eventA)
	{
		DCRGraph g = new DCRGraph();
		for(String e : events)
			g.addEvent(e);
		g.marking = g.defaultInitialMarking();
		g.marking.included.remove(eventA);
		return g;
	}	
	
	
	public static DCRGraph absence2(HashSet<String> events, String eventA)
	{
		DCRGraph g = new DCRGraph();
		for(String e : events)
			g.addEvent(e);
		g.marking = g.defaultInitialMarking();
		g.addExclude(eventA, eventA);
		return g;
	}		
	
	
	public static DCRGraph exactly1(HashSet<String> events, String eventA)
	{
		DCRGraph g = new DCRGraph();
		for(String e : events)
			g.addEvent(e);
		g.marking = g.defaultInitialMarking();
		g.addExclude(eventA, eventA);
		g.marking.pending.add(eventA);
		return g;
	}	
	
	public static DCRGraph choice(HashSet<String> events, String eventA, String eventB)
	{
		DCRGraph g = new DCRGraph();
		for(String e : events)
			g.addEvent(e);
		String blockingEvent = g.newEvent();			
		g.marking = g.defaultInitialMarking();
		g.marking.pending.add(blockingEvent);
		g.addCondition(blockingEvent, blockingEvent);		
		g.addExclude(eventA, blockingEvent);
		g.addExclude(eventB, blockingEvent);		
		return g;
	}		
	 
	public static DCRGraph exclusiveChoice(HashSet<String> events, String eventA, String eventB)
	{
		DCRGraph g = new DCRGraph();
		for(String e : events)
			g.addEvent(e);
		String blockingEvent = g.newEvent();			
		g.marking = g.defaultInitialMarking();
		g.marking.pending.add(blockingEvent);
		g.addCondition(blockingEvent, blockingEvent);		
		g.addExclude(eventA, blockingEvent);
		g.addExclude(eventB, blockingEvent);		
		g.addExclude(eventB, eventA);		
		g.addExclude(eventA, eventB);		
		return g;
	}
	
	public static DCRGraph response(HashSet<String> events, String eventA, String eventB)
	{
		DCRGraph g = new DCRGraph();
		for(String e : events)
			g.addEvent(e);
		g.marking = g.defaultInitialMarking();
		g.addResponse(eventA, eventB);
		return g;
	}	
	
	public static DCRGraph precedence(HashSet<String> events, String eventA, String eventB)
	{
		DCRGraph g = new DCRGraph();
		for(String e : events)
			g.addEvent(e);
		g.marking = g.defaultInitialMarking();
		g.addCondition(eventA, eventB);
		return g;
	}
	
	
	public static DCRGraph succession(HashSet<String> events, String eventA, String eventB)
	{
		DCRGraph g = new DCRGraph();
		for(String e : events)
			g.addEvent(e);
		g.marking = g.defaultInitialMarking();
		g.addCondition(eventA, eventB);
		g.addResponse(eventA, eventB);
		return g;
	}
	
	public static DCRGraph alternateResponse(HashSet<String> events, String eventA, String eventB)
	{
		DCRGraph g = new DCRGraph();
		for(String e : events)
			g.addEvent(e);
		g.marking = g.defaultInitialMarking();
		g.addResponse(eventA, eventB);
		g.addExclude(eventA, eventA);
		g.addInclude(eventB, eventA);
		return g;
	}
	 
	public static DCRGraph alternatePrecedence(HashSet<String> events, String eventA, String eventB)
	{
		DCRGraph g = new DCRGraph();
		for(String e : events)
			g.addEvent(e);
		g.marking = g.defaultInitialMarking();
		g.marking.included.remove(eventB);
		g.addInclude(eventA, eventB);
		g.addExclude(eventB, eventB);
		return g;
	}	 
			
	
	public static DCRGraph alternateSuccession(HashSet<String> events, String eventA, String eventB)
	{
		DCRGraph g = new DCRGraph();
		for(String e : events)
			g.addEvent(e);
		g.marking = g.defaultInitialMarking();
		g.marking.included.remove(eventB);
		g.addInclude(eventA, eventB);
		g.addExclude(eventB, eventB);
		g.addResponse(eventA, eventB);
		g.addExclude(eventA, eventA);
		g.addInclude(eventB, eventA);
		return g;
	}
	
	
	public static DCRGraph notCoExistence(HashSet<String> events, String eventA, String eventB)
	{
		DCRGraph g = new DCRGraph();
		for(String e : events)
			g.addEvent(e);
		g.marking = g.defaultInitialMarking();
		g.addExclude(eventA, eventB);
		g.addExclude(eventB, eventA);
		return g;
	}		
	
	public static DCRGraph notSuccession(HashSet<String> events, String eventA, String eventB)
	{
		DCRGraph g = new DCRGraph();
		for(String e : events)
			g.addEvent(e);
		g.marking = g.defaultInitialMarking();
		g.addExclude(eventA, eventB);		
		return g;
	}
	
	// Existence2 l: !e1(l), %!e2(l), e1(l) -->+ e2(l), e1(l) -->% e1(l)
	public static LabelledDCRGraph existence2(HashSet<String> events, String eventA)
	{
		LabelledDCRGraph g = new LabelledDCRGraph();
		for(String e : events)
			g.addEvent(e);		
		String eventB = g.newEvent();
		g.removeLabel(eventB,eventB);
		g.addLabel(eventB, eventA);		
		g.marking = g.defaultInitialMarking();
		g.marking.pending.add(eventA);
		g.marking.pending.add(eventB);
		g.marking.included.remove(eventB);
		g.addInclude(eventA, eventB);
		g.addExclude(eventA, eventA);
		return g;
	}
	

	//Existence3 l: !e1(l), %!e2(l), %!e3(l), e1(l) -->+ e2(l), e1(l) -->% e1(l), e2(l) -->+ e3(l), e2(l) -->% e2(l)
	public static LabelledDCRGraph existence3(HashSet<String> events, String eventA)
	{
		LabelledDCRGraph g = new LabelledDCRGraph();
		for(String e : events)
			g.addEvent(e);		
		String eventB = g.newEvent();
		g.removeLabel(eventB,eventB);
		g.addLabel(eventB, eventA);		
		
		String eventC = g.newEvent();
		g.removeLabel(eventC,eventC);
		g.addLabel(eventC, eventA);		
		
		g.marking = g.defaultInitialMarking();
		g.marking.pending.add(eventA);
		g.marking.pending.add(eventB);
		g.marking.pending.add(eventC);
		g.marking.included.remove(eventB);
		g.marking.included.remove(eventC);
		g.addInclude(eventA, eventB);
		g.addExclude(eventA, eventA);
		g.addInclude(eventB, eventC);
		g.addExclude(eventB, eventB);		
		return g;
	}	
	
	
	// Absence3 l: e1(l) -->% e1(l), e2(l) -->% e2(l)
	public static LabelledDCRGraph absence3(HashSet<String> events, String eventA)
	{
		LabelledDCRGraph g = new LabelledDCRGraph();
		for(String e : events)
			g.addEvent(e);		
		String eventB = g.newEvent();
		g.removeLabel(eventB,eventB);
		g.addLabel(eventB, eventA);		
		g.marking = g.defaultInitialMarking();
		g.addExclude(eventA, eventA);
		g.addExclude(eventB, eventB);
		return g;
	}	
	
	//Exactly2 l: !e1(l), !e2(l), e1(l) -->% e1(l), e2(l) -->% e2(l)
	public static LabelledDCRGraph exactly2(HashSet<String> events, String eventA)
	{
		LabelledDCRGraph g = new LabelledDCRGraph();
		for(String e : events)
			g.addEvent(e);		
		String eventB = g.newEvent();
		g.removeLabel(eventB,eventB);
		g.addLabel(eventB, eventA);		
		g.marking = g.defaultInitialMarking();
		g.marking.pending.add(eventA);
		g.marking.pending.add(eventB);				
		g.addExclude(eventA, eventA);
		g.addExclude(eventB, eventB);
		return g;
	}		
	
	// Responded Existence l m: %e2(l), e1(l) *--> f(m), f(m) -->% e1(l), f(m) -->+ e2(l)
	public static LabelledDCRGraph respondedExistence(HashSet<String> events, String eventA, String eventB)
	{
		LabelledDCRGraph g = new LabelledDCRGraph();
		for(String e : events)
			g.addEvent(e);		
		String eventA2 = g.newEvent();
		g.removeLabel(eventA2,eventA2);
		g.addLabel(eventA2, eventA);
		g.marking = g.defaultInitialMarking();
		g.marking.included.remove(eventA2);		
		g.addResponse(eventA, eventB);						
		g.addExclude(eventB, eventA);
		g.addInclude(eventB, eventA2);
		return g;
	}
	
	// Co-existence l m: %e2(l), %f2(m), e1(l) *--> f1(m),  f1(m) *--> e1(l), e1(l) -->% e1(l), f1(m) -->% f1(m), e1(l) -->+ e2(l), f1(m) -->+ f2(m)
	public static LabelledDCRGraph coExistence(HashSet<String> events, String eventA, String eventB)
	{
		LabelledDCRGraph g = new LabelledDCRGraph();
		for(String e : events)
			g.addEvent(e);		
		String eventA2 = g.newEvent();		
		g.removeLabel(eventA2,eventA2);
		g.addLabel(eventA2, eventA);		
		String eventB2 = g.newEvent();
		g.removeLabel(eventB2,eventB2);
		g.addLabel(eventB2, eventB);		
		g.marking = g.defaultInitialMarking();
		g.marking.included.remove(eventA2);
		g.marking.included.remove(eventB2);		
		g.addResponse(eventA, eventB);
		g.addResponse(eventB, eventA);		
		g.addExclude(eventA, eventA);
		g.addExclude(eventB, eventB);		
		g.addInclude(eventA, eventA2);
		g.addInclude(eventB, eventB2);
		return g;
	}
	
	//conditional condition”: C—>% A—>*B
	public static DCRGraph conditionalCondition(HashSet<String> events, String eventA, String eventB, String eventC)
	{
		DCRGraph g = new DCRGraph();
		for(String e : events)
			g.addEvent(e);				
		g.marking = g.defaultInitialMarking();		
		g.addCondition(eventA, eventB);
		g.addExclude(eventC, eventA);		
		return g;
	}
	
	//conditional response A*—>B%<—-C
	public static DCRGraph conditionalResponse(HashSet<String> events, String eventA, String eventB, String eventC)
	{
		DCRGraph g = new DCRGraph();
		for(String e : events)
			g.addEvent(e);				
		g.marking = g.defaultInitialMarking();		
		g.addResponse(eventA, eventB);
		g.addExclude(eventC, eventA);		
		return g;
	}
	
	
	//milestone: A*—>B—<>C
	public static DCRGraph milestone(HashSet<String> events, String eventA, String eventB, String eventC)
	{
		DCRGraph g = new DCRGraph();
		for(String e : events)
			g.addEvent(e);				
		g.marking = g.defaultInitialMarking();		
		g.addResponse(eventA, eventB);
		g.addMilestone(eventB, eventC);		
		return g;
	}	

	
	// toggle: A-->% B +<--C
	public static DCRGraph toggle(HashSet<String> events, String eventA, String eventB, String eventC)
	{
		DCRGraph g = new DCRGraph();
		for(String e : events)
			g.addEvent(e);				
		g.marking = g.defaultInitialMarking();		
		g.addExclude(eventA, eventB);
		g.addInclude(eventC, eventB);		
		return g;
	}		
	
	
	
	public static DCRGraph chainResponse(HashSet<String> events, String eventA, String eventB)
	{
		DCRGraph g = new DCRGraph();
		for(String e : events)
			g.addEvent(e);				
		g.marking = g.defaultInitialMarking();
		g.addResponse(eventA, eventB);
		
		for(String e : events)
			if (!e.equals(eventB))
				g.addExclude(eventA, e);
		
		for(String e : events)
			if (!e.equals(eventB))
				g.addInclude(eventB, e);
		
		return g;
	}
	
	
	public static DCRGraph chainPrecedence(HashSet<String> events, String eventA, String eventB)
	{
		DCRGraph g = new DCRGraph();
		for(String e : events)
			g.addEvent(e);				
		g.marking = g.defaultInitialMarking();
		g.marking.included.remove(eventB);
		g.addInclude(eventA, eventB);
		
		for(String e : events)
			if (!e.equals(eventA))
				g.addExclude(e, eventB);
		
		return g;
	}
	
	
	public static DCRGraph chainSuccession(HashSet<String> events, String eventA, String eventB)
	{
		DCRGraph g = new DCRGraph();
		for(String e : events)
			g.addEvent(e);				
		g.marking = g.defaultInitialMarking();
		g.marking.included.remove(eventB);		
		g.addInclude(eventA, eventB);

		g.addResponse(eventA, eventB);
		
		for(String e : events)
			if (!e.equals(eventB))
				g.addExclude(eventA, e);		
		
		for(String e : events)
			if (!e.equals(eventB))
				g.addInclude(eventB, e);
		
		g.addExclude(eventB, eventB);	
		
		return g;
	}
	

	public static DCRGraph notChainSuccession(HashSet<String> events, String eventA, String eventB)
	{
		DCRGraph g = new DCRGraph();
		for(String e : events)
			g.addEvent(e);				
		g.marking = g.defaultInitialMarking();
				
		g.addExclude(eventA, eventB);
		
		for(String e : events)
			if (!e.equals(eventA))
				g.addInclude(e, eventB);
		
		return g;
	}	
		
}
