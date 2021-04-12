//Authored by Berk Delibalta

package travelPortal;

@SuppressWarnings("serial")
public class TPException extends Exception {
	TPException(String reason) {
		super(reason);
	}
}
