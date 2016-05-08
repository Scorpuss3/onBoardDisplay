package onBoardDisplay.GUI.HUDLayouts;

/*
 * This class is for the leader board display panel. In my software there are two leader
 * boards, one for 0-60mph, and one for 1/4 mile. These are both saved in files and loaded
 * by the data handler. This panel takes the data from the data handler, and draws the list
 * of each person and their times (in order, quickest first). There is an option with a
 * button to switch between viewing the 0-60 and 1/4 mile times.
 */

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import onBoardDisplay.onBoardDisplay;
import onBoardDisplay.GUI.Menu;
import onBoardDisplay.GUI.HUDLayouts.Dash.DashPanel.UpdateLoop;
import onBoardDisplay.GUI.Menu.MenuPanel.Option;
import onBoardDisplay.GUI.components.dials.DialSkin1;
import onBoardDisplay.dataHandling.Code;
import onBoardDisplay.dataHandling.DataHandler;
import onBoardDisplay.dataHandling.PID;

public class LeaderBoard {
	public static class LeaderBoardPanel extends JPanel implements MouseListener {
		private boolean running = false;
		private String currentMode = "0-60";
		private static TreeMap<Double,String> currentLeaderBoard = onBoardDisplay.dataHandler.leaderboard060;
		private Menu.MenuPanel.Option[] buttons = new Menu.MenuPanel.Option[] {
			new Menu.MenuPanel.Option("Mode: "+currentMode,55,onBoardDisplay.graphicsHeight-60-55,350,60,null) {
				@Override
				public void action() {
					/*
					 * This method is for the mode switching button that changes between 0-60 and 1/4 mile
					 * leader boards being shown.
					 */
					if (currentMode == "0-60") {
						currentMode = "1/4 Mile";
						this.currentCaption  = "Mode: "+currentMode;
						currentLeaderBoard = onBoardDisplay.dataHandler.leaderboard014;
					} else {
						currentMode = "0-60";
						this.currentCaption  = "Mode: "+currentMode;
						currentLeaderBoard = onBoardDisplay.dataHandler.leaderboard060;
					}
				}
			},
			new Menu.MenuPanel.Option("Exit",55+400+5,onBoardDisplay.graphicsHeight-60-55,350,60,null) {
				@Override
				public void action() {
					keyAction("ESCAPE");
				}
			}
		};
		
		@Override
        public void mousePressed(MouseEvent e) {
			//Already explained in Detail Panel
            int trueXPos = e.getX();
            int trueYPos = e.getY();
            int xPos = (int)((trueXPos - onBoardDisplay.xOffset)/onBoardDisplay.graphicsMultiplier);
            int yPos = (int)((trueYPos - onBoardDisplay.yOffset)/onBoardDisplay.graphicsMultiplier);
            for (Option option : buttons) {
                if (xPos >= option.xPosition &&
                        (xPos <= (option.xPosition + option.width) &&
                        yPos >= option.yPosition &&
                        yPos <= (option.yPosition + option.height))) {
                    option.select();
                } else {
                    option.deselect();
                }
            }
            repaint();
        }
		
		@Override
        public void mouseReleased(MouseEvent e) {
            keyAction("CONFIRM");
        }
        
        public void mouseEntered(MouseEvent e) {
        }
        public void mouseExited(MouseEvent e) {
        }
        public void mouseClicked(MouseEvent e) {
        }
		
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
			//Already explained in Detail Panel
            if (running) {
                if (actionString.equals("ESCAPE")){
                    running = false;
                    onBoardDisplay.layout.show(onBoardDisplay.topLayerPanel, "menuPanel");
                    onBoardDisplay.menuPanel.startSensing();
                } else if (actionString.equals("CONFIRM")) {
                	for (Option option  : buttons) {
                        if (option.selected) {
                            option.action();
                        }
                    }
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
		
		@Override
        public void paint(Graphics g) {
			//Already explained in Detail Panel
            super.paint(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setColor(onBoardDisplay.guiColours[0]);
            g2d.fillRect(0,0,onBoardDisplay.trueWidth,onBoardDisplay.trueHeight);
            g2d.fillRect(onBoardDisplay.ModifyAspectX(0),onBoardDisplay.ModifyAspectY(0),
                    onBoardDisplay.ModifyAspect(onBoardDisplay.graphicsWidth) ,
                    onBoardDisplay.ModifyAspect(onBoardDisplay.graphicsHeight));
            g2d.setColor(onBoardDisplay.guiColours[2]);
            g2d.drawRect(onBoardDisplay.ModifyAspectX(50),
                    onBoardDisplay.ModifyAspectY(50),
                    onBoardDisplay.ModifyAspect(onBoardDisplay.graphicsWidth-100),
                    onBoardDisplay.ModifyAspect(onBoardDisplay.graphicsHeight-100));
            g2d.setFont(new Font("Gill Sans", Font.BOLD , onBoardDisplay.ModifyAspect(60)));
            g2d.drawString("Record " + currentMode + " Time", onBoardDisplay.ModifyAspectX(55),
                    onBoardDisplay.ModifyAspectY(100));
            
            g2d.setFont(new Font("Gill Sans", Font.BOLD ,
                    onBoardDisplay.ModifyAspect(30)));
            Image buttonTexture;
            for (Option option : buttons){
                if (option.selected) {
                    g2d.setColor(onBoardDisplay.guiColours[3]);
                    buttonTexture = onBoardDisplay.menuPanel.buttonPressed;
                } else {
                    g2d.setColor(onBoardDisplay.guiColours[2]);
                    buttonTexture = onBoardDisplay.menuPanel.button;
                }
                g2d.drawImage(buttonTexture,
                        onBoardDisplay.ModifyAspectX(option.xPosition),
                        onBoardDisplay.ModifyAspectY(option.yPosition),
                        onBoardDisplay.ModifyAspect(option.width),
                        onBoardDisplay.ModifyAspect(option.height),
                        this);
                g2d.drawString(option.currentCaption,
                        onBoardDisplay.ModifyAspectX(option.xPosition + 70),
                        onBoardDisplay.ModifyAspectY(option.yPosition+option.height-20));
                //The adding is needed above because strings are drawn with
                //the y co-ordinate as the bottom, not top.
            }
            g2d.setColor(onBoardDisplay.guiColours[2]);
            g2d.setFont(new Font("Gill Sans", Font.BOLD ,
                    onBoardDisplay.ModifyAspect(20)));
            Set set = currentLeaderBoard.entrySet();
    		Iterator it = set.iterator();
    		int yVal = 150;
    		boolean first = true;
    		while (it.hasNext()) {
    			if (first) {
    	            g2d.setFont(new Font("Gill Sans", Font.BOLD , onBoardDisplay.ModifyAspect(30)));
    	            yVal += 30;
    				first = false;
    			} else {
    				g2d.setFont(new Font("Gill Sans", Font.BOLD , onBoardDisplay.ModifyAspect(20)));
    			}
    			Map.Entry me = (Map.Entry)it.next();
    			g2d.drawString(me.getKey()+"    "+me.getValue(), 200, yVal);
    			yVal += 50;
    		}
		}
		
		public LeaderBoardPanel(int width,int height) {
            this.setSize(width,height);
            setUpKeyboardListener();
            addMouseListener(this);
            setVisible(true);
            System.out.println("Leaderboard Panel setup done, waiting for run command.");
        }
	}

}
