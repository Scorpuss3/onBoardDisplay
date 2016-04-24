package onBoardDisplay.carInterfacing;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Map;

import onBoardDisplay.dataHandling.DataHandler;
import onBoardDisplay.onBoardDisplay;;

public class CarInterfacing {
	public static class CarInterface {
		public boolean initialised = false;
		private byte obdMode = 0;
		private int networkMode = 0; //1 = connected, 0 = no connection...
		private Socket dataSocket;
		private PrintWriter ECUin;
		private BufferedReader ECUout;
		private int port = 35000;
		private String hostName = "192.168.0.10";
		public CarInterface() {
			System.out.print("Creating Socket Connection...");
			try {
				dataSocket = new Socket(hostName, port);
				ECUin = new PrintWriter(dataSocket.getOutputStream(),true);
				ECUout = new BufferedReader(new InputStreamReader(dataSocket.getInputStream()));
				networkMode = 1;
			} catch (UnknownHostException e) {
				System.err.println("ERROR: SOCKET CREATION FAILURE");
				try {
					dataSocket.close();
					ECUin.close();
					ECUout.close();
				} catch (IOException e1) {
				} catch (NullPointerException e2) {}
				networkMode = 0;
			} catch (IOException e) {
				System.err.println("ERROR: SOCKET CREATION FAILURE");
				try {
					dataSocket.close();
					ECUin.close();
					ECUout.close();
				} catch (IOException e1) {
				} catch (NullPointerException e2) {}
				networkMode = 0;
			}
			System.out.println("Socket Created");
			getSupportedPIDs();
			initialised = true;
		}
		
		private void setMode(Byte mode) {
			//OBD protocol mode. 01 is read data.
			obdMode = mode;
		}
		
		public void getSupportedPIDs() {
			byte[] check1 = readPID((byte)0x00,(byte)1,true, 4);
			System.out.println("got here");
			//Available 0x01(1) to 0x20(32)
			System.out.println(check1.length);
			for (int i = 1; i <=32; i++) {//dec 32 is hex 20, but starts at 1
				//System.out.print("Asking for position "); System.out.println(i-1);
				onBoardDisplay.dataHandler.supportedPIDs.put((byte)i,DataHandler.getBit(check1, i-1));
			}
			byte[] check2 = readPID((byte)0x20,(byte)1,true, 4);
			//Available 0x21(33) to 0x40(64)
			for (int i = 33; i <=64; i++) {
				onBoardDisplay.dataHandler.supportedPIDs.put((byte)i,DataHandler.getBit(check2, i-33));
			}
			byte[] check3 = readPID((byte)0x40,(byte)1,true, 6);
			//Available 0x41(65) to 0x60(96)
			//for (int i = 65; i <=96; i++) {
			//	onBoardDisplay.dataHandler.supportedPIDs.put((byte)i,DataHandler.getBit(check3, i-65));
			//}
			byte[] check4 = readPID((byte)0x60,(byte)1,true, 6);
			//Available 0x61(97) to 0x80(128)
			//for (int i = 97; i <=128; i++) {
			//	onBoardDisplay.dataHandler.supportedPIDs.put((byte)i,DataHandler.getBit(check4, i-97));
			//}
			byte[] check5 = readPID((byte)0x80,(byte)1,true, 6);
			//Available 0x81(129) to 0xA0(160)
			//for (int i = 129; i <=160; i++) {
			//	onBoardDisplay.dataHandler.supportedPIDs.put((byte)i,DataHandler.getBit(check5, i-129));
			//}
			byte[] check6 = readPID((byte)0xA0,(byte)1,true, 6);
			//Available 0xA1(161) to 0xC0(192)
			//for (int i = 161; i <=192; i++) {
			//	onBoardDisplay.dataHandler.supportedPIDs.put((byte)i,DataHandler.getBit(check6, i-161));
			//}
			byte[] check7 = readPID((byte)0xC0,(byte)1,true, 6);
			//Available 0xC1(193) to 0xE0(224)
			//for (int i = 193; i <=224; i++) {
			//	onBoardDisplay.dataHandler.supportedPIDs.put((byte)i,DataHandler.getBit(check7, i-193));
			//}
			System.out.println("Checking availability of PIDs...");
			for (Map.Entry<Byte, Boolean> entry : onBoardDisplay.dataHandler.supportedPIDs.entrySet())
			{
				Byte b = entry.getKey();
				Boolean bool = entry.getValue();
				System.out.print(DataHandler.getHexCharacters(b));
				System.out.print(" : ");
				System.out.println(bool);
			}
		}
		
		public String getVIN() {
			setMode((byte)0x09);
			byte[] vinBytes = submitToECU("09 02",20);
			for (int i = 0; i <= vinBytes.length; i++) {
				if (vinBytes[i] == (byte)0) {
					vinBytes[i] = (byte)0x23;//Cannot use UTF 0, so I chose U+0023, which is a '#' character
				}
			}
			String VIN = "";
			try {
				VIN = new String(vinBytes, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			return VIN;
		}
		
		public int getNumOfErrors() {
			byte[] statusBytes = readPID((byte)01,(byte)1,true,4);
			return statusBytes[0] & 0x7F;//First bit is not relevant
		}
		
		public short[] getErrorCodes() {
			//A single error code can be defined in 2 bytes, which is the size of short.
			int numOfErrors = getNumOfErrors();
			System.out.print("Number of error codes from pid:"); System.out.println(numOfErrors);
			byte[] rawBytes = submitToECU("03",numOfErrors*2+1);
			short[] dtcShorts = new short[rawBytes.length/2];
			int shortCount = 0;
			boolean toggle = false;
			byte bufferByte = 0x00;
			for (byte currentByte : rawBytes) {
				if (!toggle) {
					bufferByte = currentByte;
				} else {
					dtcShorts[shortCount] = (short) ((bufferByte << 8) | (currentByte & 0xFF));
					shortCount++;
				}
				toggle = !toggle;
			}
			System.out.print("Read some DTCs: ");
			System.out.println(dtcShorts[0]);
			System.out.println(dtcShorts[1]);
			return dtcShorts;//TODO turn this back on...
			//short[] fakeShorts ={(short) 2,(short) 24, (short) 4904, (short) 112};
			//return fakeShorts;
		}
		
		public byte[] submitToECU(String submitString, int expectedBytes) {
			int numOfCmdBytes = submitString.split(" ").length; 
			if (networkMode == 0) {
				byte[] ba = {(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0};// Probably covers everything, false because no connection
				return ba;
			}
			System.out.println("ECU Submit String: " + submitString);
			try {
				ECUin.println(submitString);
			} catch (NullPointerException e) {
				System.err.println("Failed to send input to ECU...");
				byte[] ba = {(byte)0};
				return ba;
			}
			String response = "";
			byte[] bytes = {};
			byte[] finalBytes = {};
			try {
				//System.out.println("RESPONSE START");
				//while ((response = ECUout.readLine()) != null) {
				while ((response = ECUout.readLine()) != ">") {
					//while (!(response = ECUout.readLine()).equals(">")) {
					System.out.println("Raw:"+response);
					//System.out.print("Length:"); System.out.println(response.length());
					if (response.length() != 0) {
						if (response.substring(response.length()-1) == "\n") {
							response = response.substring(0,response.length()-1);//Removes last character (\n I think)
						}
					
						System.out.println("Removed newline.");
						//System.out.println(response == "NO DATA");
						if (!(response.contains("STOPPED") ||response.contains("BUS INIT") || response.equals("NO DATA") || response.substring(0,1).equals(">")|| response == null|| response.equals(""))) {
							System.out.print("Accepted response as genuine: "); System.out.println(response);
							String[] asStringArray = response.split(" ");
							finalBytes = new byte[asStringArray.length-numOfCmdBytes];
							for (int i = numOfCmdBytes; i < asStringArray.length;i++) {//Starts at 2 to eliminate the first 2 bytes, which are just repeats of the command...
								//System.out.println(asStringArray[i]);
								//System.out.print("Converting string ");
								//System.out.print(asStringArray[i]);
								//System.out.print(" to byte ");
								finalBytes[i-numOfCmdBytes] = DataHandler.getByteFromHexString(asStringArray[i]);//i-2 because it was started at 2 for the string array.
								//System.out.println(finalBytes[i-2]);
							}
							System.out.println("(stripped bytes)");
							//return finalBytes; (was returning before setting up next command!
						} else if (response.contains("STOPPED")) {
							System.out.println("Rushed ELM");
						} else if (response.contains("STOPPED")) {
							System.out.println("wait...bus initialising...");
						} else if (response.equals("NO DATA")) {
							System.out.println("no data - providing correct number of empty bytes");
						} else if (response == null || response == "") {
							System.out.println("null - providing correct number of empty bytes");
							byte[] ba = new byte[expectedBytes];// Probably covers everything
							for (int ii = 0; ii <= expectedBytes; ii++) {
								ba[ii] = (byte)0;
							}
							return ba;
						} else {
							System.out.println("IGNORED POINTLESS OUTPUT");
						}
					} else {
						//System.out.println("else 1");
						return finalBytes;
					}
				}
				//System.out.println("RESPONSE END");
			} catch (IOException e) {
				e.printStackTrace();
			} catch (RuntimeException e) {
				System.err.println("Failed get ECU output or could not split into bytes properly.");
				e.printStackTrace();
				return new byte[] {0x0};
			}
			
			//FAKE TEMP STUFF BELOW...
			//TODO remove this when done
			byte[] bAr = {0x12,0x1F,0x11,0x01};
			//return bAr;
			System.out.print("About to return:");
			for (byte bayte : bytes) {
				System.out.print(bayte);
			}
			System.out.println("\nend of method.");
			return bytes;
		}
		
		public byte[] readPID(byte PID, byte mode, boolean autoRetry, int expectedBytes) {
			/* Returns the raw bytes of the PID asked for, and makes sure that some data is recieved if required.
			 * Also checks against expected number of bytes.*/
//			try {
//				if (!onBoardDisplay.dataHandler.supportedPIDs.get(PID)) {
//					//byte[] blank = new byte[expectedBytes];
//					//for (int i = 0; i < expectedBytes; i++) {
//					//	blank[i] = 0;
//					//}
//					System.out.print(PID); System.out.println("PID was not available in this vehicle...");
//				}
//			} catch (Exception e){
//				
//			}
			setMode(mode);
			//String submitString = DataHandler.getHexCharacters(obdMode) + " " + DataHandler.getHexCharacters(PID);
			String submitString = DataHandler.getHexCharacters(obdMode) + " " + DataHandler.getHexCharacters(PID) + "1";//http://stackoverflow.com/questions/21334147/send-multiple-obd-commands-together-and-get-response-simultaneously?lq=1
			System.out.print("Reading PID: "); System.out.println(submitString);
			//Adding a 1 to the end may increase speed according to
			//http://stackoverflow.com/questions/21334147/send-multiple-obd-commands-together-and-get-response-simultaneously?lq=1
			byte[] response = submitToECU(submitString, expectedBytes);
			//System.out.println("here first");
			int tryCount = 1;
			int maxTries = 3;//10;
			//if (response.length < 2) {
			System.out.print("ReadPID supplied with:");
			for (byte bte : response) {
				System.out.print(bte);
				System.out.print(" ");
			}
			System.out.print("length:"); System.out.print(response.length); System.out.print("Expected:"); System.out.println(expectedBytes);
			if (response.length != expectedBytes-2) {
				if (autoRetry) {
					//while (response.length < 2) {
					//while (response.length < expectedBytes) {
					while ((response = submitToECU(submitString, expectedBytes)).length < expectedBytes) {
						System.out.println("in loop");
						if (tryCount == maxTries) {
							System.err.println("Ran out of retries. Could not get a meaningful output from ELM for "); System.out.println(submitString);
							byte[] ba = new byte[expectedBytes-2];// Probably covers everything. -4 because this removes mode and PID bytes
							for (int ii = 0; ii < expectedBytes-2; ii++) {
								ba[ii] = (byte)0;
							}
							return ba;
						}
						tryCount ++;
//						try {
//							System.in.read();
//						} catch (IOException e) {
//							e.printStackTrace();
//						}
						//System.out.println("Retrying read...");
						//response = submitToECU(submitString, expectedBytes);
						//System.out.print("RESPONSE:");
						//for (byte byt : response) {
							//System.out.print(byt);
						//}
						//System.out.println();
						//System.out.println(response.length);
					}
				} else {
					//TODO change this to do something more useful than just be 0. The -2 is so that you remove mode and PID
					byte[] ba = new byte[expectedBytes-2];// Probably covers everything
					for (int ii = 0; ii < expectedBytes-2; ii++) {
						ba[ii] = (byte)0;
					}
					return ba;
				}
			}
			//byte[] processed = new byte[response.length-2];
			//int ii = 0;
			//System.out.print("Byte Accepted. ");
			//for (byte byt : response) {
				//System.out.print(byt);
				//if (ii < processed.length) {
				//	processed[ii] = byt;
				//	System.out.print(" ");
				//	System.out.print(byt);
				//}
				//processed[ii] = byt;
				//ii++;
			//}
			//Had been double removing the first two bytes!!!
			System.out.println("Last Rep");
			for (byte bte : response) {
				System.out.print(bte);
				System.out.print(" ");
			}
			return response;//processed;
		}
		
		public byte[] readPID(byte PID) {
			int expectedBytes = onBoardDisplay.dataHandler.decodePID(PID).maxNumOfBytes;
			return readPID(PID,(byte)0x01,false, expectedBytes);
		}
		
		public byte[] readPID(byte PID, int mode) {
			int expectedBytes = onBoardDisplay.dataHandler.decodePID(PID).maxNumOfBytes;
			return readPID((byte)PID, (byte)mode, false, expectedBytes);
		}
		
		public byte[] readPID(byte PID, boolean autoRetry) {
			int expectedBytes = onBoardDisplay.dataHandler.decodePID(PID).maxNumOfBytes;
			return readPID((byte)PID, (byte)1, autoRetry, expectedBytes);
		}
		
		public void clearErrorCodes() {
			setMode((byte)04);
			//TODO Error code Clearing Stuff.
		}
		
		public void cleanUp() {
			try {
				ECUout.close();
				ECUin.close();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (NullPointerException e) {}
		}
	}
}
