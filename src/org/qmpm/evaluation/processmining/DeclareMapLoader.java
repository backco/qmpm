package org.qmpm.evaluation.processmining;

import java.io.IOException;
import java.io.File;
import java.util.List;
import java.util.ArrayList;
import java.util.HashSet;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
 
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class DeclareMapLoader {
	
	private HashSet<String> activities;
	private HashSet<List<String>> constraints;
	
	public DeclareMapLoader(final String path) {
		this.activities = new HashSet<String>();
		this.constraints = new HashSet<List<String>>();
		parse(path);
	}
	
	public HashSet<String> getActivities()  {
		return this.activities;
	}
	public HashSet<List<String>> getConstraints() {
		return this.constraints; 
	}
	
	/*
	 *  Parse XML file
	 */
	private void parse(final String path) {
		File file = read(path);
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		DocumentBuilder builder = null;
		try {
			builder = factory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			System.out.println("Problem initializing XML parser");
			e.printStackTrace();
			System.exit(1);
		}
		Document declareMapDoc = null;
		try {
			declareMapDoc = builder.parse(file);
		} catch (SAXException | IOException e) {
			System.out.println("Problem parsing XML file");
			e.printStackTrace();
			System.exit(1);
		}
		NodeList activityNodeList = declareMapDoc.getElementsByTagName("activity");
		
		for (int i=0; i<activityNodeList.getLength(); i++) {
			NamedNodeMap activityAttributes = activityNodeList.item(i).getAttributes();
			this.activities.add(activityAttributes.getNamedItem("name").getNodeValue());
		}
		
		NodeList constraintNodeList = declareMapDoc.getElementsByTagName("constraint");
		for (int i=0; i<constraintNodeList.getLength(); i++) {
			NodeList constraintChildren = constraintNodeList.item(i).getChildNodes();
			String name = new String();
			/* ArrayList representing constraints where the first element is 
			 * the name of the constraint, followed by its parameters in the 
			 * appropriate order, e.g. response(A,B) = {"response", "A", "B"}
			 */
			List<String> constraint = new ArrayList<String>();
			
			for (int j=0; j<constraintChildren.getLength(); j++) {
				Node child = constraintChildren.item(j);
				if (child.getNodeName().equals("name") && constraint.isEmpty()) {
					name = child.getTextContent();
					constraint.add(name);
				}
				else if (child.getNodeName().equals("constraintparameters")) {
					NodeList constraintParameters = child.getChildNodes();
					for (int p=0; p<constraintParameters.getLength(); p++) {
						Node childParameter = constraintParameters.item(p);
						if (childParameter.getNodeName().equals("parameter")) {
							/**	This section ensures that parameters are put
							 *	in the correct position of the ArrayList by 
							 * checking against the "templateparameter" attribute.
							 */
							// TODO: surround with try/catch
							String templateParameterIndexString = childParameter.getAttributes().getNamedItem("templateparameter").getNodeValue();
							int templateParameterIndex = Integer.parseInt(templateParameterIndexString);
							NodeList parameters = childParameter.getChildNodes();
							for (int q=0; q<parameters.getLength(); q++) {
								Node childBranches = parameters.item(q);
								if (childBranches.getNodeName().equals("branches")) {
									// Checking 'branches'
									NodeList branches = childBranches.getChildNodes();
									Node childBranch = branches.item(0);
									
									while (templateParameterIndex >= constraint.size()) {
										constraint.add("");
									}
									if (childBranch.getNodeName().equals("branch") ) {
										String parameterName = childBranch.getAttributes().getNamedItem("name").getNodeValue();
										constraint.set(templateParameterIndex, parameterName);
									}
								}
							}
						}
					}
				}
			}
			this.constraints.add(constraint);
		}		
	}
	
	private File read(final String path) {
		File file = null;
		try {
			file = new File(path);
		} catch (NullPointerException e) {
			System.out.println("Problem loading file: " + path);
			e.printStackTrace();
			System.exit(1);
		}
		return file;
	}

	@Override
	public String toString() {
		String out = "ACTIVITIES";
		for (String activity : this.activities) {
			out += System.lineSeparator() + activity;
		}
		out += System.lineSeparator() + "CONSTRAINTS";
		for (List<String> conArray : this.constraints) {
			String conString = conArray.get(0) + "(" + conArray.get(1);
			for (int i=2; i<conArray.size(); i++) {
				conString += "," + conArray.get(i); 
			}
			conString += ")";
			out += System.lineSeparator() + conString;
		}
		return out;
	}
}

	

