package onBoardDisplay.GUI;

/*
 * This panel reads error codes for the user, and when they are decoded, it will present them
 * to the user as a list. The user can then click on all the error codes, and they will be
 * taken to the detail panel, where the information for the error code they clicked on will be
 * displayed.
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

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import onBoardDisplay.onBoardDisplay;
import onBoardDisplay.GUI.Menu.MenuPanel.Option;
import onBoardDisplay.dataHandling.Code;
import onBoardDisplay.dataHandling.DataHandler;

public class ErrorCodes {
	public static class ErrorCodePanel extends JPanel implements MouseListener {
		private boolean running = false;
		private ArrayList<Option> shownCodes = new ArrayList<>();
		private int numOfCodes = 0;
		private Menu.MenuPanel.Option[] buttons = new Menu.MenuPanel.Option[] {
			new Menu.MenuPanel.Option("Run Scan",55,onBoardDisplay.graphicsHeight-60-55,400,60,null) {
				@Override
				public void action() {
					runScan();
				}
			},
			new Menu.MenuPanel.Option("Exit",55+400+5,onBoardDisplay.graphicsHeight-60-55,400,60,null) {
				@Override
				public void action() {
					keyAction("ESCAPE");
				}
			}
		};
		
		@Override
        public void mousePressed(MouseEvent e) {
			//Explained in Detail Panel
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
            }for (Option option : shownCodes) {
                System.out.print(option.currentCaption); System.out.print(option.xPosition); System.out.println(option.yPosition);
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
			//Explained in Detail Panel
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
                	for (Option option : shownCodes) {
                		if (option.selected) {
                			option.action();
                		}
                	}
                }
                repaint();
            }
        }
		
		public void runScan() {
			/*
			 * This method is activated by a button on the GUI. It will ask the car interface instance for
			 * the error codes from the ECU, and then decode them using methods from the data hander instance.
			 * The data is then used to present a list of error codes to the user. The list is shown in the GUI
			 * as buttons, and if the user clicks on them, they are taken to the detail panel that will then
			 * display specific information for the error code they clicked on.
			 */
			System.out.println("Now running error code scan");
			short[] errorCodes = onBoardDisplay.carInterface.getErrorCodes();
			System.out.print("Num of error codes read: "); System.out.println(errorCodes.length);
			if (errorCodes.length > 0) {
				shownCodes = new ArrayList<>();
			}
			int x = 65;
			int y = 110;
			int spacing = 50;
			numOfCodes = 0;
			for (short errorCode : errorCodes) {
				numOfCodes++;
				Code decoded = onBoardDisplay.dataHandler.decodeErrorCode(onBoardDisplay.vehicleName,errorCode);
				System.out.println("Found Error Code: " + Short.toString(decoded.ID) + " : " + decoded.Description);
				String shortDescription;
				try {
					shortDescription = decoded.Description.substring(0,30);
				} catch (java.lang.NullPointerException e) {
					shortDescription = null;
				}
				Option newOption = new Option(decoded.IDString + " : " + shortDescription,
						x, y,500,40,decoded) {
					@Override
					public void action() {
						System.out.println("Activating Button Action                                               *");
						running = false;
						onBoardDisplay.layout.show(onBoardDisplay.topLayerPanel, "detailPanel");
						onBoardDisplay.detailPanel.setItem(decoded);
						onBoardDisplay.detailPanel.startRun();
					}
				};
				shownCodes.add(newOption);
				y = y + spacing;
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
			//Explained in Detail Panel
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
            g2d.drawString("Read Error Codes", onBoardDisplay.ModifyAspectX(55),
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
            if (numOfCodes > 0) {
	            for (Option option : shownCodes){
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
	                        onBoardDisplay.ModifyAspectY(option.yPosition+option.height-18));
	                //The adding is needed above because strings are drawn with
	                //the y co-ordinate as the bottom, not top.
	            }
            }
		}
		
		public ErrorCodePanel(int width,int height) {
            this.setSize(width,height);
            setUpKeyboardListener();
            addMouseListener(this);
            setVisible(true);
            System.out.println("Error Code Panel setup done, waiting for run command.");
        }
	}

}
