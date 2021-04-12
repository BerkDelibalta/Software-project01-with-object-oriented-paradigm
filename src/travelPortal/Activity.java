//Authored by Berk Delibalta

package travelPortal;

public class Activity {
	private String activityType;
	private int price;
	
	public Activity(String activityType, int price) {
		this.activityType = activityType;
		this.price = price;
	}

	public String getActivityType() {
		return activityType;
	}

	public int getPrice() {
		return price;
	}
	

}
