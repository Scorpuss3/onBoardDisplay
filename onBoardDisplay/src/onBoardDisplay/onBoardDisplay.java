package onBoardDisplay;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;

import onBoardDisplay.GUI.*;
import onBoardDisplay.GUI.HUDLayouts.*;
import onBoardDisplay.carInterfacing.*;
import onBoardDisplay.dataHandling.*;

import javax.swing.JFrame;
import javax.swing.JPanel;

import java.awt.CardLayout;
import java.awt.Color;

public class onBoardDisplay {
	private static JFrame topLayerFrame;
	public static JPanel topLayerPanel;
	public static CardLayout layout;
	public static Menu.MenuPanel menuPanel;
	public static ErrorCodes.ErrorCodePanel errorCodePanel;
	public static HUD.HUDPanel hudPanel;
	public static Detail.DetailPanel detailPanel;
	
	public static Dash.DashPanel dashPanel;
	public static TrackTest.TrackTestPanel trackTestPanel;
	public static LeaderBoard.LeaderBoardPanel leaderBoardPanel;
	public static Graph.GraphPanel graphPanel;
	
	public static CarInterfacing.CarInterface carInterface;
	public static DataHandler dataHandler;
	
	public static String vehicleName = "VOLKSWAGENPOLO6N214";//"Generic"
	public static String manufacturerName = "Volkswagen";
	public static String profileName = "John Doe";
	public static Color[] guiColours  = {Color.white, Color.black, Color.blue, Color.cyan};
	//public static Color[] guiColours  = {Color.black, Color.green, Color.red, Color.pink};
	//g2d.setColor(onBoardDisplay.guiColours[0]);
	
	public static int graphicsWidth = 1280;//Aspect 16:9
	public static int graphicsHeight = 720;
	public static int trueWidth;
	public static int trueHeight;
	public static float graphicsMultiplier;
	public static int xOffset;
	public static int yOffset;
	
	public static void calculateAspect() {
		GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
		trueWidth = gd.getDisplayMode().getWidth();
		trueHeight = gd.getDisplayMode().getHeight();
		
		if ((float)trueWidth / graphicsWidth <= (float)trueHeight / graphicsHeight) {
            // Aspect dictated by the width difference.
            graphicsMultiplier = (float)trueWidth / graphicsWidth;
        } else {
            // Aspect dictated by the height difference.
            graphicsMultiplier = (float)trueHeight / graphicsHeight;
        }
        xOffset =(int) (trueWidth - (graphicsWidth*graphicsMultiplier))/2;
        System.out.print("Aspect Offset X: "); System.out.println(xOffset);
        yOffset =(int) (trueHeight - (graphicsHeight*graphicsMultiplier))/2;
        System.out.print("Aspect Offset Y: "); System.out.println(yOffset);
	}
	
	public static int ModifyAspectX(int input) {
        int aspected =(int) (input * graphicsMultiplier);
        int translated = aspected + xOffset;
        return translated;
    }
    
    public static int ModifyAspectX(float input) {
    	return ModifyAspectX((int)input);
    }
    
    public static int ModifyAspectY(int input) {
        int aspected =(int) (input * graphicsMultiplier);
        int translated = aspected + yOffset;
        return translated;
    }
    
    public static int ModifyAspectY(float input) {
    	return ModifyAspectX((int)input);
    }
    
    public static int ModifyAspect(int input) {
        // Use for things that do not refer to exact location, like heights
        // and widths (these do not need offset).
        int aspected =(int) (input * graphicsMultiplier);
        return aspected;
    }
    
    public static int ModifyAspect(float input) {
    	return ModifyAspectX((int)input);
    }
    
    public static void shutdown(int code) {
    	carInterface.cleanUp();
    	System.exit(code);
    }
    
    public static void shutdown() {
    	shutdown(0);
    }

	public static void main(String[] args) {
		topLayerFrame = new JFrame();
		calculateAspect();
		
		System.out.println(Code.getDatabaseID((short)4639));
		System.out.println(Code.getStringFromID((short)4639));
		
		dataHandler = new DataHandler();
		carInterface = new CarInterfacing.CarInterface();
		
		menuPanel = new Menu.MenuPanel(trueWidth,trueHeight);
		errorCodePanel = new ErrorCodes.ErrorCodePanel(trueWidth,trueHeight);
		//hudPanel = new HUD.HUDPanel(trueWidth,trueHeight);
		detailPanel = new Detail.DetailPanel(trueWidth, trueHeight);
		
		dashPanel = new Dash.DashPanel(trueWidth,trueHeight);
		trackTestPanel = new TrackTest.TrackTestPanel(trueWidth,trueHeight);
		leaderBoardPanel = new LeaderBoard.LeaderBoardPanel(trueWidth,trueHeight);
		graphPanel = new Graph.GraphPanel(trueWidth,trueHeight);
		
		topLayerPanel = new JPanel(new CardLayout());
		topLayerPanel.add(menuPanel, "menuPanel");
		topLayerPanel.add(errorCodePanel,"errorCodePanel");
		//topLayerPanel.add(hudPanel,"hudPanel");
		topLayerPanel.add(detailPanel,"detailPanel");
		topLayerPanel.add(dashPanel,"dashPanel");
		topLayerPanel.add(trackTestPanel,"trackTestPanel");
		topLayerPanel.add(leaderBoardPanel,"leaderBoardPanel");
		topLayerPanel.add(graphPanel,"graphPanel");
		
		layout = (CardLayout) (topLayerPanel.getLayout());
		topLayerPanel.setVisible(true);
		topLayerFrame.add(topLayerPanel);
		
		layout.show(topLayerPanel, "menuPanel");
		topLayerFrame.setSize(trueWidth,trueHeight);
		topLayerFrame.setUndecorated(true);
		topLayerFrame.setVisible(true);
		
		menuPanel.startSensing();
		System.out.println("Testing PID Decode...");
		PID pid = dataHandler.decodePID((byte)0x10);
		byte[] fakeBytes = {(byte)0x0A,(byte)0x14};
		float f = dataHandler.decodePIDRead(fakeBytes, pid);
		System.out.println(f);
		
	}
}
