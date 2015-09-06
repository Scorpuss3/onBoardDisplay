package onBoardDisplay.dataHandling;

public class PID extends Code{
	public String unit;
	public int min;
	public int max;
	public PID (short ID, String Description, String majorLocation, String minorLocation) {
		super(ID, Description, majorLocation, minorLocation);
	}
}
