package com.team3019.VisionCode;

import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;

import javax.imageio.ImageIO;

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
	static NetworkTable table;
	public static ArrayList<MatOfPoint> contourList = new ArrayList<>();
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
		thresh_Lower = new Scalar(0,0,0);
		thresh_Higher = new Scalar(70,255,70);
		//for grey tote
		grey_Lower = new Scalar(48,60,35);
		grey_higher = new Scalar(81,84,54);
		
		//NetworkTable.setClientMode();
		//NetworkTable.setIPAddress("roborio-1719.local");
		//table = NetworkTable.getTable("SmartDashboard");
		
		//main loop of the program
		
		while(true){
			try {
				
				while(/*table.isConnected()*/  true){
					processImage();
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				break;
			}
		}
	}
	//opens a new connection to the Axis camera and opens a new snapshot "instance"
	public static void processImage(){
		try {
			System.out.println("processImage()");
			
			//NETWORKING
			//Connect to the camera
			//the url of the camera snapshot to save ##.## with your team number
			//Url url = new URL("http://10.##.##.11/axis-cgi/jpg/image.cgi");
			URL url = new URL("http://10.17.19.102/axis-cgi/jpg/image.cgi");
			URLConnection uc = url.openConnection();
			//Save the frame we just got from the camera, and write it to a file
			BufferedImage img = ImageIO.read((uc.getInputStream()));
			ImageIO.write(img, "png", new File("frame.png"));
			
			
			//PROCESSING
			//Create mats, which are basically matrixes for storing images
			Mat result = new Mat();
			Mat original = new Mat();
			Mat contrast = new Mat();
			Mat colors = new Mat();
			//Mat contours = new Mat();
			//Load frame.png into a mat
			original = Highgui.imread("frame.png");
			
			//Raise the contrast
			contrast = raiseContrast(original);
			
			//Drop everything that isn't green
			Core.inRange(contrast, thresh_Lower, thresh_Higher, colors);
			
			//Count the number of white pixels
			if(Core.countNonZero(colors) > 70000){
				System.out.println("FOUND IT");
				System.out.println(Core.countNonZero(colors));
			}else {
				System.out.println("KILL ME");
				System.out.println(Core.countNonZero(colors));
			}
			
			
			result = colors;
			
			//WRITING
			//Put the crosshairs on the image
			Core.line(result, new Point(result.width()/2,100),new Point(result.width()/2,result.height()-100), Blue);
			Core.line(result, new Point(150,result.height()/2),new Point(result.width()-150,result.height()/2), Blue);
			//Write text on the image
			Core.putText(result, "Team 1719", new Point(0,20), 
					Core.FONT_HERSHEY_PLAIN, 1, Red);
			//Write the final mat to a file
			Highgui.imwrite("rectangle.png", result);
			Highgui.imwrite("contrast.png", contrast);
			
		//mostly for debugging but errors happen
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();	
		}
	}
	
	
	public static Mat raiseContrast(Mat input){
		//Create some mats
		Mat ycrcb = input;
		Mat result = input;
		//Convert our input mat to the YCrCb colorspace
		Imgproc.cvtColor(input, ycrcb, Imgproc.COLOR_BGR2YCrCb);
		//Create and arraylist for storing the channels of the image
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