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
import java.util.ArrayList;

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

public class TrackTest {
	public static class TrackTestPanel extends JPanel implements MouseListener {
		private boolean running = false;
		private String currentMode = "0-60";
		private boolean recording = false;
		private boolean scanning = false;
		private boolean motionWaiting = false;
		private float currentSpeed = 0;
		private float currentDistance = 0;
		private float startDistance = 0;
		private String statusString = "Waiting for speed to get to 0...";
		private UpdateLoop trackTestUpdateLoop = new UpdateLoop();
		private long startTime = 0;
		private ArrayList<Double> times = new ArrayList<>();
		private Menu.MenuPanel.Option[] buttons = new Menu.MenuPanel.Option[] {
			new Menu.MenuPanel.Option("Start Recording",55,onBoardDisplay.graphicsHeight-60-55,350,60,null) {
				@Override
				public void action() {
					if (scanning) {
						this.currentCaption = "Start Recording";
						scanning = false;
						recording = false;
						motionWaiting = false;
						statusString = "Waiting for speed to get to 0...";
						System.out.println("Stopped scanning");
					} else {
						this.currentCaption = "Stop Recording";
						scanning = true;
						System.out.println("Started scanning");
					}
				}
			},
			new Menu.MenuPanel.Option("Mode: "+currentMode,55+400+5,onBoardDisplay.graphicsHeight-60-55,350,60,null) {
				@Override
				public void action() {
					if (currentMode == "0-60") {
						currentMode = "1/4 Mile";
						this.currentCaption  = "Mode: "+currentMode; 
					} else {
						currentMode = "0-60";
						this.currentCaption  = "Mode: "+currentMode;
					}
				}
			},
			new Menu.MenuPanel.Option("Exit",55+800+10,onBoardDisplay.graphicsHeight-60-55,350,60,null) {
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
		
		class UpdateLoop implements Runnable {
	        private Thread trackTestLoop;
	        @Override
	        public void run() {
	            while (true)  {//TODO change this so that it can be deactivated at program close later.
	            	if (scanning) {
	            		PID pid = onBoardDisplay.dataHandler.decodePID((byte)0x0D);
	            		currentSpeed = onBoardDisplay.dataHandler.decodePIDRead(onBoardDisplay.carInterface.readPID(pid.ID,false),pid);
    					PID pid2 = onBoardDisplay.dataHandler.decodePID((byte)0x21);
	            		currentDistance = onBoardDisplay.dataHandler.decodePIDRead(onBoardDisplay.carInterface.readPID(pid2.ID,false),pid2);
	            		repaint();
	    				if (currentMode == "0-60") {
	    					if (recording) {//waiting for reaching 60
	    						if (currentSpeed >= 10) {
	    							long endTime = System.currentTimeMillis();
	    							double elapsed = (endTime - startTime)/(double)1000;//changed to double to stop rounding!!
	    							System.out.print("\t\t\t\t\t\tSet a new " + currentMode + " time: ");
	    							System.out.println(elapsed);
	    							statusString = "Set a new " + currentMode + " time: " + String.valueOf(elapsed);
	    							times.add(elapsed);
	    							recording = false;
	    						}
	    					} else {
	    						if (motionWaiting && currentSpeed > 0) {
	    							startTime = System.currentTimeMillis();
	    							System.out.println("Started moving, waiting for 60...");
	    							statusString = "Started moving, waiting for 60...";
	    							motionWaiting = false;
	    							recording = true;
	    						} else if (currentSpeed == 0 && !motionWaiting) {
	    							motionWaiting = true;
	    							System.out.println("Reached 0mph, waiting for vehicle motion before clock started...");
	    							statusString = "Reached 0mph, waiting for vehicle motion before clock started...";
	    							motionWaiting = true;
	    						}
	    					}
	    				} else {
	    					//TODO add 1/4 mile stuff.
	    					if (recording) {//waiting for reaching 60
	    						float tripDistance = currentDistance - startDistance;
	    						if (tripDistance >= 0.4) {
	    							long endTime = System.currentTimeMillis();
	    							double elapsed = (endTime - startTime)/(double)1000;//changed to double to stop rounding!!
	    							System.out.print("\t\t\t\t\t\tSet a new " + currentMode + " time: ");
	    							System.out.println(elapsed);
	    							statusString = "Set a new " + currentMode + " time: " + String.valueOf(elapsed);
	    							times.add(elapsed);
	    							if (elapsed < onBoardDisplay.dataHandler.getBottomLeaderBoard(onBoardDisplay.dataHandler.leaderboard014)) {
	    								onBoardDisplay.dataHandler.leaderboard014.put(elapsed,onBoardDisplay.profileName);
	    								onBoardDisplay.dataHandler.leaderboard014.remove(onBoardDisplay.dataHandler.getBottomLeaderBoard(onBoardDisplay.dataHandler.leaderboard014));
	    								onBoardDisplay.dataHandler.saveLeaderBoards();
	    								System.out.println("Added item to leaderboard...");
	    							}
	    							recording = false;
	    						}
	    					} else {
	    						if (motionWaiting && currentSpeed > 0) {
	    							startTime = System.currentTimeMillis();
	    							System.out.println("Started moving, waiting for 60...");
	    							statusString = "Started moving, waiting for 60...";
	    							motionWaiting = false;
	    							recording = true;
	    							startDistance = onBoardDisplay.dataHandler.decodePIDRead(onBoardDisplay.carInterface.readPID(pid2.ID,false),pid2);
	    						} else if (currentSpeed == 0 && !motionWaiting) {
	    							motionWaiting = true;
	    							System.out.println("Reached 0mph, waiting for vehicle motion before clock started...");
	    							statusString = "Reached 0mph, waiting for vehicle motion before clock started...";
	    							motionWaiting = true;
	    						}
	    					}
	    				}
	            		onBoardDisplay.trackTestPanel.repaint();
						try {
							Thread.sleep(10);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					} else {
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
	            	//System.out.println(i++);//Stops loop from garbage collecting itself and stopping the run while waiting...
	            	//System.out.println(running);
	            	
	            }
	        }
	        
	        public void start() {
	            trackTestLoop = new Thread(this,"trackTestUpdateLoopThread");
	            trackTestLoop.start();
	        }
	        
	        public void stop() {
	        	try {
					trackTestLoop.join();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	        }
	    }
		
		public void startRun() {
			running = true;
			trackTestUpdateLoop.start();
		}
		
		public void stopRun() {
			running = false;
			trackTestUpdateLoop.stop();
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
            g2d.drawString("Record " + currentMode + " Time", onBoardDisplay.ModifyAspectX(55),
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
            g2d.drawString(statusString, onBoardDisplay.ModifyAspectX(100), onBoardDisplay.ModifyAspectY(150));
            g2d.drawString(String.valueOf(currentSpeed/(double)1.6), onBoardDisplay.ModifyAspectX(800), onBoardDisplay.ModifyAspectY(150));
            g2d.drawString(String.valueOf((currentDistance-startDistance)/(double)1.6), onBoardDisplay.ModifyAspectX(900), onBoardDisplay.ModifyAspectY(150));
            int xVal = 150;
            for (double time : times) {
            	xVal += 20;
            	g2d.drawString(String.valueOf(time), onBoardDisplay.ModifyAspectX(100), onBoardDisplay.ModifyAspectY(xVal));
            }
		}
		
		public TrackTestPanel(int width,int height) {
            this.setSize(width,height);
            setUpKeyboardListener();
            addMouseListener(this);
            UpdateLoop trackTestUpdateLoop = new UpdateLoop();
            setVisible(true);
            System.out.println("Track Test Panel setup done, waiting for run command.");
        }
	}

}
