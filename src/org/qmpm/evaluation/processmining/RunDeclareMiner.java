package org.qmpm.evaluation.processmining;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.deckfour.xes.model.XLog;
import org.processmining.plugins.declareminer.DeclareMiner;
import org.processmining.plugins.declareminer.DeclareMinerInput;
import org.processmining.plugins.declareminer.enumtypes.AprioriKnowledgeBasedCriteria;
import org.processmining.plugins.declareminer.enumtypes.DeclarePerspective;
import org.processmining.plugins.declareminer.enumtypes.DeclareTemplate;
import org.processmining.plugins.declareminer.util.Configuration;
import org.processmining.plugins.declareminer.util.MultiConfiguration;
import org.processmining.plugins.declareminer.visualizing.ConstraintTemplate;
import org.processmining.plugins.declareminer.visualizing.DeclareMinerOutput;
import org.qmpm.evaluation.enums.MinerLabel;
import org.qmpm.logtrie.core.Framework;
import org.qmpm.logtrie.enums.MetricLabel;
import org.qmpm.logtrie.enums.Outcome;
import org.qmpm.logtrie.exceptions.FileLoadException;
import org.qmpm.logtrie.exceptions.LabelTypeException;
import org.qmpm.logtrie.tools.XESTools;
import org.qmpm.logtrie.trie.Trie;

public class RunDeclareMiner extends RunMiner {

	//private String path;
	private long runTime = -1;	
	private int alpha = 0;
	private int support = 0;
	
	@Override
	public Outcome doComputation(Trie t) throws LabelTypeException, FileLoadException {
		
		XLog log;
		
		if (xLog != null) {
			log = xLog;
		} else {
			try {
				log = XESTools.loadXES(path, true);
			} catch (FileLoadException e) {
				e.printStackTrace();
				throw e;
			}
		}
		
		long startMining = System.nanoTime();

		Framework.setQuiet(true);
		
		DeclareMinerOutput output = mine(log);
		
		Framework.resetQuiet();
		
		runTime = System.nanoTime() - startMining;
		
		model = new DeclarativeModel(output);
		model.setName(getLabel().toString());

		finished();
		return Outcome.SUCCESS;
	}

	private synchronized DeclareMinerOutput mine(XLog log) {
		
		/*
		String configuration_file_path = "C:\\config.properties";
		MultiConfiguration conf = new MultiConfiguration(configuration_file_path);

		conf.setUnifiedLoggerPrunerType("replayers"); // will be obsolete after testing is done
		conf.setUnifiedLoggerTemplates(conf.templates);
		
		String miner_type = conf.miner_type;
		String output_file_type = conf.output_file_type;
		String output_path = conf.output_path;
		*/
		
		
		Set<DeclareTemplate> selectedDeclareTemplateSet = new HashSet<DeclareTemplate>();
		DeclareTemplate[] declareTemplates = DeclareTemplate.values();
		for(DeclareTemplate d : declareTemplates)
			selectedDeclareTemplateSet.add(d);
		
		Set<DeclarePerspective> declarePerspectiveSet = new HashSet<DeclarePerspective>();
		declarePerspectiveSet.add(DeclarePerspective.Control_Flow);
		
		Map<String, DeclareTemplate> templateNameStringDeclareTemplateMap = new HashMap<String, DeclareTemplate>();

		for(DeclareTemplate d : declareTemplates){
			String templateNameString = d.toString().replaceAll("_", " ").toLowerCase();
			templateNameStringDeclareTemplateMap.put(templateNameString, d);
		}

		Map<DeclareTemplate, ConstraintTemplate> declareTemplateConstraintTemplateMap = DeclareMiner.readConstraintTemplates(templateNameStringDeclareTemplateMap);	
		
		DeclareMinerInput input = new DeclareMinerInput();
		input.setAlpha(alpha);
		input.setDeclarePerspectiveSet(declarePerspectiveSet);
		input.setMinSupport(support);
		input.setSelectedDeclareTemplateSet(selectedDeclareTemplateSet);
		input.setDeclareTemplateConstraintTemplateMap(declareTemplateConstraintTemplateMap);
		input.setAprioriKnowledgeBasedCriteriaSet(new HashSet<AprioriKnowledgeBasedCriteria>());
		input.getAprioriKnowledgeBasedCriteriaSet().contains(AprioriKnowledgeBasedCriteria.AllActivitiesWithEventTypes);
		
		input.setThreadNumber(4);
		
		Framework.permitOutput();
		//System.out.println("input.getThreadNumber(): " + input.getThreadNumber());
		Framework.resetQuiet();
		
		return DeclareMiner.mineDeclareConstraints(null, log, input);
	}
	
	@Override
	public void processArgs(String[] args) {

		for (int i=0; i<args.length; i++) {
			
			switch(i) {
			
			case 0: 
				path = args[i];
				break;
				
			case 1: 
				try {
					
					int a = Integer.parseInt(args[i]);
					if (a < 0 || a > 100) throw new NumberFormatException("Out of range");
					alpha = a;
					
				} catch (NumberFormatException e) {
					Framework.permitOutput();
					System.out.println("Integer value between 0 and 100 expected for alpha threshold parameter. Received: " + args[i]);
					System.out.println("Using default value: " + alpha);
					Framework.resetQuiet();
					e.printStackTrace();
				};
				break;
				
			case 2: 
				try {
					
					int s = Integer.parseInt(args[i]);
					if (s < 0 || s > 100) throw new NumberFormatException("Out of range");
					support = s;
					
				} catch (NumberFormatException e) {
					Framework.permitOutput();
					System.out.println("Integer value between 0 and 100 expected for support parameter. Received: " + args[i]);
					System.out.println("Using default value: " + support);
					Framework.resetQuiet();
					e.printStackTrace();
				};
				break;
			}
			
		}
	}

	@Override
	public MetricLabel getLabel() {
		return MinerLabel.DeclareMiner;
	}

	@Override
	public String parametersAsString() {
		return alpha + "," + support;
	}
	
	public long getRunTime() {
		return runTime;
	}
	
}