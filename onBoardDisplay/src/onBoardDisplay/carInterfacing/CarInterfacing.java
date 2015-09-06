package onBoardDisplay.carInterfacing;

public class CarInterfacing {
	public static class CarInterface {
		private int obdMode = 0;
		public CarInterface() {
			//TODO Initialisation of car interface.
		}
		
		private void setMode(int mode) {
			obdMode = mode;
		}
		
		public int[] getSupportedPIDs() {
			setMode(05);
			//TODO Add method to retrieve supported PIDs.
		}
		
		public String getVIN() {
			setMode(09);
			//TODO VIN retrieval
		}
		
		public short[] getErrorCodes() {
			setMode(03);
			//A single error code can be defined in 2 bytes, which is the size of short.
			//TODO Error Code reading stuff.
			return new short[] {
					(short) 34, //Temporary data.
					(short) 34,
					(short) 34
			};
		}
		
		public byte[] readSensor(int PID) {
			setMode(01);
			//TODO Sensor Reading stuff.
		}
		
		public void clearErrorCodes() {
			setMode(04);
			//TODO Error code Clearing Stuff.
		}
	}	
}
