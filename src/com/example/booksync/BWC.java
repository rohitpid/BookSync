package com.example.booksync;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.opencv.core.*;

class featureSeg{
	int Length;
	int X;
}

class featureBWC{
	List<featureSeg> Above;
	List<featureSeg> Below;
	int wordLength; 
	Point wordPos;
}

public class BWC {

	private textBoxData data;
	private List<featureBWC> features;
	
	////////////////////////////
	// Comparator for sorting //
	////////////////////////////
	Comparator<featureSeg> xCompare = new Comparator<featureSeg>(){
		public int compare(featureSeg f1, featureSeg f2){
			if (f1.X > f2.X) return 1;
			else return -1;
		}
	};
	
	///////////////////
	// Constructor 1 //
	///////////////////
	public BWC(){
		features = new ArrayList<featureBWC>();
		data = new textBoxData();
	}

	///////////////////
	// Constructor 2 //
	///////////////////
	public BWC(textBoxData BWCData){
		data = BWCData;
		features = new ArrayList<featureBWC>();
		detectFeatures();
	}
	
	////////////////////////
	// Get Feature Values //
	////////////////////////
	public List<featureBWC> getFeatures(){
		return features;
	}
	
	////////////////////////
	// Get Feature Values //
	////////////////////////
	public List<featureBWC> getFeatures(textBoxData BWCData){
		data = BWCData;
		detectFeatures();
		return features;
	}
	
	//////////////////////
	// Feature Detector //
	//////////////////////
	private void detectFeatures(){
		
		List<featureSeg> above = new ArrayList<featureSeg>();
		List<featureSeg> below = new ArrayList<featureSeg>();
		featureSeg intermed = new featureSeg();
		featureBWC intermedFeat = new featureBWC();
		Point p = new Point();
		
		List<Rect> WB = new ArrayList<Rect>();
		WB = data.wordBoxes;
		Rect box = new Rect();
		Rect boxL = new Rect();
		
		int xMin, yMin, xMax, yMax = 0;
		
		int S = WB.size();
		
		int h = data.letterH;
		int w = data.letterW;
		
		// count relevant boxes
		for (int k=0; k<S; k++){
			box = WB.get(k);
			xMin = box.x;
			yMin = box.y;
			xMax = box.x + box.width;
			yMax = box.y + box.height;
			for (int l = 0; l<S; l++){
				if (k!=l){
					boxL = WB.get(l);
					if (Math.abs(yMax-boxL.y)<3*h/4 && xMin<(boxL.x+boxL.width) && xMax>(boxL.x)){
						intermed.X = boxL.x;
						intermed.Length = boxL.width/boxL.height;
						below.add(intermed);
					}
					if (Math.abs(yMin-boxL.y-boxL.height)<3*h/4 && xMin<(boxL.x+boxL.width) && xMax>boxL.x){
						intermed.X = boxL.x;
						intermed.Length = boxL.width/boxL.height;
						above.add(intermed);
					}
				}
			}
			if ((above.size()+below.size())>2 && above.size()>0 && below.size()>0){
			//if ((above.size()+below.size())==5){
			
				Collections.sort(above,xCompare);
				Collections.sort(below,xCompare);
				p.x = xMin;
				p.y = yMin;
				
				intermedFeat.Above = above;
				intermedFeat.Below = below;
				intermedFeat.wordPos = p;
				intermedFeat.wordLength = box.width/w;
				
				features.add(intermedFeat);
			}
			
			
		}
		
	}
	
}
