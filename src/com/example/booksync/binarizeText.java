package com.example.booksync;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class binarizeText {

	private Mat imgGRAY;
	private Mat imgBINARY;
	private Mat imgBINARYrotated;
	private Mat imgBINARYrotatedCROPPED;
	private Rect boxIntermed;
	
	//////////////////
	// Constructors //
	//////////////////
	public binarizeText(){
		instantiate();
	}
	
	public binarizeText(Mat img, boolean isGray){
		instantiate();
		if(isGray) imgGRAY = img;
		else Imgproc.cvtColor(img, imgGRAY, Imgproc.COLOR_BGR2GRAY);
		binarize();
		imgBINARYrotated = imgBINARY;
		//rotate();
		crop();
	}
	
	// Get Binary Image //
	public Mat getBINARYrotatedCROPPED(Mat img, boolean isGray){
		if (isGray) imgGRAY = img;
		else Imgproc.cvtColor(img, imgGRAY, Imgproc.COLOR_BGR2GRAY);
		binarize();
		imgBINARYrotated = imgBINARY;
		crop();
		return imgBINARYrotatedCROPPED;
	}
	public Mat getBinary(){
		return imgBINARYrotatedCROPPED;
	}
	
	//////////////////////
	// Get binary image //
	//////////////////////
	// Morphological filtering + Otsu's method
	private void binarize(){
		Mat gray = imgGRAY.clone();
		Mat intermed = new Mat();

		List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		MatOfPoint contour = new MatOfPoint();
		Mat hierarchy = new Mat();

		// Morphological Closing
		Imgproc.morphologyEx(gray, intermed, Imgproc.MORPH_CLOSE, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(20,20)));
		// Subtract Background
		Core.subtract(intermed, gray, intermed);
		// Otsu's Method
		Imgproc.threshold(intermed.clone(), intermed, 0, 255, Imgproc.THRESH_OTSU);
		
		imgBINARY = intermed.clone();
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

	
	//////////
	// Crop //
	//////////
	private void crop(){
		
		Mat margins = new Mat();
		Mat hierarchy = new Mat();
		List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		
		Size S = imgBINARYrotated.size();
		
		MatOfPoint contour = new MatOfPoint();
		
		// Dilation with vertical bar, l = S.height
		Imgproc.morphologyEx(imgBINARYrotated.clone(), margins, Imgproc.MORPH_DILATE, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(1,(int)S.height/4)));

		// Get rid of margins
		Imgproc.findContours(margins, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
		if (!contours.isEmpty()){
			int xMin = 0;
			int xMax = (int) S.width-1;
			int minX = 0;
			int maxX = 0;
			for (int k = 0;k<contours.size();k++){
			contour = contours.get(k);
			getContourRect(contour);
			minX = boxIntermed.x;
			maxX = boxIntermed.x + boxIntermed.width;
			if ((maxX < S.width/4) & (maxX > xMin)) xMin = maxX;
			if ((minX > 3* S.width/4) & (minX < xMax)) xMax = minX;	
			}
		Rect rectCrop = new Rect(xMin,0,xMax-xMin,(int) S.height);

		imgBINARYrotatedCROPPED = new Mat(imgBINARYrotated.clone(),rectCrop);
		}
	}
	
	private void instantiate(){
		imgGRAY = new Mat();
		imgBINARY = new Mat();
		imgBINARYrotated = new Mat();
		imgBINARYrotatedCROPPED = new Mat();
		boxIntermed = new Rect();
	}
	
	////////////
	// Rotate //
	////////////
	private void rotate(){
		
		imgBINARYrotated = imgBINARY;
		
		//Mat lines = new Mat();
		//Mat dst = new Mat();
		
		//Imgproc.HoughLinesP(dst, lines, 1, Math.PI/360, 100);
	}
	
}
