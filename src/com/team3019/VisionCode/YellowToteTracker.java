package com.team3019.VisionCode;

import java.awt.GridLayout;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JSlider;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import edu.wpi.first.wpilibj.networktables.NetworkTable;

/*
 * @author: Elijah Kaufman and his team
 * 
 * Note From Author: If you use this code or the algorithm
 * please give credit to Elijah Kaufman and FRC team 3019, Firebird Robotics
 */


public class YellowToteTracker{
	public static Scalar Red,Blue,Green,Yellow,thresh_Lower,thresh_Higher,grey_Lower,grey_higher;
	public static ArrayList<MatOfPoint> contourList = new ArrayList<>();
	static NetworkTable table;
	static final int MIN_VAL = 0;
	static final int MAX_VAL = 255;
	static final int INIT_VAL = 0;
	static final int NONZERO_MIN = 0;
	static final int NONZERO_MAX = 70000000;
	static final int NONZERO_INIT = 70000;
	static boolean foundit = false;
	//Window frame
	static JFrame frame = new JFrame ("VisionTracker");
	//Sliders
	static JSlider lowBlue = new JSlider(JSlider.HORIZONTAL, MIN_VAL, MAX_VAL, INIT_VAL);
	static JSlider lowGreen = new JSlider(JSlider.HORIZONTAL, MIN_VAL, MAX_VAL, INIT_VAL);
	static JSlider lowRed = new JSlider(JSlider.HORIZONTAL, MIN_VAL, MAX_VAL, INIT_VAL);
	static JSlider highBlue = new JSlider(JSlider.HORIZONTAL, MIN_VAL, MAX_VAL, INIT_VAL);
	static JSlider highGreen = new JSlider(JSlider.HORIZONTAL, MIN_VAL, MAX_VAL, INIT_VAL);
	static JSlider highRed = new JSlider(JSlider.HORIZONTAL, MIN_VAL, MAX_VAL, INIT_VAL);
	static JSlider nonZeroThresh = new JSlider(JSlider.HORIZONTAL, NONZERO_MIN, NONZERO_MAX, NONZERO_INIT);
	public static void main(String[] args) {
		System.out.println("MAIN");
		
		//required for openCV to work -call before any functions of oCV are used
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		
		//Define scalars, basically stored colors
		Red = new Scalar(0, 0, 255);
		Blue = new Scalar(255, 0, 0);
		Green = new Scalar(0, 255, 0);
		Yellow = new Scalar(0, 255, 255);
		//Our green thresholds
		//thresh_Lower = new Scalar(0,30,0);
		//thresh_Higher = new Scalar(70,255,70);
		//for grey tote
		grey_Lower = new Scalar(48,60,35);
		grey_higher = new Scalar(81,84,54);
		
		//NETWORKING INIT
		System.out.println("Networking init");
		NetworkTable.setClientMode();
		NetworkTable.setIPAddress("roboRIO-1719.local");
		table = NetworkTable.getTable("SmartDashboard");
		table.putBoolean("foundtarget", foundit);
		System.out.println("Window init");
		//WINDOW INIT
		//Close on exit
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		//Setup labels
		JLabel lowBlueL = new JLabel("Low Blue");
		JLabel lowGreenL = new JLabel("Low Green");
		JLabel lowRedL = new JLabel("Low Red");
		JLabel highBlueL = new JLabel("High Blue");
		JLabel highGreenL = new JLabel("High Green");
		JLabel highRedL = new JLabel("High Red");
		JLabel nonZeroL = new JLabel("Non-Zero Threshold");		
		
		//Add components to the frame
		//TODO Make the image viewable in swing
		frame.setLayout(new GridLayout(12,1));
		frame.add(lowBlueL);
		frame.add(lowBlue);
		frame.add(lowGreenL);
		frame.add(lowGreen);
		frame.add(lowRedL);
		frame.add(lowRed);
		frame.add(highBlueL);
		frame.add(highBlue);
		frame.add(highGreenL);
		frame.add(highGreen);
		frame.add(highRedL);
		frame.add(highRed);
		frame.add(nonZeroL);
		frame.add(nonZeroThresh);

		//Add labels to the frame
		//Sizing
		frame.pack();
		//Show the frame
		frame.setVisible(true);
		
		//main loop of the program
		
		while(true){
			try {
				
				while(/*table.isConnected()*/  true){
					processImage();
				}
			} catch (Exception e) {
				e.printStackTrace();
				break;
			}
		}
	}
	//opens a new connection to the Axis camera and opens a new snapshot "instance"
	public static void processImage(){
		try {
			System.out.println("processImage()");
			
			System.out.println((int)lowBlue.getValue());
			//NETWORKING
			//Connect to the camera
			//the url of the camera snapshot to save ##.## with your team number
			//Url url = new URL("http://10.##.##.11/axis-cgi/jpg/image.cgi");
			URL url = new URL("http://10.17.19.102/axis-cgi/jpg/image.cgi");
			URLConnection uc = url.openConnection();
			//Save the frame we just got from the camera, and write it to a file
			BufferedImage img = ImageIO.read((uc.getInputStream()));
			ImageIO.write(img, "png", new File("frame.png"));
			
			//Pull from our sliders
			//thresh_Lower = new Scalar(lowBlue.getValue(),lowGreen.getValue(),lowRed.getValue());
			//thresh_Higher = new Scalar(highBlue.getValue(),highGreen.getValue(),highRed.getValue());
			thresh_Lower = new Scalar(30,30,30);
			thresh_Higher = new Scalar(90,255,90);
			//PROCESSING
			//Create mats, which are basically matrixes for storing images
			Mat result = new Mat();
			Mat original = new Mat();
			Mat colors = new Mat();
			//Mat contours = new Mat();
			//Load frame.png into a mat
			original = Highgui.imread("frame.png");
			//Raise the contrast
			//contrast = raiseContrast(original);
			
			//Drop everything that isn't green
			Core.inRange(original, thresh_Lower, thresh_Higher, colors);
			
			//TODO Shaping?
			//Count the number of white pixels
			System.out.println("COUNT");
			int nonZeroThreshold = 100000;
			if(Core.countNonZero(colors) > nonZeroThreshold){
				System.out.println("FOUND IT");
				System.out.println("nonZeroCount " + Core.countNonZero(colors));
				System.out.println("nonZeroThresh" + nonZeroThreshold);
				System.out.println("B" + highBlue.getValue());
				System.out.println("G" + highGreen.getValue());
				System.out.println("R" + highRed.getValue());
				foundit = true;
			}else {
				System.out.println("KILL ME");
				System.out.println("nonZeroCount" + Core.countNonZero(colors));
				System.out.println("nonZeroThresh" + nonZeroThreshold);
				System.out.println("B" + highBlue.getValue());
				System.out.println("G" + highGreen.getValue());
				System.out.println("R" + highRed.getValue());
				foundit = false;
			}
			//Post results to smartdashboard
			table.putBoolean("foundtarget", foundit);
			result = colors;
			
			//WRITING
			//Put the crosshairs on the image
			//Core.line(result, new Point(result.width()/2,100),new Point(result.width()/2,result.height()-100), Blue);
			//Core.line(result, new Point(150,result.height()/2),new Point(result.width()-150,result.height()/2), Blue);
			//Write text on the image
			if(foundit){
			Core.putText(result, "Team 1719 - TARGET FOUND", new Point(0,20), 
					Core.FONT_HERSHEY_PLAIN, 1, Green);
			}else{
				Core.putText(result, "Team 1719 - TARGET LOST", new Point(0,20),
						Core.FONT_HERSHEY_PLAIN, 1, Red);
			}
			//Write the final mat to a file
			Highgui.imwrite("rectangle.png", result);
			//Highgui.imwrite("contrast.png", contrast);
			
		//mostly for debugging but errors happen
		} catch (Exception e) {
			e.printStackTrace();	
		}
	}
	
	
	public static Mat raiseContrast(Mat input){
		//Create some mats
		Mat ycrcb = input;
		Mat result = input;
		//Convert our input mat to the YCrCb colorspace
		Imgproc.cvtColor(input, ycrcb, Imgproc.COLOR_BGR2YCrCb);
		//Create an arraylist for storing the channels of the image
		ArrayList<Mat> channels = new ArrayList<Mat>();
		//Split our mat into the arrayList of channels
		Core.split(ycrcb, channels);
		//Convert the arrayList to an array
		Mat[] channelArr = new Mat[channels.size()];
		channelArr = channels.toArray(channelArr);
		//The contrast command
		Imgproc.equalizeHist(channelArr[0], channelArr[0]);
		//Convert the array back to an arrayList
		ArrayList<Mat> channelFinal = new ArrayList<Mat>(Arrays.asList(channelArr));
		//Merge the channels back into the mat
		Core.merge(channelFinal,ycrcb);
		//Convert the mat back to the BGR colospace
		Imgproc.cvtColor(ycrcb,result,Imgproc.COLOR_YCrCb2BGR);
		return result;
	}
	
	
	
	
	

	
	
	
	
	
	
	
	
	
	
	
	
	
}