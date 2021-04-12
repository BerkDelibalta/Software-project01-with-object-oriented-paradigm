//Authored by Berk Delibalta

package travelPortal;

public class Evaluation {

	private String participant;
	private int point;
	
	public Evaluation(String participant, int point) {
		this.participant=participant;
		this.point=point;
	}

	public String getParticipant() {
		return participant;
	}

	public int getPoint() {
		return point;
	}

}
