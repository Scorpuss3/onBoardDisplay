package onBoardDisplay.dataHandling;

import java.awt.Image;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.sql.*;

import javax.imageio.ImageIO;

public class DataHandler {
	public static char[] hexChars = "0123456789ABCDEF".toCharArray();
	public Map<Byte,Boolean> supportedPIDs = new LinkedHashMap<Byte,Boolean>();//total number PIDs 225
	public static Image unknownTextureFront, unknownTextureSide, unknownTextureTop,
	exhTextureFront, exhTextureSide, exhTextureTop,
	getTextureFront, getTextureSide, getTextureTop,
	fulTextureFront, fulTextureSide, fulTextureTop,
	colTextureFront, colTextureSide, colTextureTop,
	srsTextureFront, srsTextureSide, srsTextureTop,
	acnTextureFront, acnTextureSide, acnTextureTop,
	conTextureFront, conTextureSide, conTextureTop,
	disTextureFront, disTextureSide, disTextureTop,
	unkTextureFront, unkTextureSide, unkTextureTop;
	public static final HashMap<String, String> majorLocationCodeDescriptions = new HashMap<String,String>() {{
		put("EXH", "Exhaust System");
		put("GET", "Gearbox / Transmission");
		put("FUL", "Fuel System");
		put("COL", "Cooling System");
		put("SRS", "Safety System");
		put("ACN", "Air Conditioning");
		put("CON", "Controls / Pedals");
		put("DIS", "Display / Dashboard");
		put("UNK", "Unknown");
	}};
	public static final HashMap<String, Image[]> majorLocationCodeTextures = new HashMap<String,Image[]>() {{
		put("EXH", new Image[] {exhTextureFront, exhTextureSide, exhTextureTop});
		//put("GET", new Image[] {getTextureFront, getTextureSide, getTextureTop});
		//put("FUL", new Image[] {fulTextureFront, fulTextureSide, fulTextureTop});
		//put("COL", new Image[] {colTextureFront, colTextureSide, colTextureTop});
		//put("SRS", new Image[] {srsTextureFront, srsTextureSide, srsTextureTop});
		//put("ACN", new Image[] {acnTextureFront, acnTextureSide, acnTextureTop});
		//put("CON", new Image[] {conTextureFront, conTextureSide, conTextureTop});
		//put("DIS", new Image[] {disTextureFront, disTextureSide, disTextureTop});
		put("UNK", new Image[] {unkTextureFront, unkTextureSide, unkTextureTop});
	}};
	private Connection codeC, PIDC;
	
	public DataHandler() {
		//TODO Add test to see if using resource pack, then react accordingly.
		loadCarResources("generic");
		loadDatabaseConnection();
	}
	
	public static class Location {
		public int xPos = 0;
		public int yPos = 0;
		public int zPos = 0;
		public int width = 0;
		public int height = 0;
		public int depth = 0;
		
		public Location (int xPos, int yPos, int zPos, int width, int height, int depth) {
			//Locations are all percentages
			//Origin is (corner of) front-right tyre of car from above, and top of sky.
			//y is height
			//x is along width of car from front view
			//z is along length of car from side
			this.xPos = xPos;
			this.yPos = yPos;
			this.zPos = zPos;
			this.height = height;
			this.width = width;
			this.depth = depth;
		}
		
		public Location (int xPos, int yPos, int zPos) {
			this.xPos = xPos;
			this.yPos = yPos;
			this.zPos = zPos;
		}
	}
	
	public static String getMajorLocationFromCode (String locationCode) {
		String description;
		description = majorLocationCodeDescriptions.get(locationCode);
		return description;
	}
	
	public static Location getMinorLocation (String locationName) {
		//TODO Add location database for Error Codes and PIDs
		return new Location(25,30,40);
	}
	
	//TODO Remove this when possible...
	//public static Code decodeErrorCode (short code) {
	//	//TODO Add error code decoding.
	//	return new Code((short)1,"A Made up Code","UNK", "Steering Wheel");
	//}
	
	public static boolean getBit(byte[] byteArray, int position) {
		int byteNumber = position / 8;//did not have -1, so index off by 1!
		//int startingBitValue = (int) Math.pow(2,byteNumber*8 + 1);
		//int precedingTotal = startingBitValue - 1;
		int bytePosition = 8 - ((position % 8)+1);
		int bitValue = (int) Math.pow(2,bytePosition);
		if ((byteArray[byteNumber] & (byte)bitValue) == ((byte)bitValue & (byte)bitValue)) {
			return true;
		} else {
			return false;
		}
	}
	
	public static String getHexCharacters(byte b) {
		int bytePart1 = ((byte)(b >>> 4) & 0x0F);
		int bytePart2 = (b & 0x0F);
		return ((Character)hexChars[bytePart1]).toString() + ((Character)hexChars[bytePart2]).toString(); 
	}
	
	public static byte getByteFromHexString(String s) throws RuntimeException {
		//Will be 2's Compliment now.
		if (s.length() > 2) {
			throw new RuntimeException("Can only supply this function for 1 byte at a time. Gave more that 2 hex characters: "+s);
		}
		//Turns out there were a few build-in functions I could use together.
		Byte b = new Byte((byte)Integer.parseInt(s,16));
		return (byte)b;
	}
	
	public void loadCarResources(String resourceName) {
		if (resourceName == "generic") {
			try {
				Set set = majorLocationCodeTextures.entrySet();
				Iterator it = set.iterator();
				while (it.hasNext()) {
					Map.Entry me = (Map.Entry)it.next();
					Image[] buffered = new Image[3];
					for (int i = 0; i <3; i++) {
						String suffix = null;
						switch (i) {
						case 0:
							suffix = "Front.png";
							break;
						case 1:
							suffix = "Side.png";
							break;
						case 2:
							suffix = "Top.png";
							break;
						}
						String full = "/onBoardDisplay/Res/genericTextures/" + (String)me.getKey() + suffix;
						System.out.println("Loading Texture: " + full);
						buffered[i] = ImageIO.read(getClass().getResourceAsStream(full));
					}
					majorLocationCodeTextures.put((String)me.getKey(),buffered);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public void loadDatabaseConnection () {
		//http://wiki.ci.uchicago.edu/VDS/VDSDevelopment/UsingSQLite
		try {
			codeC = DriverManager.getConnection("jdbc:sqlite:errorCodes.db");
			codeC.setAutoCommit(false);
			PIDC = DriverManager.getConnection("jdbc:sqlite:PIDs.db");
			PIDC.setAutoCommit(false);
			//Statement st = c.createStatement();
			//int rc = st.executeUpdate( "INSERT INTO x(b) VALUES('qwer')" );
			//System.out.println( "insert returns " + rc );
		    //rs.close();
		    //st.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public Code decodeErrorCode (short id) {
		Code newCode = new Code();
		int tableId = Code.getDatabaseID(id);
		Statement st;
		try {
			st = codeC.createStatement();
			ResultSet rs = st.executeQuery( "SELECT * FROM Generic WHERE Id = "+Integer.toString(tableId) );
			while (rs.next()) {
				newCode.ID = id;
				newCode.Description = rs.getString(2);//Columns start at 1!!!
				newCode.IDString = rs.getString(3);
				newCode.minorLocation = rs.getString(4);
				newCode.majorLocation = rs.getString(5);
			}
			st.close();
			//c.commit();
			//c.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return newCode;
	}
	
	public PID decodePID (byte id) {
		PID newPID = new PID();
		int tableId = PID.getDatabaseID(id);
		Statement st;
		try {
			st = PIDC.createStatement();
			ResultSet rs = st.executeQuery( "SELECT * FROM Mode01 WHERE Id = "+Integer.toString(tableId) );
			while (rs.next()) {
				newPID.ID = id;
				newPID.maxNumOfBytes = rs.getInt(2)+2;//Number stored in database does not include the confirmation of mode & PID sent by the ELM327
				newPID.min = rs.getInt(3);
				newPID.max = rs.getInt(4);
				newPID.unit = rs.getString(5);
				newPID.IDString = rs.getString(6);
				newPID.Description = rs.getString(7);//Columns start at 1!!!
				newPID.minorLocation = "";//TODO add these to database and read from it
				newPID.majorLocation = "";
			}
			st.close();
			//c.commit();
			//c.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return newPID;
	}
}
