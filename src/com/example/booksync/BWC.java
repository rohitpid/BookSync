package com.example.booksync;

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
	
	
	Comparator<featureSeg> xCompare = new Comparator<featureSeg>(){
		public int compare(featureSeg f1, featureSeg f2){
			if (f1.X > f2.X) return 1;
			else return -1;
		}
	};
	
	/////////////////
	// Constructor //
	/////////////////
	public BWC(textBoxData BWCData){
		data = BWCData;
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
		
		List<featureSeg> above = null;
		List<featureSeg> below = null;
		featureSeg intermed = null;
		featureBWC intermedFeat = null;
		Point p = null;
		
		List<Rect> WB = null;
		WB = data.wordBoxes;
		Rect box = null;
		Rect boxL = null;
		
		int xMin, yMin, xMax, yMax;
		int[] boxAbove;
		int[] boxBelow;
		int counter;
		
		int S = WB.size();
		
		int h = data.letterH;
		int w = data.letterW;
		
		// count relevant boxes
		for (int k=0; k<S; k++){
			boxAbove = null;
			boxBelow = null;
			counter = 0;
			box = WB.get(k);
			xMin = box.x;
			yMin = box.y;
			xMax = box.x + box.width;
			yMax = box.y + box.height;
			for (int l = 0; l<S; l++){
				if (k!=l){
					boxL = WB.get(l);
					if (Math.abs(yMax-boxL.y)<h && xMin<(boxL.x+boxL.width) && xMax>(boxL.x)){
						intermed.X = boxL.x;
						intermed.Length = boxL.width/w;
						below.add(intermed);
					}
					if (Math.abs(yMin-boxL.y-boxL.height)<h && xMin<(boxL.x+boxL.width) && xMax>boxL.x){
						intermed.X = boxL.x;
						intermed.Length = boxL.width/w;
						above.add(intermed);
					}
				}
			}
			if ((above.size()+below.size())>2 && above.size()>0 && below.size()>0){
				
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

	
