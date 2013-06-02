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
	Mat imgGRAY;
	Mat imgBINARY;
	Mat imgBINARYWORDS;
	
	int letterW;
	int letterH;
	
	List<Rect> wordBoxes;
}

public class textBox {
	
	private Mat imgGRAY;
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
	public textBox(Mat img, boolean isGray){
		if(isGray) imgGRAY = img;
		else Imgproc.cvtColor(img, imgGRAY, Imgproc.COLOR_BGR2GRAY);
		textIMGProcessing();
	}
	
	///////////////////
	// Get Results 1 //
	///////////////////
	public textBoxData getTextBoxData(Mat img, boolean isGray){
		if (isGray) imgGRAY = img;
		else Imgproc.cvtColor(img, imgGRAY, Imgproc.COLOR_BGR2GRAY);
		
		textIMGProcessing();
		
		//getTextBoxData();
		
		return data;
	}
	
	///////////////////
	// Get Results 2 //
	///////////////////
	public textBoxData getTextBoxData(){

		data.imgGRAY = imgGRAY;
		data.imgBINARY = imgBINARY;
		data.imgBINARYWORDS = imgBINARYWORDS;
		data.letterW = letterW;
		data.letterH = letterH;
		
		return data;
	}
	
	///////////////////////////
	// Text Image Processing //
	///////////////////////////
	public void textIMGProcessing(){
		morphGRAY2BIN();
		letterSIZE();
		connectedComponentAnalysis();
	}
	
	//////////////////////
	// Get binary image //
	//////////////////////
	// Morphological filtering + Otsu's method + get rid of vertical margins
	private void morphGRAY2BIN(){
		Size S = imgGRAY.size();
		int T = imgGRAY.type();
		Mat intermed = new Mat();
		Mat margins = new Mat();
		
		List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		MatOfPoint contour = new MatOfPoint();
		Mat hierarchy = new Mat();
		
		int maxX = 0;
		int minX = 0;
		int[] x = null;
		
		Mat matLeft = new Mat();
		Mat matRight = new Mat();
		List<Mat> matList = new ArrayList<Mat>();
		Mat andMat = new Mat();
		
		// Morphological Closing
		Imgproc.morphologyEx(imgGRAY, intermed, Imgproc.MORPH_CLOSE, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(20,20)));
		// Subtract Background
		Core.subtract(intermed, imgGRAY, intermed);
		// Otsu's Method
		Imgproc.adaptiveThreshold(intermed, intermed, 1, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 101, 0);
		// Dilation with vertical bar, l = S.height
		Imgproc.morphologyEx(intermed, margins, Imgproc.MORPH_DILATE, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(S.height,1)));
		// Get rid of margins
		Imgproc.findContours(imgGRAY, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
		if (!contours.isEmpty()){
			for (int k = 0;k<contours.size();k++){
				maxX = 0;
				minX = 0;
				contour = contours.get(k);
				getContourRect(contour);
				
				Size a = new Size();
				
				if (maxX < S.width/5){
					matLeft = Mat.zeros(new Size(boxIntermed.x + boxIntermed.width,S.height),T);
					matRight = Mat.ones(new Size(S.width-boxIntermed.x-boxIntermed.width,S.height), T);
					matList.add(matLeft);
					matList.add(matRight);
					Core.hconcat(matList,andMat);
					Core.multiply(andMat, intermed, intermed);
					matList.clear();

				}
				if (minX > S.width*4/5){
					matLeft = Mat.ones(new Size(S.width-boxIntermed.x,S.height),T);
					matRight = Mat.zeros(new Size(boxIntermed.x,S.height), T);
					matList.add(matLeft);
					matList.add(matRight);
					Core.hconcat(matList,andMat);
					Core.multiply(andMat, intermed, intermed);
					matList.clear();
				}
			}
		}
		imgBINARY = intermed;
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
		
		Imgproc.findContours(imgBINARY, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
		
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
		Size S = imgBINARY.size();
		List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		MatOfPoint contour = new MatOfPoint();
		Mat hierarchy = new Mat();
		double a = 0;
		Point p1 = new Point();
		Point p2 = new Point();
		Scalar s = new Scalar(.7, .7, .7);

		// Dilate horizontally, erode vertically
		Mat SE = new Mat();
		int wantedVal = (int) Math.ceil((double)letterW/2);
		SE = Mat.ones(new Size(wantedVal,2), 0);
		Imgproc.morphologyEx(imgBINARY, intermed, Imgproc.MORPH_DILATE, SE);
		wantedVal = (int) Math.ceil((double)letterH/4);
		SE = Mat.ones(new Size(2,wantedVal), 0);
		Imgproc.morphologyEx(imgBINARY, intermed, Imgproc.MORPH_ERODE, SE);
		//SE = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(Math.ceil((double)letterW/2),2), new Point(1,1));
		//Imgproc.morphologyEx(intermed, intermed, Imgproc.MORPH_CLOSE, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(letterH/4,2)));
		
		
		// save result as grayscale image
		imgBINARYWORDS.convertTo(intermed, imgGRAY.type());

		// Get bounding boxes
		//Imgproc.findContours(intermed, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
	/*if (!contours.isEmpty()){
			for (int k = 0; k < contours.size(); k++){
				contour = contours.get(k);
				a = Imgproc.contourArea(contour);
				if (a > 0.8*letterW*letterH && a < 30*letterW*letterH){
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
		*/
	}
	
	////////////////////////
	// Get contour height //
	////////////////////////
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
	
	private void InstantiateVals(){
		imgGRAY = new Mat();
		imgBINARY = new Mat();
		imgBINARYWORDS = new Mat();
		
		this.letterW = 0;
		this.letterH = 0;
		
		boxIntermed = new Rect();
		wordBoxes = new ArrayList<Rect>();
	}
	
}
