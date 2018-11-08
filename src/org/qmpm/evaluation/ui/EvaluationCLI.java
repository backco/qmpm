package org.qmpm.evaluation.ui;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.qmpm.logtrie.core.Framework;
import org.qmpm.evaluation.core.ModelFramework;
import org.qmpm.evaluation.enums.CrossValidationType;
import org.qmpm.evaluation.enums.EvaluationMetricLabel;
import org.qmpm.evaluation.enums.MinerLabel;
import org.qmpm.logtrie.ui.CLI;
import org.qmpm.logtrie.ui.LambdaOption;

public class EvaluationCLI implements CLI {
	
	public static final String USAGE = "qmpm [OPTION]...[FILE/PATH]...\nNote: option arguments should follow the option flag, separated by spaces";
	
	public Options generateOptions() {

		final Options options = new Options();

		options.addOption( new LambdaOption("q", "quiet", false, "quiet output", () -> ModelFramework.setQuiet(true)) );
		options.addOption( new LambdaOption("v", "verbose", false, "verbose output", () -> ModelFramework.setVerbose(true)) );	   
		options.addOption( new LambdaOption("h", "help", false, "print this message", () -> printHelp(options)) );
		options.addOption( new LambdaOption("t", "time", false, "print time elapsed for computing metric", () -> ModelFramework.showTime(true)) );
		options.addOption( new LambdaOption("s", "show-progress", false, "show progress in console (do not use if piping to output file)", () -> ModelFramework.showProgress(true)) );

		final LambdaOption timeoutOption = new LambdaOption("T", "timeout", true, "set time limit for metric computation");
		timeoutOption.setArgs(1);
		timeoutOption.setArgName("SECONDS");
		timeoutOption.setOperation( () -> ModelFramework.setTimeout(timeoutOption) );
		options.addOption(timeoutOption);

		final LambdaOption sigDigOption = new LambdaOption("D", "significant-digits", true, "set precision of output (no. of significant digits)");
		sigDigOption.setArgs(1);
		sigDigOption.setArgName("DIGITS");
		sigDigOption.setOperation( () -> ModelFramework.setSigDigits(sigDigOption) );
		options.addOption(sigDigOption);
		options.addOption( new LambdaOption("F", "flatten", false, "ignore multiple occurrences of same sequence", () -> Framework.setFlatten(true)) );

		options.addOption( new LambdaOption("f", "fitness", false, "model-log fitness", () -> ModelFramework.addMetric(EvaluationMetricLabel.Fitness)) );
		options.addOption( new LambdaOption("P", "precision", false, "model-log precision", () -> ModelFramework.addMetric(EvaluationMetricLabel.Precision)) );
		options.addOption( new LambdaOption("p", "precision-normalized", false, "model-log precision (normalized)", () -> ModelFramework.addMetric(EvaluationMetricLabel.PrecisionNormalized)) );
		options.addOption( new LambdaOption("l", "precision-lower", false, "model-log precision lower bound (i.e. precision of flower model)", () -> ModelFramework.addMetric(EvaluationMetricLabel.PrecisionFlower)) );
		options.addOption( new LambdaOption("g", "generalization-event", false, "model-log generalization (event-based)", () -> ModelFramework.addMetric(EvaluationMetricLabel.GeneralizationEventBased)) );
		options.addOption( new LambdaOption("G", "generalization-state", false, "model-log generalization (state-based)", () -> ModelFramework.addMetric(EvaluationMetricLabel.GeneralizationStateBased)) );
		options.addOption( new LambdaOption("m", "model-size", false, "model-size (constraints/edges for declarative/imperative models)", () -> ModelFramework.addMetric(EvaluationMetricLabel.ModelSize)) );
		options.addOption( new LambdaOption("T", "mining-time", false, "mining-time (milliseconds)", () -> ModelFramework.addMetric(EvaluationMetricLabel.MiningTime_ms)) );

		final LambdaOption crossValOption = new LambdaOption("c", "cross-validation", true, "K-fold cross validation");
		crossValOption.setArgs(1);
		crossValOption.setArgName("FOLDS(K)");
		crossValOption.setOperation( () -> ModelFramework.setCrossValidation(CrossValidationType.KFold, crossValOption));
		options.addOption(crossValOption);
		
		
		options.addOption( new LambdaOption("I", "inductive-miner", false, "run Inductive Miner", () -> ModelFramework.addMiner(MinerLabel.InductiveMiner) ));
		
		
		
		final LambdaOption minerfulOption = new LambdaOption("M", "minerful", false, "run MINERful");
		minerfulOption.setArgs(2);
		minerfulOption.setArgName("CONF, INTFACTOR");
		minerfulOption.setOperation( () -> ModelFramework.addMiner(MinerLabel.MINERful, minerfulOption) );
		options.addOption(minerfulOption);

		final LambdaOption minerfulAutoOption = new LambdaOption("a", "minerful-auto", false, "run MINERful with automatic parameter tuning using:\nSTEP (0.0-1.0): step size resolution\nCONSTRAINTS (int): number of constraints allowed");
		minerfulAutoOption.setArgs(2);
		minerfulAutoOption.setArgName("STEP, CONSTRAINTS");
		minerfulAutoOption.setOperation( () -> ModelFramework.addMiner(MinerLabel.MINERfulAuto, minerfulAutoOption) );
		options.addOption(minerfulAutoOption);
		
		return options;
	}
	
	public CommandLine generateCommandLine(Options options, String[] commandLineArguments) {

		final CommandLineParser cmdLineParser = new DefaultParser();
		CommandLine commandLine = null;
		
		try {
			commandLine = cmdLineParser.parse(options, commandLineArguments);
		} catch (ParseException parseException) {
			System.out.println("ERROR: Unable to parse command-line arguments due to: " + parseException.getMessage());
			printHelp(options);
			System.exit(1);
		}
		
		return commandLine;
	}
	
	public void printHelp(Options options) {
		
		ModelFramework.permitOutput();
		HelpFormatter formatter = new HelpFormatter();
		formatter.setWidth(130);
		formatter.setOptionComparator(null);
		formatter.printHelp(USAGE, options );
		ModelFramework.resetQuiet();
	}
}
