package org.qmpm.evaluation.enums;

import java.time.Instant;

public enum CrossValidationType {
	None("None"),
	KFold("k-Fold"),
	KFoldShuffle("k-fold: shuffled"),
	KFoldNoTwin("k-fold: twins removed from validation set"),
	KFoldShuffleNoTwin("k-fold: shuffled, twins removed from validation set"),
	KFoldFlattenExpand("k-fold, flatten-partition-expand (avoids twins without skrinking log)");

	private String desc;
	private int k = 1;
	String timeStamp = "";
	private boolean inSample = false;
	private boolean outSample = true;

	CrossValidationType(String shortDesc) {
		this.desc = shortDesc;
	}

	public int getK() {
		return this.k;
	}

	public void setK(int k) {
		this.k = k;
	}

	public String shortDescription() {
		return this.desc;
	}

	public String labelType() {
		return this.getClass().getSimpleName();
	}

	public String getTimeStamp() {

		if (this.timeStamp.equals("")) {
			this.timeStamp = Instant.now().toString().replaceAll(":", "-");
		}
		return this.timeStamp;
	}

	public boolean getInSample() {
		return this.inSample;
	}

	public boolean getOutSample() {
		return this.outSample;
	}

	public void setInSample(boolean b) {
		this.inSample = b;
	}

	public void setOutSample(boolean b) {
		this.outSample = b;
	}
}
