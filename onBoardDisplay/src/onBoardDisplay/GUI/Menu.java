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
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;
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

import onBoardDisplay.*;
import onBoardDisplay.dataHandling.PID;

public class Menu {
	public static class MenuPanel extends JPanel implements MouseListener {
        protected Option[] shownOptions;
        protected boolean sensing = false;
        public Image button;
		public Image buttonPressed;
        protected Option[] mainMenu = new Option[] {
            new Option("Display Readings") {
                @Override
                public void action() {
                    shownOptions = currentInfoMenu;
                }
            },
            new Option("Track Day") {
                @Override
                public void action() {
                    shownOptions = trackDayMenu;
                }
            },
            new Option("Error Codes") {
                @Override
                public void action() {
                    shownOptions = errorCodesMenu;
                }
            },
            new Option("Settings") {
                @Override
                public void action() {
                    shownOptions = settingsMenu;
                }
            },
            new Option("Exit") {
                @Override
                public void action() {
                	onBoardDisplay.shutdown();
                }
            }
        };
        protected Option[] currentInfoMenu = new Option[] {
                new Option("Specific Value") {
                    @Override
                    public void action() {
                        onBoardDisplay.layout.show(onBoardDisplay.topLayerPanel, "hudPanel");
                        onBoardDisplay.hudPanel.layout.show(onBoardDisplay.hudPanel.hudTopLayerPanel,"rawReadSpecificPanel");
                        onBoardDisplay.hudPanel.rawReadSpecificPanel.startRun();
                        stopSensing();
                    }
                },
                new Option("Cylinder Info") {
                    @Override
                    public void action() {
                        onBoardDisplay.layout.show(onBoardDisplay.topLayerPanel, "hudPanel");
                        onBoardDisplay.hudPanel.layout.show(onBoardDisplay.hudPanel.hudTopLayerPanel,"cylinderPanel");
                        onBoardDisplay.hudPanel.cylinderPanel.startRun();
                        stopSensing();
                    }
                },
                new Option("Show Dash") {
                    @Override
                    public void action() {
                        //onBoardDisplay.layout.show(onBoardDisplay.topLayerPanel, "hudPanel");
                        //onBoardDisplay.hudPanel.layout.show(onBoardDisplay.hudPanel.hudTopLayerPanel,"dashPanel");
                        //onBoardDisplay.hudPanel.dashPanel.startRun();
                    	onBoardDisplay.layout.show(onBoardDisplay.topLayerPanel, "dashPanel");
                    	onBoardDisplay.dashPanel.startRun();
                        stopSensing();
                    }
                },
                new Option("Graph Changes") {
                    @Override
                    public void action() {
                        onBoardDisplay.layout.show(onBoardDisplay.topLayerPanel, "graphPanel");
                        onBoardDisplay.graphPanel.startRun();
                        stopSensing();
                    }
                },
                //TODO add options for other HUDs.
                new Option("Back") {
                    @Override
                    public void action() {
                        shownOptions = mainMenu;
                    }
                }
            };
        
        protected Option[] trackDayMenu = new Option[] {
                new Option("0 - 60 & 1/4 Mile") {
                    @Override
                    public void action() {
                        onBoardDisplay.layout.show(onBoardDisplay.topLayerPanel, "trackTestPanel");
                        onBoardDisplay.trackTestPanel.startRun();
                        stopSensing();
                    }
                },
                new Option("Leaderboards") {
                    @Override
                    public void action() {
                    	onBoardDisplay.layout.show(onBoardDisplay.topLayerPanel, "leaderBoardPanel");
                        onBoardDisplay.leaderBoardPanel.startRun();
                        stopSensing();
                    }
                },
                new Option("BHP Calculation") {
                    @Override
                    public void action() {
                        onBoardDisplay.layout.show(onBoardDisplay.topLayerPanel, "hudPanel");
                        onBoardDisplay.hudPanel.layout.show(onBoardDisplay.hudPanel.hudTopLayerPanel,"dashPanel");
                        onBoardDisplay.hudPanel.dashPanel.startRun();
                        stopSensing();
                    }
                },
                new Option("Back") {
                    @Override
                    public void action() {
                        shownOptions = mainMenu;
                    }
                }
            };
        
        protected Option[] errorCodesMenu = new Option[] {
                new Option("Read All Codes") {
                    @Override
                    public void action() {
                        onBoardDisplay.layout.show(onBoardDisplay.topLayerPanel, "errorCodePanel");
                        onBoardDisplay.errorCodePanel.startRun();
                        stopSensing();
                    }
                },
                new Option("Back") {
                    @Override
                    public void action() {
                        shownOptions = mainMenu;
                    }
                }
            };
        
        protected Option[] settingsMenu = new Option[] {
            new Option("Aspect: 16:9") {
                @Override
                public void action() {
                    if (onBoardDisplay.graphicsWidth == 1280) {
                        onBoardDisplay.graphicsWidth = 960;
                        this.currentCaption = "Aspect: 4:3";
                    } else {
                        onBoardDisplay.graphicsWidth = 1280;
                        this.currentCaption = "Aspect: 16:9";
                    }
                    onBoardDisplay.calculateAspect();
                }
            },
            new Option("Change Colours") {
                @Override
                public void action() {
                	String[] availableColourStrings = onBoardDisplay.dataHandler.colourNames.keySet().toArray(new String[onBoardDisplay.dataHandler.colourNames.size()]);
            		JPanel dialogPanel = new JPanel();
            		dialogPanel.add(new JLabel("Select Colours (default: BLACK, GREEN, RED, PINK):"));
                    JComboBox[] comboBoxes = new JComboBox[4];
                    for (int i = 0; i< 4; i++) {
                    	DefaultComboBoxModel model = new DefaultComboBoxModel();
                    	for (String colourString: availableColourStrings) {
                            model.addElement(colourString);
                        }
                    	Set set = onBoardDisplay.dataHandler.colourNames.entrySet();
                		Iterator it = set.iterator();
                		while (it.hasNext()) {
                			Map.Entry me = (Map.Entry)it.next();
                			if ((Color) me.getValue()==onBoardDisplay.guiColours[i]) {
                                model.setSelectedItem(me.getKey());
                			}
                		}
                    	JComboBox comboBox= new JComboBox(model);
                        dialogPanel.add(comboBox);
                        comboBoxes[i] = comboBox;
                    }
                    int confirmed = JOptionPane.showConfirmDialog(null, dialogPanel, "PID", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
                    if (confirmed == JOptionPane.OK_OPTION) {
                    	System.out.print("Setting new colours:");
                    	for (int i = 0; i < 4; i++) {
                    		try {
                    			onBoardDisplay.guiColours[i] = onBoardDisplay.dataHandler.colourNames.get((String)comboBoxes[i].getSelectedItem());
                    			System.out.print((String)comboBoxes[i].getSelectedItem()+ " ");
                    		} catch (Exception e) {
                    		}
                    		System.out.println();
                    	}
                    }
                }
            },
            new Option("Back") {
                @Override
                public void action() {
                    shownOptions = mainMenu;
                }
            }
        };
        
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
            g2d.drawRect(onBoardDisplay.ModifyAspectX(100),
                    onBoardDisplay.ModifyAspectY(100),
                    onBoardDisplay.ModifyAspect(onBoardDisplay.graphicsWidth-200),
                    onBoardDisplay.ModifyAspect(onBoardDisplay.graphicsHeight-200));
            g2d.setFont(new Font("Gill Sans", Font.BOLD , onBoardDisplay.ModifyAspect(90)));
            g2d.drawString("On-Board Display", onBoardDisplay.ModifyAspectX((onBoardDisplay.graphicsWidth/2)-380),
                    onBoardDisplay.ModifyAspectY(80));
            
            g2d.setFont(new Font("Gill Sans", Font.BOLD ,
                    onBoardDisplay.ModifyAspect(30)));
            int spacing = 100;
            Image buttonTexture;
            for (Option option : shownOptions){
                if (option.selected) {
                    g2d.setColor(onBoardDisplay.guiColours[3]);
                    buttonTexture = buttonPressed;
                } else {
                    g2d.setColor(onBoardDisplay.guiColours[2]);
                    buttonTexture = button;
                }
                //option.xPosition =(onBoardDisplay.graphicsWidth/2)-(option.width/2);
                option.xPosition = (onBoardDisplay.graphicsWidth/8);
                spacing += option.height + 5;
                option.yPosition = spacing;
                //g2d.drawRect(onBoardDisplay.ModifyAspectX(option.xPosition),
                //        onBoardDisplay.ModifyAspectY(option.yPosition),
                //        onBoardDisplay.ModifyAspect(option.width),
                //        onBoardDisplay.ModifyAspect(option.height));
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
        }
    
        public static class Option{
            public String currentCaption;
            public int xPosition;
            public int yPosition;
            public int width = 400;//a test comment.
            public int height = 60;
            public boolean selected;
            public Object link; //For optional extra use.
            
            public Option (String currentCaption) {
                this.currentCaption = currentCaption;
            }
            
            public Option(String currentCaption, int xPosition, int yPosition, int width, int height, Object link) {
            	this.currentCaption = currentCaption;
            	this.xPosition = xPosition;
            	this.yPosition = yPosition;
            	this.width = width;
            	this.height = height;
            	this.link = link;
            }
            
            public void select() {
                this.selected = true;
            }
            
            public void deselect() {
                this.selected = false;
            }
            
            public void setcapttion(String newCaption) {
                this.currentCaption = newCaption;
            }
            
            public void action() {
                System.out.println("This option has no purpose.");
            }
        }
        
        @Override
        public void mousePressed(MouseEvent e) {
            int trueXPos = e.getX();
            int trueYPos = e.getY();
            int xPos = (int)((trueXPos - onBoardDisplay.xOffset)/onBoardDisplay.graphicsMultiplier);
            int yPos = (int)((trueYPos - onBoardDisplay.yOffset)/onBoardDisplay.graphicsMultiplier);
            //System.out.print("Mouse press at "); System.out.print(xPos);
            //System.out.print(","); System.out.println(yPos);
            for (Option option : shownOptions) {
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

            inputMap.put(KeyStroke.getKeyStroke("pressed UP"), "UP");
            actionMap.put("UP", new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent arg0) {
                    keyAction("UP");
                }
            });
            
            inputMap.put(KeyStroke.getKeyStroke("pressed DOWN"), "DOWN");
            actionMap.put("DOWN", new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent arg0) {
                    keyAction("DOWN");
                }
            });
            
            inputMap.put(KeyStroke.getKeyStroke("pressed SPACE"), "CONFIRM");
            inputMap.put(KeyStroke.getKeyStroke("pressed ENTER"), "CONFIRM");
            actionMap.put("CONFIRM", new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent arg0) {
                    keyAction("CONFIRM");
                }
            });
        }
        
        private int getSelectedIndex() {
            for (Option option : shownOptions) {
                if (option.selected) {
                    return Arrays.asList(shownOptions).indexOf(option);
                }
            }
            return 0; // Selects first if none already selected.
        }
    
        private void keyAction (String actionString) {
            if (sensing) {
                if (actionString.equals("UP")){
                    int ui = getSelectedIndex();
                    (shownOptions[ui]).deselect();
                    try {
                        (shownOptions[ui-1]).select();
                    } catch (Exception e) {
                        (shownOptions[shownOptions.length -1]).select();
                    }
                } else if (actionString.equals("DOWN")) {
                    int di = getSelectedIndex();
                    (shownOptions[di]).deselect();
                    try {
                        (shownOptions[di+1]).select();
                    } catch (Exception e) {
                        (shownOptions[0]).select();
                    }
                } else if (actionString.equals("CONFIRM")) {
                    for (Option option  : shownOptions) {
                        if (option.selected) {
                            option.action();
                        }
                    }
                }
                repaint();
            }
        }
        
        public void startSensing() {
            sensing = true;
        }
        
        public void stopSensing() {
            sensing = false;
        }
        
        public MenuPanel(int width,int height) {
            this.setSize(width,height);
            this.shownOptions = mainMenu;
            setUpKeyboardListener();
            addMouseListener(this);
            try {
                button = ImageIO.read(getClass().getResourceAsStream("/onBoardDisplay/Res/Button.png"));
                buttonPressed = ImageIO.read(getClass().getResourceAsStream("/onBoardDisplay/Res/ButtonSelected.png"));
            }catch(Exception e){
                System.err.println(e);
                System.err.println("...So could not load button textures.");
            }
            setVisible(true);
            System.out.println("Setup done.");
        }
    }
}
