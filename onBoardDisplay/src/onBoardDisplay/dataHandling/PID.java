package onBoardDisplay.dataHandling;

public class PID {
	public byte ID;
	public String IDString;
	public String Description;
	public String unit;
	public int maxNumOfBytes;//Cannot rely on getting this many (e.g. error codes present). This means some readings need special consideration.)
	public int min;
	public int max;
	public String majorLocation;
	public String minorLocation;
	
	public PID (byte ID, String Description, String majorLocation, String minorLocation, int maxNumOfBytes) {
		this.ID = ID;
		this.IDString = getStringFromID(ID);
		this.Description = Description;
		this.majorLocation = majorLocation;
		this.minorLocation = minorLocation;
		this.maxNumOfBytes = maxNumOfBytes;
	}
	
	public PID() {
	}
	
	public static String getStringFromID(byte byt) {
		return DataHandler.getHexCharacters(byt);
	}
	
	public static int getDatabaseID(byte ID) {
		int finalInt = 0;
		finalInt = finalInt | ID;
		return finalInt;
	}
	
	public static short getIDfromString(String str) {
		return DataHandler.getByteFromHexString(str);
	}
	
	
}
