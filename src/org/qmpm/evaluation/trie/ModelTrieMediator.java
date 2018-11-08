package org.qmpm.evaluation.trie;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.qmpm.evaluation.core.ModelFramework;
import org.qmpm.evaluation.enums.CrossValidationType;
import org.qmpm.evaluation.enums.EvaluationMetricLabel;
import org.qmpm.evaluation.enums.MinerLabel;
import org.qmpm.evaluation.metrics.Fitness;
import org.qmpm.evaluation.processmining.GenericProcessModel;
import org.qmpm.evaluation.processmining.RunMiner;
import org.qmpm.logtrie.core.Framework;
import org.qmpm.logtrie.enums.MetricLabel;
import org.qmpm.logtrie.exceptions.LabelTypeException;
import org.qmpm.logtrie.exceptions.ProcessTransitionException;
import org.qmpm.logtrie.trie.AbstractTrieMediator;
import org.qmpm.logtrie.trie.Trie;
//import org.qmpm.logtrie.trie.TrieImpl;
//import org.qmpm.logtrie.trie.AbstractTrieMediator.TrieAttributes;
import org.qmpm.logtrie.metrics.Metric;
import org.qmpm.logtrie.metrics.MetricThread;
import org.qmpm.logtrie.tools.FileInfo;
import org.qmpm.logtrie.tools.FileInfoFactory;
import org.qmpm.logtrie.tools.TimeTools;
import org.qmpm.logtrie.tools.XESTools;
import org.qmpm.logtrie.ui.ProgObsThread;

public class ModelTrieMediator extends AbstractTrieMediator {
	
	private class CVProg {}
	
	public class ModelTrieAttributes<T extends Collection<? extends List<? extends Object>>> extends TrieAttributes<T> {

		public static final String MINERS = "MINERS"; 
		private Metric miner = null;
		private CrossValidationType cvType = CrossValidationType.None;
		
		ModelTrieAttributes(Trie t, String n, FileInfo<T> f) {
			super(t, n, f);
		}
		
		public Metric getMiner() {
			return miner;
		}

		public void setMiner(Metric m) {
			miner = m;
			setInfo(m.toString());
		}
		
		public void setCVType(CrossValidationType cvType) {
			this.cvType = cvType;
		}

		public CrossValidationType getCVType() {
			return cvType;
		}
	}
	

	private Map<Trie, ModelTrieAttributes<? extends Collection<? extends List<? extends Object>>>> modTrieAttMap = new HashMap<>();
	private CrossValidationType cvType = CrossValidationType.None;

	public void addMiner(MinerLabel m) {
		
		String[] args = {};
		addMiner(m, args);
	}
	
	public void addMiner(MinerLabel m, String[] args) {
		
		addMetric(m, args);
	}
	
	@Override
	public ModelTrieAttributes<? extends Collection<? extends List<? extends Object>>> getTrieAttributes(Trie t) {
		
		return modTrieAttMap.get(t);
	}
	
	public <T extends Collection<? extends List<? extends Object>>> void addModel(GenericProcessModel m, FileInfo<T> fi) {
		
		Trie t = new ModelTrie(m);
		TrieAttributes<T> trieAtt = new TrieAttributes<T>(t, fi.getName(), fi);
		setTrieAttributes(t, trieAtt);
		tries.add(t);
	}
	
	@Override
	public <T extends Collection<? extends List<? extends Object>>> void setTrieAttributes(Trie t, TrieAttributes<T> mta) {
		modTrieAttMap.put(t,  (ModelTrieAttributes<T>) mta);
	}

	protected void beforeTrieBuild(Trie t, GenericProcessModel m) {
		
		((ModelTrie) t).setup(m);
	}
	
	@Override
	protected <T extends Collection<? extends List<? extends Object>>> boolean beforeTrieBuild(ListIterator<Trie> trieIterator, Trie t, int current, int total, String format) {
		
		ModelTrieAttributes<T> ta =  (ModelTrieAttributes<T>) getTrieAttributes(t);
		RunMiner m = (RunMiner) ta.getMiner();
		
		if (!ta.getCVType().equals(CrossValidationType.None)) {
			
			CVProg placeHolder = new CVProg();
			progObs.register(placeHolder);
			ProgObsThread cvProgThread = new ProgObsThread(progObs, placeHolder, showProgress);

			cvProgThread.setPreLabel(preLabel);
			cvProgThread.setEndLabel(CVPARTITIONING);
			cvProgThread.start();
			
			tries.remove(t);
			FileInfo<T> mainFile = ta.getFile(); 
			String mainFileDir = mainFile.getFile().getParent();
			String mainFileName = mainFile.getFile().getName();
			String timeStamp = Instant.now().toString().replaceAll(":", "-"); 			
			String cvDir =  "cross-validation-" + ta.getCVType().toString() + "-" + ta.getCVType().getTimeStamp();
			
			List<FileInfo<T>> partitionedLog = FileInfoFactory.partition(mainFile, ta.getCVType().getK());
			
			int k = 1;
			
			for (int j = 0; j<ta.getCVType().getK(); j++ ) {
				
				List<FileInfo<T>> trainingParted = new ArrayList<>();
				trainingParted.addAll(partitionedLog);
				
				String validationPath = "";
				if (mainFileName.toLowerCase().endsWith(".xes")) {
					validationPath = mainFileDir + File.separator + 
							cvDir + File.separator +
							(j+1) + "-validation-" + mainFileName.substring(0, mainFileName.length()-4) + ".xes";
				}
				
				
				FileInfo<T> validation = FileInfoFactory.build(validationPath, false);
				validation.append(trainingParted.remove(j));
				
				String trainingPath = "";
				if (mainFileName.toLowerCase().endsWith(".xes")) {
					trainingPath = mainFileDir + File.separator + 
							cvDir + File.separator +
							(j+1) + "-training-" + mainFileName.substring(0, mainFileName.length()-4) + ".xes";
				}
				
				FileInfo<T> training = FileInfoFactory.build(trainingPath, false);
				for (FileInfo<T> part : trainingParted) {
					training.append(part);
				}

				/*
				try {
					training.saveAs(trainingPath);
				} catch (FileNotFoundException e1) {
					e1.printStackTrace();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				
				try {
					validation.saveAs(validationPath);
				} catch (FileNotFoundException e1) {
					e1.printStackTrace();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				*/

				Trie trainingTrie = new ModelTrie();
				Trie validationTrie = new ModelTrie();
				
				ModelTrieAttributes<T> trainingTA =  new ModelTrieAttributes(trainingTrie, training.getName(), training);
				ModelTrieAttributes<T> validationTA =  new ModelTrieAttributes(validationTrie, validation.getName(), validation);
				
				if (m != null) {
					
					String[] args = m.getArgs();
					args[0] = trainingPath;
					
					RunMiner cvMiner = (RunMiner) m.getLabel().delegate(args);
					
					cvProgThread.setEndLabel(CVMINING);
					
					cvMiner.setLog((XLog) training.getLoadedFile());
					trainingTA.setMiner(cvMiner);
					trainingTA.setName(trainingTA.getName() + "-" + trainingTA.getInfo());
					setTrieAttributes(trainingTrie,trainingTA);
					
					MetricThread metThread = new MetricThread(cvMiner, trainingTrie);
					
					cvMiner.registerProgObs(progObs);
					cvMiner.setTimeout(timeout);
					cvMiner.setSigDigits(sigDigs);
					
					long startMining = System.nanoTime();
					metThread.start();
					
					try {
						metThread.join();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					
					((ModelTrie) trainingTrie).setup(cvMiner.getModel());
					((ModelTrie) validationTrie).setup(cvMiner.getModel());
					validationTA.setInfo(cvMiner.toString());
					//validationTA.setName(validationTA.getName());
				}
				
				setTrieAttributes(validationTrie,validationTA);
				trieIterator.add(validationTrie);
				tries.add(validationTrie);
				try {
					List<String> revPath = pathAsRevList(validation.getFile());
					filePathTrie.insert(revPath, false);
				} catch (ProcessTransitionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (LabelTypeException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				/*
				try {
					addFile(validationPath);
				} catch (LabelTypeException e) {
					e.printStackTrace();
				}
				*/
				
				progObs.updateProgress(placeHolder, ((double) (j+1))/ta.getCVType().getK());
			}
			
			progObs.setFinished(placeHolder, true);
			
			try {
				cvProgThread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			//if (!containsMetric(EvaluationMetricLabel.Fitness)) {
			//	addMetric(EvaluationMetricLabel.Fitness, new String[0]);
			//}
			updateAllMetrics();
			return false;
			
		} else {
			
			if (m != null) {
				
				ProgObsThread progThread = new ProgObsThread(progObs, m, showProgress);
				
				MetricThread metThread = new MetricThread(m, t);
				
				progThread.setCurrent(current);
				progThread.setTotal(tries.size());
				progThread.setPreLabel(preLabel);
				progThread.start();
				
				metThread.start();
				
				try {
					metThread.join();
					progThread.join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}				
				
				((ModelTrie) t).setup(m.getModel());
				ta.setInfo(m.toString());
				//ta.setName(ta.getName());
			}
			return true;
		}
	}

	@Override
	public void setupTries() {
		
		Map<MetricLabel, List<List<String>>> minerMap = getMetrics(MinerLabel.class.getSimpleName());
		
		for (FileInfo fi : files) {
			
			for (MetricLabel l : minerMap.keySet()) {
				
				for (List<String> args : minerMap.get(l)) {
					
					Trie t = new ModelTrie();					
					ModelTrieAttributes trieAtt = new ModelTrieAttributes(t, fi.getName(), fi);
					trieAtt.cvType = cvType;
					
					String[] argsArray = new String[args.size()+1];
					
					argsArray[0] = trieAtt.file.getFile().getPath();
					
					for (int i=1; i<args.size()+1; i++) {
						argsArray[i] = args.get(i-1);
					}
					
					Metric m = l.delegate(argsArray);
					
					m.registerProgObs(progObs);
					m.setTimeout(timeout);
					if (m.getSigDigs() < 0) m.setSigDigits(sigDigs);
					
					trieAtt.setMiner(m);
					//trieAtt.setName(trieAtt.getName());
					tries.add(t);
					setTrieAttributes(t, trieAtt);
				}
			}
		}
		removeMetrics(MinerLabel.class.getSimpleName());
	}
	
	@Override
	protected String getLastMetric() {
		return EvaluationMetricLabel.class.getSimpleName();
	}
	
	public void setCrossValidation(CrossValidationType cv, int k) {
		cv.setK(k);
		cvType = cv;
	}
}
