package alma.scheduling.datamodel;

public class Entity {

	private long id;
	private double score;
	
	public Entity(long id, double score) {
		super();
		this.id = id;
		this.score = score;
	}
	
	public Entity() {
		super();
	}
	
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public double getScore() {
		return score;
	}
	public void setScore(double score) {
		this.score = score;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		else if (!(obj instanceof Entity))
			return false;
		else if (((Entity)obj).getId() == this.getId())
			return true;
		else
			return false;
			
	}

	@Override
	public int hashCode() {
		return super.hashCode();
	}

	@Override
	public String toString() {
		return "(" + id +"," + score + ")";
	}
	
	
	
	
}
