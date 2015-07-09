package onBoardDisplay.dataHandling;

public class Code {
	public short ID;
	public String Description;
	public String majorLocation;
	public String minorLocation;
	
	public Code (short ID, String Description, String majorLocation, String minorLocation) {
		this.ID = ID;
		this.Description = Description;
		this.majorLocation = majorLocation;
		this.minorLocation = minorLocation;
	}
}
