package onBoardDisplay.GUI.HUDLayouts;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import onBoardDisplay.onBoardDisplay;
import onBoardDisplay.GUI.HUD;
import onBoardDisplay.GUI.Menu;
import onBoardDisplay.GUI.Detail.DetailPanel.DetailMode;
import onBoardDisplay.GUI.Detail.DetailPanel.ImageType;
import onBoardDisplay.GUI.Menu.MenuPanel.Option;
import onBoardDisplay.GUI.components.Dial;
import onBoardDisplay.GUI.components.dials.DialSkin1;
import onBoardDisplay.dataHandling.Code;
import onBoardDisplay.dataHandling.DataHandler;
import onBoardDisplay.dataHandling.PID;
import onBoardDisplay.dataHandling.DataHandler.Location;

public class Dash {
	public static class DashPanel extends JPanel implements MouseListener {
		private static boolean running = false;
		private UpdateLoop dashUpdateLoop = new UpdateLoop();
		private static PID[] pidList = {onBoardDisplay.dataHandler.decodePID((byte)0x0C),
				onBoardDisplay.dataHandler.decodePID((byte)0x05),
				onBoardDisplay.dataHandler.decodePID((byte)0x0B),
				onBoardDisplay.dataHandler.decodePID((byte)0x0D),
				};
		private static DialSkin1[] dialList = {
				new DialSkin1(pidList[0],100,150,200,200,pidList[0].min,pidList[0].max,(float)0.8),
				//new DialSkin1(pidList[1],100,400,200,200,pidList[1].min,pidList[1].max,(float)0.8),
				new DialSkin1(pidList[2],500,150,200,200,pidList[2].min,pidList[2].max,(float)0.8),
				new DialSkin1(pidList[3],500,400,200,200,pidList[3].min,pidList[3].max,(float)0.8)
		};
		
		private Menu.MenuPanel.Option[] buttons = new Menu.MenuPanel.Option[] {
			new Menu.MenuPanel.Option("Exit",55,onBoardDisplay.graphicsHeight-60-55,400,60,null) {
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
            //System.out.print("Mouse press at "); System.out.print(xPos);
            //System.out.print(","); System.out.println(yPos);
            for (Option option : buttons) {
                //System.out.print(option.currentCaption); System.out.print(option.xPosition); System.out.println(option.yPosition);
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
                    //TODO link back to previous screen by starting its run and showing it.
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
		
		class UpdateLoop implements Runnable {
	        private Thread dashLoop;
	        @Override
	        public void run() {
	        	int i = 0;
	            while (true)  {//TODO change this so that it can be deactivated at program close later.
	            	if (running) {
	            		System.out.println("now refreshing");
						for (DialSkin1 selectedDial : dialList) {
							byte[] rawBytes = onBoardDisplay.carInterface.readPID(selectedDial.pid.ID, false);//TODO consider turning autoretry on...
							System.out.print("Decoded Value:"); System.out.println(onBoardDisplay.dataHandler.decodePIDRead(rawBytes, selectedDial.pid));
							selectedDial.update(onBoardDisplay.dataHandler.decodePIDRead(rawBytes, selectedDial.pid),selectedDial.pid.unit);
						}
						//onBoardDisplay.hudPanel.dashPanel.repaint();
						onBoardDisplay.dashPanel.repaint();
						//try {
						//	Thread.sleep(100);
						//} catch (InterruptedException e) {
						//	// TODO Auto-generated catch block
						//	e.printStackTrace();
						//}
					}
	            	//System.out.println(i++);//Stops loop from garbage collecting itself and stopping the run while waiting...
	            	//System.out.println(running);
	            	
	            }
	        }
	        
	        public void start() {
	            dashLoop = new Thread(this,"dashUpdateLoopThread");
	            dashLoop.start();
	        }
	        
	        public void stop() {
	        	try {
					dashLoop.join();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	        }
	    }
		
		public void startRun() {
			running = true;
			dashUpdateLoop.start();
		}
		
		public void stopRun() {
			running = false;
			dashUpdateLoop.stop();
		}
		
		@Override
        public void paint(Graphics g) {
            super.paint(g);
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
            g2d.drawString("Virtual Dashboard", onBoardDisplay.ModifyAspectX(55),
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
            
            for (DialSkin1 selectedDial : dialList) {
            	selectedDial.draw(g2d, this);
			}
		}
		
		public DashPanel(int width,int height) {
            this.setSize(width,height);
            setUpKeyboardListener();
            addMouseListener(this);
            UpdateLoop dashUpdateLoop = new UpdateLoop();
            //dashUpdateLoop.start();
            setVisible(true);
            System.out.println("Dash Panel setup done, waiting for run command.");
        }
	}
}
