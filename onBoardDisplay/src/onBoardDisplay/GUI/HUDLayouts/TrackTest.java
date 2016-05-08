package onBoardDisplay.GUI.HUDLayouts;

/*
 * This class describes the panel for track day recording functions- recording both 0-60 and 1/4 mile
 * times. When the user tells the software to start recording, it will wait for the vehicle to halt,
 * and then start timing when it starts moving again. Then, depending on the current mode, it will
 * stop the timer either when the vehicle reaches 60mph, or reaches a distance of 1/4 mile.
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
		private float oldSpeed = 0;
		private float currentDistance = 0;
		private float startDistance = 0;
		private long oldTime = 0;
		private long newTime = 0;
		private String statusString = "Waiting for speed to get to 0...";
		private UpdateLoop trackTestUpdateLoop = new UpdateLoop();
		private long startTime = 0;
		private ArrayList<Double> times = new ArrayList<>();
		private Menu.MenuPanel.Option[] buttons = new Menu.MenuPanel.Option[] {
			new Menu.MenuPanel.Option("Start Recording",55,onBoardDisplay.graphicsHeight-60-55,350,60,null) {
				@Override
				public void action() {
					/*
					 * This method is for the recording button. When clicked, it will start 'scanning', which is
					 * when it is waiting for the vehicle to stop.
					 */
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
					/*
					 * This method will switch the mode between 0-60 and 1/4 mile when called by the button.
					 */
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
		
		class UpdateLoop implements Runnable {
			/*
			 * This thread holds the detail for how the timing works. It works in stages, each stage selecetd through 
			 * booleans, and the first started by clicking a button. First of all, when the recording is started, the
			 * software starts 'scanning'. It will do nothing until the vehicle reaches 0mph. At this point, it will
			 * wait until there is motion again, and start the timer. During this stage the thread is continually
			 * polling the sensors to see if the objective (defined by the mode) has bee reached. When it is, the timer
			 * is stopped, and the time is added to an array (which is displayed on-screen to the user). If the time is
			 * good enough, it will be automatically added to the leader board with the current profile name in
			 * settings as the name. 
			 */
	        private Thread trackTestLoop;
	        @Override
	        public void run() {
	            while (true)  {
	            	if (scanning) {
	            		oldTime = newTime;
	            		newTime = System.currentTimeMillis();
	            		oldSpeed = currentSpeed;
	            		PID pid = onBoardDisplay.dataHandler.decodePID((byte)0x0D);
	            		currentSpeed = onBoardDisplay.dataHandler.decodePIDRead(onBoardDisplay.carInterface.readPID(pid.ID,false),pid);
    					//PID pid2 = onBoardDisplay.dataHandler.decodePID((byte)0x31);
	            		//currentDistance = onBoardDisplay.dataHandler.decodePIDRead(onBoardDisplay.carInterface.readPID(pid2.ID,false),pid2);
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
	    					if (recording) {//waiting for reaching 1/4 mile
	    						newTime = System.currentTimeMillis();
	    						float newDistance = (currentSpeed-oldSpeed)*(System.currentTimeMillis()-oldTime);//Integrating speed/
	    						currentDistance += newDistance;
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
	    							System.out.println("Started moving, waiting for 1/4 Mile...");
	    							statusString = "Started moving, waiting for 1/4 Mile...";
	    							motionWaiting = false;
	    							recording = true;
	    							//startDistance = onBoardDisplay.dataHandler.decodePIDRead(onBoardDisplay.carInterface.readPID(pid2.ID,false),pid2);
	    							startDistance = 0;
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
							e.printStackTrace();
						}
					} else {
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
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
			//Already explained in Detail Panel.
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
