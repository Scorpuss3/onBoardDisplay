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
	public static class HUDPanel extends JPanel {
		public CardLayout layout;
		public Cylinders.CylinderPanel cylinderPanel;
		public RawReadSpecific.RawReadSpecificPanel rawReadSpecificPanel;
		public Dash.DashPanel dashPanel;
		public Graph.GraphPanel graphPanel;
		public JPanel hudTopLayerPanel;
		
		public HUDPanel(int width,int height) {
            this.setSize(width,height);
            
            cylinderPanel = new Cylinders.CylinderPanel(width,height);
            rawReadSpecificPanel =  new RawReadSpecific.RawReadSpecificPanel(width,height);
            dashPanel = new Dash.DashPanel(width,height);
            graphPanel = new Graph.GraphPanel(width,height);
            
            hudTopLayerPanel = new JPanel(new CardLayout());
            hudTopLayerPanel.add(cylinderPanel,"cylinderPanel");
            hudTopLayerPanel.add(rawReadSpecificPanel, "rawReadSpecificPanel");
            hudTopLayerPanel.add(dashPanel,"dashPanel");
            hudTopLayerPanel.add(graphPanel,"graphPanel");
            layout = (CardLayout) (hudTopLayerPanel.getLayout());
            hudTopLayerPanel.setVisible(true);
            add(hudTopLayerPanel);
            
            layout.show(hudTopLayerPanel, "dashPanel"); //Is only the default, should be reset before this panel is shown.
            setSize(onBoardDisplay.trueWidth,onBoardDisplay.trueHeight);
            setVisible(true);
            System.out.println("All HUD setup done.");
        }
	}

	public static class HUDLayoutPanel extends JPanel{
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
		
		public void startRun() {
			running = true;
		}
		
		public void stopRun() {
			running = false;
		}
		
		public HUDLayoutPanel(int width,int height) {
            this.setSize(width,height);
            setUpKeyboardListener();
            setVisible(true);
            System.out.println("HUD Layout setup done, waiting for run command.");
        }
	}
}
