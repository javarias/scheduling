package alma.scheduling.psm.util;

import java.io.Serializable;

public class ProposalComparison implements Serializable{
	
	private static final long serialVersionUID = -1551287987856008670L;
	
	private String entityID;
	private double ph1mScore;
	private double localScore;
	private int ph1mRank;
	private int localRank;
	
	public String getEntityID() {
		return entityID;
	}
	public void setEntityID(String entityID) {
		this.entityID = entityID;
	}
	public double getPh1mScore() {
		return ph1mScore;
	}
	public void setPh1mScore(double ph1mScore) {
		this.ph1mScore = ph1mScore;
	}
	public double getLocalScore() {
		return localScore;
	}
	public void setLocalScore(double localScore) {
		this.localScore = localScore;
	}
	public int getPh1mRank() {
		return ph1mRank;
	}
	public void setPh1mRank(int ph1mRank) {
		this.ph1mRank = ph1mRank;
	}
	public int getLocalRank() {
		return localRank;
	}
	public void setLocalRank(int localRank) {
		this.localRank = localRank;
	}
}
