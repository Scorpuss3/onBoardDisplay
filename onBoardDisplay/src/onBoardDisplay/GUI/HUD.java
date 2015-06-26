package onBoardDisplay.GUI;

import java.awt.CardLayout;
import java.awt.event.ActionEvent;
import java.awt.event.MouseListener;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import onBoardDisplay.onBoardDisplay;
import onBoardDisplay.GUI.HUDLayouts.*;

public class HUD {
	public class HUDPanel extends JPanel {
		public static CardLayout layout;
		public static Cylinders.CylinderPanel cylinderPanel;
		public static RawReadSpecific.RawReadSpecificPanel rawReadSpecificPanel;
		public static Dash.DashPanel dashPanel;
		public static Graph.GraphPanel graphPanel;
		
		public HUDPanel(int width,int height) {
            this.setSize(width,height);
            
            cylinderPanel = new Cylinders.CylinderPanel(width,height);
            rawReadSpecificPanel =  new RawReadSpecific.RawReadSpecificPanel(width,hieght);
            
            //TODO Add rest of cardlayout stuff to mimic the way it is used in the onBoardDisplay class.
            
            setVisible(true);
            System.out.println("All HUD setup done.");
        }
	}

	public class HUDLayoutPanel extends JPanel{
		private boolean running = false;
		
		public void setUpKeyboardListener() {
            ActionMap actionMap = this.getActionMap();
            InputMap inputMap = this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);

            inputMap.put(KeyStroke.getKeyStroke("pressed ESCAPE"), "ESCAPE");
            actionMap.put("ESCAPE", new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent arg0) {
                    keyAction("ESCAPE");
                }
            });
        }
		
		private void keyAction (String actionString) {
            if (running) {
                if (actionString.equals("ESCAPE")){
                    running = false;
                    onBoardDisplay.layout.show(onBoardDisplay.topLayerPanel, "menuPanel");
                    onBoardDisplay.menuPanel.startSensing();
                }
                repaint();
            }
        }
		
		public HUDLayoutPanel(int width,int height) {
            this.setSize(width,height);
            setUpKeyboardListener();
            setVisible(true);
            System.out.println("HUD Layout setup done, waiting for run command.");
        }
	}
}
