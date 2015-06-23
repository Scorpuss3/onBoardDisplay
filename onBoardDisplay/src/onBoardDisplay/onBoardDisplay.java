package onBoardDisplay;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;

import onBoardDisplay.GUI.*;

import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.CardLayout;

public class onBoardDisplay {
	private static JFrame topLayerFrame;
	private static JPanel topLayerPanel;
	public static CardLayout layout;
	private static Menu.MenuPanel menuPanel;
	private static ErrorCodes.ErrorCodePanel errorCodePanel;
	private static HUD.HUDPanel hudPanel;
	
	
	public static int graphicsWidth = 1280;//Aspect 16:9
	public static int graphicsHeight = 720;
	public static int trueWidth;
	public static int trueHeight;
	public static float graphicsMultiplier;
	public static int xOffset;
	public static int yOffset;
	
	private static void calculateAspect() {
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
    
    public static int ModifyAspectY(int input) {
        int aspected =(int) (input * graphicsMultiplier);
        int translated = aspected + yOffset;
        return translated;
    }
    
    public static int ModifyAspect(int input) {
        // Use for things that do not refer to exact location, like heights
        // and widths (these do not need offset).
        int aspected =(int) (input * graphicsMultiplier);
        return aspected;
    }

	public static void main(String[] args) {
		topLayerFrame = new JFrame();
		calculateAspect();
		
		menuPanel = new Menu.MenuPanel(trueWidth,trueHeight);
		errorCodePanel = new ErrorCodes.ErrorCodePanel(trueWidth,trueHeight);
		hudPanel = new HUD.HUDPanel(trueWidth,trueHeight);
		
		topLayerPanel = new JPanel(new CardLayout());
		topLayerPanel.add(menuPanel, "menuPanel");
		topLayerPanel.add(errorCodePanel,"errorCodePanel");
		topLayerPanel.add(hudPanel,"hudPanel");
		layout = (CardLayout) (topLayerPanel.getLayout());
	}
}
