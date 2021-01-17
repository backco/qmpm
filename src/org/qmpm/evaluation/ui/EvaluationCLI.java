package org.qmpm.evaluation.ui;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.qmpm.evaluation.core.ModelFramework;
import org.qmpm.evaluation.enums.CrossValidationType;
import org.qmpm.evaluation.enums.EvaluationMetricLabel;
import org.qmpm.evaluation.enums.MinerLabel;
import org.qmpm.logtrie.core.Framework;
import org.qmpm.logtrie.ui.CLI;
import org.qmpm.logtrie.ui.LambdaOption;

public class EvaluationCLI implements CLI {

	public static final String USAGE = "qmpm [OPTION]...[FILE/PATH]...\nNote: option arguments should follow the option flag, separated by spaces";

	@Override
	public Options generateOptions() {

		final Options options = new Options();

		options.addOption(new LambdaOption("q", "quiet", false, "quiet output", () -> ModelFramework.setQuiet(true)));
		options.addOption(
				new LambdaOption("v", "verbose", false, "verbose output", () -> ModelFramework.setVerbose(true)));
		options.addOption(new LambdaOption("h", "help", false, "print this message", () -> this.printHelp(options)));
		options.addOption(new LambdaOption("t", "time", false, "print time elapsed for computing metric",
				() -> ModelFramework.showTime(true)));
		options.addOption(new LambdaOption("s", "show-progress", false,
				"show progress in console (do not use if piping to output file)",
				() -> ModelFramework.showProgress(true)));

		final LambdaOption modelFileOption = new LambdaOption("l", "file", true,
				"read model from <FILE>. Should be .decl or .pnml");
		modelFileOption.setArgs(1);
		modelFileOption.setArgName("FILE");
		modelFileOption.setOperation(() -> ModelFramework.loadModelFile(modelFileOption));
		options.addOption(modelFileOption);
		
		final LambdaOption timeoutOption = new LambdaOption("T", "timeout", true,
				"set time limit for metric computation");
		timeoutOption.setArgs(1);
		timeoutOption.setArgName("SECONDS");
		timeoutOption.setOperation(() -> ModelFramework.setTimeout(timeoutOption));
		options.addOption(timeoutOption);

		final LambdaOption sigDigOption = new LambdaOption("D", "significant-digits", true,
				"set precision of output (no. of significant digits)");
		sigDigOption.setArgs(1);
		sigDigOption.setArgName("DIGITS");
		sigDigOption.setOperation(() -> ModelFramework.setSigDigits(sigDigOption));
		options.addOption(sigDigOption);
		options.addOption(new LambdaOption("F", "flatten", false, "ignore multiple occurrences of same sequence",
				() -> Framework.setFlatten(true)));

		options.addOption(new LambdaOption("f", "fitness", false, "model-log fitness",
				() -> ModelFramework.addMetric(EvaluationMetricLabel.Fitness)));
		options.addOption(new LambdaOption("P", "precision", false, "model-log precision",
				() -> ModelFramework.addMetric(EvaluationMetricLabel.Precision)));
		options.addOption(new LambdaOption("p", "precision-normalized", false, "model-log precision (normalized)",
				() -> ModelFramework.addMetric(EvaluationMetricLabel.PrecisionNormalized)));
		options.addOption(new LambdaOption("g", "generalization-event", false, "model-log generalization (event-based)",
				() -> ModelFramework.addMetric(EvaluationMetricLabel.GeneralizationEventBased)));
		options.addOption(new LambdaOption("G", "generalization-state", false, "model-log generalization (state-based)",
				() -> ModelFramework.addMetric(EvaluationMetricLabel.GeneralizationStateBased)));
		options.addOption(new LambdaOption("m", "model-size", false,
				"model-size (constraints/edges for declarative/imperative models)",
				() -> ModelFramework.addMetric(EvaluationMetricLabel.ModelSize)));
		options.addOption(new LambdaOption("T", "mining-time", false, "mining-time (milliseconds)",
				() -> ModelFramework.addMetric(EvaluationMetricLabel.MiningTime_ms)));
		options.addOption(new LambdaOption("u", "unique", false, "ratio of unique traces to total traces (0.0 - 1.0)",
				() -> Framework.addMetric(EvaluationMetricLabel.UniqueTraces)));
		options.addOption(new LambdaOption("S", "size", false,
				"size of log/collection (this includes sequences which failed to be inserted)",
				() -> Framework.addMetric(EvaluationMetricLabel.CollectionSize)));

		final LambdaOption crossValOption = new LambdaOption("c", "cross-validation", true,
				"K-fold cross validation.\n'i': compute metric on training data\n'o': compute metric on validation data\n'b':compute on both training and validation data");
		crossValOption.setArgs(2);
		crossValOption.setArgName("FOLDS(K), IN/OUT-SAMPLE");
		crossValOption.setOperation(() -> ModelFramework.setCrossValidation(CrossValidationType.KFold, crossValOption));
		options.addOption(crossValOption);

		final LambdaOption crossValNoTwinOption = new LambdaOption("C", "cross-validation-no-twin", true,
				"K-fold cross validation with traces seen in training set removed from validation set. Avoids testing on training data.\n'i': compute metric on training data\n'o': compute metric on validation data\n'b':compute on both training and validation data");
		crossValNoTwinOption.setArgs(2);
		crossValNoTwinOption.setArgName("FOLDS(K), IN/OUT-SAMPLE");
		crossValNoTwinOption.setOperation(
				() -> ModelFramework.setCrossValidation(CrossValidationType.KFoldNoTwin, crossValNoTwinOption));
		options.addOption(crossValNoTwinOption);

		final LambdaOption crossValShuffleOption = new LambdaOption("r", "cross-validation-shuffle", true,
				"K-fold cross validation - shuffle data before partition. \n'i': compute metric on training data\n'o': compute metric on validation data\n'b':compute on both training and validation data");
		crossValShuffleOption.setArgs(2);
		crossValShuffleOption.setArgName("FOLDS(K), IN/OUT-SAMPLE");
		crossValShuffleOption.setOperation(
				() -> ModelFramework.setCrossValidation(CrossValidationType.KFoldShuffle, crossValShuffleOption));
		options.addOption(crossValShuffleOption);

		final LambdaOption crossValShuffleNoTwinOption = new LambdaOption("R", "cross-validation-shuffle-no-twin", true,
				"K-fold cross validation - shuffle data before partition - traces seen in training set removed from validation set. Avoids testing on training data.\n'i': compute metric on training data\n'o': compute metric on validation data\n'b':compute on both training and validation data");
		crossValShuffleNoTwinOption.setArgs(2);
		crossValShuffleNoTwinOption.setArgName("FOLDS(K), IN/OUT-SAMPLE");
		crossValShuffleNoTwinOption.setOperation(() -> ModelFramework
				.setCrossValidation(CrossValidationType.KFoldShuffleNoTwin, crossValShuffleNoTwinOption));
		options.addOption(crossValShuffleNoTwinOption);

		final LambdaOption crossValFlattenExpandOption = new LambdaOption("V", "cross-validation-flatten-expand", true,
				"K-fold cross validation - flatten, split, re-expand log - traces seen in training set not put in validation set, but notraces are removed. Avoids testing on training data. Folds(partitions) will likely be unequal in size\n'i': compute metric on training data\n'o': compute metric on validation data\n'b':compute on both training and validation data");
		crossValFlattenExpandOption.setArgs(2);
		crossValFlattenExpandOption.setArgName("FOLDS(K), IN/OUT-SAMPLE");
		crossValFlattenExpandOption.setOperation(() -> ModelFramework
				.setCrossValidation(CrossValidationType.KFoldFlattenExpand, crossValFlattenExpandOption));
		options.addOption(crossValFlattenExpandOption);

		final LambdaOption declareMinerOption = new LambdaOption("d", "declare-miner", false,
				"run Declare Miner (control-flow perspective)");
		declareMinerOption.setArgs(2);
		declareMinerOption.setArgName("ALPHA, SUPPORT");
		declareMinerOption.setOperation(() -> ModelFramework.addMiner(MinerLabel.DeclareMiner, declareMinerOption));
		options.addOption(declareMinerOption);

		options.addOption(new LambdaOption("L", "flower-miner", false, "run Flower Miner",
				() -> ModelFramework.addMiner(MinerLabel.FlowerMiner)));
		options.addOption(new LambdaOption("I", "inductive-miner", false, "run Inductive Miner",
				() -> ModelFramework.addMiner(MinerLabel.InductiveMiner)));

		final LambdaOption minerfulOption = new LambdaOption("M", "minerful", false, "run MINERful");
		minerfulOption.setArgs(2);
		minerfulOption.setArgName("CONF, INTFACTOR");
		minerfulOption.setOperation(() -> ModelFramework.addMiner(MinerLabel.MINERful, minerfulOption));
		options.addOption(minerfulOption);

		final LambdaOption minerfulAutoOption = new LambdaOption("a", "minerful-auto", false,
				"run MINERful with automatic parameter tuning using:\nSTEP (0.0-1.0): step size resolution\nCONSTRAINTS (int): number of constraints allowed");
		minerfulAutoOption.setArgs(2);
		minerfulAutoOption.setArgName("STEP, CONSTRAINTS");
		minerfulAutoOption.setOperation(() -> ModelFramework.addMiner(MinerLabel.MINERfulAuto, minerfulAutoOption));
		options.addOption(minerfulAutoOption);

		return options;
	}

	@Override
	public CommandLine generateCommandLine(Options options, String[] commandLineArguments) {

		final CommandLineParser cmdLineParser = new DefaultParser();
		CommandLine commandLine = null;

		try {
			commandLine = cmdLineParser.parse(options, commandLineArguments);
		} catch (ParseException parseException) {
			System.out.println("ERROR: Unable to parse command-line arguments due to: " + parseException.getMessage());
			this.printHelp(options);
			System.exit(1);
		}

		return commandLine;
	}

	@Override
	public void printHelp(Options options) {

		ModelFramework.permitOutput();
		HelpFormatter formatter = new HelpFormatter();
		formatter.setWidth(120);
		formatter.setOptionComparator(null);
		formatter.printHelp(USAGE, options);
		ModelFramework.resetQuiet();
	}
}
