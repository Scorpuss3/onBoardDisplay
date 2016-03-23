package onBoardDisplay.GUI.components.dials;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;

import javax.swing.JPanel;

import onBoardDisplay.onBoardDisplay;
import onBoardDisplay.GUI.components.Dial;
import onBoardDisplay.dataHandling.PID;

public class BarWidget extends Dial{
	private boolean drawHorizontal;
	private float readingProportion;
	private int max, min;
	public BarWidget(PID pid, int startX, int startY, int realWidth, int realHeight, int min, int max, boolean drawHorizontal) {
		super(pid, startX, startY, 400, 400, realWidth, realHeight);
		this.max = max;
		this.min = min;
		this.drawHorizontal = drawHorizontal;
		Text t1 = new Text(pid.Description,20,"Gill Sans",onBoardDisplay.guiColours[1],400,40);
		Text t2 = new Text("NODATA",15,"Gill Sans",onBoardDisplay.guiColours[1],0,200);
		Text[] tArray = {t1,t2};
		this.texts = tArray;
	}
	
	public void update(float reading, String unit) {
		String stringReading = Float.toString(reading);
		texts[1].displayText = stringReading + " " + unit;
		readingProportion = (reading-min) / (float)(max-min);
	}
	
	public void draw(Graphics2D g2d, JPanel panel) {
		g2d.setColor(onBoardDisplay.guiColours[1]);
		g2d.fillRect(onBoardDisplay.ModifyAspectX(startX),
				onBoardDisplay.ModifyAspectY(startY),
				onBoardDisplay.ModifyAspectX((400*xMod)),
				onBoardDisplay.ModifyAspectY((400*yMod)));
		g2d.setColor(onBoardDisplay.guiColours[2]);
		if (drawHorizontal) {
			g2d.drawLine(onBoardDisplay.ModifyAspectX(startX + xMod*(400*(readingProportion))),
					onBoardDisplay.ModifyAspectY(startY),
					onBoardDisplay.ModifyAspectX(startX + xMod*(400*(readingProportion))),
					onBoardDisplay.ModifyAspectY(startY + (400*yMod)));
		} else {
			g2d.drawLine(onBoardDisplay.ModifyAspectX(startX),
					onBoardDisplay.ModifyAspectY(startY + yMod*(400-400*(readingProportion))),
					onBoardDisplay.ModifyAspectX(startX + (400*xMod)),
					onBoardDisplay.ModifyAspectY(startY + yMod*(400-400*(readingProportion))));
		}
		AffineTransform original = g2d.getTransform();
		g2d.rotate(Math.PI/2);
		for (Text text : texts) {
			g2d.setFont(new Font(text.font, Font.PLAIN ,
					onBoardDisplay.ModifyAspect(text.fontSize)));
			g2d.drawString(text.displayText,
					onBoardDisplay.ModifyAspectY(startY + (text.relativeY) * yMod),//The X and Y are swapped around because co-ordinates are swapped for rotation...
					onBoardDisplay.ModifyAspectX(startX + (text.relativeX) * xMod));
		}
		g2d.setTransform(original);
	}
}
