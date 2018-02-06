//JP Whilden, 2018. York FRC Robotics					~NOTE! USE CTRL+F, THESE ARE IMPORTANT
package colorRecognition;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.Arrays;

import javax.swing.JFrame;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

class ColorLineRecognition {
	//Declare vars
	private int xAvr, yAvr, hLineCount, xPos, yPos, width, height;
	public final int CAM_WIDTH, CAM_HEIGHT; // 	~NOTE! VARIABLE IS PUBLIC AND READ-ONLY
	public final int RED = 170, BLUE = 120; // 	~NOTE! VARIABLE IS PUBLIC AND READ-ONLY
	private boolean showImg = false;
	private BufferedImage image;//Matrix from cam converted to image
	private ImgPanel panel;		//The debuging panel
	private VideoCapture video;
	//{80,120,200,256} are good for blue, {80,75,200,256} are good for red
	private int[] SV_LIM = {80,120,200,256}; // lower S and V, upper S and V
	private int[] HL_VALS = {70,40,100}; // Thresh, Min len, Max dist
	
	//Import library
	static{ System.loadLibrary(Core.NATIVE_LIBRARY_NAME); }
	
	//Constructor
	public ColorLineRecognition(int captureId) {
		image = null;
		panel = new ImgPanel(image);
		video = new VideoCapture(captureId);
		//Take photo to get camera's limits
		Mat img = new Mat();
		video.read(img);
		CAM_WIDTH = img.width();
		CAM_HEIGHT = img.height();
		xPos = 0;
		yPos = 0;
		width = 0;
		height = 0;
	}
	
	public ColorLineRecognition(int captureId, boolean img) {
		this(captureId);
		showImg = img;
	}
	
	//Take picture
	public void imageCapture(int hue, int t) {
		//Create empty matrix
		Mat img = new Mat();
		//Take capture from video and save to img matrix
		video.read(img);
		//Replace cam with photo
//		img = Imgcodecs.imread("C:/exampleImg/line4.bmp");
		//Limit image width to crop bounds
		try {
			if (width > 0) {
				img = img.colRange(xPos,xPos+width);
			}
		} catch (Exception e) {
			System.out.println("Width exceded.");
		}
		//Limit image height to crop bounds
		try {
			if (height > 0) {
				img = img.rowRange(yPos,yPos+height);
			}
		} catch (Exception e) {
			System.out.println("Height exceded.");
		}
		//Convert matrix from video to useable image
		image = matToBufferedImage(img);
		//Make a copy of img to be threshholded
		Mat thr = img.clone();
		//Convert to HSV
		Imgproc.cvtColor(thr,thr,Imgproc.COLOR_BGR2HSV);
		
		// 		--- Threshhold and Blur ---
		Core.inRange(thr,new Scalar(hue-t, SV_LIM[0], SV_LIM[1]),new Scalar(hue+t, SV_LIM[2], SV_LIM[3]), thr);
		int scale = 7;
		Mat element = Imgproc.getStructuringElement( Imgproc.MORPH_ELLIPSE,
				 new Size( 2*scale + 1, 2*scale+1 ),
                 new Point( scale, scale ) );
		Imgproc.dilate(thr, thr, element);
		scale = 2;
		element = Imgproc.getStructuringElement( Imgproc.MORPH_ELLIPSE,
				 new Size( 2*scale + 1, 2*scale+1 ),
                 new Point( scale, scale ) );
		Imgproc.erode(thr, thr, element);
		Imgproc.medianBlur(thr, thr, 5);
		
		//		--- Get Hugh Lines ---
		Mat lines = new Mat();
		Imgproc.HoughLinesP(thr, lines, 1, 3.1415926/180, HL_VALS[0], HL_VALS[1], HL_VALS[2]);
		hLineCount = lines.rows();
		
		//		--- Draw lines ---
    	for( int i = 0; i < lines.rows(); i++ ) {
			//System.out.println("T: " + i);
			Imgproc.line( thr, new Point(lines.get(i,0)[0], lines.get(i,0)[1]),
		            new Point(lines.get(i,0)[2], lines.get(i,0)[3]), new Scalar(120,255,120), 3);
		}

    	//		--- If Lines Found, Get Median Line ---			~NOTE! MAY NOT BE FINAL
    	if (lines.rows() > 0) {
	    	double[] c1 = new double[lines.rows()], c2 = new double[lines.rows()], 
	    			c3 = new double[lines.rows()], c4 = new double[lines.rows()];
	    	Point lPoint, rPoint;
	    	//Assemble arrays
	    	for( int i = 0; i < lines.rows(); i++ ) {
	    		c1[i] = lines.get(i,0)[0];
	    		c2[i] = lines.get(i,0)[1];
	    		c3[i] = lines.get(i,0)[2];
	    		c4[i] = lines.get(i,0)[3];
			}
	    	//Sort arrays
	    	Arrays.sort(c1);
	    	Arrays.sort(c2);
	    	Arrays.sort(c3);
	    	Arrays.sort(c4);
	    	lPoint = new Point(c1[c1.length/2],c2[c2.length/2]);
	    	rPoint = new Point(c3[c3.length/2],c4[c4.length/2]);
	    	// Get midpoint
	    	xAvr = Math.abs((int)(c1[c1.length/2]+c3[c3.length/2])/2);
			yAvr = Math.abs((int)(c2[c2.length/2]+c4[c4.length/2])/2);
			panel.setAvr(xAvr,yAvr);
//	    	System.out.println("Med line: "+lPoint+", "+rPoint);
			// Draw median line
			Imgproc.line(thr, lPoint, rPoint, new Scalar(256,256,256), 2);
    	}
    	
    	//Display Image
    	if (showImg)
    		image = matToBufferedImage(img);
    	else
    		image = matToBufferedImage(thr);
		
	}
	
	//Set x, y, width and height used to crop image
	public void cropImage(int x, int y, int width, int height) {
		xPos = x;
		yPos = y;
		this.width = width;
		this.height = height;
	}
	
	//Return number of lines on screen
	public int getLineCount() {
		return hLineCount;
	}
	
	//End video capture				~NOTE! THE USER MUST CALL THIS THEMSELVES, MAY CAUSE DATA LEAK!
	public void endCapture() {
		//Stop recording
		video.release();
	}
	
	//Change saturation and value bounds
	public void setSV(int[] a) {
		for (int i=0;i<4;i++) {
			if (a[i] > 0)
				SV_LIM[i] = a[i];
		}
	}
	
	//Change hugh-line properties
	public void setHL(int[] a) {
		for (int i=0;i<3;i++) {
			if (a[i] > 0)
				HL_VALS[i] = a[i];
		}
	}
	
	//	-------------------------	~NOTE! ONLY USED WITH JPANEL, MAY NOT BE FINAL!	-------------------------
	
	//Convert the matrix grabbed from the video feed into a useable BufferedImage	
	//Credit to https://goo.gl/fEpHrk
	private static BufferedImage matToBufferedImage(Mat original)
	{
		// init
		BufferedImage image = null;
		int width = original.width(), height = original.height(), channels = original.channels();
		byte[] sourcePixels = new byte[width * height * channels];
		original.get(0, 0, sourcePixels);
		
		if (original.channels() > 1)
		{
			image = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
		}
		else
		{
			image = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
		}
		final byte[] targetPixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
		System.arraycopy(sourcePixels, 0, targetPixels, 0, sourcePixels.length);
		
		return image;
	}	
	
	//		--- Maybe Not Final Methods ---
	//Get averages (coordinates of center of midpoint line)
	public int[] getAverages() {
		int[] avr = {xAvr,yAvr};
		return avr;
	}

	//Take picture to reset crop
	public void resetCrop() {
		Mat img = new Mat();
		video.read(img);
		xPos = 0;
		yPos = 0;
		width = img.width();
		height = img.height();
	}

	//Show gui with last image
	public void createGui(int w, int h) {
		//Create JFrame
		JFrame gui = new JFrame("Image");
		panel = new ImgPanel(image);
		Container pane = gui.getContentPane();		
		gui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		gui.setSize(w,h);
		pane.add(panel);
		
		gui.setVisible(true);
	}
	
	//Update Gui
	public void updateGui(int hue, int t) {
		imageCapture(hue, t);
		if (panel != null)
			panel.updateImg(image);
	}
}
