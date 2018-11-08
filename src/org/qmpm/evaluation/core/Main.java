package org.qmpm.evaluation.core;

import org.qmpm.evaluation.trie.ModelTrieMediator;
import org.qmpm.evaluation.ui.EvaluationCLI;
import org.qmpm.logtrie.ui.CLI;

public class Main {

	public static void main(String[] args) {
		
		CLI evaluationCLI = new EvaluationCLI();
		
		ModelFramework.run(evaluationCLI, args, new ModelTrieMediator());
	}

}