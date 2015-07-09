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

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import onBoardDisplay.*;

public class Menu {
	public static class MenuPanel extends JPanel implements MouseListener {
        protected Option[] shownOptions;
        protected boolean sensing = false;
        protected Image button, buttonPressed;
        protected Option[] mainMenu = new Option[] {
            new Option("Display Readings") {
                @Override
                public void action() {
                    shownOptions = currentInfoMenu;
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
                    System.exit(1);
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
                        onBoardDisplay.layout.show(onBoardDisplay.topLayerPanel, "hudPanel");
                        onBoardDisplay.hudPanel.layout.show(onBoardDisplay.hudPanel.hudTopLayerPanel,"dashPanel");
                        onBoardDisplay.hudPanel.dashPanel.startRun();
                        stopSensing();
                    }
                },
                new Option("Graph Changes") {
                    @Override
                    public void action() {
                        onBoardDisplay.layout.show(onBoardDisplay.topLayerPanel, "hudPanel");
                        onBoardDisplay.hudPanel.layout.show(onBoardDisplay.hudPanel.hudTopLayerPanel,"graphPanel");
                        onBoardDisplay.hudPanel.graphPanel.startRun();
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
            System.out.println("Painting....");
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setColor(Color.BLACK);
            g2d.fillRect(0,0,onBoardDisplay.trueWidth,onBoardDisplay.trueHeight);
            g2d.setColor(Color.BLACK);
            g2d.fillRect(onBoardDisplay.ModifyAspectX(0),onBoardDisplay.ModifyAspectY(0),
                    onBoardDisplay.ModifyAspect(onBoardDisplay.graphicsWidth) ,
                    onBoardDisplay.ModifyAspect(onBoardDisplay.graphicsHeight));
            g2d.setColor(Color.RED);
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
                    g2d.setColor(Color.PINK);
                    buttonTexture = buttonPressed;
                } else {
                    g2d.setColor(Color.RED);
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
        }
    
        static class Option{
            protected String currentCaption;
            protected int xPosition;
            protected int yPosition;
            protected int width = 400;//a test comment.
            protected int height = 60;
            protected boolean selected;
            protected Object link; //For optional extra use.
            
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
            System.out.print("Mouse press at "); System.out.print(xPos);
            System.out.print(","); System.out.println(yPos);
            for (Option option : shownOptions) {
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
