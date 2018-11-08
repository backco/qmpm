package org.qmpm.evaluation.processmining;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.SortedSet;

import minerful.concept.ProcessModel;
import minerful.concept.TaskChar;
import minerful.concept.TaskCharSet;
import minerful.concept.constraint.Constraint;
import net.dcrgraphs.core.Automata;
import net.dcrgraphs.core.DCRTemplateFactory;

public class MINERful2DCR {
	
	public static Automata convert(ProcessModel pm) {
		HashSet<String> events = new HashSet<String>();
		for (TaskChar tc : pm.getProcessAlphabet()) {
			events.add(tc.toString());
		}
		SortedSet<Constraint> constraints = pm.getAllConstraints();		
		
		Automata dcrAutomata = new Automata();
		
		for (Constraint constraint : constraints) {
			ArrayList<String> params = new ArrayList<String>();
			for (TaskCharSet param : constraint.getParameters()) {
				StringBuilder sb = new StringBuilder();
				sb.append(param);
				params.add(sb.toString());
			}
			switch(constraint.getTemplateName().toLowerCase()) {
			case "absence": 			dcrAutomata.addDCRGraph(DCRTemplateFactory.absence(				events, params.get(0))); 					break;
			case "absence2": 			dcrAutomata.addDCRGraph(DCRTemplateFactory.absence2(			events, params.get(0))); 					break;
			case "absence3": 			dcrAutomata.addDCRGraph(DCRTemplateFactory.absence3(			events, params.get(0))); 					break;
			case "alternateprecedence":	dcrAutomata.addDCRGraph(DCRTemplateFactory.alternatePrecedence(	events, params.get(0), params.get(1)));		break;
			case "alternateresponse": 	dcrAutomata.addDCRGraph(DCRTemplateFactory.alternateResponse(	events, params.get(0), params.get(1)));		break;
			case "alternatesuccession":	dcrAutomata.addDCRGraph(DCRTemplateFactory.alternateSuccession(	events, params.get(0), params.get(1)));		break;
			case "atmostone": 			dcrAutomata.addDCRGraph(DCRTemplateFactory.absence2(			events, params.get(0))); 					break;
			case "chainprecedence": 	dcrAutomata.addDCRGraph(DCRTemplateFactory.chainPrecedence(		events, params.get(0), params.get(1)));		break;
			case "chainresponse": 		dcrAutomata.addDCRGraph(DCRTemplateFactory.chainResponse(		events, params.get(0), params.get(1)));		break;
			case "chainsuccession": 	dcrAutomata.addDCRGraph(DCRTemplateFactory.chainSuccession(		events, params.get(0), params.get(1)));		break;
			case "choice": 				dcrAutomata.addDCRGraph(DCRTemplateFactory.choice(				events, params.get(0), params.get(1)));		break;
			case "coexistence": 		dcrAutomata.addDCRGraph(DCRTemplateFactory.coExistence(			events, params.get(0), params.get(1)));		break;
			case "co-existence": 		dcrAutomata.addDCRGraph(DCRTemplateFactory.coExistence(			events, params.get(0), params.get(1)));		break;
			case "end":		 			dcrAutomata.addDCRGraph(DCRTemplateFactory.existence(			events, params.get(0))); 					break; // DCR Graphs and DECLARE do not cover the concept "end".
			case "exactly1": 			dcrAutomata.addDCRGraph(DCRTemplateFactory.exactly1(			events, params.get(0))); 					break;
			case "exactly2": 			dcrAutomata.addDCRGraph(DCRTemplateFactory.exactly2(			events, params.get(0))); 					break;
			case "exclusivechoice": 	dcrAutomata.addDCRGraph(DCRTemplateFactory.exclusiveChoice(		events, params.get(0), params.get(1)));		break;
			case "existence": 			dcrAutomata.addDCRGraph(DCRTemplateFactory.existence(			events, params.get(0))); 					break;
			case "existence2": 			dcrAutomata.addDCRGraph(DCRTemplateFactory.existence2(			events, params.get(0))); 					break;
			case "existence3": 			dcrAutomata.addDCRGraph(DCRTemplateFactory.existence3(			events, params.get(0))); 					break;
			case "init": 				dcrAutomata.addDCRGraph(DCRTemplateFactory.init(				events, params.get(0))); 					break;
			case "notchainsuccession":	dcrAutomata.addDCRGraph(DCRTemplateFactory.notChainSuccession(	events, params.get(0), params.get(1)));		break;
			case "notcoexistence": 		dcrAutomata.addDCRGraph(DCRTemplateFactory.notCoExistence(		events, params.get(0), params.get(1)));		break;
			case "notco-existence": 	dcrAutomata.addDCRGraph(DCRTemplateFactory.notCoExistence(		events, params.get(0), params.get(1)));		break;
			case "notsuccession": 		dcrAutomata.addDCRGraph(DCRTemplateFactory.notSuccession(		events, params.get(0), params.get(1)));		break;
			case "participation": 		dcrAutomata.addDCRGraph(DCRTemplateFactory.existence(			events, params.get(0))); 					break;
			case "precedence": 			dcrAutomata.addDCRGraph(DCRTemplateFactory.coExistence(			events, params.get(0), params.get(1)));		break;
			case "respondedexistence": 	dcrAutomata.addDCRGraph(DCRTemplateFactory.respondedExistence(	events, params.get(0), params.get(1)));		break;
			case "response": 			dcrAutomata.addDCRGraph(DCRTemplateFactory.response(			events, params.get(0), params.get(1)));		break;
			case "succession": 			dcrAutomata.addDCRGraph(DCRTemplateFactory.succession(			events, params.get(0), params.get(1)));		break;
			default: System.out.println("UNRECOGNIZED CONSTRAINT TYPE: " +  constraint.getTemplateName().toLowerCase()); System.exit(1);
			}
		}
		return dcrAutomata;
	}
}
