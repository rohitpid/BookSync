package com.example.booksync;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Rect;
import org.opencv.core.Size;

import org.opencv.imgproc.*;

class textBoxData{
	Mat imgBINARY;
	Mat imgBINARYWORDS;
	
	int letterW;
	int letterH;
	
	List<Rect> wordBoxes;
}

public class textBox {
	
	private Mat imgBINARY;
	private Mat imgBINARYWORDS;
	
	private int letterW;
	private int letterH;
	
	private Rect boxIntermed;
	
	private List<Rect> wordBoxes;
	
	private textBoxData data;
	
	///////////////////
	// Constructor 1 //
	///////////////////
	public textBox(){
		InstantiateVals();
	}
	
	///////////////////
	// Constructor 2 //
	///////////////////
	public textBox(Mat img){
		imgBINARY = img;
		textIMGProcessing();
	}
	
	///////////////////
	// Get Results 1 //
	///////////////////
	public textBoxData getTextBoxData(Mat img){
		
		imgBINARY = img;
		
		textIMGProcessing();
		saveTextBoxData();
		
		return data;
	}
	
	///////////////////
	// Get Results 2 //
	///////////////////
	public textBoxData getTextBoxData(){

		saveTextBoxData();
		
		return data;
	}
	
	///////////////////////
	// Save textBox data //
	///////////////////////
	private void saveTextBoxData(){

		data.imgBINARY = imgBINARY.clone();
		data.imgBINARYWORDS = imgBINARYWORDS.clone();
		data.letterW = letterW;
		data.letterH = letterH;
		data.wordBoxes = wordBoxes;
	
	}
	
	///////////////////////////
	// Text Image Processing //
	///////////////////////////
	public void textIMGProcessing(){
		letterSIZE();
		connectedComponentAnalysis();
	}
	
	//////////////////////////
	// Identify letter size //
	//////////////////////////
	private void letterSIZE(){
		
		List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		Mat hierarchy = new Mat();
		MatOfPoint contour = new MatOfPoint();
		
		int[] widths = null;
		int[] heights = null;
		
		int s = 0;
		
		Imgproc.findContours(imgBINARY.clone(), contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
		
		if (!contours.isEmpty()){
			s = contours.size();
			widths = new int[s];
			heights = new int[s];
			for (int k = 0; k < s; k++){
				contour = contours.get(k);
				getContourRect(contour);
				widths[k] = boxIntermed.width;
				heights[k] = boxIntermed.height;
			}
		
			Arrays.sort(widths);
			Arrays.sort(heights);
		}
		letterW = widths[s/2];
		letterH = heights[s/2];
	}
	
	///////////////////////////////////////////////////////
	// Connected Component Analysis + Get Bounding Boxes //
	///////////////////////////////////////////////////////
	private void connectedComponentAnalysis(){
	
		Mat intermed = new Mat();
		List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		MatOfPoint contour = new MatOfPoint();
		Mat hierarchy = new Mat();
		double a = 0;
		Point p1 = new Point();
		Point p2 = new Point();
		Scalar s = new Scalar(150,150,150);

		// Dilate horizontally, erode vertically
		Mat SE = new Mat();
		int wantedVal = (int) Math.ceil((double)letterW/2);
		SE = Mat.ones(new Size(wantedVal,3), 0);
		Imgproc.morphologyEx(imgBINARY.clone(), intermed, Imgproc.MORPH_DILATE, SE);
		wantedVal = (int) Math.ceil((double)letterH/4);
		SE = Mat.ones(new Size(1,wantedVal), 0);
		Imgproc.morphologyEx(intermed, intermed, Imgproc.MORPH_ERODE, SE);
		
		// save result as grayscale image
		imgBINARYWORDS = intermed.clone();
		int T = imgBINARY.type();
		imgBINARYWORDS.convertTo(imgBINARYWORDS, T);
		
		// Get bounding boxes
		Imgproc.findContours(intermed, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
		if (!contours.isEmpty()){
			for (int k = 0; k < contours.size(); k++){
				contour = contours.get(k);
				a = Imgproc.contourArea(contour);
				if (a > 0.5*letterW*letterH && a < 30*letterW*letterH){
					getContourRect(contour);
					// Save wordbox data
					wordBoxes.add(boxIntermed);
					// Draw wordbox in imgBINARYWORDS
					p1.x = boxIntermed.x;
					p1.y = boxIntermed.y;
					p2.x = boxIntermed.x+boxIntermed.width;
					p2.y = boxIntermed.y+boxIntermed.height;
					Core.rectangle(imgBINARYWORDS,p1,p2,s,3);
				}
			}
		}
	
	}
	
	//////////////////////
	// Get contour size //
	//////////////////////
	private void getContourRect(MatOfPoint contour){
		

		Point[] points = contour.toArray();
		int nPoints = points.length;
		Point v = points[0];
		
		int xMax = (int) v.x;
		int xMin = (int) v.x;
		int yMax = (int) v.y;
		int yMin = (int) v.y;
		
		for (int i = 1;i<nPoints;i++){
			v = points[i];
			if (v.x < xMin) xMin = (int) v.x;
			if (v.x > xMax) xMax = (int) v.x;
			if (v.y < yMin) yMin = (int) v.y;
			if (v.y > yMax) yMax = (int) v.y;
		}

		boxIntermed.x = xMin;
		boxIntermed.y = yMin;
		boxIntermed.width = xMax - xMin;
		boxIntermed.height = yMax - yMin;
	}
	
	////////////////////////
	// Instantiate Values //
	////////////////////////
	private void InstantiateVals(){
		imgBINARY = new Mat();
		imgBINARYWORDS = new Mat();
		
		this.letterW = 0;
		this.letterH = 0;
		
		boxIntermed = new Rect();
		wordBoxes = new ArrayList<Rect>();
		
		data = new textBoxData();
	}
	
}
