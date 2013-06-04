package com.example.booksync;

import java.io.File;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.os.Environment;
import android.widget.ImageView;

public class jpgRead {

	private Mat imgGray;
	
	//////////////////
	// Constructors //
	//////////////////
	public jpgRead(String imgName){
		jpgReadProcess(imgName);
	}
	
	public jpgRead(){
		imgGray = Mat.zeros(new Size(10,10), 0);
	}
	
	///////////////
	// Get Image //
	///////////////
	public Mat getImg(){
		return imgGray;
	}
	
	public Mat getImg(String imgName){
		jpgReadProcess(imgName);
		return imgGray;
	}
	
	///////////////
	// Set Image //
	///////////////
	public void setImg(String imgName){
		jpgReadProcess(imgName);
	}
	
	////////////////
	// Open Image //
	////////////////
	private void jpgReadProcess(String imgName){
		
		// Find Image
		File sdCard = Environment.getExternalStorageDirectory();
		File imgFile = new File(sdCard.getAbsolutePath()+imgName);
		if(imgFile.exists()) imgGray = Highgui.imread(imgFile.getAbsolutePath(),0);
		else imgGray = Mat.zeros(new Size(10,10), 0);
		
		// Resize Image
		Imgproc.resize(imgGray, imgGray, new Size(640.0,480.0));
	}

}
