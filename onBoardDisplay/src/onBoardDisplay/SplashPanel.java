package onBoardDisplay;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

public class SplashPanel extends JPanel{
	BufferedImage image;
	
	public SplashPanel() {
		try {
			image = ImageIO.read(getClass().getResourceAsStream("/onBoardDisplay/Res/LoadingBar.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
    public void paint(Graphics g) {
		System.out.println("Painting Splash Screen...");
        super.paint(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.drawImage(image, 0, 0, this);
	}
}
