package onBoardDisplay.GUI;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

public class MyGraphics {
	public static Image scaleImageFromText(String text, int width, int height) {
		AffineTransform stretch = new AffineTransform();
        int f = 21; // Font size in px
        Font myFont = new Font("Comic Sans",Font.PLAIN,f);
        FontMetrics fontMetrics = new Canvas().getFontMetrics(myFont);
        int w = fontMetrics.stringWidth(text); // image width
        int h = fontMetrics.getHeight(); // image height
        //h= fontMetrics.getAscent() + fontMetrics.getDescent();
        //h = fontMetrics.getStringBounds(text, (Graphics2D));

        final BufferedImage bi = new BufferedImage(
                w,h,BufferedImage.TYPE_INT_RGB);
        Graphics2D g = bi.createGraphics();
        g.setFont(myFont);
        g.setRenderingHint(
                RenderingHints.KEY_TEXT_ANTIALIASING, 
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // paint BG
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, w, h);
        g.setColor(Color.BLACK);

        g.drawString(text, 0, f);
        // stretch
        stretch.concatenate(
                AffineTransform.getScaleInstance(1.18, 1d));
        g.setTransform(stretch);

        g.dispose();
        return bi;
	}
}
