package onBoardDisplay.dataHandling;

import java.awt.Image;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

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
	
	public DataHandler() {
		//TODO Add test to see if using resource pack, then react accordingly.
		loadCarResources("generic");
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
	
	public static Code decodeErrorCode (short code) {
		//TODO Add error code decoding.
		return new Code((short)1,"A Made up Code","UNK", "Steering Wheel");
	}
	
	public static boolean getBit(byte[] byteArray, int position) {
		int byteNumber = position / 8;
		//int startingBitValue = (int) Math.pow(2,byteNumber*8 + 1);
		//int precedingTotal = startingBitValue - 1;
		int bytePosition = 8 - ((position % 8)+1);
		int bitValue = (int) Math.pow(2,bytePosition);
		if ((byteArray[byteNumber] & (byte)bitValue) == (byte)bitValue) {
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
}
