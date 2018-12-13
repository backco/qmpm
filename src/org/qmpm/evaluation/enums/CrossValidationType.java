package org.qmpm.evaluation.enums;

import java.time.Instant;

public enum CrossValidationType {
	None("None"),
	KFold("k-Fold"),
	KFoldShuffle("k-fold, shuffled"),
	KFoldNoTwin("k-fold, no twinning"),
	KFoldShuffleNoTwin("k-fold, shuffled, no twinning");
	
	private String desc;
	private int k = 1;
	String timeStamp = "";
	private boolean inSample = false;
	private boolean outSample = true;
	
	CrossValidationType(String shortDesc) {
		desc = shortDesc;
	}
	
	public int getK() {
		return k;
	}
	
	public void setK(int k) {
		this.k = k;
	}

	public String shortDescription() {
		return desc;
	}
	
	public String labelType() {
		return this.getClass().getSimpleName();
	}
	
	public String getTimeStamp() {
		
		if (timeStamp.equals("")) timeStamp = Instant.now().toString().replaceAll(":", "-");
		return timeStamp;
	}
	
	public boolean getInSample() {
		return inSample;
	}
	
	public boolean getOutSample() {
		return outSample;
	}
	
	public void setInSample(boolean b) {
		inSample = b;
	}
	
	public void setOutSample(boolean b) {
		outSample = b;
	}
}
