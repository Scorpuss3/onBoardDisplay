package onBoardDisplay.GUI.components.dials;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Stroke;

import javax.swing.JPanel;

import onBoardDisplay.onBoardDisplay;
import onBoardDisplay.GUI.components.Dial;

public class DialSkin1 extends Dial{
	public DialSkin1(int startX, int startY, int realWidth, int realHeight, int min, int max, float circleProportion) {
		super(startX, startY, 400, 400, realWidth, realHeight);
		Text t1 = new Text("Dial 1",20,"Gill Sans",Color.GREEN,150,100);
		Text t2 = new Text("NODATA",15,"Gill Sans",Color.GREEN,140,370);
		Text[] tArray = {t1,t2};
		this.texts = tArray;
		
		Pin pin = new Pin(Color.red,200,200,200,min,max,5,(float)circleProportion);
		Pin[] pArray = {pin};
		this.pins = pArray;
		
		ImagePart i1 = new ImagePart("DialSkin1_1.png",0,0,400,400,this);//Background
		ImagePart[] iArray = {i1};
		this.images = iArray;
	}
	
	public void update(int reading, String unit) {
		texts[1].displayText = Integer.toString(reading) + " " + unit;
		pins[0].update(reading);
	}
	
	public void draw(Graphics2D g2d, JPanel panel) {
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
				onBoardDisplay.ModifyAspectX(startY + (pins[0].endY) * yMod),
				onBoardDisplay.ModifyAspectX(startX + (pins[0].originX) * xMod),
				onBoardDisplay.ModifyAspectX(startY + (pins[0].originX) * yMod));
		g2d.setStroke(old);
	}
}