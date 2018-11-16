package org.qmpm.evaluation.processmining;

import java.io.File;
import java.util.Properties;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.PropertyConfigurator;
import org.deckfour.xes.model.XLog;
import org.qmpm.evaluation.core.ModelFramework;
import org.qmpm.evaluation.enums.MinerLabel;
import org.qmpm.logtrie.core.Framework;
import org.qmpm.logtrie.enums.MetricLabel;
import org.qmpm.logtrie.enums.Outcome;
import org.qmpm.logtrie.exceptions.FileLoadException;
import org.qmpm.logtrie.exceptions.LabelTypeException;
import org.qmpm.logtrie.tools.XESTools;
import org.qmpm.logtrie.trie.Trie;

import minerful.MinerFulMinerLauncher;
import minerful.MinerFulSimplificationLauncher;
import minerful.concept.ProcessModel;
import minerful.index.comparator.modular.ConstraintSortingPolicy;
import minerful.miner.params.MinerFulCmdParameters;
import minerful.params.InputCmdParameters;
import minerful.params.SystemCmdParameters;
import minerful.params.SystemCmdParameters.DebugLevel;
import minerful.params.ViewCmdParameters;
import minerful.postprocessing.params.PostProcessingCmdParameters;
import minerful.postprocessing.params.PostProcessingCmdParameters.PostProcessingAnalysisType;

public class RunMINERful extends RunMiner {

	//String path;
	double confidence = 0.0;
	double interestFactor = 0.0;
	int numConstraints = 0;
	private long runTime = -1;
	
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
		
		String baseName = FilenameUtils.getBaseName(path);
		
		long startMining = System.nanoTime();
		
		InputCmdParameters inputParams = new InputCmdParameters();
		MinerFulCmdParameters minerFulParams = new MinerFulCmdParameters();
		ViewCmdParameters viewParams = new ViewCmdParameters();
		//OutputModelParameters outParams = new OutputModelParameters();
		SystemCmdParameters systemParams = new SystemCmdParameters();
		PostProcessingCmdParameters postParams = new PostProcessingCmdParameters();
		
		// Specifies the type of post-processing analysis, through which getting rid of redundancies or conflicts in the process model
		postParams.postProcessingAnalysisType = PostProcessingAnalysisType.HIERARCHYCONFLICT;
		// Policies according to which constraints are ranked in terms of significance. The position in the array reflects the order with which the policies are used. When a criterion does not establish which constraint in a pair should be put ahead in the ranking, the following one in the array is utilised. 
		ConstraintSortingPolicy[] policy1 = new ConstraintSortingPolicy[]{ConstraintSortingPolicy.ACTIVATIONTARGETBONDS, ConstraintSortingPolicy.FAMILYHIERARCHY, ConstraintSortingPolicy.SUPPORTCONFIDENCEINTERESTFACTOR};
		//ConstraintSortingPolicy[] policy2 = new ConstraintSortingPolicy[]{ConstraintSortingPolicy.SUPPORTCONFIDENCEINTERESTFACTOR, ConstraintSortingPolicy.ACTIVATIONTARGETBONDS, ConstraintSortingPolicy.FAMILYHIERARCHY};
		//ConstraintSortingPolicy[] policy3 = new ConstraintSortingPolicy[]{ConstraintSortingPolicy.FAMILYHIERARCHY, ConstraintSortingPolicy.SUPPORTCONFIDENCEINTERESTFACTOR, ConstraintSortingPolicy.ACTIVATIONTARGETBONDS};
		//ConstraintSortingPolicy[] policy4 = new ConstraintSortingPolicy[]{ConstraintSortingPolicy.ACTIVATIONTARGETBONDS, ConstraintSortingPolicy.SUPPORTCONFIDENCEINTERESTFACTOR, ConstraintSortingPolicy.FAMILYHIERARCHY};
		//ConstraintSortingPolicy[] policy5 = new ConstraintSortingPolicy[]{ConstraintSortingPolicy.FAMILYHIERARCHY, ConstraintSortingPolicy.ACTIVATIONTARGETBONDS, ConstraintSortingPolicy.SUPPORTCONFIDENCEINTERESTFACTOR};
		//ConstraintSortingPolicy[] policy6 = new ConstraintSortingPolicy[]{ConstraintSortingPolicy.SUPPORTCONFIDENCEINTERESTFACTOR, ConstraintSortingPolicy.FAMILYHIERARCHY, ConstraintSortingPolicy.ACTIVATIONTARGETBONDS};
		
		postParams.sortingPolicies = policy1;
		viewParams.machineReadableResults = false;

		postParams.supportThreshold = 1.0;
		postParams.confidenceThreshold = confidence;
		postParams.interestFactorThreshold = interestFactor;
		
		// With the following option set up to "false", redundant/inconsistent/below-thresholds constraints are retained in the model, although marked as redundant/inconsistent/below-thresholds
		// postParams.cropRedundantAndInconsistentConstraints = false;
		postParams.cropRedundantAndInconsistentConstraints = true;
		
		systemParams.debugLevel = DebugLevel.none;
		
		//inputParams.inputLogFile = new File(path);
		MinerFulMinerLauncher miFuMiLa = new MinerFulMinerLauncher(inputParams, minerFulParams, postParams, systemParams);
		//miFuMiLa.logger.configureLogging(DebugLevel.none);
		
		// SUPPRESS CONSOLE OUTPUT 
		
		Properties debugProperties = new Properties();
        debugProperties.setProperty("log4j.rootLogger", "OFF" + ", A1");
        debugProperties.setProperty("log4j.appender.A1", "org.apache.log4j.ConsoleAppender");
        debugProperties.setProperty("log4j.appender.A1.Threshold", "OFF");
        debugProperties.setProperty("log4j.appender.A1.layout", "org.apache.log4j.PatternLayout");
        debugProperties.setProperty("log4j.appender.A1.layout.ConversionPattern", "%p [%t] %c{2} (%M:%L) - %m%n");
        PropertyConfigurator.configure(debugProperties);
        
		
		ProcessModel processModel = miFuMiLa.mine(log);
		MinerFulSimplificationLauncher miFuSiLa = new MinerFulSimplificationLauncher(processModel, postParams);
		ProcessModel simplifiedProcessModel = miFuSiLa.simplify();
		
		runTime = System.nanoTime() - startMining;
		
		/*
		for (minerful.concept.constraint.Constraint c : simplifiedProcessModel.getAllConstraints()) {
			System.out.println(c.getTemplateName());
			System.out.println(c.toString());
		}
		*/
		if (simplifiedProcessModel.howManyConstraints() == 0) {
			System.out.println("");
			System.out.println("NOTE: The model for " + baseName + " is empty! Check if parameters are too strict");
			System.out.println("");
			// return null;
			model = new DeclarativeModel(simplifiedProcessModel);
		} else {
			//if (saveFile) {
				/*
				if (MINERFUL_SAVEFORMATS.contains(OtherLabel.FileExtension.XML.toString())) {
					outParams.fileToSaveAsConDec = new File(variantName + ".xml");
				}
				if (MINERFUL_SAVEFORMATS.contains(OtherLabel.FileExtension.CSV.toString())) {
					outParams.fileToSaveConstraintsAsCSV = new File(variantName + ".csv");
				}
				MinerFulOutputManagementLauncher outputMgt = new MinerFulOutputManagementLauncher();
				outputMgt.manageOutput(simplifiedProcessModel, viewParams, outParams, systemParams);
				*/
				// TODO: Implement saveFile 
			//}
			/*
			if (VERBOSE) {
				MinerFulOutputManagementLauncher outputMgt = new MinerFulOutputManagementLauncher();
				outputMgt.manageOutput(simplifiedProcessModel, viewParams, outParams, systemParams);
			}
			*/
			
			numConstraints = simplifiedProcessModel.howManyConstraints();
			
		    model = new DeclarativeModel(simplifiedProcessModel);
		    model.setName(getLabel().toString() + "_" + String.format("%.3f", confidence) + "-" + String.format("%.3f", interestFactor) + "-" + ((DeclarativeModel) model).getModelSize());
			//return new Metrics(modelFileName + ".xml", logFileName, FLATTEN_LOG);
		}

		finished();
		return Outcome.SUCCESS;
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
					
					double conf = Double.parseDouble(args[i]);
					if (conf < 0 || conf > 1) throw new NumberFormatException("Out of range");
					confidence = conf;
					
				} catch (NumberFormatException e) {
					Framework.permitOutput();
					System.out.println("Double value between 0 and 1 expected for confidence threshold parameter. Received: " + args[i]);
					System.out.println("Using default value: " + confidence);
					Framework.resetQuiet();
					e.printStackTrace();
				};
				break;
				
			case 2: 
				try {
					
					double intFac = Double.parseDouble(args[i]);
					if (intFac < 0 || intFac > 1) throw new NumberFormatException("Out of range");
					interestFactor = intFac;
					
				} catch (NumberFormatException e) {
					Framework.permitOutput();
					System.out.println("Double value between 0 and 1 expected for interest factor parameter. Received: " + args[i]);
					System.out.println("Using default value: " + interestFactor);
					Framework.resetQuiet();
					e.printStackTrace();
				};
				break;
			}
			
		}
		
	}

	@Override
	public MetricLabel getLabel() {
		return MinerLabel.MINERful;
	}

	@Override
	public String parametersAsString() {

		return confidence + "," + interestFactor;
	}
	
	public void setConfidenceThreshold(double c) {
		confidence = c;
	}

	public void setInterestFactorThreshold(double i) {
		interestFactor = i;
	}

	public long getRunTime() {
		return runTime;
	}
}
