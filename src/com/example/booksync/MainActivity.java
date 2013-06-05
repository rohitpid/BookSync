package com.example.booksync;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.MediaPlayer;
import android.net.Uri;
import android.opengl.Matrix;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import android.graphics.Bitmap.Config;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import com.googlecode.tesseract.android.TessBaseAPI;

public class MainActivity extends Activity {

    private static final int PICTURE_RESULT = 0;
    private ImageView imageView;
    private Uri outputFileUri;
    String APP_DIR = Environment.getExternalStorageDirectory()+"/BookSync/";

    // image reader
    jpgRead reader;
    // binaraize text
    binarizeText textBin;
    // get text box data
    textBox tBox;
    textBoxData imgData;
   // get BWC features
    bwcFeatures bwcProcessor;
    //ArrayList<bwcFeature> features;
    
    // image
    Mat img;
    
    // image & text viewer
    ImageView imgViewer;
	TextView txtViewer;
	
	// media player
	MediaPlayer mplayer;
	int mPlayerStart;
	
	// Training features
	ArrayList<ArrayList<bwcFeature>> trainingVectors;
	// Scores
	List<Scores> listOfScores;
    
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
		
        
        mplayer = new MediaPlayer();
        File sdCard = Environment.getExternalStorageDirectory();
        File audioSource = new File(sdCard.getAbsolutePath()+"/BookSync/GreatExpectationsUnabridgedPart1_mp332_chen.mo.david.mp3");
        mplayer.setOnSeekCompleteListener(new MediaPlayer.OnSeekCompleteListener() {
            @Override
            public void onSeekComplete(MediaPlayer mp) {
                // Start the song 30 seconds in
                mp.start();
            }
        });
        
        //mPlayerStart = 30000;
        //mplayer.seekTo(mPlayerStart);
        
        reader = new jpgRead();
        textBin = new binarizeText();
        tBox = new textBox();
        imgData = new textBoxData();
        bwcProcessor = new bwcFeatures();
        //features = new ArrayList<bwcFeature>();
        
        // Instantiate image and text viewers
        imgViewer = (ImageView) findViewById(R.id.imageView1);
		txtViewer = (TextView) findViewById(R.id.textView1);
        
		// Instantiate images
		img = new Mat();
		
		int k = 0;
		int i = 0;
		
		trainingVectors = new ArrayList<ArrayList<bwcFeature>>();
		listOfScores = new ArrayList<Scores>();
		
		//ArrayList<bwcFeature> features;
		
        // Train Features
	
		ArrayList<bwcFeature> features = new ArrayList<bwcFeature>();
		String txt2 = "";
		for (k = 0;k<10;k++){
		
			//String imgName = "/bookSync/test"+Integer.toString(k)+".jpg";
			String imgName = "/bookSync/test1.jpg";
			img = reader.getImg(imgName);
			// Get Binary Image
			img = textBin.getBINARYrotatedCROPPED(img,true);
			// Get Text Box Data
			imgData = new textBoxData();
			imgData = tBox.getTextBoxData(img);
			img = imgData.imgBINARYWORDS.clone();			// set image to binary words information
			// Get BWC Features
			features = bwcProcessor.getFeatures(new textBoxData(imgData.imgBINARY, imgData.imgBINARYWORDS, imgData.letterW, imgData.letterH, imgData.wordBoxes));
			txt2 = txt2 + "," + features.size();
			trainingVectors.add(features);
				
		}

		
		// Set Specific Image
		//String imgName = "/bookSync/test2.jpg";
		//img = reader.getImg(imgName);
		//img = textBin.getBINARYrotatedCROPPED(img,true);
		//imgData = tBox.getTextBoxData(img);
		//img = imgData.imgBINARYWORDS.clone();
		
		//ArrayList<bwcFeature> features = new ArrayList<bwcFeature>();
		//features = bwcProcessor.getFeatures(new textBoxData(imgData.imgBINARY, imgData.imgBINARYWORDS, imgData.letterW, imgData.letterH, imgData.wordBoxes));
		//features = bwcProcessor.getFeatures(imgData);
		
		//listOfScores = kNearestNeighbor(features.get(0), trainingVectors.get(0));
		
		//String txt = "listOfScores: "+Integer.toString(listOfScores.size()) + "trainingVectors[1]: "+Integer.toString(trainingVectors.get(0).size());
		
		//String txt = Integer.toString(features.size());
		//for (k=0;k<10;k++) txt = txt+","+Double.toString(features.get(k).vals[0]);
		
		String txt = "";
		for (k=0;k<10;k++) txt = txt+","+Integer.toString(trainingVectors.get(k).size());
		txt = txt + ";" + txt2;
		
		/*String txt = "";
		for (i=0;i < 10;i++){
			txt = txt +"; "+Integer.toString(listOfScores.get(i).page)+"."+Double.toString(listOfScores.get(i).score);
		}*/
		
		/*
		String txt = "";
		for (i = 0;i<10;i++){
			for (k = 0;k<5;k++){
				txt = txt + "," + Double.toString(features.get(i).vals[k]);
			}
			txt = txt + ";";
		}*/
		
     	// Display Image
		
     	Bitmap imgBMP = Bitmap.createBitmap((int) img.size().width, (int) img.size().height, Config.RGB_565);
     	Utils.matToBitmap(img, imgBMP);
     	imgViewer.setImageBitmap(imgBMP);
        txtViewer.setText(txt);
       		
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        this.imageView = (ImageView)this.findViewById(R.id.imageView1);
        return true;
    }
    
    /** Called when the user clicks the Open Camera Button */
    public void openCamera(View view) {
        // Do something in response to button
    	Intent camera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE); 
    	File file = new File(APP_DIR, "image.jpg");
    	outputFileUri = Uri.fromFile(file);
		camera.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
		//camera.putExtra("return-data", true);
        this.startActivityForResult(camera, PICTURE_RESULT);
    }
    
    public void openMediaPlayer(View view){
    	Intent intent = new Intent();  
    	intent.setAction(android.content.Intent.ACTION_VIEW);
    	File file = new File(APP_DIR+"GreatExpectations1.mp3");
    	System.out.println(file.toString());
    	intent.setDataAndType(Uri.fromFile(file), "audio/*");
    	startActivity(intent);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {  
        if (requestCode == PICTURE_RESULT && resultCode == RESULT_OK) {
        	System.out.println(outputFileUri.toString());
        	
        	//Read Photo from file saved on sdcard
        	Bitmap photo = BitmapFactory.decodeFile(APP_DIR+"image.jpg");
        	/*jpgRead reader = new jpgRead();
        	Mat img = reader.getImg("/BookSync/image1.jpg");*/
        	
        	//Bitmap photo = (Bitmap) data.getExtras().get("data");        	
        	//photo = photo.copy(Bitmap.Config.ARGB_8888, true);
        	
        	//Process image by binarizing
        	Mat cvIMG = new Mat();
        	Utils.bitmapToMat(photo, cvIMG);
        	Imgproc.cvtColor(cvIMG,cvIMG,Imgproc.COLOR_RGB2GRAY);
        	binarizeText textBin = new binarizeText();
        	Mat img = textBin.getBINARYrotatedCROPPED(cvIMG,true);
        	photo = Bitmap.createBitmap((int) img.size().width, (int)img.size().height, Config.ARGB_8888);
        	Utils.matToBitmap(img, photo);
        	
        	//Perform OCR
        	TessBaseAPI baseApi = new TessBaseAPI();
        	// DATA_PATH = Path to the storage
        	// lang for which the language data exists, usually "eng"
        	baseApi.init(APP_DIR, "eng");      	
        	baseApi.setImage(photo);
        	String recognizedText = baseApi.getUTF8Text();
        	System.out.println(recognizedText);
        	baseApi.end();
        	
        	// Display processed image and text
        	imageView.setImageBitmap(photo);
        	TextView text = (TextView) findViewById(R.id.textView1);
        	text.setText(recognizedText);
            
        }  
    }
	
	private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this){
		public void onManagerConnected(int status){
			switch (status){
			case LoaderCallbackInterface.SUCCESS:
			break;
			default:
			break;
			}
		}
	};
	
	static{
		if (!OpenCVLoader.initDebug()){}
		else {
			//System.loadLibrary("libopencv_highgui.a");
			//System.loadLibrary("my_jni_lib2");
		}
	}
	
	public void nextImage(View view){
	}
	
	public List<Double> normalizeVector(List<Double> ls){
		double sum = 0.0;
		for(int i=0;i<ls.size();i++){
			sum = sum+ls.get(i);
		}
		for(int i=0;i<ls.size();i++){
			ls.set(i, ls.get(i)/sum);
		}
		return ls;
	}
	
	public double euclidianDistance(bwcFeature feature1,bwcFeature feature2){
		double sum = 0.0;
		for(int i=0;i<feature1.vals.length;i++){
			sum = Math.pow(feature1.vals[i]-feature2.vals[i],2);
		}
		double distance = Math.sqrt(sum);
		return distance;
	}
	
	public List<Scores> kNearestNeighbor(bwcFeature testFeatures, List<bwcFeature> trainFeatures){
		List<Scores> rankedPages = new ArrayList<Scores>();
		int page = 0;
		double score = 0;
		for(int i=0;i<trainFeatures.size();i++){
			rankedPages.add(new Scores(euclidianDistance(testFeatures,trainFeatures.get(i)),i));
		}
		Collections.sort(rankedPages);
		return rankedPages;
	}
}
