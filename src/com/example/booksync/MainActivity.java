package com.example.booksync;

import java.io.File;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
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
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import com.googlecode.tesseract.android.TessBaseAPI;

public class MainActivity extends Activity {

    private static final int PICTURE_RESULT = 0;
    private ImageView imageView;
    private Uri outputFileUri;
    String APP_DIR = Environment.getExternalStorageDirectory()+"/BookSync/";

	jpgRead reader;
	Mat img;
	Bitmap imgBMP;
	ImageView imgViewer;
	textBoxData imgData;
	textBox tBox;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
		
		// Initialise
		imgViewer = (ImageView) findViewById(R.id.imageView1);
		reader = new jpgRead();
		tBox = new textBox();
		imgData = new textBoxData();
		
		// Read Image
		String imgName = APP_DIR+"GE_328_2.jpg";
		img = reader.getImg(imgName);
		imgBMP = Bitmap.createBitmap((int) img.size().width, (int) img.size().height, Config.RGB_565);
		Utils.matToBitmap(img, imgBMP);
		imgViewer.setImageBitmap(imgBMP);
		
		// Play with textBox
		//tBox.getTextBoxData(img, true);
		
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
        	Mat img = textBin.getBinary(cvIMG,true);
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
    
}
