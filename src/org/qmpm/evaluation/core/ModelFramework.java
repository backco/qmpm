package org.qmpm.evaluation.core;

import java.util.Arrays;

import org.apache.commons.cli.Option;
import org.qmpm.evaluation.enums.CrossValidationType;
import org.qmpm.evaluation.enums.MinerLabel;
import org.qmpm.evaluation.trie.ModelTrieMediator;
import org.qmpm.logtrie.core.Framework;

public class ModelFramework extends Framework {

	// protected static ModelTrieMediator trieMediator = new ModelTrieMediator();

	public static void addMiner(MinerLabel m, Option o) {

		String[] args = cmd.getOptionValues(o.getLongOpt());

		for (int i = 0; i < args.length; i += o.getArgs()) {

			String[] subArgs = Arrays.copyOfRange(args, i, i + o.getArgs());
			((ModelTrieMediator) trieMediator).addMiner(m, subArgs);
		}
	}

	public static void addMiner(MinerLabel m) {
		((ModelTrieMediator) trieMediator).addMiner(m, new String[0]);
	}

	public static void setCrossValidation(CrossValidationType cvType, Option o) {

		String[] args = cmd.getOptionValues(o.getLongOpt());

		int k = 1;
		try {
			k = Integer.parseInt(args[0]);
		} catch (NumberFormatException e) {
			ModelFramework.permitOutput();
			System.out.println(
					"Positive integer value expected for k-fold cross validation parameter. Received: " + args[0]);
			System.out.println("Using default value: " + k);
			ModelFramework.resetQuiet();
		}

		boolean inSample = false;
		boolean outSample = true;
		if (String.valueOf(args[1]).toLowerCase().equals("i")) {
			inSample = true;
			outSample = false;
		} else if (String.valueOf(args[1]).toLowerCase().equals("o")) {
			inSample = false;
			outSample = true;
		} else if (String.valueOf(args[1]).toLowerCase().equals("b")) {
			inSample = true;
			outSample = true;
		}

		((ModelTrieMediator) trieMediator).setCrossValidation(cvType, k, inSample, outSample);
	}

	public static void loadModelFile(Option o) {

		String[] args = cmd.getOptionValues(o.getLongOpt());

		for (int i = 0; i < args.length; i += o.getArgs()) {

			String[] subArgs = Arrays.copyOfRange(args, i, i + o.getArgs());
			((ModelTrieMediator) trieMediator).addMiner(MinerLabel.LoadModel, subArgs);
		}
	}
}
