package onBoardDisplay.GUI.HUDLayouts;

/*
 * This panel works with the dash panel. It can only be reached by the user if they select a layout
 * and then click 'Edit' in the GUI. At this point, the information about the Dash current layout
 * will be transferred to this class, and the customisation will begin. The user has the option to
 * create as many bars and dials as they like. They can add them by specifying the PID that they will
 * display, and then can click where they want the top left, and then bottom right corner. With Bars
 * they can then specify whether they want the bar to move horizontally or vertically. The user can also
 * delete any widget they want if they have drawn it in the wrong place or want a change This means the
 * user can create any layout they could want to their own taste, with widgets where they want,
 * whatever size they want.
 */

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
import onBoardDisplay.GUI.Menu;
import onBoardDisplay.GUI.HUDLayouts.Dash.DashPanel.DashArrangement;
import onBoardDisplay.GUI.Menu.MenuPanel.Option;
import onBoardDisplay.GUI.components.dials.BarWidget;
import onBoardDisplay.GUI.components.dials.DialSkin1;
import onBoardDisplay.GUI.components.dials.GraphWidget;
import onBoardDisplay.dataHandling.PID;

public class DashCustomisation {
	public static class DashCustomisationPanel extends JPanel implements MouseListener {
		public static boolean running = false;
		DialSkin1[] dialList = new DialSkin1[] {};
		BarWidget[] barList = new BarWidget[] {};
		private PID newPid;
		private String helpMessage = "";
		private boolean addingDial = false;
		private boolean addingBar = false;
		private int bufferX, bufferY, bufferHeight, bufferWidth, bufferMin, bufferMax;
		private float bufferCircleProportion = (float)0.8;
		private boolean bufferIsHorizontal = false;
		private int addingStage = 0;//1 = Starts coordinates, 2=width and height, 3=orientation (bar only)
		private boolean deleting = false;
		private Menu.MenuPanel.Option[] buttons = new Menu.MenuPanel.Option[] {
			new Menu.MenuPanel.Option("Add Dial",55,onBoardDisplay.graphicsHeight-60-55,250,60,null) {
				@Override
				public void action() {
					/*
					 * This method starts the addition of a new Dial. The various buffer values held as class attributes
					 * will be progressively filled as it goes through the stages of planting a Dial from a dialog for
					 * PID selection (from the data handler), to clicking where the new item is wanted.
					 */
					try {
						helpMessage = "Adding new Dial...";
						newPid = onBoardDisplay.dataHandler.selectSupportedPIDsDialog(1,false)[0];
		        		helpMessage = "Click for the top left corner";
						bufferMin = newPid.min;
						bufferMax = newPid.max;
						addingDial = true;
						addingBar = false;
						addingStage = 1;
					} catch (Exception e) {
					}
				}
			},
			new Menu.MenuPanel.Option("Add Bar",55+250+5,onBoardDisplay.graphicsHeight-60-55,250,60,null) {
				@Override
				public void action() {
					/*
					 * The add bar button does the same thing as the 'add dial' one, but adds a bar...
					 */
					try{
						helpMessage = "Adding new Bar...";
						newPid = onBoardDisplay.dataHandler.selectSupportedPIDsDialog(1,false)[0];
		        		helpMessage = "Click for the top left corner";
						bufferMin = newPid.min;
						bufferMax = newPid.max;
						addingBar = true;
						addingDial = false;
						addingStage = 1;
					} catch (Exception e) {
					}
				}
			},
			new Menu.MenuPanel.Option("Delete Item",55+500+10,onBoardDisplay.graphicsHeight-60-55,250,60,null) {
				@Override
				public void action() {
					/*
					 * This button allows the user to delete any widget present on the layout. Once they have
					 * selected it, the next item they click on will be deleted. If they click on blank space,
					 * nothing will be deleted.
					 */
					System.out.println("Started item deleting, waiting for selection.");
					helpMessage = "Click on Item to Delete";
					deleting = true;
					helpMessage = "";
				}
			},
			new Menu.MenuPanel.Option("Save",55+750+10,onBoardDisplay.graphicsHeight-60-55,250,60,null) {
				@Override
				public void action() {
					/*
					 * This method is for the save button. It will load the currently edited layout into an object
					 * of the DashArrangement class from Dash, and then set it to the currentArrangement in Dash,
					 * at which point it will start updating the widgets with data. This method also saves the
					 * layouts to file by calling the method in the data handler.
					 */
					DashArrangement newArrangement = new DashArrangement(onBoardDisplay.dashPanel.currentArrangement.name, true,
							dialList,
							barList,
							new GraphWidget[] {}
					);
					onBoardDisplay.dashPanel.currentArrangement = newArrangement;
					onBoardDisplay.dashPanel.arrangements[onBoardDisplay.dashPanel.currentArrangementID] = newArrangement;
					onBoardDisplay.dataHandler.saveDashArrangements();
					keyAction("ESCAPE");
				}
			},
			new Menu.MenuPanel.Option("Exit",55+1000+15,onBoardDisplay.graphicsHeight-60-55,150,60,null) {
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
            switch (addingStage) {
            case 0:
            	addingStage--;
            	break;
            case 1:
            	bufferX = xPos;
            	bufferY = yPos;
        		helpMessage = "Click for the bottom right corner";
            	break;
            case 2:
            	bufferWidth = xPos - bufferX;
            	bufferHeight = yPos - bufferY;;
            	if (addingBar) {
            		helpMessage = "Left Click for Vertical, Right click for Horizontal";
            	} else {
            		addBufferItem();
            	}
            	break;
            case 3:
            	if (addingBar) {
            		if (e.getButton()==MouseEvent.BUTTON1) {
            			bufferIsHorizontal = false;
            		} else if (e.getButton()==MouseEvent.BUTTON2) {
            			bufferIsHorizontal = true;
            		}
            	}
            	addBufferItem();
            	break;
            }
            addingStage++;
            
            //Deletes dial or bar when clicked on.
            if (deleting) {
            	int deleteIndex = -1;
            	boolean deletingDial = false, deletingBar = false;
            	int count = 0;
            	for (DialSkin1 currentDial : dialList) {
            		if (xPos >= currentDial.startX &&
                            (xPos <= (currentDial.startX + currentDial.realWidth) &&
                            yPos >= currentDial.startY &&
                            yPos <= (currentDial.startY + currentDial.realHeight))) {
            			deleteIndex = count; 
            			deletingDial = true;
        				System.out.println("Found item to be deleted.");
                    }
            		count++;
            	}
            	count = 0;
            	for (BarWidget currentBar : barList) {
            		if (xPos >= currentBar.startX &&
                            (xPos <= (currentBar.startX + currentBar.realWidth) &&
                            yPos >= currentBar.startY &&
                            yPos <= (currentBar.startY + currentBar.realHeight))) {
            			deleteIndex = count; 
            			deletingBar = true;
        				System.out.println("Found item to be deleted.");
                    }
            		count++;
            	}
            	if (deletingDial) {
            		DialSkin1[] newDials = new DialSkin1[dialList.length-1];
            		int newIndex = 0;
            		for (int i = 0; i< dialList.length; i++) {
            			if (i != deleteIndex) {
            				newDials[newIndex] = dialList[i];
            				newIndex++;
            			}
            		}
            		dialList = newDials;
            	}
            	if (deletingBar) {
            		BarWidget[] newBars = new BarWidget[barList.length-1];
            		int newIndex = 0;
            		for (int i = 0; i< barList.length; i++) {
            			if (i != deleteIndex) {
            				newBars[newIndex] = barList[i];
            				newIndex++;
            			}
            		}
            		barList = newBars;
            	}
            	deleting = false;
            }
            repaint();
        }
		
		public void addBufferItem() {
			/*
			 * This is called by both the 'add' buttons for dials and bars. It will create an empty
			 * dial or bar object in the current layout's list, which can be edited as the changes
			 * progress. This list will then be added to a new layout object at the end when it is
			 * then saved to the Dash class.
			 */
			if (addingDial) {
				DialSkin1[] newDialList = new DialSkin1[dialList.length+1];
				for (int i = 0; i < dialList.length; i++) {
					newDialList[i] = dialList[i];
				}
				newDialList[dialList.length] = new DialSkin1(newPid,bufferX,bufferY,bufferWidth,
						bufferHeight,bufferMin,bufferMax,bufferCircleProportion);
				dialList = newDialList;
			} else {
				BarWidget[] newBarList = new BarWidget[barList.length+1];
				for (int i = 0; i < barList.length; i++) {
					newBarList[i] = barList[i];
				}
				newBarList[barList.length] = new BarWidget(newPid,bufferX,bufferY,bufferWidth,
						bufferHeight,newPid.min,newPid.max,bufferIsHorizontal);
				barList = newBarList;
			}
    		helpMessage = "Item Added";
    		addingDial = false; addingBar = false;
    		addingStage = 0;
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
                    onBoardDisplay.layout.show(onBoardDisplay.topLayerPanel, "dashPanel");
                    onBoardDisplay.dashPanel.startRun();
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
			repaint();
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
            g2d.drawString("Virtual Dashboard", onBoardDisplay.ModifyAspectX(55),
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
            
            for (DialSkin1 selectedDial : dialList) {
            	selectedDial.draw(g2d, this);
			}
            for (BarWidget selectedBar : barList) {
            	selectedBar.draw(g2d, this);
			}
            g2d.drawString(helpMessage, onBoardDisplay.ModifyAspectX(100), onBoardDisplay.ModifyAspectY(150));
		}
		
		public DashCustomisationPanel(int width,int height) {
            this.setSize(width,height);
            setUpKeyboardListener();
            addMouseListener(this);
            setVisible(true);
            System.out.println("Dash Customisation Panel setup done, waiting for run command.");
        }
	}
}

