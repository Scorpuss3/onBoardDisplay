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
import onBoardDisplay.GUI.HUDLayouts.Dash.DashPanel.DashArrangement;
import onBoardDisplay.GUI.Menu.MenuPanel.Option;
import onBoardDisplay.GUI.components.Dial;
import onBoardDisplay.GUI.components.dials.BarWidget;
import onBoardDisplay.GUI.components.dials.DialSkin1;
import onBoardDisplay.GUI.components.dials.GraphWidget;
import onBoardDisplay.dataHandling.Code;
import onBoardDisplay.dataHandling.DataHandler;
import onBoardDisplay.dataHandling.PID;
import onBoardDisplay.dataHandling.DataHandler.Location;

public class DashCustomisation {
	public static class DashCustomisationPanel extends JPanel implements MouseListener {
		public static boolean running = false;
		private DialSkin1[] dialList = new DialSkin1[] {};
		private BarWidget[] barList = new BarWidget[] {};
		private PID newPid;
		private String helpMessage = "";
		private boolean addingDial = false;
		private boolean addingBar = false;
		private int bufferX, bufferY, bufferHeight, bufferWidth, bufferMin, bufferMax;
		private float bufferCircleProportion = (float)0.8;
		private boolean bufferIsHorizontal = false;
		private int addingStage = 0;//1 = Starts coordinates, 2=width and height, 3=orientation (bar only)
		private Menu.MenuPanel.Option[] buttons = new Menu.MenuPanel.Option[] {
			new Menu.MenuPanel.Option("Add Dial",55,onBoardDisplay.graphicsHeight-60-55,400,60,null) {
				@Override
				public void action() {
					try {
						newPid = onBoardDisplay.dataHandler.selectSupportedPIDsDialog(1)[0];
						bufferMin = newPid.min;
						bufferMax = newPid.max;
						helpMessage = "Adding new Bar...";
						addingDial = true;
						addingStage = 1;
					} catch (Exception e) {
					}
				}
			},
			new Menu.MenuPanel.Option("Add Bar",55+400+5,onBoardDisplay.graphicsHeight-60-55,350,60,null) {
				@Override
				public void action() {
					try{
						newPid = onBoardDisplay.dataHandler.selectSupportedPIDsDialog(1)[0];
						bufferMin = newPid.min;
						bufferMax = newPid.max;
						helpMessage = "Adding new Bar...";
						addingBar = true;
						addingStage = 1;
					} catch (Exception e) {
					}
				}
			},
			new Menu.MenuPanel.Option("Save",55+800+10,onBoardDisplay.graphicsHeight-60-55,300,60,null) {
				@Override
				public void action() {
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
			new Menu.MenuPanel.Option("Exit",55+1200+15,onBoardDisplay.graphicsHeight-60-55,300,60,null) {
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
            switch (addingStage) {
            case 0:
            	addingStage--;
        		helpMessage = "Click for the top eft corner";
            	break;
            case 1:
            	bufferX = xPos;
            	bufferY = yPos;
        		helpMessage = "Click for the bottom right corner";
            	break;
            case 2:
            	bufferWidth = xPos - bufferX;
            	bufferHeight = xPos - bufferY;;
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
            repaint();
        }
		
		public void addBufferItem() {
			if (addingDial) {
				DialSkin1[] newDialList = new DialSkin1[dialList.length+1];
				for (int i = 0; i < dialList.length; i++) {
					newDialList[i] = dialList[i];
				}
				newDialList[dialList.length] = new DialSkin1(newPid,bufferX,bufferY,bufferWidth,
						bufferHeight,newPid.min,newPid.max,bufferCircleProportion);
			} else {
				BarWidget[] newBarList = new BarWidget[barList.length+1];
				for (int i = 0; i < barList.length; i++) {
					newBarList[i] = barList[i];
				}
				newBarList[barList.length] = new BarWidget(newPid,bufferX,bufferY,bufferWidth,
						bufferHeight,newPid.min,newPid.max,bufferIsHorizontal);
			}
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
                    onBoardDisplay.layout.show(onBoardDisplay.topLayerPanel, "dashPanel");
                    onBoardDisplay.dashPanel.startRun();
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
		
		public void startRun() {
			running = true;
			repaint();
		}
		
		public void stopRun() {
			running = false;
		}
		
		@Override
        public void paint(Graphics g) {
            super.paint(g);
            System.out.println("Painting custo...");
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
            //barWidget.update(onBoardDisplay.dataHandler.decodePIDRead(new byte[] {0x11}, barWidget.pid),barWidget.pid.unit);
            //barWidget.draw(g2d, this);
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

