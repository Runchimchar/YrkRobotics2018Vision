package colorRecognition;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

import org.opencv.core.Mat;

public class ImgPanel extends JPanel {
	private BufferedImage image;
	private int lDiff, rDiff, xAvr, yAvr;
	boolean isCounter = false;
	Mat lines;
	public ImgPanel(BufferedImage b, int l, int r) {
		image = b;
		lDiff = l;
		rDiff = r;
		isCounter = true;
	}
	public ImgPanel(BufferedImage b) {
		image = b;
		lDiff = 0;
		rDiff = 0;
		xAvr = 0;
		yAvr = 0;
		lines = new Mat();
	}
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
        if (image != null) {
            Graphics2D g2d = (Graphics2D) g.create();
            int x = (getWidth() - image.getWidth()) / 2;
            int y = (getHeight() - image.getHeight()) / 2;
            g2d.drawImage(image, x, y, this);
            g2d.setColor(Color.magenta);
            g2d.drawOval(xAvr-5+x,yAvr-5+y,10,10);
            if (isCounter) {
	            g2d.setColor(Color.magenta);
	            g2d.drawString(Integer.toString(lDiff), getWidth()/2-100, getHeight()-20);
	            g2d.drawString(Integer.toString(rDiff), getWidth()/2+100, getHeight()-20);
	            g2d.drawLine(getWidth()/2,0,getWidth()/2,getHeight());
            }
            
            g2d.dispose();
        }
	}
	public void updateImg(BufferedImage b, int l, int r) {
		image = b;
		lDiff = l;
		rDiff = r;
		repaint();
	}
	public void updateImg(BufferedImage b) {
		image = b;
		repaint();
	}
	public void setAvr(int x, int y) {
		xAvr = x;
		yAvr = y;
	}
	public void setLines(Mat l) {
		lines = l;
	}
}
