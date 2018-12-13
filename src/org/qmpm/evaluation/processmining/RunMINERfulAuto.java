package org.qmpm.evaluation.processmining;

import java.util.ArrayList;
import java.util.List;

import org.deckfour.xes.model.XLog;
import org.qmpm.evaluation.enums.MinerLabel;
import org.qmpm.logtrie.core.Framework;
import org.qmpm.logtrie.enums.MetricLabel;
import org.qmpm.logtrie.enums.Outcome;
import org.qmpm.logtrie.exceptions.FileLoadException;
import org.qmpm.logtrie.exceptions.LabelTypeException;
import org.qmpm.logtrie.tools.MathTools;
import org.qmpm.logtrie.tools.XESTools;
//import org.qmpm.logtrie.tools.MathTools;
import org.qmpm.logtrie.trie.Trie;

public class RunMINERfulAuto extends RunMiner {

	//String path;
	double confidence = 0.0;
	double interestFactor = 0.0;
	double resolution = 0.05;
	int maxModelSize = Integer.MAX_VALUE;
	int constraints = -1;
	private long runTime = -1;
	
	int progress = 0;
	int total = (int) Math.ceil(1.0 / resolution);
	
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
		
		GenericProcessModel result = null;
		
		String text = Double.toString(Math.abs(resolution));	// For addressing double rounding issues
		int resPlaces = text.length() - text.indexOf('.') + 1;
		
		for (double currentConfidence = 1.0; currentConfidence >= 0.0; currentConfidence -= resolution) {

			currentConfidence = MathTools.round(currentConfidence, resPlaces);
			
			List<Double> interestFactorArray = new ArrayList<Double>();
			double threshold = currentConfidence;
			
			while (threshold >= 0) {
				// Note that values are in descending order
				//interestFactorArray.add(threshold);
				interestFactorArray.add(MathTools.round(threshold, resPlaces));
				threshold -= resolution;
			}
			
			int lowerBound = 0;

			int upperBound = interestFactorArray.size()-1;
			
			boolean closeAsPossible = false;
			int modelSize = -1;
			
			// Binary search
			
			while (!closeAsPossible && !(modelSize == maxModelSize) && !(interestFactorArray.size() == 0)) {
				
				int half; 
				
				// Make sure to check max and min value
				if ((upperBound - lowerBound)/2.0 < 1 && upperBound == interestFactorArray.size()-1) {
					half = interestFactorArray.size()-1;
				}
				else if ((upperBound - lowerBound)/2.0 < 1 && lowerBound == 0) {
					half = 0;
				} else if ((upperBound - lowerBound)/2.0 < 1) {
					closeAsPossible = true;
					break;
				} else {
					half = lowerBound + (int) Math.ceil((upperBound - lowerBound)/2.0);
				}
				
				double currentInterestFactor = interestFactorArray.get(half);
				
									
				RunMINERful rm = new RunMINERful();
				rm.setLog(log);
				//rm.setPath(path);
				rm.setConfidenceThreshold(currentConfidence);
				rm.setInterestFactorThreshold(currentInterestFactor);
				rm.doComputation(t);
				
				GenericProcessModel currentModel = rm.getModel();
				
				modelSize = currentModel.getModelSize();
				
				if (result == null) {
					confidence = currentConfidence;
					interestFactor = currentInterestFactor;
					result = currentModel;
				} 			
				
				if (modelSize == maxModelSize) {

					model = result;
					runTime = System.nanoTime() - startMining;
					finished();
					return Outcome.SUCCESS;
				} else if (modelSize <= maxModelSize) {
					
					if ( modelSize > result.getModelSize() ) {
						confidence = currentConfidence;
						interestFactor = currentInterestFactor;
						result = currentModel;
					}
					
					lowerBound = half;
					
				} else {
					
					if (modelSize < result.getModelSize()) {
						confidence = currentConfidence;
						interestFactor = currentInterestFactor;
						result = currentModel;
					}
					
					upperBound = half;
					
					//if (closeAsPossible == true) {
					//	closeAsPossible = false;
					//	upperBound = lowerBound;
					//}
				}
				
				if (half == 0 || half == interestFactorArray.size() - 1)	closeAsPossible = true;
			
			}
			/*
			updateProgress((double) ++progress / total);
			if (getOutcome() != Outcome.CONTINUE) {
				return getOutcome();
			}
			*/
				
		}
			
		model = result;
		
		runTime = System.nanoTime() - startMining;

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
					
					double res = Double.parseDouble(args[i]);
					if (res < 0 || res > 1) throw new NumberFormatException("Out of range");
					resolution = res;
					
				} catch (NumberFormatException e) {
					Framework.permitOutput();
					System.out.println("Double value between 0 and 1 expected for resolution parameter. Received: " + args[i]);
					System.out.println("Using default value: " + resolution);
					Framework.resetQuiet();
					e.printStackTrace();
				};
				break;	
				
			case 2: 
				try {
					
					int maxCon = Integer.parseInt(args[i]);
					if (maxCon <= 0) throw new NumberFormatException("Out of range");
					maxModelSize = maxCon;
					
				} catch (NumberFormatException e) {
					Framework.permitOutput();
					System.out.println("Integer value greater than 0 expected for max constraints parameter. Received: " + args[i]);
					System.out.println("Using default value: " + maxModelSize);
					Framework.resetQuiet();
					e.printStackTrace();
				};
				break;
			}			
		}		
		Framework.resetQuiet();
	}

	@Override
	public MetricLabel getLabel() {
		return MinerLabel.MINERfulAuto;
	}

	@Override
	public String parametersAsString() {
		
		return (model == null ? "N/A" : (confidence + "," + interestFactor));
	}

	@Override
	public GenericProcessModel getModel() {
		return model;
	}
	
	public void setMaxConstraints(int c) {
		maxModelSize = c;
	}

	public long getRunTime() {
		return runTime;
	}
}