package onBoardDisplay.dataHandling;

public class DataHandler {
	public static class Location {
		public static int xPos = 0;
		public static int yPos = 0;
		public static int zPos = 0;
		public static int width = 0;
		public static int height = 0;
		public static int depth = 0;
		
		public Location (int xPos, int yPos, int zPos, int width, int height, int depth) {
			this.xPos = xPos;
			this.yPos = yPos;
			this.zPos = xPos;
			this.height = height;
			this.width = width;
			this.depth = depth;
		}
		
		public Location (int xPos, int yPos, int zPos) {
			this.xPos = xPos;
			this.yPos = yPos;
			this.zPos = xPos;
		}
	}
	public static Location getMajorLocation (String locationName) {
		//TODO Add location database for Error Codes and PIDs
		return new Location(10,10,10,40,40,40);
	}
	
	public static Location getMinorLocation (String locationName) {
		//TODO Add location database for Error Codes and PIDs
		return new Location(10,10,10);
	}
	
	public static Code decodeErrorCode (short code) {
		//TODO Add error code decoding.
		return new Code((short)1,"A Made up Code","Engine Bay", "Cylinder 1");
	}
}
