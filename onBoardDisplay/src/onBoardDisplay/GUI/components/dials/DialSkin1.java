package onBoardDisplay.GUI.components.dials;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

import onBoardDisplay.onBoardDisplay;
import onBoardDisplay.GUI.components.Dial;
import onBoardDisplay.dataHandling.PID;

public class DialSkin1 extends Dial{
	public DialSkin1(PID pid, int startX, int startY, int realWidth, int realHeight, int min, int max, float circleProportion) {
		super(pid, startX, startY, 400, 400, realWidth, realHeight);
		Text t1 = new Text(pid.Description,20,"Gill Sans",onBoardDisplay.guiColours[1],150,100);
		Text t2 = new Text("NODATA",15,"Gill Sans",onBoardDisplay.guiColours[1],140,370);
		Text[] tArray = {t1,t2};
		this.texts = tArray;
		
		Pin pin = new Pin(onBoardDisplay.guiColours[2],200,200,200,min,max,5,(float)circleProportion);
		Pin[] pArray = {pin};
		this.pins = pArray;
		
		ImagePart i1 = new ImagePart("DialSkin1_1.png",0,0,400,400,this);//Background
		ImagePart[] iArray = {i1};
		this.images = iArray;
	}
	
	public void update(float reading, String unit) {
		String stringReading = Float.toString(reading);
		texts[1].displayText = stringReading + " " + unit;
		pins[0].update(reading);
	}
	
	public void draw(Graphics2D g2d, JPanel panel) {
		for (int x = 0; x <images[0].image.getWidth(); x++) {
			for (int y = 0; y< images[0].image.getHeight(); y++) {
				Color oldColor = new Color(images[0].image.getRGB(x,y),true);
				Color newRGBColor = onBoardDisplay.guiColours[1];
				images[0].image.setRGB(x, y, new Color(newRGBColor.getRed(),newRGBColor.getGreen(),newRGBColor.getBlue(),oldColor.getAlpha()).getRGB());
			}
		}
		//(images[0].image).setRGB(images[0].image.getWidth(),images[0].image.getHeight(),new Color(255,0,0).getRGB());
	    //BufferedImage img = new BufferedImage(images[0].image.getWidth(null), images[0].image.getHeight(null),
	    //    BufferedImage.TRANSLUCENT);
	    //Graphics2D graphics = img.createGraphics();
	    //Color newColor = new Color(100, 0, 0, 0 /* alpha needs to be zero */);
	    //graphics.setXORMode(newColor);
	    //graphics.drawImage(images[0].image, 0, 0, null);
	    //graphics.dispose();
		g2d.drawImage(images[0].image,
				onBoardDisplay.ModifyAspectX(startX + (images[0].relativeX) * xMod),
				onBoardDisplay.ModifyAspectY(startY + (images[0].relativeY) * yMod),
				onBoardDisplay.ModifyAspect(images[0].relativeWidth * xMod),
				onBoardDisplay.ModifyAspect(images[0].relativeHeight * yMod),
				panel);
		System.out.println("Image Location Data:");
		System.out.println(startX + (images[0].relativeX) * xMod);
		System.out.println(startY + (images[0].relativeY) * yMod);
		System.out.println(images[0].relativeWidth * xMod);
		System.out.println(images[0].relativeHeight * yMod);
		for (Text text : texts) {
			g2d.setFont(new Font(text.font, Font.PLAIN ,
					onBoardDisplay.ModifyAspect(text.fontSize)));
			g2d.drawString(text.displayText,
					onBoardDisplay.ModifyAspectX(startX + (text.relativeX) * xMod),
					onBoardDisplay.ModifyAspectY(startY + (text.relativeY) * yMod));
		}
		g2d.setColor(pins[0].color);
		Stroke old = g2d.getStroke();
		g2d.setStroke(new BasicStroke(pins[0].width*xMod));
		g2d.drawLine(onBoardDisplay.ModifyAspectX(startX + (pins[0].endX) * xMod),
				onBoardDisplay.ModifyAspectY(startY + (pins[0].endY) * yMod),
				onBoardDisplay.ModifyAspectX(startX + (pins[0].originX) * xMod),
				onBoardDisplay.ModifyAspectY(startY + (pins[0].originX) * yMod));
		g2d.setStroke(old);
	}
}
