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

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import onBoardDisplay.onBoardDisplay;
import onBoardDisplay.dataHandling.*;
import onBoardDisplay.dataHandling.DataHandler.Location;
import onBoardDisplay.GUI.Menu.MenuPanel.Option;
import onBoardDisplay.GUI.components.dials.DialSkin1;
import onBoardDisplay.GUI.MyGraphics;

public class Detail {
	public static class DetailPanel extends JPanel implements MouseListener {
		private boolean running = false;
		private DetailMode mode = null;
		private PID chosenPID;
		private Code chosenCode;
		private Image currentFrontImg, currentSideImg, currentTopImg;
		
		public enum DetailMode {
			PID, DTC //PID is reading data mode and includes a display of reading. DTC is error code view.
		}
		
		public enum ImageType {
			FRONT, TOP, SIDE //Used for pinpoint drawing to decide which co-ordinates are used.
		}
		
		public void setMode(DetailMode newMode) {
			mode = newMode;
		}
		
		public void setItem(Object item) {
			try {
				PID PIDItem = (PID) item;
				mode = DetailMode.PID;
				chosenPID = PIDItem;
			} catch (Exception e){
				Code codeItem = (Code) item;
				mode = DetailMode.DTC;
				chosenCode = codeItem;
			}
		}
		
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
                    if (mode == DetailMode.DTC) {
                    	onBoardDisplay.layout.show(onBoardDisplay.topLayerPanel, "errorCodePanel");
                    	onBoardDisplay.errorCodePanel.startRun();
                    } else {
                    	onBoardDisplay.layout.show(onBoardDisplay.topLayerPanel, "menuPanel");
                    	onBoardDisplay.menuPanel.startSensing();
                    }
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
			if (mode == DetailMode.PID) {
				//TODO Add timer to refresh display.
			} else if (mode == null) {
				System.err.println("Could not show detail pane because mode was not set. Has setItem been called?");
				running = false;
			}
		}
		
		public void stopRun() {
			running = false;
		}
		
		@Override
        public void paint(Graphics g) {
            super.paint(g);
            //System.out.println("Painting....");
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
            g2d.drawString("Detail", onBoardDisplay.ModifyAspectX(55),
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
                    onBoardDisplay.ModifyAspect(25)));
            String topLine, descriptionLine, minorLocation, majorLocation;
            if (mode == DetailMode.PID) {
            	topLine = "OBDII PID No: " + Short.valueOf(chosenPID.ID).toString();
            	descriptionLine = "Description: " + chosenPID.Description;
            	majorLocation = chosenPID.majorLocation;
            	minorLocation = chosenPID.minorLocation;
            } else {
            	topLine = "Error Code No: " + chosenCode.IDString;
            	descriptionLine = "Description: " + chosenCode.Description;
            	majorLocation = chosenCode.majorLocation;
            	minorLocation = chosenCode.minorLocation;
            }
            g2d.drawString(topLine,
                    onBoardDisplay.ModifyAspectX(70),
                    onBoardDisplay.ModifyAspectY(140));
        	g2d.drawString(descriptionLine,
                    onBoardDisplay.ModifyAspectX(70),
                    onBoardDisplay.ModifyAspectY(190));
        	g2d.drawString("Location: " + DataHandler.getMajorLocationFromCode(majorLocation) + " , " + minorLocation,
                    onBoardDisplay.ModifyAspectX(70),
                    onBoardDisplay.ModifyAspectY(240));
        	
        	System.out.println("\t\t\t\t\t\t\t*"+majorLocation);
        	Image[] imageSet = DataHandler.majorLocationCodeTextures.get(majorLocation);
        	currentFrontImg = imageSet[0];
        	currentSideImg = imageSet[1];
        	currentTopImg = imageSet[2];
        	int fromTop = 60;
        	int spacing = 10;
        	int width = 550;
        	int frontWidth = 250;
        	int height = 190;
        	g2d.drawImage(DataHandler.majorLocationCodeTextures.get("UNK")[0],
                    onBoardDisplay.ModifyAspectX(onBoardDisplay.graphicsWidth- (width/2) - (frontWidth/2) - fromTop),
                    onBoardDisplay.ModifyAspectY(fromTop),
                    onBoardDisplay.ModifyAspect(frontWidth),
                    onBoardDisplay.ModifyAspect(height),
                    this);
        	g2d.drawImage(currentFrontImg,
                    onBoardDisplay.ModifyAspectX(onBoardDisplay.graphicsWidth- (width/2) - (frontWidth/2) - fromTop),
                    onBoardDisplay.ModifyAspectY(fromTop),
                    onBoardDisplay.ModifyAspect(frontWidth),
                    onBoardDisplay.ModifyAspect(height),
                    this);
        	drawPinpoint(g2d,
        			onBoardDisplay.graphicsWidth- (width/2) - (frontWidth/2) - fromTop,
        			fromTop,
        			frontWidth,
        			height,
        			minorLocation,
        			ImageType.FRONT);
        	g2d.drawImage(DataHandler.majorLocationCodeTextures.get("UNK")[1],
                    onBoardDisplay.ModifyAspectX(onBoardDisplay.graphicsWidth-width-fromTop),
                    onBoardDisplay.ModifyAspectY(fromTop+height+spacing),
                    onBoardDisplay.ModifyAspect(width),
                    onBoardDisplay.ModifyAspect(height),
                    this);
        	g2d.drawImage(currentSideImg,
                    onBoardDisplay.ModifyAspectX(onBoardDisplay.graphicsWidth-width-fromTop),
                    onBoardDisplay.ModifyAspectY(fromTop+height+spacing),
                    onBoardDisplay.ModifyAspect(width),
                    onBoardDisplay.ModifyAspect(height),
                    this);
        	drawPinpoint(g2d,
        			onBoardDisplay.graphicsWidth-width-fromTop,
        			fromTop+height+spacing,
        			width,
        			height,
        			minorLocation,
        			ImageType.SIDE);
        	g2d.drawImage(DataHandler.majorLocationCodeTextures.get("UNK")[2],
                    onBoardDisplay.ModifyAspectX(onBoardDisplay.graphicsWidth-width-fromTop),
                    onBoardDisplay.ModifyAspectY(fromTop+height*2+spacing*2),
                    onBoardDisplay.ModifyAspect(width),
                    onBoardDisplay.ModifyAspect(height),
                    this);
        	g2d.drawImage(currentTopImg,
                    onBoardDisplay.ModifyAspectX(onBoardDisplay.graphicsWidth-width-fromTop),
                    onBoardDisplay.ModifyAspectY(fromTop+height*2+spacing*2),
                    onBoardDisplay.ModifyAspect(width),
                    onBoardDisplay.ModifyAspect(height),
                    this);
        	drawPinpoint(g2d,
        			onBoardDisplay.graphicsWidth-width-fromTop,
        			fromTop+height*2+spacing*2,
        			width,
        			height,
        			minorLocation,
        			ImageType.TOP);
        	//DialSkin1 myDial = new DialSkin1(new PID(),100,350,200,200,0,180,(float)0.8);
        	//myDial.update(0,"degrees");
        	//myDial.draw(g2d, this);
        	//myDial.update(90,"degrees");
        	//myDial.draw(g2d, this);
        	//myDial.update(180,"degrees");
        	//myDial.draw(g2d, this);
        	//myDial.update(45,"degrees");
        	//myDial.draw(g2d, this);
        	//myDial.update(135,"degrees");
        	//myDial.draw(g2d, this);
        	
        	//DialSkin1 myDial2 = new DialSkin1(400,100,200,200,0,6000);
        	//myDial2.update(1500,"rpm");
        	//myDial2.draw(g2d, this);
        	//g2d.drawImage(MyGraphics.scaleImageFromText("TEST TEXT",200,300),500,0,200,300,this);
		}
		
		private static void drawPinpoint(Graphics2D g2d, int imgX, int imgY, int imgWidth, int imgHeight, String minorLocationName, ImageType imgType) {
			Location minorLocation = onBoardDisplay.dataHandler.getMinorLocation(onBoardDisplay.vehicleName, minorLocationName);
			int pinX,pinY;
			System.out.print("Pinpoint locs: ");
			System.out.print(minorLocation.xPos);
			System.out.print(minorLocation.yPos);
			System.out.println(minorLocation.zPos);
			if (imgType == ImageType.FRONT) {
				pinX = imgX + (imgWidth * minorLocation.xPos)/100;
				pinY = imgY + (imgHeight * minorLocation.yPos)/100;
			} else if (imgType == ImageType.SIDE) {
				pinX = imgX + (imgWidth * minorLocation.zPos)/100;
				pinY = imgY + (imgHeight * minorLocation.yPos)/100;
			} else {
				pinX = imgX + (imgWidth * minorLocation.zPos)/100;
				pinY = imgY + (imgHeight * minorLocation.xPos)/100;
			}
			//Vertical line
			if(!(minorLocation.xPos==0 && minorLocation.yPos==0 && minorLocation.zPos==0)) {
				g2d.drawLine(onBoardDisplay.ModifyAspectX(pinX),
						onBoardDisplay.ModifyAspectY(imgY),
						onBoardDisplay.ModifyAspectX(pinX),
						onBoardDisplay.ModifyAspectY(imgY + imgHeight));
				//Horizontal Line
				g2d.drawLine(onBoardDisplay.ModifyAspectX(imgX),
						onBoardDisplay.ModifyAspectY(pinY),
						onBoardDisplay.ModifyAspectX(imgX + imgWidth),
						onBoardDisplay.ModifyAspectY(pinY));
				//Pinpoint Circle
				int cirRad = 2;
				g2d.drawOval(onBoardDisplay.ModifyAspectX(pinX-cirRad),
						onBoardDisplay.ModifyAspectY(pinY-cirRad),
						onBoardDisplay.ModifyAspect(cirRad*2),
						onBoardDisplay.ModifyAspect(cirRad*2));
			}
		}
		
		public DetailPanel(int width,int height) {
            this.setSize(width,height);
            setUpKeyboardListener();
            addMouseListener(this);
            setVisible(true);
            System.out.println("Detail Panel setup done, waiting for run command.");
        }
	}

}
