package onBoardDisplay.dataHandling;

/*
 * This class contains most of the methods for handling all the underlying data moving around
 * in the software. It holds variables for all the textures, and data structures for many
 * other things like leader boards, supported PIDs, database informations and codes etc. It also
 * provides the methods for handling and manipulating all this data. This includes the
 * connections to the various mySQL databases the software uses to keep track of the error codes,
 * PIDs, manufacturers, dash board layouts and profiles.
 */

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.sql.*;

import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

//import com.sun.prism.paint.Color;

import onBoardDisplay.onBoardDisplay;
import onBoardDisplay.GUI.HUDLayouts.Dash;
import onBoardDisplay.GUI.HUDLayouts.Dash.DashPanel.DashArrangement;
import onBoardDisplay.GUI.components.dials.BarWidget;
import onBoardDisplay.GUI.components.dials.DialSkin1;
import onBoardDisplay.GUI.components.dials.GraphWidget;

public class DataHandler {
	public static char[] hexChars = "0123456789ABCDEF".toCharArray();
	public Map<Byte,Boolean> supportedPIDs = new LinkedHashMap<Byte,Boolean>();//total number PIDs 225
	public static TreeMap<Double,String> leaderboard060 = new TreeMap<Double,String>(){};// 0-60 leaderboard TreeMap because keeps order
	public static TreeMap<Double,String> leaderboard014 = new TreeMap<Double,String>(){};// 1/4 mile leaderboard
	public static Image unknownTextureFront, unknownTextureSide, unknownTextureTop,
	exhTextureFront, exhTextureSide, exhTextureTop,
	engTextureFront, engTextureSide, engTextureTop,
	fulTextureFront, fulTextureSide, fulTextureTop,
	colTextureFront, colTextureSide, colTextureTop,
	drtTextureFront, drtTextureSide, drtTextureTop,
	conTextureFront, conTextureSide, conTextureTop,
	intTextureFront, intTextureSide, intTextureTop,
	iceTextureFront, iceTextureSide, iceTextureTop,
	ligTextureFront, ligTextureSide, ligTextureTop,
	powTextureFront, powTextureSide, powTextureTop,
	brkTextureFront, brkTextureSide, brkTextureTop,
	safTextureFront, safTextureSide, safTextureTop,
	unkTextureFront, unkTextureSide, unkTextureTop;
	public static final HashMap<String, String> majorLocationCodeDescriptions = new HashMap<String,String>() {{
		put("EXH", "Exhaust System");
		put("ENG", "Engine Block");
		put("FUL", "Fuel System");
		put("COL", "Cooling System");
		put("DRT", "Drivetrain");
		put("INT", "Air Intake");
		put("CON", "Controls / Pedals");
		put("ICE", "In-Car Entertainment");
		put("BRK", "Braking");
		put("LIG", "Lighting");
		put("POW", "Starting and Power Supply");
		put("UNK", "Unknown");
		put("SAF", "Safety");
		
	}};
	public static final HashMap<String, Image[]> majorLocationCodeTextures = new HashMap<String,Image[]>() {{
		put("EXH", new Image[] {exhTextureFront, exhTextureSide, exhTextureTop});
		put("ENG", new Image[] {engTextureFront, engTextureSide, engTextureTop});
		put("DRT", new Image[] {drtTextureFront, drtTextureSide, drtTextureTop});
		put("FUL", new Image[] {fulTextureFront, fulTextureSide, fulTextureTop});
		put("INT", new Image[] {intTextureFront, intTextureSide, intTextureTop});
		put("CON", new Image[] {conTextureFront, conTextureSide, conTextureTop});
		put("ICE", new Image[] {iceTextureFront, iceTextureSide, iceTextureTop});
		put("COL", new Image[] {colTextureFront, colTextureSide, colTextureTop});
		put("BRK", new Image[] {brkTextureFront, brkTextureSide, brkTextureTop});
		put("LIG", new Image[] {ligTextureFront, ligTextureSide, ligTextureTop});
		put("POW", new Image[] {powTextureFront, powTextureSide, powTextureTop});
		put("UNK", new Image[] {unkTextureFront, unkTextureSide, unkTextureTop});
		put("SAF", new Image[] {safTextureFront, safTextureSide, safTextureTop});
	}};
	public static final HashMap<String, Color> colourNames = new HashMap<String,Color>() {{
		put("BLACK", Color.BLACK);
		put("BLUE", Color.BLUE);
		put("CYAN", Color.CYAN);
		put("DARK GRAY", Color.DARK_GRAY);
		put("GRAY", Color.GRAY);
		put("GREEN", Color.GREEN);
		put("LIGHT GRAY", Color.LIGHT_GRAY);
		put("MAGENTA", Color.MAGENTA);
		put("ORANGE", Color.ORANGE);
		put("PINK", Color.PINK);
		put("RED", Color.RED);
		put("WHITE", Color.WHITE);
		put("YELLOW", Color.YELLOW);
		
	}};
	private Connection codeC, PIDC, locationC;
	
	public DataHandler() {
		/*
		 * Initiates the connections to all the external data holding files. Some are databases, and
		 * others (like leader boards) are just plain text files.
		 */
		//TODO Add test to see if using resource pack, then react accordingly.
		loadOptions();
		loadCarResources("generic");
		loadDatabaseConnection();
		loadLeaderBoards();
		leaderboard014.put((double)180,"New Person");
		saveLeaderBoards();
	}
	
	public static class Location {
		/*
		 * This child class does not inherit but is most still defined within data handling as this
		 * is all the class is used for. A location represents a location in the 3D cuboid that
		 * represents the vehicle being tested. This location is used to plot the location of errors
		 * on canvas to show the user.
		 */
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
		/*
		 * Each location has been given a short location by me (e.g. ENG = engine block). This
		 * method just converts between the code and the friendly name that is displayed in the
		 * GUI.
		 */
		String description;
		description = majorLocationCodeDescriptions.get(locationCode);
		return description;
	}
	
	public Location getMinorLocation (String vehicleName, String locationName) {
		/*
		 * This method returns a Location mad up of the coordinates it gets from the location database
		 * for the vehicle in question. the parameters are the vehicleName and locationName, which are
		 * both used in the database to retrieve the information. The coordinates represent a 3D location
		 * on the vehicle being monitored.
		 */
		//in percentages...
		int x=0, y=0, z=0;
		Statement st;
		try {
			st = locationC.createStatement();
			System.out.println("Searching database for location of " + locationName + " in " + vehicleName);
			ResultSet rs = st.executeQuery( "SELECT * FROM "+ vehicleName +" WHERE LocationName = '"+locationName+"'" );
			while (rs.next()) {
				x = rs.getInt(2);//Columns start at 1!!!
				y = rs.getInt(3);
				z = rs.getInt(4);
			}
			st.close();
			//c.commit();
			//c.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		System.out.print("Location of Component found: "); System.out.print(x); System.out.print(y); System.out.println(z);
		return new Location(x,y,z);
	}
	
	public String getMajorLocation (String vehicleName, String locationName) {
		/*
		 * This just gets the major location (general area like Engine block) given a vertain component
		 * from the vehicle (e.g. cylinder 1). This is just a short coded major location name, which can
		 * then be decoded for the GUI using the getMajorLocationFromCode() method.
		 */
		//in percentages...
		String majorLocation = "";
		Statement st;
		try {
			st = locationC.createStatement();
			System.out.println("Searching database for (Major) location of " + locationName + " in " + vehicleName);
			ResultSet rs = st.executeQuery( "SELECT LocationSystem FROM "+ vehicleName +" WHERE LocationName = '"+locationName+"'" );
			while (rs.next()) {
				majorLocation = rs.getString(1);
			}
			st.close();
			//c.commit();
			//c.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		System.out.print("Location of Component found: "); System.out.print(majorLocation);
		return majorLocation;
	}
	
	public static boolean getBit(byte[] byteArray, int position) {
		/* 
		 * A multipurpose method used in many places in my software. Given an array of bytes, it will return
		 * a single bit of data given the location it is needed from. This is useful, for example, where some
		 * PIDs return multiple bits of different data in one byte, where each bit represents something different.
		 * This method allows you to extract only what you want.
		 */
		try {
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
		} catch (Exception e) {
			System.out.println("Failed to get requested bit");
			return false;
		}
	}
	
	public static String getHexCharacters(byte b) {
		/*
		 * This method returns the characters for the hexadecimal that would represent the
		 * byte given as the parameter.
		 */
		int bytePart1 = ((byte)(b >>> 4) & 0x0F);
		int bytePart2 = (b & 0x0F);
		return ((Character)hexChars[bytePart1]).toString() + ((Character)hexChars[bytePart2]).toString(); 
	}
	
	public static byte getByteFromHexString(String s) throws RuntimeException {
		/*
		 * Gets a byte value from a hexadecimal string that is given as a parameter (the
		 * opposite of the method above). At the moment it only works for one byte at a
		 * time, and I have a found no need for more bytes being processed at the same
		 * time. 
		 */
		//Will be 2's Compliment now.
		if (s.length() > 2) {
			throw new RuntimeException("Can only supply this function for 1 byte at a time. Gave more that 2 hex characters: "+s);
		}
		//Turns out there were a few build-in functions I could use together.
		Byte b = new Byte((byte)Integer.parseInt(s,16));
		return (byte)b;
	}
	
	public PID[] getAvailablePids() {
		/*
		 * This takes the map created in the carInterfacing class, and extracts only the PIDs available to use
		 * with this particular vehicle, and returns them in an array.
		 */
		Set set = supportedPIDs.entrySet();
		Iterator it = set.iterator();
		int tally = 0;
		while (it.hasNext()) {
			Map.Entry me = (Map.Entry)it.next();
			if (((boolean) me.getValue())) {
				tally += 1;
			}
		}
		PID[] pids = new PID[tally];
		int index = 0;
		set = supportedPIDs.entrySet();
		it = set.iterator();
		while (it.hasNext()) {
			Map.Entry me = (Map.Entry)it.next();
			if (((boolean) me.getValue())) {
				PID newPid = onBoardDisplay.dataHandler.decodePID((byte)me.getKey());
				System.out.print("Found PID:"); System.out.println(newPid.Description);
				pids[index] = newPid;
				index++;
			}
		}
		return pids;
	}
	
	public PID[] getAllPids() {
		/*
		 * Similar to the above method, but simply returns an array of all possible PIDs that
		 * any vehicle could support. This allows the user to do things like design dash board
		 * layouts without being connected to a vehicle to tell them what PIDs can be used.
		 */
		PID[] allPIDs = new PID[101];
		for (int i = 0; i <=100; i++) {
			System.out.print("Getting "); System.out.println((byte)i);
			allPIDs[i] = decodePID((byte)i);
		}
		return allPIDs;
	}
	
	public PID[] selectSupportedPIDsDialog(int numberToChoose, boolean onlyUseAvailable) {
		/*
		 * This method creates a pop-up box on the user's screen that will ask them for a selection of PIDs.
		 * Depending on the parameters, this may be from the PIDs currently avilable on the vehicle, or
		 * perhaps all PIDs possible on any vehicle. Another parameter is numberToChoose, which specifies
		 * how many PIDs the user must select using drop-down menus.
		 */
		PID[] selected  = new PID[numberToChoose];
		JPanel dialogPanel = new JPanel();
		dialogPanel.setLayout(new BoxLayout(dialogPanel,BoxLayout.Y_AXIS));//Makes panel add items vertically...
		dialogPanel.add(new JLabel("Select " + Integer.toString(numberToChoose)+" PIDs:"));
		PID[] selectionUsed;
		if (onlyUseAvailable) {
			selectionUsed = getAvailablePids();
		} else {
			selectionUsed = getAllPids();
		}
		
		JComboBox[] comboBoxes = new JComboBox[numberToChoose];
		for (int i = 0; i < numberToChoose; i++) {
			DefaultComboBoxModel model = new DefaultComboBoxModel();
	        for (PID pid: selectionUsed) {
	            //model.addElement(pid.ID)
	        	try {
	        	model.addElement(pid.Description);
	        	} catch (Exception e) {//Sometimes no description found when weird PID. Some not included in list, mainly pid 0.
	        	}
	        }
	        JComboBox comboBox= new JComboBox(model);
	        dialogPanel.add(comboBox);
	        comboBoxes[i] = comboBox;
		}
        int confirmed = JOptionPane.showConfirmDialog(null, dialogPanel, "PID", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (confirmed == JOptionPane.OK_OPTION) {
        	for (int i = 0; i < numberToChoose; i++) {
        		try {
        			//selected[0] = onBoardDisplay.dataHandler.decodePID((byte)comboBoxes[i].getSelectedItem());
        			System.out.print("Description was: "); System.out.println((String)comboBoxes[i].getSelectedItem());
        			for (PID pid: selectionUsed) {
        	            //model.addElement(pid.ID);
        				if (pid.Description.equals((String)comboBoxes[i].getSelectedItem())) {
        					System.out.print("Matched...");System.out.println(pid.Description);
        					selected[i] = pid;
        				}
        	        }
        		} catch (Exception e) {
        			System.out.println("Failed, added fake pid.");
        			selected[i] = new PID();
        		}
        	}
        }
        return selected;
	}
	
	public void loadDashArrangements() {
		/*
		 * This method loads the dash board arrangement data into the Dash class by extracting them from a text
		 * file encoded using my own method for identifying each dash widget on the screen. Each is identified
		 * with their locations and other important information such as dial type and PID to be displayed.
		 */
		DashArrangement[] allArrangements = new DashArrangement[] {};
		try {
			InputStream inputStream = new FileInputStream("dashArrangements");
			int streamSize = inputStream.available();
			char[] chars014 = new char[streamSize];
			String currentLine = "";
			DashArrangement currentArrangement = new DashArrangement("", true, new DialSkin1[] {}, new BarWidget[] {},	new GraphWidget[] {});
			boolean isFirstArr = true;
			for (int i = 0; i < streamSize; i++) {
				char character = (char)inputStream.read();
				if (character == '\n'){
					//System.out.println(currentLine);
					if (currentLine.substring(0,1).toCharArray()[0]=='@') {
						if (!isFirstArr) {
							System.out.println("Adding new Layout");
							DashArrangement[] newArrangements = new DashArrangement[allArrangements.length+1];
							for (int ii = 0; ii<allArrangements.length; ii++) {
								newArrangements[ii] = allArrangements[ii];
							}
							newArrangements[allArrangements.length] = currentArrangement;
							allArrangements = newArrangements;
						}
						isFirstArr = false;
						System.out.print("New Layout Found"); System.out.println(currentLine.substring(1,currentLine.length()));
						currentArrangement = new DashArrangement(currentLine.substring(1,currentLine.length()), true, new DialSkin1[] {}, new BarWidget[] {},	new GraphWidget[] {});
					}
					if (currentLine.substring(0,1).toCharArray()[0]=='#') {
						System.out.println("Adding new Layout");
						DashArrangement[] newArrangements = new DashArrangement[allArrangements.length+1];
						for (int ii = 0; ii<allArrangements.length; ii++) {
							newArrangements[ii] = allArrangements[ii];
						}
						newArrangements[allArrangements.length] = currentArrangement;
						allArrangements = newArrangements;
					}
					if (currentLine.substring(0,1).toCharArray()[0]=='*') {
						String stringIsEditable = currentLine.substring(1,currentLine.length());
						boolean isEditable = Boolean.parseBoolean(stringIsEditable);
						currentArrangement.customisable = isEditable;
					}
					if (currentLine.substring(0,1).toCharArray()[0]=='D') {
						String stringGraphDetails = currentLine.substring(1,currentLine.length());
						String[] parts = stringGraphDetails.split(" ");
						PID newPid = onBoardDisplay.dataHandler.decodePID(onBoardDisplay.dataHandler.getByteFromHexString(parts[0]));//Byte.valueOf(parts[0]));
						DialSkin1 newDial = new DialSkin1(newPid,
								Integer.parseInt(parts[1]),
								Integer.parseInt(parts[2]),
								Integer.parseInt(parts[3]),
								Integer.parseInt(parts[4]),newPid.min,newPid.max,Float.parseFloat(parts[5]));
						DialSkin1[] newDials = new DialSkin1[currentArrangement.dialList.length+1];
						for (int ii = 0; ii<currentArrangement.dialList.length; ii++) {
							newDials[ii] = currentArrangement.dialList[ii];
						}
						newDials[currentArrangement.dialList.length] = newDial;
						currentArrangement.dialList = newDials;
					}
					if (currentLine.substring(0,1).toCharArray()[0]=='B') {
						String stringBarDetails = currentLine.substring(1,currentLine.length());
						String[] parts = stringBarDetails.split(" ");
						PID newPid = onBoardDisplay.dataHandler.decodePID(onBoardDisplay.dataHandler.getByteFromHexString(parts[0]));//Byte.valueOf(parts[0]));
						BarWidget newBar = new BarWidget(newPid,
								Integer.parseInt(parts[1]),
								Integer.parseInt(parts[2]),
								Integer.parseInt(parts[3]),
								Integer.parseInt(parts[4]),newPid.min,newPid.max,Boolean.parseBoolean(parts[5]));
						BarWidget[] newBars = new BarWidget[currentArrangement.barList.length+1];
						for (int ii = 0; ii<currentArrangement.barList.length; ii++) {
							newBars[ii] = currentArrangement.barList[ii];
						}
						newBars[currentArrangement.barList.length] = newBar;
						currentArrangement.barList = newBars;
					}
					if (currentLine.substring(0,1).toCharArray()[0]=='G') {
						String stringGraphDetails = currentLine.substring(1,currentLine.length());
						String[] parts = stringGraphDetails.split(" ");
						PID newPid = onBoardDisplay.dataHandler.decodePID(onBoardDisplay.dataHandler.getByteFromHexString(parts[0]));//Byte.valueOf(parts[0]));
						GraphWidget newGraph = new GraphWidget(new PID[] {newPid},
								Integer.parseInt(parts[1]),
								Integer.parseInt(parts[2]),
								Integer.parseInt(parts[3]),
								Integer.parseInt(parts[4]),Integer.parseInt(parts[5]));
						GraphWidget[] newGraphs = new GraphWidget[currentArrangement.graphList.length+1];
						for (int ii = 0; ii<currentArrangement.graphList.length; ii++) {
							newGraphs[ii] = currentArrangement.graphList[ii];
						}
						newGraphs[currentArrangement.graphList.length] = newGraph;
						currentArrangement.graphList = newGraphs;
					}
					currentLine = "";
				} else {
					if (character != '\r'){
						currentLine += character;
					}
				}
			}
			inputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println(allArrangements.length);
		Dash.DashPanel.arrangements = allArrangements;
	}
	
	public void saveDashArrangements() {
		/*
		 * Same as  the above method but backwards, saves all currently held dash board layouts into
		 * the text file. This includes custom ones the user may have created.
		 */
		System.out.println("Saving Dash Layouts...");
		try {
			File oldFile = new File("dashArrangements");
			oldFile.renameTo(new File("dashArrangements.old"));
			oldFile = new File("dashArrangements.old");
			OutputStream outputStream = new FileOutputStream("dashArrangements");
			for (DashArrangement currentArrangement : Dash.DashPanel.arrangements) {
				outputStream.write(("@"+currentArrangement.name).getBytes("UTF-8"));
				outputStream.write('\n');
				outputStream.write(("*"+((Boolean)currentArrangement.customisable).toString()).getBytes("UTF-8"));
				outputStream.write('\n');
				for (DialSkin1 dial : currentArrangement.dialList) {
					outputStream.write(("D").getBytes("UTF-8"));
					outputStream.write((getHexCharacters(dial.pid.ID)).getBytes("UTF-8"));
					outputStream.write((" ").getBytes("UTF-8"));
					outputStream.write((Integer.toString(dial.startX)).getBytes("UTF-8"));
					outputStream.write((" ").getBytes("UTF-8"));
					outputStream.write((Integer.toString(dial.startY)).getBytes("UTF-8"));
					outputStream.write((" ").getBytes("UTF-8"));
					outputStream.write((Integer.toString(dial.realWidth)).getBytes("UTF-8"));
					outputStream.write((" ").getBytes("UTF-8"));
					outputStream.write((Integer.toString(dial.realHeight)).getBytes("UTF-8"));
					outputStream.write((" ").getBytes("UTF-8"));
					outputStream.write((Float.toString(dial.pins[0].circleProportion)).getBytes("UTF-8"));
					outputStream.write('\n');
				}
				for (BarWidget bar : currentArrangement.barList) {
					outputStream.write(("B").getBytes("UTF-8"));
					outputStream.write((getHexCharacters(bar.pid.ID)).getBytes("UTF-8"));
					outputStream.write((" ").getBytes("UTF-8"));
					outputStream.write((Integer.toString(bar.startX)).getBytes("UTF-8"));
					outputStream.write((" ").getBytes("UTF-8"));
					outputStream.write((Integer.toString(bar.startY)).getBytes("UTF-8"));
					outputStream.write((" ").getBytes("UTF-8"));
					outputStream.write((Integer.toString(bar.realWidth)).getBytes("UTF-8"));
					outputStream.write((" ").getBytes("UTF-8"));
					outputStream.write((Integer.toString(bar.realHeight)).getBytes("UTF-8"));
					outputStream.write((" ").getBytes("UTF-8"));
					outputStream.write((((Boolean)bar.drawHorizontal).toString()).getBytes("UTF-8"));
					outputStream.write('\n');
				}
				for (GraphWidget graph : currentArrangement.graphList) {
					outputStream.write(("G").getBytes("UTF-8"));
					outputStream.write((getHexCharacters(graph.displayedPIDs[0].ID)).getBytes("UTF-8"));
					outputStream.write((" ").getBytes("UTF-8"));
					outputStream.write((Integer.toString(graph.startX)).getBytes("UTF-8"));
					outputStream.write((" ").getBytes("UTF-8"));
					outputStream.write((Integer.toString(graph.startY)).getBytes("UTF-8"));
					outputStream.write((" ").getBytes("UTF-8"));
					outputStream.write((Integer.toString(graph.realWidth)).getBytes("UTF-8"));
					outputStream.write((" ").getBytes("UTF-8"));
					outputStream.write((Integer.toString(graph.realHeight)).getBytes("UTF-8"));
					outputStream.write((" ").getBytes("UTF-8"));
					outputStream.write((Integer.toString(graph.maxMemoryRecords)).getBytes("UTF-8"));
					outputStream.write('\n');
				}
			}
			outputStream.write('#');
			outputStream.write('\n');
			oldFile.delete();
			outputStream.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void loadCarResources(String resourceName) {
		/*
		 * This method loads all the car image textures based on the vehicle name that is being scanned.
		 * It also uses a generic image set for vehicles that are not currently in the databases. The
		 * images are also tinted using the tinting method also in this class. This is the reason the
		 * software has to be re-started when the GUI colours are changed. Perhaps an improvement would
		 * be that the raw images are stored, and are tinted when they are drawn onto the screen. This
		 * would allow better flowing use of the software.
		 */
		//TODO Remove below declaration...
		resourceName = "generic";
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
						if (((String)me.getKey()).equals("UNK")) {
							buffered[i] = tintBufferedImage(full,onBoardDisplay.guiColours[1]);
						} else {
							buffered[i] = tintBufferedImage(full,onBoardDisplay.guiColours[2]);
						}
					}
					majorLocationCodeTextures.put((String)me.getKey(),buffered);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public BufferedImage tintBufferedImage(String location, Color tintColour) {
		/*
		 * This method takes an image location and tinting colour as its parameters. It creates an image
		 * object from the image location, and then places a tinting over the top. This meant I had to
		 * create my images very specifically with only the colour black, and varying amounts of
		 * transparency. It also means that all images on their own are mono-colour, so I had to seperate
		 * some images into layered textures.
		 */
		BufferedImage img = new BufferedImage(1,1,1);
		try {
			img = ImageIO.read(getClass().getResourceAsStream(location));
		} catch (IOException e) {
			e.printStackTrace();
		}
		for (int x = 0; x <img.getWidth(); x++) {
			for (int y = 0; y< img.getHeight(); y++) {
				Color oldColor = new Color(img.getRGB(x,y),true);
				Color newRGBColor = tintColour;
				int finalColorAsInt = (oldColor.getAlpha() << 24) | (newRGBColor.getRed() << 16) | (newRGBColor.getGreen() << 8) | newRGBColor.getBlue();
				//img.setRGB(x, y, new Color(newRGBColor.getRed(),newRGBColor.getGreen(),newRGBColor.getBlue(),oldColor.getAlpha()).getRGB());
				img.setRGB(x, y, finalColorAsInt);
			}
		}
		return img;
	}
	
	public void loadDatabaseConnection () {
		/*
		 * This method creates the connection to the databases the software uses. This includes those for
		 * error codes, PIDs, and locations of components for different vehicles.
		 */
		//http://wiki.ci.uchicago.edu/VDS/VDSDevelopment/UsingSQLite
		try {
			codeC = DriverManager.getConnection("jdbc:sqlite:errorCodes.db");
			codeC.setAutoCommit(false);
			PIDC = DriverManager.getConnection("jdbc:sqlite:PIDs.db");
			PIDC.setAutoCommit(false);
			locationC = DriverManager.getConnection("jdbc:sqlite:MinorLocations.db");
			locationC.setAutoCommit(false);
			//Statement st = c.createStatement();
			//int rc = st.executeUpdate( "INSERT INTO x(b) VALUES('qwer')" );
			//System.out.println( "insert returns " + rc );
		    //rs.close();
		    //st.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void loadLeaderBoards() {
		/*
		 * This method loads the leader boards for the 0-60mph and 1/4 mile times from the text file
		 * they are stored in. They are already sorted because they are held in a data structure that
		 * only moves though its contents in sorted order when they are read back.
		 */
		try {
			System.out.println("Reading leaderboards");
			InputStream stream014 = new FileInputStream("leaderBoard014");
			InputStream stream060 = new FileInputStream("leaderBoard060");
			int size014 = stream014.available();
			int size060 = stream060.available();
			char[] chars014 = new char[size014];
			String nameString014 = "";
			String scoreString014 = "";
			boolean getName = false;
			System.out.println("O-1/4 Mile Leaderboard:");
			for (int i = 0; i < size014; i++) {
				char character = (char)stream014.read();
				if (character == '\n'){
					if (!getName) {
						getName = true;
					} else {
						//Adds to leaderboard a new record with score chars014 and name nameString014
						System.out.print("\t"+nameString014);
						System.out.print(" : ");
						System.out.println(Double.parseDouble(scoreString014));
						leaderboard014.put(Double.parseDouble(scoreString014), nameString014);
						nameString014 = "";
						scoreString014 = "";
						getName = false;
					}
					
				} else {
					if (character != '\r'){
						if (getName) {
							nameString014 += character;
						} else {
							scoreString014 += character;
						}
					}
				}
			}
			
			char[] chars060 = new char[size060];
			String scoreString060 = "";
			String nameString060 = "";
			getName = false;
			System.out.println("O-60 Mile Leaderboard:");
			for (int i = 0; i < size060; i++) {
				char character = (char)stream060.read();
				if (character == '\n'){
					if (!getName) {
						getName = true;
					} else {
						//Adds to leaderboard a new record with score chars014 and name nameString014
						System.out.print("\t"+nameString060);
						System.out.print(" : ");
						System.out.println(Double.parseDouble(scoreString060));
						leaderboard060.put(Double.parseDouble(scoreString060), nameString060);
						nameString060 = "";
						scoreString060 = "";
						getName = false;
					}
					
				} else {
					if (character != '\r'){//A \r character appears at end of user's name, returning cursor to start...
						if (getName) {
							nameString060 += character;
						} else {
							scoreString060 += character;
						}
					}
				}
			}
			stream014.close();
			stream060.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void saveLeaderBoards() {
		/*
		 * same as above method but for saving.
		 */
		System.out.println("Saving Leaderboards...");
		if (leaderboard014.size() == 0 | leaderboard060.size() == 0) {
			System.err.println("Tried to save empty leaderboard - aborted.");
			return;//prevents accidentally deleting leaderboard if save called before loaded...
		}
		try {
			File oldFile = new File("leaderBoard014");
			oldFile.renameTo(new File("leaderBoard014.old"));
			oldFile = new File("leaderBoard014.old");
			OutputStream stream014 = new FileOutputStream("leaderBoard014");
			Set set = leaderboard014.entrySet();
			Iterator it = set.iterator();
			while (it.hasNext()) {
				Map.Entry me = (Map.Entry)it.next();
				stream014.write(me.getKey().toString().getBytes("UTF-8"));
				stream014.write('\n');
				stream014.write(((String)me.getValue()).getBytes("UTF-8"));
				stream014.write('\n');
			}
			oldFile.delete();
			
			oldFile = new File("leaderBoard060");
			oldFile.renameTo(new File("leaderBoard060.old"));
			oldFile = new File("leaderBoard060.old");
			OutputStream stream060 = new FileOutputStream("leaderBoard060");
			set = leaderboard060.entrySet();
			it = set.iterator();
			while (it.hasNext()) {
				Map.Entry me = (Map.Entry)it.next();
				stream060.write(me.getKey().toString().getBytes("UTF-8"));
				stream060.write('\n');
				stream060.write(((String)me.getValue()).getBytes("UTF-8"));
				stream060.write('\n');
			}
			oldFile.delete();
			stream014.close();
			stream060.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public double getBottomLeaderBoard(TreeMap<Double,String> tm) {
		/*
		 * This method takes an argument that represents one of the leader boards, and returns
		 * the last value in the list (the one with the longest time, seeing as quicker is
		 * better). This is used for comparisons in other methods, like the one that calculates
		 * whether a new record it good enough to be stored in the top 10 values.
		 */
		double lastResult = 0;
		Set set = tm.entrySet();
		Iterator it = set.iterator();
		while (it.hasNext()) {
			Map.Entry me = (Map.Entry)it.next();
			lastResult = (double)me.getKey();
		}
		return lastResult;
	}
	
	public String getGUIColourName(Color color) {
		/*
		 * This method returns a friendly name for a colour object used in the software. This allows
		 * them to seem nicer when presented to the user in the dialog to select different GUI colours
		 * in the settings screen.
		 */
		Set set = onBoardDisplay.dataHandler.colourNames.entrySet();
		Iterator it = set.iterator();
		while (it.hasNext()) {
			Map.Entry me = (Map.Entry)it.next();
			if ((Color) me.getValue()==color) {
                return (String)me.getKey();
			}
		}
		return null;
	}
	
	public void saveOptions() {
		/*
		 * Saves some of the variables it is useful to keep stored without needing to be entered again. These
		 * values are unrelated, as this is just a mis-matched data storage file. Everything is stored as a
		 * string, so must be converted in an exact order so they are decoded in the right order.
		 */
		File oldFile = new File("onBoardDisplayConfig");
		oldFile.renameTo(new File("onBoardDisplayConfig.old"));
		oldFile = new File("onBoardDisplayConfig.old");
		OutputStream outputStream;
		try {
			outputStream = new FileOutputStream("onBoardDisplayConfig");
			//GUI Colours
			for (int i= 0; i < onBoardDisplay.guiColours.length; i++) {
				outputStream.write((getGUIColourName(onBoardDisplay.guiColours[i])).getBytes("UTF-8"));
				outputStream.write('\n');
			}
			outputStream.write(onBoardDisplay.vehicleName.getBytes("UTF-8"));
			outputStream.write('\n');
			outputStream.write(onBoardDisplay.manufacturerName.getBytes("UTF-8"));
			outputStream.write('\n');
			outputStream.write(onBoardDisplay.profileName.getBytes("UTF-8"));
			outputStream.write('\n');
			//TODO add other options to save...
			
			oldFile.delete();
			outputStream.close();
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void loadOptions() {
		/*
		 * same as above but loads the data. This data is mostly held in the onBoardDisplay main class,
		 * is it is overall important running information like profile and vehicle name (along with
		 * current GUI colours,  so they are kept saved when you re-open the software).
		 */
		System.out.println("Reading Config Options");
		try {
			InputStream inputStream = new FileInputStream("onBoardDisplayConfig");
			int streamSize = inputStream.available();
			char[] chars014 = new char[streamSize];
			String currentLine = "";
			int lineNum = 0;
			for (int i = 0; i < streamSize; i++) {
				char character = (char)inputStream.read();
				if (character == '\n'){
					switch (lineNum) {
					case 0:
						onBoardDisplay.guiColours[0] = colourNames.get(currentLine);
						break;
					case 1:
						onBoardDisplay.guiColours[1] = colourNames.get(currentLine);
						break;
					case 2:
						onBoardDisplay.guiColours[2] = colourNames.get(currentLine);
						break;
					case 3:
						onBoardDisplay.guiColours[3] = colourNames.get(currentLine);
						break;
					case 4:
						onBoardDisplay.vehicleName  = currentLine;
						break;
					case 5:
						onBoardDisplay.manufacturerName  = currentLine;
						break;
					case 6:
						onBoardDisplay.profileName  = currentLine;
						break;
					default:
						break;
					}
					lineNum++;
					currentLine = "";
				} else {
					if (character != '\r'){
						currentLine += character;
					}
				}
			}
			inputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public Code decodeErrorCode (String vehicleName, short id) {
		/*
		 * This method is used to create an instance of the Code class for error codes. It is given the
		 * ID of the code, and it is then looked up in the error code database. Because the error codes
		 * are different for each manufacturer, this data is gathered from the onBoardDisplay class. The
		 * data retrieved from the database is then entered as attributes to the error code object
		 * created, which is then returned.
		 */
		Code newCode = new Code();
		int tableId = Code.getDatabaseID(id);
		String tableName;
		Statement st;
		try {
			st = codeC.createStatement();
			if ((short)2601 < id && id < (short)8192) {
				tableName = onBoardDisplay.manufacturerName;
			} else {
				tableName = "Generic";
			}
			ResultSet rs = st.executeQuery( "SELECT * FROM " + tableName +" WHERE Id = "+Integer.toString(tableId) );
			boolean found = false;
			while (rs.next()) {
				newCode.ID = id;
				newCode.Description = rs.getString(2);//Columns start at 1!!!
				newCode.IDString = rs.getString(3);
				newCode.minorLocation = rs.getString(4);
				newCode.majorLocation = getMajorLocation(vehicleName,newCode.minorLocation);
				found = true;
			}
			if (!found) {
				newCode.IDString = Code.getStringFromID(id);
				newCode.Description = "Unknown- code not found in database...";
				newCode.majorLocation = "UNK";
			}
			st.close();
			//c.commit();
			//c.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return newCode;
	}
	
	public PID decodePID (byte id) {
		/* 
		 * The same as the above method, but for PIDs. Decodes the data from the PID database so that it
		 * can be used in the PID class. These do not vary with manufacturer, so the process is slightly
		 * simpler.
		 */
		PID newPID = new PID();
		int tableId = PID.getDatabaseID(id);
		Statement st;
		try {
			st = PIDC.createStatement();
			ResultSet rs = st.executeQuery( "SELECT * FROM Mode01 WHERE Id = "+Integer.toString(tableId) );
			while (rs.next()) {//Should only ever loop through once...
				newPID.ID = id;
				newPID.maxNumOfBytes = rs.getInt(2)+2;//Number stored in database does not include the confirmation of mode & PID sent by the ELM327
				newPID.min = rs.getInt(3);
				newPID.max = rs.getInt(4);
				newPID.unit = rs.getString(5);
				newPID.IDString = rs.getString(6);
				newPID.Description = rs.getString(7);//Columns start at 1!!
				//TODO Missed out status colours
				newPID.isBitEncoded = rs.getInt(9)==1;
				String[] rawParameters = new String[4];
				if (!newPID.isBitEncoded) {
					rawParameters[0] = rs.getString(10);
					rawParameters[1] = rs.getString(11);
					rawParameters[2] = rs.getString(12);
					rawParameters[3] = rs.getString(13);
					newPID.conversionParameters = convertMessyNumbers(rawParameters);
				}
				newPID.minorLocation = "";//TODO add these to database and read from it
				newPID.majorLocation = "";
			}
			st.close();
			//c.commit();
			//c.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return newPID;
	}
	
	public float[] convertMessyNumbers(String[] rawStrings) {
		/*
		 * This method is used closely with the PID database when reading values from the sensors. Each PID
		 * has its own formula for calculating the real value from the bytes the adaptor returns (which is
		 * usually 2 bytes that must be combined in some way). To be able to store this information in a
		 * database as text, I added fields for an addition, multiplication, then another addition, and
		 * another multiplication. I found that I could convert almost every formula into this format so I
		 * would be able so store it. Some formulas included the letter 'B', which was my code for the value
		 * of the second byte needing to be included in the calculation in this position. This was a good
		 * way of getting around the struggle of storing working formulas, as an alternative would have been
		 * a seperate method for every sensor available to read.
		 */
		float[] finalParameters = new float[5];
		for (int i = 0; i < rawStrings.length; i++) {
			if (rawStrings[i].equals("B")) {
				finalParameters[i] = 0;
				finalParameters[4] = 1; 
			} else {
				String workString = rawStrings[i];
				boolean negative = false;
				float numerator = 0;
				float denominator = 0;
				if (workString.substring(0, 1)== "-") {
					negative = true;
					workString = workString.substring(1,workString.length());
				}
				int divisorIndex = workString.indexOf("/");
				if (-1 == divisorIndex) {
					//Division symbol not found
					numerator = Float.parseFloat(workString);
					denominator = 1;
				} else {
					numerator = Float.parseFloat(workString.substring(0,divisorIndex));
					denominator = Float.parseFloat(workString.substring(divisorIndex + 1, workString.length()));
				}
				finalParameters[i] = numerator/denominator;
				if (negative) {
					finalParameters[i] *= -1;
				}
			}
		}
		return finalParameters;
	}
	
	public float decodePIDRead(byte[] rawBytes, PID pid) {
		/* converts the raw data from the ECU into recognisable information.
		 * The PIds used with this method should be direct conversions, not bit-encoded,
		 * which is why there is a check. The bit-encoded ones should be used with raw data
		 * where they are needed. I am assuming in this method that there are only two bytes,
		 * A and B, which should be true.
		 * The method also uses ints instead of bytes, and PIDs' raw data is not 2's compliment,
		 * so would be messed up if i tried to push values into byte types before float.
		 */
		System.out.println("Collected:");
		for (byte b : rawBytes) {
			System.out.print(b);
		}
		float workValue = rawBytes[0] * pid.conversionParameters[0];
		if (pid.conversionParameters[4] == 1) {
			//B is used
			workValue += rawBytes[1];
		} else {
			workValue += pid.conversionParameters[1];
		}
		workValue *= pid.conversionParameters[2];
		workValue += pid.conversionParameters[3];
		return workValue;
	}
}
