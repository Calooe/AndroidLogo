package com.telecom.androidlogo;

import java.io.Console;
import java.io.FileInputStream;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.FeatureDetector;
import org.opencv.features2d.Features2d;
import org.opencv.features2d.KeyPoint;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

public class AnalysisActivity extends FragmentActivity {		
	
	ImageView imageViewResult;
	
	private static final String TAG = "Analysis_Activity";	//pour les log debug
	
		
	static {			
		
		OpenCVLoader.initDebug();
		}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_analysis);	
		
		imageViewResult = (ImageView) findViewById(R.id.imageViewResult);		

		//recupération de l'image en 4 lignes
		Bundle extras = getIntent().getExtras();
		String imgpath =  extras.getString("PathToImage");				
		Bitmap image = BitmapFactory.decodeFile(imgpath);		
		imageViewResult.setImageBitmap(image);
		
		//recupération de l'image en 1 seul ligne
		imageViewResult.setImageBitmap(BitmapFactory.decodeFile((String) getIntent().getStringExtra("PathToImage")));	
					
		
		//analyse
		
		
		Mat inputMat = new Mat();
		Mat mRgba = inputMat.clone();
		Mat outputMat = new Mat();
		Utils.bitmapToMat(image, inputMat);
		MatOfKeyPoint points = new MatOfKeyPoint();
		
	//	points.fromList(listOfBestKeypoints);

		FeatureDetector fast = FeatureDetector.create(FeatureDetector.ORB);
		fast.detect(inputMat, points);

		
		Toast.makeText(getApplicationContext(),String.valueOf(points.size().toString()), 
                Toast.LENGTH_SHORT).show();
		
		
		
		
		/*
		 
		 List<KeyPoint> listOfKeypoints = points.toList();
			Collections.sort(listOfKeypoints, new Comparator<KeyPoint>() {
                    @Override
                    public int compare(KeyPoint kp1, KeyPoint kp2) {
                        // Sort them in descending order, so the best response KPs will come first
                        return (int) (kp2.response - kp1.response);
                    }
                });
				List<KeyPoint> listOfBestKeypoints = new ArrayList<KeyPoint>(listOfKeypoints.subList(0, 500));
				points.fromList(listOfBestKeypoints);
		
		*/
		
		
		
		
		
		/*
		Imgproc.cvtColor(inputMat, mRgba, Imgproc.COLOR_RGBA2RGB,4);
		Scalar color = new Scalar(DOT_COLOR_R, DOT_COLOR_G, DOT_COLOR_B);
		Features2d.drawKeypoints(mRgba, points, mRgba, color , 3);
		Imgproc.cvtColor(mRgba, outputMat, Imgproc.COLOR_RGB2RGBA);
		Utils.matToBitmap(outputMat, image);
		picture.setImageBitmap(image);*/
			
		
		/*Mat img_object = new Mat();
		Utils.bitmapToMat(image, img_object);

		Mat img_object_fin = new Mat();
		
		Imgproc.cvtColor(img_object, img_object_fin, Imgproc.COLOR_RGB2GRAY); //passage en gris
		
		
		
		Bitmap image2;
		image2 = Bitmap.createBitmap(image);
		
		Utils.matToBitmap(img_object_fin, image2);

		imageViewResult.setImageBitmap(image2);
		
		
		*/
		
				/*
		
		MatOfKeyPoint objectKeypoints = new MatOfKeyPoint();
		
		//Mat img_object = Highgui.imread(imgpath, Highgui.IMREAD_GRAYSCALE);
		
		Utils.bitmapToMat(image, img_object_fin);	
		
		
		FeatureDetector fd;
		fd = FeatureDetector.create(FeatureDetector.ORB);
		
		fd.detect(img_object_fin, objectKeypoints);
		
		Toast.makeText(getApplicationContext(),String.valueOf(objectKeypoints.size().toString()), 
                Toast.LENGTH_SHORT).show();
		*/
	}
		

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.analysis, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
		
}


