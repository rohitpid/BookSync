package com.example.booksync;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Point;
import org.opencv.core.Rect;

public class bwcFeatures {

	
	private ArrayList<bwcFeature> features;
	private textBoxData data;
	
	/////////////////
	// Constructor //
	/////////////////
	public bwcFeatures(){
		data = new textBoxData();
		features = new ArrayList<bwcFeature>();
	}
	public bwcFeatures(textBoxData tData){
		data = tData;
		features = new ArrayList<bwcFeature>();
	}
	
	public ArrayList<bwcFeature> getFeatures(textBoxData tData){
		data = tData;
		features = new ArrayList<bwcFeature>();
		
		process();
		
		return features;
	}
	
	private void process(){
		
		Rect box = new Rect();
		Rect boxI = new Rect();
		int xMin = 0;
		int yMin = 0;
		int xMax = 0;
		int yMax = 0;
		
		int h = data.letterH;
		int w = data.letterW;
		
		int k = 0;
		int i = 0;
		
		List<Double> above;
		List<Double> below;
		List<Integer> aboveX = new ArrayList<Integer>();
		List<Integer> belowX = new ArrayList<Integer>();
	
		bwcFeature featureIntermed;
		
		double vals[] = {0,0,0,0,0};
		
		for (k = 0;k < data.wordBoxes.size();k++){
			box = data.wordBoxes.get(k);
			xMin = box.x;
			yMin = box.y;
			xMax = box.x + box.width;
			yMax = box.y + box.height;
			below = new ArrayList<Double>();
			above = new ArrayList<Double>();
			
			//featureIntermed = new bwcFeature();
			for (i = 0; (i < data.wordBoxes.size()) && i!=k;i++){
				
				boxI = data.wordBoxes.get(i);

				if ( (Math.abs(yMax-boxI.y)<1.5*(double)h) && (xMin<=(boxI.x+boxI.width) && xMax>=boxI.x) ) {
					if (above.size() == 0){
						aboveX.add(boxI.x);
						above.add((double)boxI.width/(double)boxI.height);
					}
					else if (above.size() == 1){
						if (aboveX.get(0) < boxI.x){
							aboveX.add(boxI.x);
							above.add((double)boxI.width/(double)boxI.height);
						}
						else{
							aboveX.add(aboveX.get(0));
							above.add(above.get(0));
							aboveX.set(0, boxI.x);
							above.set(0, (double)boxI.width/(double)boxI.height);
						}
						 
					}
					else if (above.size() > 1){
						
						if (aboveX.get(0) < boxI.x){
							aboveX.set(1, aboveX.get(0));
							above.set(1,above.get(0));
							aboveX.set(0, boxI.x);
							above.set(0, (double)boxI.width/(double)boxI.height);
						}
						else if (aboveX.get(1) < boxI.x){
							aboveX.set(1, boxI.x);
							above.set(1,(double)boxI.width/(double)boxI.height);
						}
					}
				}
				if ( (Math.abs(yMin-boxI.y-boxI.height)<1.5*(double)h) && (xMin<=(boxI.x+boxI.width) && xMax>=boxI.x) ) {
					if (below.size() == 0){
						belowX.add(boxI.x);
						below.add((double)boxI.width/(double)boxI.height);
					}
					else if (below.size() == 1){
						if (belowX.get(0) < boxI.x){
							belowX.add(boxI.x);
							below.add((double)boxI.width/(double)boxI.height);
						}
						else{
							belowX.add(belowX.get(0));
							below.add(below.get(0));
							belowX.set(0, boxI.x);
							below.set(0, (double)boxI.width/(double)boxI.height);
						}
						 
					}
					else if (below.size() > 1){
						
						if (belowX.get(0) < boxI.x){
							belowX.set(1, belowX.get(0));
							below.set(1,below.get(0));
							belowX.set(0, boxI.x);
							below.set(0, (double)boxI.width/(double)boxI.height);
						}
						else if (below.get(1) < boxI.x){
							belowX.set(1, boxI.x);
							below.set(1,(double)boxI.width/(double)boxI.height);
						}
					}
				}
				
			}
			
			if ((above.size() > 1) && (below.size() > 1)){
				vals[0] = above.get(0);
				vals[1] = above.get(1);
				vals[2] = box.width/box.height;
				vals[3] = below.get(0);
				vals[4] = below.get(1);
				
				features.add(new bwcFeature(vals.clone(),new Point(xMin,yMin)));
				
			}
			
		}
		
	}
	
}
