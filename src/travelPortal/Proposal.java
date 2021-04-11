package travelPortal;

import java.util.LinkedList;
import java.util.List;



public class Proposal {


	private String agency;
	private String period;
	private int minNP;
	private int maxNP;
	private int price;
	private List<Activity> activities;
	private List<Participant> attendees;



	
	public Proposal(String agency, String period, int minNP, int maxNP, int price) {
		this.agency = agency;
		this.period = period;
		this.minNP = minNP;
		this.maxNP = maxNP;
		this.price = price;
		activities = new LinkedList<>();
	    attendees = new LinkedList<>();
	  
	
	}
	
	public String getAgency() {
		return agency;
	}

	public String getPeriod() {
		return period;
	}
	public int getMinNP() {
		return minNP;
	}
	public int getMaxNP() {
		return maxNP;
	}
	public int getPrice() {
		return price;
	}
	
	public void AddActivities(Activity activity) {
		activities.add(activity);
	}
	
	public List<Activity> getActivities() {
		return activities;
	}

	public List<Participant> getAttendees() {
		return attendees;
	}

	public void setAttendees(Participant attendee) {
		attendees.add(attendee);
	}




}
