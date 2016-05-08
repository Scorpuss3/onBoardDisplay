package onBoardDisplay.dataHandling;

/*
 * This class is the template for the object used to represent PIDs in my code. It has many attributes
 * that can be associated with the PID sensors, including things like a friendlier description of what
 * the sensor represents, minimum and maximum reading values, and locations.
 */

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
	public boolean isBitEncoded;
	public float[] conversionParameters = new float[5];//5 are used because the last one is 1 if byte B is used rather than parameter 2.
	
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
		/* This method gets the name of the PID from the ID number. As it happens, PIDs are
		 * usually just referred to the hexadecimal value of their ID, which is what I
		 * calculated here.
		 */
		return DataHandler.getHexCharacters(byt);
	}
	
	public static int getDatabaseID(byte ID) {
		/*
		 * Bytes can represent negative values, so the byte IDs that were used to represent the
		 * PID sensors in my software could not be directly used as table IDs in the database.
		 * I therefore had to convert the byte to an integer, while ignoring the fact that it
		 * was 2's complement, which generates a different integer for each PID. I then used the
		 * method above to convert back.
		 */
		int finalInt = 0;
		finalInt = finalInt | ID;
		return finalInt;
	}
	
	public static short getIDfromString(String str) {
		//Uses the hex characters to get a byte value for the ID of the PID.
		return DataHandler.getByteFromHexString(str);
	}
	
	
}
