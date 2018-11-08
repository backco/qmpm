package org.qmpm.evaluation.processmining;

import java.util.ArrayList;
import java.util.List;

import org.qmpm.evaluation.enums.MinerLabel;
import org.qmpm.logtrie.core.Framework;
import org.qmpm.logtrie.enums.MetricLabel;
import org.qmpm.logtrie.enums.Outcome;
import org.qmpm.logtrie.exceptions.FileLoadException;
import org.qmpm.logtrie.exceptions.LabelTypeException;
import org.qmpm.logtrie.tools.MathTools;
//import org.qmpm.logtrie.tools.MathTools;
import org.qmpm.logtrie.trie.Trie;

public class RunMINERfulAuto extends RunMiner {

	//String path;
	double confidence = 0.0;
	double interestFactor = 0.0;
	double resolution = 0.05;
	int maxConstraints = Integer.MAX_VALUE;
	int constraints = -1;
	private long runTime = -1;
	
	int progress = 0;
	int total = (int) Math.ceil(1.0 / resolution);
	
	@Override
	public Outcome doComputation(Trie t) throws LabelTypeException, FileLoadException {
		
		long startMining = System.nanoTime();
		
		GenericProcessModel result = null;
		
		String text = Double.toString(Math.abs(resolution));	// For addressing double rounding issues
		int resPlaces = text.length() - text.indexOf('.') + 1;
		
		for (double currentConfidence = 1.0; currentConfidence > 0.0; currentConfidence -= resolution) {

			//currentConfidence = MathTools.round(currentConfidence, resPlaces);
			
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
			int numOfConstraints = -1;
			
			// Binary search
			
			while (!closeAsPossible && !(numOfConstraints == maxConstraints) && !(interestFactorArray.size() == 0)) {
				int half = lowerBound + (int) Math.ceil((upperBound - lowerBound)/2.0); 
				
				if ((upperBound - lowerBound) <= 1) {
					
					closeAsPossible = true;
				
				} else {
					
					double currentInterestFactor = interestFactorArray.get(half);
										
					RunMINERful rm = new RunMINERful();
					rm.setPath(path);
					rm.setConfidenceThreshold(currentConfidence);
					rm.setInterestFactorThreshold(currentInterestFactor);
					rm.doComputation(t);
					
					GenericProcessModel currentModel = rm.getModel();
					
					numOfConstraints = ((DeclarativeModel) currentModel).getNumOfConstraints();
					
					if (result == null) {
						result = currentModel;
					} 			
					
					if (numOfConstraints <= maxConstraints) {
						
						if (numOfConstraints > ((DeclarativeModel) result).getNumOfConstraints()) {
							confidence = currentConfidence;
							interestFactor = currentInterestFactor;
							result = currentModel;
						}
						
						lowerBound = half;
						
					} else {
						
						upperBound = half;
						
						if (closeAsPossible == true) {
							closeAsPossible = false;
							upperBound = lowerBound;
						}
					}
				}
			}
			
			updateProgress((double) ++progress / total);
			if (getOutcome() != Outcome.CONTINUE) {
				return getOutcome();
			}
				
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
					maxConstraints = maxCon;
					
				} catch (NumberFormatException e) {
					Framework.permitOutput();
					System.out.println("Integer value greater than 0 expected for max constraints parameter. Received: " + args[i]);
					System.out.println("Using default value: " + maxConstraints);
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
		maxConstraints = c;
	}

	public long getRunTime() {
		return runTime;
	}
}