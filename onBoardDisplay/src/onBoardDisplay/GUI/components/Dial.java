package onBoardDisplay.GUI.components;

/*
 * This class defines the overall dial object. Dials are used as widgets within other panels,
 * most of the time the dash panel. They have several attributes that specify the location and
 * shape in which they are drawn. They can contain text displays, and 'pins' that sweep like on
 * an analogue dial to be able to show changing values. This class is not used directly, but as
 * a basis for other derived classes.
 */

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

import onBoardDisplay.onBoardDisplay;
import onBoardDisplay.dataHandling.PID;

public class Dial {	
	//All components' locations are specified as proportions of generic sizes shown (including width and height) for scalability.
	public PID pid;
	public int realWidth, realHeight;//True GUI Dimentions (before aspect recalculations performed)
	public int genericWidth, genericHeight;//Used for positioning dial contents.
	public int startX, startY;
	public float xMod, yMod, fontMod;//Font needs separate modifier, currently calculated from width mod.
	protected ImagePart[] images;
	protected Text[] texts;
	public Pin[] pins;
	
	public Dial(PID pid, int startX, int startY, int genericWidth, int genericHeight, int realWidth, int realHeight) {
		this.pid = pid;
		this.startX = startX;
		this.startY = startY;
		this.genericWidth = genericWidth;//Larger numbers encouraged for detail, but mainly here for proportions.
		this.genericHeight = genericHeight;
		this.realWidth = realWidth;
		this.realHeight = realHeight;
		xMod = (float)realWidth / (float)genericWidth;
		yMod = (float)realHeight / (float)genericHeight;
		fontMod = xMod;
		//System.out.print("Create new dial: ");
		//System.out.println(xMod);
		//System.out.println(yMod);
		//System.out.println(fontMod);
	}
	
	public class Pin {
		public int relativeLength, originX, originY, endX, endY, min, max,width;
		public float circleProportion;//How much of a full circle the pin will turn, centre of gap is at base.
		public Color color;
		
		public Pin(Color color,int relativeLength, int originX, int originY, int min, int max, int width, float circleProportion) {
			this.color = color;
			this.relativeLength = relativeLength;
			this.originX = originX;
			this.originY = originY;
			this.min = min;
			this.max = max;
			this.circleProportion = circleProportion;
			this.width = width;
		}
		
		public void update(float reading) {
			/*
			 * The update method allows the values of the dial components to change when other
			 * information does. For example, when the sensor value changes, the text displaying
			 * the value will change, and so will the angle that the pin is drawn at.
			 */
			float asProportion = (reading-min) / (float)(max-min);
			int angle,angleFromDown,xDirection,yDirection,modX,modY;
			float circleProportionBelowOrigin = (circleProportion-(float)0.5)/2;
			//System.out.println("1/2 Proportion of Circle used below origin: " + String.valueOf(circleProportionBelowOrigin));
			//System.out.println("Proportion to fill: " + String.valueOf(asProportion));
			angleFromDown =((int) (asProportion * circleProportion * 360)) + ((int) (90-(circleProportionBelowOrigin*360)));
			//System.out.println("Angle from down: " + String.valueOf(angleFromDown));
			if (asProportion < 0.5) {
				xDirection = -1;
			} else {
				xDirection = 1;
			}
			if (asProportion < circleProportionBelowOrigin||
					asProportion > (1-circleProportionBelowOrigin)) {
				yDirection = 1;//greater y means down
				if (xDirection == 1) {
					angle = angleFromDown - 270;
				} else {
					angle = 90 - angleFromDown;//90 to swap adj and opp
				}
			} else {
				yDirection = -1;//greater y means down
				if (xDirection == 1) {
					angle = 90 - (angleFromDown - 180);//90 to swap adj and opp
				} else {
					angle = angleFromDown - 90;
				}
			}
			//System.out.println("Angle in quadrant: " + String.valueOf(angle));
			//System.out.println("XDirection: " + String.valueOf(xDirection));
			//System.out.println("YDirection: " + String.valueOf(yDirection));
			//X needs to always be the adj, Y needs to always be the opp...
			//hyp is length of line...
			modX = ((int) (Math.cos(Math.toRadians(angle))*this.relativeLength)) * xDirection;
			modY = ((int) (Math.sin(Math.toRadians(angle))*this.relativeLength)) * yDirection;
			//System.out.println("modX: " + String.valueOf(modX));
			//System.out.println("modY: " + String.valueOf(modY));
			this.endX = originX + modX;
			this.endY = originY + modY;
		}
	}
	
	public class ImagePart {
		/*
		 * the dials can contain images, which I have used for backgrounds. This just allows the images
		 * to be loaded and handled as part of the dial.
		 */
		public BufferedImage image;
		public int relativeX;
		public int relativeY;
		public int relativeWidth;
		public int relativeHeight;
		
		public ImagePart(String skinName, int relativeX, int relativeY, int relativeWidth, int relativeHeight, Dial loader) {
			this.image = loader.loadDialSkin(skinName);
			this.relativeX = relativeX;
			this.relativeY = relativeY;
			this.relativeHeight = relativeHeight;
			this.relativeWidth = relativeWidth;
		}
	}
	
	public class Text {
		//The dials can also contain text, for displaying values, PID names, and units.
		public String displayText;
		public int fontSize;
		public String font;
		public Color color;
		public int relativeX;
		public int relativeY;
		
		public Text (String displayText, int fontSize, String font, Color color, int x, int y) {
			this.displayText = displayText;
			this.fontSize = fontSize;
			this.font = font;
			this.color = color;
			this.relativeX = x;
			this.relativeY = y;
		}
	}
	
	protected BufferedImage loadDialSkin(String skinName) {
		String full = "/onBoardDisplay/Res/GUIComponentTextures/" + skinName;
		return onBoardDisplay.dataHandler.tintBufferedImage(full, onBoardDisplay.guiColours[1]);
	}
}
