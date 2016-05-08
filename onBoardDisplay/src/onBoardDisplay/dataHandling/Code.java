package onBoardDisplay.dataHandling;

/*
 * This class is the template for the error code objects that my software uses. It has attributes
 * for all the data an error code needs, including place holders for information about it (e.g.
 * description) that will be read from the database when detail is needed to be displayed by the
 * software after the error codes are read. It also contains methods for some of the manipulation
 * specific to error code data.
 */

import java.util.Arrays;

public class Code {
	public short ID;
	public String Description;
	public String majorLocation;
	public String minorLocation;
	public String IDString;
	
	public static String[] DTCCharacters = {"P","C","B","U"};
	public static String[] hexDigits = {"0","1","2","3","4","5","6","7","8","9","A","B","C","D","E","F"};
	
	public Code (short ID, String Description, String majorLocation, String minorLocation) {
		this.ID = ID;
		this.IDString = getStringFromID(ID);
		this.Description = Description;
		this.majorLocation = majorLocation;
		this.minorLocation = minorLocation;
	}
	
	public Code() {
	}
	
	public static int getDatabaseID(short ID) {
		/*TO continue using shorts, the IDs used in my software are signed, so have
		 * Weird values. To be able to interface with a database that only has
		 * positive keys, I have to convert from signed to unsigned in the form of an
		 * integer, which works because it has a larger size, so the negative leftmost bit is never used 
		 */
		int finalInt = 0;
		finalInt = finalInt | ID;
		return finalInt;
	}
	
	public static String getStringFromID(short srt) {
		/*
		 * This method returns the string name of the error code (the friendlier name like P1010) when given
		 * the stored short ID number used to store it in the system and reference it in the ECU. The lines
		 * shown here are extracting specific bytes from the number, and then passing them through maps to
		 * to get the letter values they represent.
		 */
		String finalString = "";
		//Are encoded as described for mode 3 at https://en.wikipedia.org/wiki/OBD-II_PIDs#Mode_3_.28no_PID_required.29
		finalString += DTCCharacters[(srt>>14)&0x03];//First two bits
		finalString += hexDigits[(srt>>12)&0x03];//Second two bits
		finalString += hexDigits[(srt>>8)&0x0F];//Next four bits
		finalString += hexDigits[(srt>>4)&0x0F];//Next four bits
		finalString += hexDigits[(srt)&0x0F];//Last four bits
		return finalString;
	}
	
	public static short getIDfromString(String str) {
		/* 
		 * This method is the exact opposite of the one above, converting the friendlier name into a number
		 * identifier that can be used in the software.
		 */
		short finalShort = (short)0;
		finalShort = (short) (finalShort | (short)(Arrays.asList(DTCCharacters).indexOf(Character.toString(str.charAt(0)))<<14));//First two bits
		finalShort = (short) (finalShort | (short)(Arrays.asList(hexDigits).indexOf(Character.toString(str.charAt(1)))<<12));//Second two bits
		finalShort = (short) (finalShort | (short)(Arrays.asList(hexDigits).indexOf(Character.toString(str.charAt(2)))<<8));//Next Four bits
		finalShort = (short) (finalShort | (short)(Arrays.asList(hexDigits).indexOf(Character.toString(str.charAt(3)))<<4));//Next Four bits
		finalShort = (short) (finalShort | (short)Arrays.asList(hexDigits).indexOf(Character.toString(str.charAt(4))));//Next Four bits
		return finalShort;
	}
}
