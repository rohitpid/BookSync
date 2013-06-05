package com.example.booksync;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Mat;
import org.opencv.core.Rect;

public class textBoxData {

	public Mat imgBINARY;
	public Mat imgBINARYWORDS;
	
	public int letterW;
	public int letterH;
	
	public List<Rect> wordBoxes;
	
	public textBoxData(Mat imgBINARYin, Mat imgBINARYWORDSin, int w, int h, List<Rect> wordBoxesIN){
		imgBINARY = imgBINARYin;
		imgBINARYWORDS = imgBINARYWORDSin;
		letterW = w;
		letterH = h;
		wordBoxes = wordBoxesIN;
	}
	
	public textBoxData(){
		imgBINARY = new Mat();
		imgBINARYWORDS = new Mat();
		letterW = 0;
		letterH = 0;
		wordBoxes = new ArrayList<Rect>();
	}
	
}
