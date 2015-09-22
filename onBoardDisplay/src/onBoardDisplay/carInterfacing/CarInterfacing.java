package onBoardDisplay.carInterfacing;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Map;

import onBoardDisplay.dataHandling.DataHandler;
import onBoardDisplay.onBoardDisplay;;

public class CarInterfacing {
	public static class CarInterface {
		private int obdMode = 0;
		private Socket dataSocket;
		private PrintWriter ECUin;
		private BufferedReader ECUout;
		private int port = 35000;
		private String hostName = "192.168.0.10";
		public CarInterface() {
			//TODO Initialisation of car interface.
			System.out.print("Creating Socket Connection...");
			try {
				dataSocket = new Socket(hostName, port);
				ECUin = new PrintWriter(dataSocket.getOutputStream(),true);
				ECUout = new BufferedReader(new InputStreamReader(dataSocket.getInputStream()));
			} catch (UnknownHostException e) {
				System.err.println("ERROR: SOCKET CREATION FAILURE");
				//TODO uncomment this before release
				//System.exit(1);
			} catch (IOException e) {
				System.err.println("ERROR: SOCKET CREATION FAILURE");
				//TODO uncomment this before release
				//System.exit(1);
			}
			System.out.println("Socket Created");
			getSupportedPIDs();
		}
		
		private void setMode(int mode) {
			//OBD protocol mode. 01 is read data.
			obdMode = mode;
		}
		
		public void getSupportedPIDs() {
			setMode(05);
			byte[] check1 = readPID(0x00);
			//Available 0x01(1) to 0x20(32)
			for (int i = 1; i <=32; i++) {//dec 32 is hex 20, but starts at 1
				onBoardDisplay.dataHandler.supportedPIDs.put((byte)i,DataHandler.getBit(check1, i-1));
			}
			byte[] check2 = readPID(0x20);
			//Available 0x21(33) to 0x40(64)
			for (int i = 33; i <=64; i++) {
				onBoardDisplay.dataHandler.supportedPIDs.put((byte)i,DataHandler.getBit(check2, i-33));
			}
			byte[] check3 = readPID(0x40);
			//Available 0x41(65) to 0x60(96)
			for (int i = 65; i <=96; i++) {
				onBoardDisplay.dataHandler.supportedPIDs.put((byte)i,DataHandler.getBit(check3, i-65));
			}
			byte[] check4 = readPID(0x60);
			//Available 0x61(97) to 0x80(128)
			for (int i = 97; i <=128; i++) {
				onBoardDisplay.dataHandler.supportedPIDs.put((byte)i,DataHandler.getBit(check4, i-97));
			}
			byte[] check5 = readPID(0x80);
			//Available 0x81(129) to 0xA0(160)
			for (int i = 129; i <=160; i++) {
				onBoardDisplay.dataHandler.supportedPIDs.put((byte)i,DataHandler.getBit(check5, i-129));
			}
			byte[] check6 = readPID(0xA0);
			//Available 0xA1(161) to 0xC0(192)
			for (int i = 161; i <=192; i++) {
				onBoardDisplay.dataHandler.supportedPIDs.put((byte)i,DataHandler.getBit(check6, i-161));
			}
			byte[] check7 = readPID(0xC0);
			//Available 0xC1(193) to 0xE0(224)
			for (int i = 193; i <=224; i++) {
				onBoardDisplay.dataHandler.supportedPIDs.put((byte)i,DataHandler.getBit(check7, i-193));
			}
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
			setMode(9);
			byte[] vinBytes = submitToECU((byte)0x02);
			String VIN = "UNAVAILABLE";
			try {
				VIN = new String(vinBytes, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			return VIN;
		}
		
		public short[] getErrorCodes() {
			setMode(03);
			//A single error code can be defined in 2 bytes, which is the size of short.
			//TODO Error Code reading stuff.
			byte[] rawBytes = submitToECU(null);
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
			System.out.print(dtcShorts[0]);
			System.out.println(dtcShorts[1]);
			return dtcShorts;
		}
		
		public byte[] submitToECU(Byte id) {
			//TODO Sensor Reading stuff.
			if (id != null) {
				String submitString = Integer.toString(obdMode) + " " + DataHandler.getHexCharacters(id);
			} else {
				String submitString = Integer.toString(obdMode);
			}
			//System.out.println("ECU Submit String: " + submitString);
			//ECUin.println(submitString);
			//String response;
			//try {
			//	while ((response = ECUout.readLine()) != null) {
			//		//TODO Handle ECU output...
			//	}
			//} catch (IOException e) {
			//	e.printStackTrace();
			//}
			
			//FAKE TEMP STUFF BELOW...
			//TODO remove this when done
			byte[] bAr = {0x12,0x1F,0x11,0x01};
			return bAr;
		}
		
		public byte[] readPID(byte PID) {
			setMode(01);
			return submitToECU(PID);
		}
		
		public byte[] readPID(int PIDInt) {
			byte PID = (byte) PIDInt;
			return readPID(PID);
		}
		
		public void clearErrorCodes() {
			setMode(04);
			//TODO Error code Clearing Stuff.
		}
	}	
}
