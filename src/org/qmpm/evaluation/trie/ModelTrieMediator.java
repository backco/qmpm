package org.qmpm.evaluation.trie;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.qmpm.evaluation.enums.CrossValidationType;
import org.qmpm.evaluation.enums.EvaluationMetricLabel;
import org.qmpm.evaluation.enums.MinerLabel;
import org.qmpm.evaluation.processmining.GenericProcessModel;
import org.qmpm.evaluation.processmining.RunMiner;
import org.qmpm.logtrie.core.Framework;
import org.qmpm.logtrie.elementlabel.ElementLabel;
import org.qmpm.logtrie.enums.MetricLabel;
import org.qmpm.logtrie.exceptions.LabelTypeException;
import org.qmpm.logtrie.exceptions.NodeNotFoundException;
//import org.qmpm.logtrie.trie.TrieImpl;
//import org.qmpm.logtrie.trie.AbstractTrieMediator.TrieAttributes;
import org.qmpm.logtrie.metrics.Metric;
import org.qmpm.logtrie.metrics.MetricThread;
import org.qmpm.logtrie.tools.FileInfo;
import org.qmpm.logtrie.tools.FileInfoFactory;
import org.qmpm.logtrie.tools.MathTools;
import org.qmpm.logtrie.tools.XESTools;
import org.qmpm.logtrie.tools.XLogFile;
import org.qmpm.logtrie.trie.AbstractTrieMediator;
import org.qmpm.logtrie.trie.Trie;
import org.qmpm.logtrie.trie.Trie.Node;
import org.qmpm.logtrie.trie.TrieImpl;
import org.qmpm.logtrie.ui.ProgObsThread;

public class ModelTrieMediator extends AbstractTrieMediator {

	private class CVProg {
	}

	public class ModelTrieAttributes<T extends Collection<? extends List<? extends Object>>> extends TrieAttributes<T> {

		public static final String MINERS = "MINERS";
		private Metric miner = null;
		private boolean mineThisTrie = true;
		private CrossValidationType cvType = CrossValidationType.None;

		ModelTrieAttributes(Trie t, String n, FileInfo<T> f) {
			super(t, n, f);
		}

		public Metric getMiner() {
			return this.miner;
		}

		public void setMiner(Metric m, boolean mine) {
			this.mineThisTrie = mine;
			this.miner = m;
			this.setInfo(m.toString());
		}

		public void setCVType(CrossValidationType cvType) {
			this.cvType = cvType;
		}

		public CrossValidationType getCVType() {
			return this.cvType;
		}
	}

	private Map<Trie, ModelTrieAttributes<? extends Collection<? extends List<? extends Object>>>> modTrieAttMap = new HashMap<>();
	private CrossValidationType cvType = CrossValidationType.None;

	public void addMiner(MinerLabel m) {

		String[] args = {};
		this.addMiner(m, args);
	}

	public void addMiner(MinerLabel m, String[] args) {

		this.addMetric(m, args);
	}

	@Override
	public ModelTrieAttributes<? extends Collection<? extends List<? extends Object>>> getTrieAttributes(Trie t) {

		return this.modTrieAttMap.get(t);
	}

	public <T extends Collection<? extends List<? extends Object>>> void addModel(GenericProcessModel m,
			FileInfo<T> fi) {

		Trie t = new ModelTrie(m);
		TrieAttributes<T> trieAtt = new TrieAttributes<>(t, fi.getName(), fi);
		this.setTrieAttributes(t, trieAtt);
		this.tries.add(t);
	}

	protected void beforeTrieBuild(Trie t, GenericProcessModel m) {

		((ModelTrie) t).setup(m);
	}

	@Override
	public void setupTries() {

		Map<MetricLabel, List<List<String>>> minerMap = this.getMetrics(MinerLabel.class.getSimpleName());

		int c = 1;

		for (FileInfo fi : this.files) {

			if (minerMap.keySet().isEmpty()) {
				Trie t = new TrieImpl();
				ModelTrieAttributes trieAtt = new ModelTrieAttributes(t, fi.getName(), fi);
				this.tries.add(t);
				this.setTrieAttributes(t, trieAtt);
			}

			for (MetricLabel l : minerMap.keySet()) {

				for (List<String> args : minerMap.get(l)) {

					Trie t = new ModelTrie();
					ModelTrieAttributes trieAtt = new ModelTrieAttributes(t, fi.getName(), fi);
					trieAtt.cvType = this.cvType;

					String[] argsArray = new String[args.size() + 1];

					argsArray[0] = trieAtt.file.getFile().getPath();

					for (int i = 1; i < args.size() + 1; i++) {
						argsArray[i] = args.get(i - 1);
					}

					Metric m = l.delegate(argsArray);

					m.registerProgObs(this.progObs);
					m.setTimeout(this.timeout);
					if (m.getSigDigs() < 0) {
						m.setSigDigits(this.sigDigs);
					}

					trieAtt.setMiner(m, true);
					// trieAtt.setName(trieAtt.getName());
					this.tries.add(t);
					this.setTrieAttributes(t, trieAtt);

					try {
						this.crossValidation(t, c++, this.files.size());
					} catch (Exception e) {
						System.out.println("PROBLEM RUNNING CROSS VALIDATION!!!");
						e.printStackTrace();
					}
				}
			}
		}
		this.removeMetrics(MinerLabel.class.getSimpleName());
	}

	@Override
	protected String getLastMetric() {
		return EvaluationMetricLabel.class.getSimpleName();
	}

	public void setCrossValidation(CrossValidationType cv, int k) {

		cv.setK(k);
		cv.setInSample(false);
		cv.setOutSample(true);
		this.cvType = cv;
	}

	public void setCrossValidation(CrossValidationType cv, int k, boolean inSample, boolean outSample) {

		cv.setK(k);
		cv.setInSample(inSample);
		cv.setOutSample(outSample);
		this.cvType = cv;
	}

	@Override
	protected boolean needToBuildTries() {

		for (MetricLabel m : this.getMetrics(EvaluationMetricLabel.class.getSimpleName()).keySet()) {
			if (!m.equals(EvaluationMetricLabel.ModelSize) && !m.equals(EvaluationMetricLabel.MiningTime_ms)) {
				return true;
			}
		}

		return false;
	}

	protected <T extends Collection<? extends List<? extends Object>>> void crossValidation(Trie t, int current,
			int total) throws Exception {

		ModelTrieAttributes<T> ta = (ModelTrieAttributes<T>) this.getTrieAttributes(t);
		RunMiner m = (RunMiner) ta.getMiner();

		if (!ta.getCVType().equals(CrossValidationType.None)) {

			CVProg placeHolder = new CVProg();
			this.progObs.register(placeHolder);
			ProgObsThread cvProgThread = new ProgObsThread(this.progObs, placeHolder, this.showProgress);

			cvProgThread.setPreLabel(this.preLabel);
			cvProgThread.setEndLabel(this.CVPARTITIONING);
			cvProgThread.start();

			this.tries.remove(t);
			FileInfo<T> mainFile = ta.getFile();

			mainFile.sort();

			if (mainFile.getLoadedFile() instanceof XLog) {
				if (!XESTools.isSorted((XLog) mainFile.getLoadedFile())) {
					Framework.permitOutput();
					System.out.println(
							"This is a temporary fix: The XLog was found to be unordered immediately prior to partitioning for cross-validation as would be expected. Aborting...");
					System.exit(1);
				}
			} else {
				Framework.permitOutput();
				System.out.println("This is a temporary fix: Expected XLog for cross-validation, received: "
						+ mainFile.getLoadedFile().getClass().getSimpleName() + ". Aborting...");
				System.exit(1);
			}

			if (this.cvType.equals(CrossValidationType.KFoldShuffle)
					|| this.cvType.equals(CrossValidationType.KFoldShuffleNoTwin)) {
				mainFile.shuffle();
			}

			String mainFileDir = mainFile.getFile().getParent();
			String mainFileName = mainFile.getFile().getName();
			String cvDir = "cross-validation-" + ta.getCVType().toString() + "-" + ta.getCVType().getTimeStamp();

			List<FileInfo<T>> partitionedLog = FileInfoFactory.partition(mainFile, ta.getCVType().getK());

			for (int j = 0; j < ta.getCVType().getK(); j++) {

				Trie trainingTrie = new ModelTrie();
				Trie validationTrie = new ModelTrie();
				ModelTrieAttributes<T> trainingTA;
				ModelTrieAttributes<T> validationTA;
				String trainingPath = "";
				String validationPath = "";
				String partitionPath = "";

				if (mainFileName.toLowerCase().endsWith(".xes")) {
					validationPath = mainFileDir + File.separator + cvDir + File.separator + (j + 1) + "-validation-"
							+ mainFileName.substring(0, mainFileName.length() - 4) + ".xes";
					trainingPath = mainFileDir + File.separator + cvDir + File.separator + (j + 1) + "-training-"
							+ mainFileName.substring(0, mainFileName.length() - 4) + ".xes";
					partitionPath = mainFileDir + File.separator + cvDir + File.separator + (j + 1) + "-partition-"
							+ mainFileName.substring(0, mainFileName.length() - 4) + ".xes";
				}

				List<FileInfo<T>> trainingParted = new ArrayList<>();
				FileInfo<T> training = FileInfoFactory.build(trainingPath, false);
				FileInfo<T> validation = FileInfoFactory.build(validationPath, false);

				if (this.cvType.equals(CrossValidationType.KFoldFlattenExpand)) {

					XLogFile validationFI = new XLogFile(mainFile.getName(), false);
					XLogFile trainingFI = new XLogFile(mainFile.getName(), false);
					FileInfo<T> partition = FileInfoFactory.build(partitionPath, false);
					partition.append(mainFile);
					Trie partitionTrie = new TrieImpl();
					TrieAttributes<?> partitionTA = new TrieAttributes<>(partitionTrie, mainFile.getName(), partition);

					this.setTrieAttributes(partitionTrie, ta);
					this.buildTrie(partitionTrie, false);

					// Partition

					List<List<Node>> partedNodes = MathTools.partition(partitionTrie.getEndNodeSet(),
							ta.getCVType().getK());

					// TODO: Consolidate

					for (int i = 0; i < partedNodes.size(); i++) {
						if (i == j) {
							XLog log = validationFI.getXFactory().createLog();
							for (Node n : partedNodes.get(i)) {
								List<ElementLabel> trace = partitionTrie.getVisitingPrefix(n);
								XTrace xTrace = XESTools.toXtrace(trace, validationFI.getXFactory());
								for (int h = 0; h < n.getEndVisits(); h++) {
									log.add(xTrace);
								}
							}

							validationFI.setXLog(log);
							validation.append((FileInfo<T>) validationFI);

						} else {
							XLog log = trainingFI.getXFactory().createLog();
							for (Node n : partedNodes.get(i)) {
								List<ElementLabel> trace = partitionTrie.getVisitingPrefix(n);
								XTrace xTrace = XESTools.toXtrace(trace, trainingFI.getXFactory());
								for (int h = 0; h < n.getEndVisits(); h++) {
									log.add(xTrace);
								}
							}
							trainingFI.setXLog(log);
							training.append((FileInfo<T>) trainingFI);
						}
					}

					try {
						t.addElementLabels(XESTools.getAllActivities((XLog) ta.getFile().getLoadedFile()));
					} catch (LabelTypeException e2) {
						System.out.println(e2.getMessage());
						e2.printStackTrace();
					}

					validationTrie.setAssociatedTrie(t);

					trainingTA = new ModelTrieAttributes<>(trainingTrie, training.getName(), training);
					validationTA = new ModelTrieAttributes<>(validationTrie, validation.getName(), validation);

				} else {

					trainingParted.addAll(partitionedLog);

					validation.append(trainingParted.remove(j));

					for (FileInfo<T> part : trainingParted) {
						training.append(part);
					}

					try {
						t.addElementLabels(XESTools.getAllActivities((XLog) ta.getFile().getLoadedFile()));
					} catch (LabelTypeException e2) {
						System.out.println(e2.getMessage());
						e2.printStackTrace();
					}
					validationTrie.setAssociatedTrie(t);

					trainingTA = new ModelTrieAttributes<>(trainingTrie, training.getName(), training);
					validationTA = new ModelTrieAttributes<>(validationTrie, validation.getName(), validation);
				}

				if (m != null) {

					String[] args = m.getArgs();
					args[0] = trainingPath;

					RunMiner cvMiner = (RunMiner) m.getLabel().delegate(args);

					// cvProgThread.setEndLabel(MINING);

					cvMiner.setLog((XLog) training.getLoadedFile());
					trainingTA.setMiner(cvMiner, true);
					validationTA.setMiner(cvMiner, false);
					trainingTA.setName(trainingTA.getName()); // + "-" + trainingTA.getInfo());
					this.setTrieAttributes(trainingTrie, trainingTA);

					cvMiner.registerProgObs(this.progObs);
					cvMiner.setTimeout(this.timeout);
					cvMiner.setSigDigits(this.sigDigs);

					/*
					 * MetricThread metThread = new MetricThread(cvMiner, trainingTrie);
					 *
					 *
					 *
					 * long startMining = System.nanoTime(); metThread.start();
					 *
					 * try { metThread.join(); } catch (InterruptedException e) {
					 * e.printStackTrace(); }
					 *
					 * ((ModelTrie) trainingTrie).setup(cvMiner.getModel()); ((ModelTrie)
					 * validationTrie).setup(cvMiner.getModel());
					 */
					validationTA.setInfo(cvMiner.toString());
					// updateAllMetrics();
					// validationTA.setName(validationTA.getName());
					this.setTrieAttributes(validationTrie, validationTA);
				}

				if (this.cvType.equals(CrossValidationType.KFoldNoTwin)
						|| this.cvType.equals(CrossValidationType.KFoldShuffleNoTwin)) {
					if (this.verbose) {
						System.out.println("Looking for twins...");
					}
					this.setTrieAttributes(trainingTrie, trainingTA);
					this.buildTrie(trainingTrie, false, j + 1, ta.getCVType().getK());
					List<List<? extends Object>> twins = new ArrayList<>();
					for (List<? extends Object> seq : validation.getLoadedFile()) {
						if (this.verbose) {
							try {
								System.out.println("seq: " + XESTools.xTraceToString((XTrace) seq));
							} catch (LabelTypeException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
						}
						Node n;
						try {
							n = trainingTrie.search(seq);
						} catch (LabelTypeException e) {
							n = null;
						} catch (NodeNotFoundException e) {
							n = null;
						}
						if (n != null) {
							if (this.verbose) {
								System.out.println("Found a twin!");
							}
							twins.add(seq);
						}
						if (this.verbose) {
							System.out.println(n);
						}
					}

					for (List<? extends Object> seq : twins) {
						validation.getLoadedFile().remove(seq);
					}
					if (this.verbose) {
						System.out.println(
								"Removed " + twins.size() + " of " + (twins.size() + validation.getLoadedFile().size())
										+ " traces from validation set which also appear in training set");
					}
				}

				if (this.cvType.getOutSample()) {
					this.setTrieAttributes(validationTrie, validationTA);
					// trieIterator.add(validationTrie);
					this.tries.add(validationTrie);
				}

				if (this.cvType.getInSample()) {
					this.setTrieAttributes(trainingTrie, trainingTA);
					// trieIterator.add(trainingTrie);
					this.tries.add(trainingTrie);
				}

				/*
				 * try { List<String> revPath = pathAsRevList(validation.getFile());
				 * filePathTrie.insert(revPath, false); } catch (ProcessTransitionException e) {
				 * // TODO Auto-generated catch block e.printStackTrace(); } catch
				 * (LabelTypeException e) { // TODO Auto-generated catch block
				 * e.printStackTrace(); }
				 */
				/*
				 * try { addFile(validationPath); } catch (LabelTypeException e) {
				 * e.printStackTrace(); }
				 */

				this.progObs.updateProgress(placeHolder, (double) (j + 1) / ta.getCVType().getK());
			}

			this.progObs.setFinished(placeHolder, true);

			try {
				cvProgThread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			// if (!containsMetric(EvaluationMetricLabel.Fitness)) {
			// addMetric(EvaluationMetricLabel.Fitness, new String[0]);
			// }
			// updateAllMetrics();
			// return false;
			System.out.println("FINISHED SETTING UP CROSS-FOLD VALIDATION TRIES:");
		}
	}

	@Override
	protected <T extends Collection<? extends List<?>>> void setTrieAttributes(Trie t, TrieAttributes<?> ta) {
		this.modTrieAttMap.put(t, (ModelTrieAttributes<?>) ta);
	}

	@Override
	protected <T extends Collection<? extends List<?>>> boolean beforeTrieBuild(ListIterator<Trie> trieIterator, Trie t,
			int current, int total, String labelFormat) {

		ModelTrieAttributes<T> ta = (ModelTrieAttributes<T>) this.getTrieAttributes(t);
		RunMiner m = (RunMiner) ta.getMiner();

		if (m != null) {

			ProgObsThread progThread = new ProgObsThread(this.progObs, m, this.showProgress);

			MetricThread metThread = new MetricThread(m, t);

			progThread.setCurrent(current);
			progThread.setTotal(this.tries.size());
			progThread.setPreLabel(this.preLabel);
			progThread.setEndLabel(this.MINING + "(" + m.getLabel() + ")");
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
			// ta.setName(ta.getName());
			return true;
		}
		return false;
	}
}