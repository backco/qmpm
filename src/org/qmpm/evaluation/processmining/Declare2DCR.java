package org.qmpm.evaluation.processmining;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.regex.*;

import net.dcrgraphs.core.Automata;
import net.dcrgraphs.core.DCRTemplateFactory;

import org.processmining.plugins.declareminer.visualizing.DeclareMinerOutput;
import org.qmpm.evaluation.processmining.DeclareMapLoader;
import org.processmining.plugins.declareminer.visualizing.ConstraintDefinition;

public class Declare2DCR {
	
	public static Automata convert(DeclareMinerOutput dmo) {
		
		Vector<ConstraintDefinition> constraintDefs = dmo.getAllDiscoveredConstraints();
		HashSet<List<String>> constraints = new HashSet<>();
		HashSet<String> activities = new HashSet<>();
		
		for (String act : dmo.getAllActivities().values()) {
			activities.add(act);
		}
		
		for (ConstraintDefinition cd : constraintDefs) {
			
			
			
			List<String> cl = new ArrayList<>();
			String regex = "^(.*?): \\[";
			
			for (int i=cd.parameterCount(); i>0; i--) {
				regex += (i==1 ? "(.*?)\\]$" : "(.*?)\\], \\[");
			}
			
			Pattern pat = Pattern.compile(regex);
			Matcher mat = pat.matcher(cd.getCaption());
			
			while (mat.find()) {
				for (int i=0; i<=cd.parameterCount(); i++) {
					cl.add(mat.group(i+1));
				}
			}
			
			constraints.add(cl);
		}
		
		return convert(activities, constraints);
	}
	
	public static Automata convert(DeclareMapLoader dml) {
		
		HashSet<List<String>> constraints = dml.getConstraints();
		
		return convert(dml.getActivities(), constraints);
	}
	
	public static Automata convert(HashSet<String> activities, Set<List<String>> constraints) {

		Automata dcrAutomata = new Automata();
		
		for (List<String> constraint : constraints) {
			
			switch(constraint.get(0).toLowerCase()) {
				case "absence": 			dcrAutomata.addDCRGraph(DCRTemplateFactory.absence(				activities, constraint.get(1))); 					break;
				case "absence2": 			dcrAutomata.addDCRGraph(DCRTemplateFactory.absence2(			activities, constraint.get(1))); 					break;
				case "absence3": 			dcrAutomata.addDCRGraph(DCRTemplateFactory.absence3(			activities, constraint.get(1))); 					break;
				case "alternate precedence":dcrAutomata.addDCRGraph(DCRTemplateFactory.alternatePrecedence(	activities, constraint.get(1), constraint.get(2))); break;
				case "alternate response": 	dcrAutomata.addDCRGraph(DCRTemplateFactory.alternateResponse(	activities, constraint.get(1), constraint.get(2))); break;
				case "alternate succession":dcrAutomata.addDCRGraph(DCRTemplateFactory.alternateSuccession(	activities, constraint.get(1), constraint.get(2))); break;
				case "chain precedence": 	dcrAutomata.addDCRGraph(DCRTemplateFactory.chainPrecedence(		activities, constraint.get(1), constraint.get(2))); break;
				case "chain response": 		dcrAutomata.addDCRGraph(DCRTemplateFactory.chainResponse(		activities, constraint.get(1), constraint.get(2))); break;
				case "chain succession": 	dcrAutomata.addDCRGraph(DCRTemplateFactory.chainSuccession(		activities, constraint.get(1), constraint.get(2))); break;
				case "choice": 				dcrAutomata.addDCRGraph(DCRTemplateFactory.choice(				activities, constraint.get(1), constraint.get(2))); break;
				case "co-existence": 		dcrAutomata.addDCRGraph(DCRTemplateFactory.coExistence(			activities, constraint.get(1), constraint.get(2))); break;
				case "exactly1": 			dcrAutomata.addDCRGraph(DCRTemplateFactory.exactly1(			activities, constraint.get(1))); 					break;
				case "exactly2": 			dcrAutomata.addDCRGraph(DCRTemplateFactory.exactly2(			activities, constraint.get(1))); 					break;
				case "exclusive choice": 	dcrAutomata.addDCRGraph(DCRTemplateFactory.exclusiveChoice(		activities, constraint.get(1), constraint.get(2))); break;
				case "existence": 			dcrAutomata.addDCRGraph(DCRTemplateFactory.existence(			activities, constraint.get(1))); 					break;
				case "existence2": 			dcrAutomata.addDCRGraph(DCRTemplateFactory.existence2(			activities, constraint.get(1))); 					break;
				case "existence3": 			dcrAutomata.addDCRGraph(DCRTemplateFactory.existence3(			activities, constraint.get(1))); 					break;
				case "init": 				dcrAutomata.addDCRGraph(DCRTemplateFactory.init(				activities, constraint.get(1))); 					break;
				case "not chain succession":dcrAutomata.addDCRGraph(DCRTemplateFactory.notChainSuccession(	activities, constraint.get(1), constraint.get(2))); break;
				case "not co-existence": 	dcrAutomata.addDCRGraph(DCRTemplateFactory.notCoExistence(		activities, constraint.get(1), constraint.get(2))); break;
				case "not succession": 		dcrAutomata.addDCRGraph(DCRTemplateFactory.notSuccession(		activities, constraint.get(1), constraint.get(2))); break;
				case "precedence": 			dcrAutomata.addDCRGraph(DCRTemplateFactory.precedence(			activities, constraint.get(1), constraint.get(2))); break;
				case "responded existence": dcrAutomata.addDCRGraph(DCRTemplateFactory.respondedExistence(	activities, constraint.get(1), constraint.get(2))); break;
				case "response": 			dcrAutomata.addDCRGraph(DCRTemplateFactory.response(			activities, constraint.get(1), constraint.get(2))); break;
				case "succession": 			dcrAutomata.addDCRGraph(DCRTemplateFactory.succession(			activities, constraint.get(1), constraint.get(2))); break;
				default: System.out.println("UNRECOGNIZED CONSTRAINT TYPE: " +  constraint.get(0).toLowerCase()); System.exit(1);
			}
		}
		return dcrAutomata;
	}

}