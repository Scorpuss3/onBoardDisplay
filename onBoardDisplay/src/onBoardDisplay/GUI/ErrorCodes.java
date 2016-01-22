package onBoardDisplay.GUI;

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
            int trueXPos = e.getX();
            int trueYPos = e.getY();
            int xPos = (int)((trueXPos - onBoardDisplay.xOffset)/onBoardDisplay.graphicsMultiplier);
            int yPos = (int)((trueYPos - onBoardDisplay.yOffset)/onBoardDisplay.graphicsMultiplier);
            System.out.print("Mouse press at "); System.out.print(xPos);
            System.out.print(","); System.out.println(yPos);
            for (Option option : buttons) {
                System.out.print(option.currentCaption); System.out.print(option.xPosition); System.out.println(option.yPosition);
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
			System.out.println("Now running error code scan");
			short[] errorCodes = onBoardDisplay.carInterface.getErrorCodes();
			System.out.print("Num of error codes read: "); System.out.println(errorCodes.length);
			int x = 65;
			int y = 110;
			int spacing = 50;
			numOfCodes = 0;
			for (short errorCode : errorCodes) {
				numOfCodes++;
				Code decoded = onBoardDisplay.dataHandler.decodeErrorCode(errorCode);
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
			//TODO Add scan running stuff here to collect data and prepare for painting.
		}
		
		public void startRun() {
			running = true;
		}
		
		public void stopRun() {
			running = false;
		}
		
		@Override
        public void paint(Graphics g) {
            super.paint(g);
            System.out.println("Painting....");
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setColor(Color.BLACK);
            g2d.fillRect(0,0,onBoardDisplay.trueWidth,onBoardDisplay.trueHeight);
            g2d.fillRect(onBoardDisplay.ModifyAspectX(0),onBoardDisplay.ModifyAspectY(0),
                    onBoardDisplay.ModifyAspect(onBoardDisplay.graphicsWidth) ,
                    onBoardDisplay.ModifyAspect(onBoardDisplay.graphicsHeight));
            g2d.setColor(Color.RED);
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
                    g2d.setColor(Color.PINK);
                    buttonTexture = onBoardDisplay.menuPanel.buttonPressed;
                } else {
                    g2d.setColor(Color.RED);
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
            g2d.setFont(new Font("Gill Sans", Font.BOLD ,
                    onBoardDisplay.ModifyAspect(20)));
            if (numOfCodes > 0) {
	            for (Option option : shownCodes){
	                if (option.selected) {
	                    g2d.setColor(Color.PINK);
	                    buttonTexture = onBoardDisplay.menuPanel.buttonPressed;
	                } else {
	                    g2d.setColor(Color.RED);
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
