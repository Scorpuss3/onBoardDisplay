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
import onBoardDisplay.GUI.components.dials.GraphWidget;
import onBoardDisplay.dataHandling.Code;
import onBoardDisplay.dataHandling.DataHandler;
import onBoardDisplay.dataHandling.PID;

public class Graph {
	public static class GraphPanel extends JPanel implements MouseListener {
		private boolean running = false;
		private boolean recording = false;
		private static PID[] pidList = {onBoardDisplay.dataHandler.decodePID((byte)0x0C),
				onBoardDisplay.dataHandler.decodePID((byte)0x05),
				onBoardDisplay.dataHandler.decodePID((byte)0x0B),
				onBoardDisplay.dataHandler.decodePID((byte)0x0D),
				};
		private String statusString = "Not Recording";
		private UpdateLoop graphUpdateLoop;// = new UpdateLoop();
		private long startTime = 0;
		private GraphWidget graph = new GraphWidget(pidList,100,180,700,600,200);
		private Menu.MenuPanel.Option[] buttons = new Menu.MenuPanel.Option[] {
			new Menu.MenuPanel.Option("Start Recording",55,onBoardDisplay.graphicsHeight-60-55,350,60,null) {
				@Override
				public void action() {
					if (recording) {
						this.currentCaption = "Start Recording";
						recording = false;
						statusString = "Not Recording";
						System.out.println("Stopped scanning");
					} else {
						this.currentCaption = "Stop Recording";
						recording = true;
						startTime = System.currentTimeMillis();
						statusString = "Recording";
						System.out.println("Started scanning");
						graph.clear();
					}
				}
			},
			new Menu.MenuPanel.Option("Select PIDs",55+400+5,onBoardDisplay.graphicsHeight-60-55,350,60,null) {
				@Override
				public void action() {
					//TODO Add graphing PID selection
					PID[] selected = onBoardDisplay.dataHandler.selectSupportedPIDsDialog(4);
					try {
						if (selected[1]!=null) {
							pidList = selected;
							graph = new GraphWidget(pidList,100,180,700,600,200);
						} else {
							System.out.println("Tried to create list with null PIDs, ignored...");
						}
					} catch (Exception e) {
						System.out.println("Tried to create list with null PIDs, ignored...");
					}
					return;
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
	        private Thread graphLoop;
	        @Override
	        public void run() {
	        	int i = 0;
	            while (true)  {//TODO change this so that it can be deactivated at program close later.
	            	if (running && recording) {
	            		System.out.println("now refreshing");
	            		long time = System.currentTimeMillis()-startTime;
						graph.update(time);
						onBoardDisplay.graphPanel.repaint();
						//try {
						//	Thread.sleep(200);
						//} catch (InterruptedException e) {
						//	// TODO Auto-generated catch block
						//	e.printStackTrace();
						//}
		            } else {//Keeps the while loop from garbage collecting itself.
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
	            	i++;
	            }
	        }
	        
	        public void start() {
	            graphLoop = new Thread(this,"graphUpdateLoopThread");
	            graphLoop.start();
	        }
	        
	        public void stop() {
	        	try {
					graphLoop.join();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	        }
	    }
		
		public void startRun() {
			running = true;
			graphUpdateLoop.start();
		}
		
		public void stopRun() {
			running = false;
			graphUpdateLoop.stop();
		}
		
		@Override
        public void paint(Graphics g) {
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
            g2d.drawString("Graph Changes", onBoardDisplay.ModifyAspectX(55),
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
            graph.draw(g2d, this);
		}
		
		public GraphPanel(int width,int height) {
            this.setSize(width,height);
            setUpKeyboardListener();
            addMouseListener(this);
            graphUpdateLoop = new UpdateLoop();
            setVisible(true);
            System.out.println("Graph Panel setup done, waiting for run command.");
        }
	}

}
