package onBoardDisplay.GUI.HUDLayouts;

/*
 * This panel is the one that shows real-time data on dash board styled layouts. It can hold many
 * different layouts in its array, and allows the user to add to the list by creating new ones. The
 * layouts consist of a group of 'widgets' that will display data in a variety of ways that is 
 * continuously updated. The user can also edit the layouts by linking to the DashCustomisation
 * panel. Only custom panels can be edited; some preset ones are read-only.
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

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.DefaultComboBoxModel;
import javax.swing.InputMap;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import onBoardDisplay.onBoardDisplay;
import onBoardDisplay.GUI.HUD;
import onBoardDisplay.GUI.Menu;
import onBoardDisplay.GUI.Detail.DetailPanel.DetailMode;
import onBoardDisplay.GUI.Detail.DetailPanel.ImageType;
import onBoardDisplay.GUI.Menu.MenuPanel.Option;
import onBoardDisplay.GUI.components.Dial;
import onBoardDisplay.GUI.components.dials.BarWidget;
import onBoardDisplay.GUI.components.dials.DialSkin1;
import onBoardDisplay.GUI.components.dials.GraphWidget;
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
		//private static BarWidget barWidget = new BarWidget(pidList[1],900,150,30,200,pidList[1].min,pidList[1].max,false);
		private Menu.MenuPanel.Option[] buttons = new Menu.MenuPanel.Option[] {
			new Menu.MenuPanel.Option("Select Layout",55,onBoardDisplay.graphicsHeight-60-55,300,60,null) {
				@Override
				public void action() {
					/*
					 * This method is for the 'Select Layout' button, which brings up a dialog of all the dash
					 * layouts currently available. When one is selected, it is put into the 'currentArrangement'
					 * variable, which is the one accessed by all the other methods.
					 */
					JPanel dialogPanel = new JPanel();
					dialogPanel.add(new JLabel("Select layout to use:"));
					DefaultComboBoxModel model = new DefaultComboBoxModel();
			        for (DashArrangement ar: arrangements) {
			            model.addElement(ar.name);
			        }
			        JComboBox comboBox= new JComboBox(model);
			        dialogPanel.add(comboBox);
			        int confirmed = JOptionPane.showConfirmDialog(null, dialogPanel, "PID", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
			        if (confirmed == JOptionPane.OK_OPTION) {
			        	String selectedName = (String) comboBox.getSelectedItem();
			        	for (int i = 0; i < arrangements.length; i++) {
				            if (arrangements[i].name == selectedName) {
				            	System.out.print("Set Dash Arrangement to" + arrangements[i].name + " "); System.out.println(arrangements[i].customisable);
				            	currentArrangement = arrangements[i];
				            	currentArrangementID = i;
				            }
				        } 
			        }
				}
			},
			new Menu.MenuPanel.Option("New Layout",55+300+5,onBoardDisplay.graphicsHeight-60-55,300,60,null) {
				@Override
				public void action() {
					/*
					 * This method is for the 'New Layout' button. It creates a new blank layout that can be
					 * edited by the user in the dash customisation panel. 
					 */
					String name = JOptionPane.showInputDialog(null,"Enter New Dash Layout Name:");
					DashArrangement newArr = new DashArrangement(name, true,
							new DialSkin1[] {},
							new BarWidget[] {},
							new GraphWidget[] {}
					);
					DashArrangement[] newArrangements = new DashArrangement[arrangements.length+1];
					for (int i = 0; i < arrangements.length; i++) {
						newArrangements[i] = arrangements[i];
					}
					newArrangements[arrangements.length] = newArr;
					arrangements = newArrangements;
					currentArrangementID = arrangements.length-1;
					currentArrangement = newArr;
				}
			},
			new Menu.MenuPanel.Option("Edit Layout",55+600+10,onBoardDisplay.graphicsHeight-60-55,300,60,null) {
				@Override
				public void action() {
					/*
					 * This method submits the current layout to the dash customisation panel, where it can be edited
					 * by the user. This will only work if the current layout is marked with permission to edit, as some
					 * are kept as protected presets.
					 */
					if (currentArrangement.customisable) {
						System.out.println("Starting Layout Edit.");
						onBoardDisplay.dashCustomisationPanel.dialList = currentArrangement.dialList;
						onBoardDisplay.dashCustomisationPanel.barList = currentArrangement.barList;
                        running = false;
	                    onBoardDisplay.layout.show(onBoardDisplay.topLayerPanel, "dashCustomisationPanel");
                    	onBoardDisplay.dashCustomisationPanel.startRun();
					} else {
						System.out.println("Tried to edit non-customisable dash arrangement.");
					}
				}
			},
			new Menu.MenuPanel.Option("Exit",55+900+15,onBoardDisplay.graphicsHeight-60-55,250,60,null) {
				@Override
				public void action() {
					keyAction("ESCAPE");
				}
			}
		};
		int currentArrangementID = 0;
		/*
		 * This list defines some basic layouts, however, this is not needed unless there is no dash layout
		 * file created yet.
		 */
		public static DashArrangement[] arrangements = new DashArrangement[] {
				new DashArrangement("Default", false,
						new DialSkin1[] {new DialSkin1(pidList[0],100,150,200,200,pidList[0].min,pidList[0].max,(float)0.8),
								new DialSkin1(pidList[1],100,400,200,200,pidList[1].min,pidList[1].max,(float)0.8),
								new DialSkin1(pidList[2],500,150,200,200,pidList[2].min,pidList[2].max,(float)0.8),
								new DialSkin1(pidList[3],500,400,200,200,pidList[3].min,pidList[3].max,(float)0.8)
						},
						new BarWidget[] {},
						new GraphWidget[] {}
				),
				new DashArrangement("Default 2", false,
						new DialSkin1[] {new DialSkin1(pidList[0],100,150,200,200,pidList[0].min,pidList[0].max,(float)0.8),
								new DialSkin1(pidList[2],500,150,200,200,pidList[2].min,pidList[2].max,(float)0.8),
								new DialSkin1(pidList[3],500,400,200,200,pidList[3].min,pidList[3].max,(float)0.8)
						},
						new BarWidget[] {new BarWidget(pidList[1],900,150,60,400,pidList[1].min,pidList[1].max,false)
						},
						new GraphWidget[] {}
				),
				new DashArrangement("Custom 1", true,
						new DialSkin1[] {},
						new BarWidget[] {},
						new GraphWidget[] {}
				),
		};
		DashArrangement currentArrangement = arrangements[0];
		
		public static class DashArrangement {
			/*
			 * This is the class that defines the dash board layouts as an object. Each has an array that
			 * contains the various types of widgets that it may contain. This includes dials, bars, and
			 * 'mini' graphs (which are limited to one plotted value for now for dash panel use, or there
			 * would be too much delay between refreshes).
			 */
			public String name;
			public boolean customisable;
			public DialSkin1[] dialList;
			public BarWidget[] barList;
			public GraphWidget[] graphList;
			
			public DashArrangement(String name, boolean customisable, DialSkin1[] dl, BarWidget[] bl, GraphWidget[] gl) {
				this.name = name;
				this.customisable = customisable;
				dialList = dl;
				barList = bl;
				graphList = gl;
			}
		}
		
		@Override
        public void mousePressed(MouseEvent e) {
			//Already explained in Detail Panel
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
			 * This thread structure is how I run my updating GUI panels. The multi-threading is needed
			 * because I need to continuously be updating the data that is displayed in the dials, while
			 * at the same time refreshing the GUI drawing and widget statuses. Also, the GUI needs to
			 * remain interactive so the user can exit the screen or use buttons. This thread simply contains
			 * code to read and decode all of the PIDs being used in this panel, saving them to global
			 * variables.
			 */
	        private Thread dashLoop;
	        @Override
	        public void run() {
	        	int i = 0;
	            while (true)  {//TODO change this so that it can be deactivated at program close later.
	            	if (running) {
	            		//System.out.println("now refreshing");
						for (DialSkin1 selectedDial : currentArrangement.dialList) {
							byte[] rawBytes = onBoardDisplay.carInterface.readPID(selectedDial.pid.ID, false);//TODO consider turning autoretry on...
							System.out.print("Decoded Value:"); System.out.println(onBoardDisplay.dataHandler.decodePIDRead(rawBytes, selectedDial.pid));
							selectedDial.update(onBoardDisplay.dataHandler.decodePIDRead(rawBytes, selectedDial.pid),selectedDial.pid.unit);
						}
						
						for (BarWidget selectedBar : currentArrangement.barList) {
							byte[] rawBytes = onBoardDisplay.carInterface.readPID(selectedBar.pid.ID, false);//TODO consider turning autoretry on...
							System.out.print("Decoded Value:"); System.out.println(onBoardDisplay.dataHandler.decodePIDRead(rawBytes, selectedBar.pid));
							selectedBar.update(onBoardDisplay.dataHandler.decodePIDRead(rawBytes, selectedBar.pid),selectedBar.pid.unit);
						}
						for (GraphWidget selectedGraph : currentArrangement.graphList) {
							byte[] rawBytes = onBoardDisplay.carInterface.readPID(selectedGraph.displayedPIDs[0].ID, false);//TODO consider turning autoretry on...
							System.out.print("Decoded Value:"); System.out.println(onBoardDisplay.dataHandler.decodePIDRead(rawBytes, selectedGraph.displayedPIDs[0]));
							selectedGraph.update(System.currentTimeMillis());
						}
						//onBoardDisplay.hudPanel.dashPanel.repaint();
						onBoardDisplay.dashPanel.repaint();
						//try {
						//	Thread.sleep(100);
						//} catch (InterruptedException e) {
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
            g2d.drawString("Dash - "+currentArrangement.name, onBoardDisplay.ModifyAspectX(55),
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
            
            for (DialSkin1 selectedDial : currentArrangement.dialList) {
            	selectedDial.draw(g2d, this);
			}
            for (BarWidget selectedBar : currentArrangement.barList) {
            	selectedBar.draw(g2d, this);
			}
            for (GraphWidget selectedGraph : currentArrangement.graphList) {
            	selectedGraph.draw(g2d, this);
			}
            //barWidget.update(onBoardDisplay.dataHandler.decodePIDRead(new byte[] {0x11}, barWidget.pid),barWidget.pid.unit);
            //barWidget.draw(g2d, this);
		}
		
		public DashPanel(int width,int height) {
            this.setSize(width,height);
            setUpKeyboardListener();
            addMouseListener(this);
            UpdateLoop dashUpdateLoop = new UpdateLoop();
            //dashUpdateLoop.start();
            setVisible(true);
            onBoardDisplay.dataHandler.loadDashArrangements();
            System.out.println("Dash Panel setup done, waiting for run command.");
        }
	}
}
