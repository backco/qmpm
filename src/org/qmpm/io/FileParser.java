package org.qmpm.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import minerful.concept.ProcessModel;
import minerful.concept.TaskChar;
import minerful.concept.TaskCharArchive;
import minerful.concept.TaskCharFactory;
import minerful.concept.constraint.Constraint;
import minerful.concept.constraint.ConstraintsBag;
import minerful.concept.constraint.existence.AtMostOne;
import minerful.concept.constraint.existence.ExactlyOne;
import minerful.concept.constraint.existence.Init;
import minerful.concept.constraint.existence.Participation;
import minerful.concept.constraint.relation.AlternatePrecedence;
import minerful.concept.constraint.relation.AlternateResponse;
import minerful.concept.constraint.relation.AlternateSuccession;
import minerful.concept.constraint.relation.ChainPrecedence;
import minerful.concept.constraint.relation.ChainResponse;
import minerful.concept.constraint.relation.ChainSuccession;
import minerful.concept.constraint.relation.CoExistence;
import minerful.concept.constraint.relation.NotChainSuccession;
import minerful.concept.constraint.relation.NotCoExistence;
import minerful.concept.constraint.relation.NotSuccession;
import minerful.concept.constraint.relation.Precedence;
import minerful.concept.constraint.relation.RespondedExistence;
import minerful.concept.constraint.relation.Response;
import minerful.concept.constraint.relation.Succession;

public class FileParser {

	public static Object getModelPNML(File pnmlModel) throws Exception {

		PnmlImportNet pnmlImportNet = new PnmlImportNet();
		
		return pnmlImportNet.importFromStream(null, new FileInputStream(pnmlModel), pnmlModel.getAbsolutePath(), pnmlModel.length());
	}
	
	// Gets valid constraint strings from the model file
	public static ProcessModel getModelDECL(File declModel) {
		List<String> cL = new ArrayList<String>();
		try {
			Scanner sc = new Scanner(declModel);
			Pattern p = Pattern.compile("\\w+(\\[.*\\]) \\|");
			while (sc.hasNextLine()) {
				String line = sc.nextLine();
				if (!line.startsWith("activity") && !line.startsWith("bind")) {
					Matcher m = p.matcher(line);
					if (m.find()) {
						cL.add(line);
					}
				}
			}
			sc.close();
		} catch (FileNotFoundException e) {
			// TODO: Should throw an exception instead of returning an empty list
			System.out.println("Can not load constraints from model: " + declModel.getAbsolutePath());
		}

		List<String> allActivitiesInvolved = new ArrayList<String>();
		
		for(String s : cL) {
			allActivitiesInvolved.addAll(getInvolvedActivities(s));
		}
		
		Set<String> allActivities = new TreeSet<String>(allActivitiesInvolved);
		
		TaskCharFactory tChFactory = new TaskCharFactory();

		List<TaskChar> tcList = allActivities.stream().map(activity -> tChFactory.makeTaskChar(activity)).collect(Collectors.toList());
		TaskChar[] tcArray = (TaskChar[]) tcList.toArray(new TaskChar[tcList.size()]);
		TaskCharArchive taChaAr = new TaskCharArchive(tcArray);

		ConstraintsBag bag = new ConstraintsBag(taChaAr.getTaskChars());

		for(String s : cL) {
			bag.add(parseConstraint(s, taChaAr));
		}
		
		return new ProcessModel(taChaAr, bag);
	}

	private static Constraint parseConstraint(String c, TaskCharArchive taChaAr) {

		String temp = getWithoutParanthesis(c);
		List<TaskChar> involved = getInvolvedActivities(c).stream().map(activity -> taChaAr.getTaskChar(activity)).collect(Collectors.toList());
		Constraint constraint = getConstraint(temp, involved);

		return constraint;
	}

	private static String getWithoutParanthesis(String selected) {
		int index = selected.indexOf('[');
		return selected.substring(0, index);
	}
	
	private static List<String> getInvolvedActivities(String template) {
		int leftP = template.indexOf('[');
		int rightP = template.indexOf(']');
		String inBetween = template.substring(leftP+1, rightP);
		int comma = inBetween.indexOf(',');
		if(comma != -1) {
			return Arrays.asList(inBetween.substring(0, comma), inBetween.substring(comma+2));
		}
		else {
			return Arrays.asList(inBetween);
		}
	}
	
	private static Constraint getConstraint(String template, List<TaskChar> involved) {
		if(involved.size() == 1) {
			if(template.equals("Init")) {
				return new Init(involved.get(0));
			}
			else if(template.startsWith("Existence")) {
				return new Participation(involved.get(0));
			}
			else if(template.equals("Exactly1")) {
				return new ExactlyOne(involved.get(0));
			}
			else if(template.equals("Absence2")) {
				return new AtMostOne(involved.get(0));
			}
			else return null;
		}
		else {
			if(template.equals("Response")) {
				return new Response(involved.get(0),involved.get(1));
			}
			if(template.equals("Alternate Response")) {
				return new AlternateResponse(involved.get(0),involved.get(1));
			}
			if(template.equals("Chain Response")) {
				return new ChainResponse(involved.get(0),involved.get(1));
			}
			if(template.equals("Precedence")) {
				return new Precedence(involved.get(0),involved.get(1));
			}
			if(template.equals("Alternate Precedence")) {
				return new AlternatePrecedence(involved.get(0),involved.get(1));
			}
			if(template.equals("Chain Precedence")) {
				return new ChainPrecedence(involved.get(0),involved.get(1));
			}
			if(template.equals("Succession")) {
				return new Succession(involved.get(0),involved.get(1));
			}
			if(template.equals("Alternate Succession")) {
				return new AlternateSuccession(involved.get(0),involved.get(1));
			}
			if(template.equals("Chain Succession")) {
				return new ChainSuccession(involved.get(0),involved.get(1));
			}
			if(template.equals("CoExistence")) {
				return new CoExistence(involved.get(0),involved.get(1));
			}
			if(template.equals("Responded Existence")) {
				return new RespondedExistence(involved.get(0),involved.get(1));
			}
			if(template.equals("Not CoExistence")) {
				return new NotCoExistence(involved.get(0),involved.get(1));
			}
			if(template.equals("Not Chain Succession")) {
				return new NotChainSuccession(involved.get(0),involved.get(1));
			}
			if(template.equals("Not Succession")) {
				return new NotSuccession(involved.get(0),involved.get(1));
			}
			return null;
		}
	}
}
