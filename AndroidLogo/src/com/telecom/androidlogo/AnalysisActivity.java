package com.telecom.androidlogo;

import java.io.Console;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.features2d.DMatch;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;
import org.opencv.features2d.Features2d;
import org.opencv.features2d.KeyPoint;
import org.opencv.highgui.Highgui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

/*
vector<Point>    : MatOfPoint
vector<Point2f>  : MatOfPoint2f
vector<Point3i>  : MatOfPoint3
vector<Point3f>  : MatOfPoint3f
vector<KeyPoint> : MatOfKeyPoint
vector<DMatch>   : MatOfDMatch
vector<Rect>     : MatOfRect
vector<uchar>    : MatOfByte
vector<char>     : MatOfByte
vector<int>      : MatOfInt
vector<float>    : MatOfFloat
vector<double>   : MatOfDouble
vector<Vec4i>    : MatOfInt4
vector<Vec4f>    : MatOfFloat4
vector<Vec6f>    : MatOfFloat6
*/


public class AnalysisActivity extends Activity {		
	
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
		String PathToFile =  extras.getString("PathToImage");				
		Bitmap image = BitmapFactory.decodeFile(PathToFile);		
		imageViewResult.setImageBitmap(image);
		
		String PathToLogo = "/storage/emulated/0/DCIM/Camera/edflogo.jpg";
		
		int MIN_MATCH_THRESHOLD = 200;
		int MAX_MATCH_THRESHOLD = 500;
				
		    Mat refMat = Highgui.imread(PathToLogo, Highgui.IMREAD_GRAYSCALE);
		    Mat srcMat = Highgui.imread(PathToFile, Highgui.IMREAD_GRAYSCALE);

		    MatOfDMatch matches = new MatOfDMatch();
		    MatOfDMatch goodMatches = new MatOfDMatch();

		    LinkedList<DMatch> listOfGoodMatches = new LinkedList<DMatch>();

		    LinkedList<Point> refObjectList = new LinkedList<Point>();
		    LinkedList<Point> srcObjectList = new LinkedList<Point>();

		    MatOfKeyPoint refKeypoints = new MatOfKeyPoint();
		    MatOfKeyPoint srcKeyPoints = new MatOfKeyPoint();

		    Mat refDescriptors = new Mat();
		    Mat srcDescriptors = new Mat();

		    MatOfPoint2f reference = new MatOfPoint2f();
		    MatOfPoint2f source = new MatOfPoint2f();

		    FeatureDetector orbFeatureDetector = FeatureDetector.create(FeatureDetector.ORB);
		    orbFeatureDetector.detect(refMat, refKeypoints);
		    orbFeatureDetector.detect(srcMat, srcKeyPoints);

		    DescriptorExtractor descriptorExtractor = DescriptorExtractor.create(DescriptorExtractor.ORB);
		    descriptorExtractor.compute(refMat, refKeypoints, refDescriptors);
		    descriptorExtractor.compute(srcMat, srcKeyPoints, srcDescriptors);

		    DescriptorMatcher matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMING);
		    matcher.match(refDescriptors, srcDescriptors, matches);

		    double max_dist = 0;
		    double min_dist = 100;
		    List<DMatch> matchesList = matches.toList();

		    for (int i = 0; i < refDescriptors.rows(); i++) {
		        Double distance = (double) matchesList.get(i).distance;
		        if (distance < min_dist) min_dist = distance;
		        if (distance > max_dist) max_dist = distance;
		    }

		    for (int i = 0; i < refDescriptors.rows(); i++) {
		        if (matchesList.get(i).distance < 3 * min_dist) {
		            listOfGoodMatches.add(matchesList.get(i));
		        }
		    }

		    goodMatches.fromList(listOfGoodMatches);

		    List<KeyPoint> refObjectListKeypoints = refKeypoints.toList();
		    List<KeyPoint> srcObjectListKeypoints = srcKeyPoints.toList();

		    for (int i = 0; i < listOfGoodMatches.size(); i++) {
		        refObjectList.addLast(refObjectListKeypoints.get(listOfGoodMatches.get(i).queryIdx).pt);
		        srcObjectList.addLast(srcObjectListKeypoints.get(listOfGoodMatches.get(i).trainIdx).pt);
		    }

		    reference.fromList(refObjectList);
		    source.fromList(srcObjectList);

		    String result;
		    
		    Log.d(TAG, "listOfGoodMatches.size = " + listOfGoodMatches.size());
		    
		    if(listOfGoodMatches.size() > MIN_MATCH_THRESHOLD && listOfGoodMatches.size() < MAX_MATCH_THRESHOLD) {
		        result = "They MATCH!";
		    } else {
		        result = "They DON'T match!";
		    }

		    AlertDialog alert = new AlertDialog.Builder(this)
		            .setMessage(result)
		            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
		                @Override
		                public void onClick(DialogInterface dialog, int which) {
		                    // close
		                }
		            }).create();
		    alert.show();

		    Mat outputImage = new Mat();

		    Features2d.drawMatches(refMat, refKeypoints, srcMat, srcKeyPoints, goodMatches, outputImage);

		    Bitmap bitmap = Bitmap.createBitmap(outputImage.cols(), outputImage.rows(), Bitmap.Config.ARGB_8888);

		    Utils.matToBitmap(outputImage, bitmap);

		    imageViewResult.setImageBitmap(bitmap);		
		
		
		/*
		 FeatureDetector detector = FeatureDetector.create(FeatureDetector.ORB);
		    DescriptorExtractor descriptor = DescriptorExtractor.create(DescriptorExtractor.ORB);;
		    DescriptorMatcher matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMING);
		    
		    //first image
		    Mat img1 = Highgui.imread(PathToFile, Highgui.IMREAD_GRAYSCALE);
		    Mat descriptors1 = new Mat();
		    MatOfKeyPoint keypoints1 = new MatOfKeyPoint();
		    detector.detect(img1, keypoints1);
		    descriptor.compute(img1, keypoints1, descriptors1);
		    //second image
		    Mat img2 = Highgui.imread(PathToLogo, Highgui.IMREAD_GRAYSCALE);
		    Mat descriptors2 = new Mat();
		    MatOfKeyPoint keypoints2 = new MatOfKeyPoint();
		    detector.detect(img2, keypoints2);
		    descriptor.compute(img2, keypoints2, descriptors2);
		    //matcher should include 2 different image's descriptors
		    MatOfDMatch  matches = new MatOfDMatch();             
		    matcher.match(descriptors1,descriptors2,matches);
		    Log.d(TAG, "size " + matches.size());
		    //feature and connection colors
		    Scalar RED = new Scalar(255,0,0);
		    Scalar GREEN = new Scalar(0,255,0);
		    
		    //output image
		    Mat outputImg = new Mat();
		    MatOfByte drawnMatches = new MatOfByte();
		    //this will draw all matches, works fine
		    Features2d.drawMatches(img1, keypoints1, img2, keypoints2, matches, 
		            outputImg, GREEN, RED,  drawnMatches, Features2d.NOT_DRAW_SINGLE_POINTS);
		    
		    int DIST_LIMIT = 100;
		    
		    List<DMatch> matchList = matches.toList();
		    List<DMatch> matches_final = new ArrayList<DMatch>();
		    for(int i=0; i<matchList.size(); i++){
		        if(matchList.get(i).distance <= DIST_LIMIT){
		            matches_final.add(matches.toList().get(i));
		        }
		    }
		    MatOfDMatch matches_final_mat = new MatOfDMatch();
		    matches_final_mat.fromList(matches_final);
		    Bitmap bitmap = Bitmap.createBitmap(outputImg.cols(), outputImg.rows(), Bitmap.Config.ARGB_8888);
		    Utils.matToBitmap(outputImg, bitmap);
		    imageViewResult.setImageBitmap(bitmap);
		    
		*/
		
		
		
		
		
		
		
		
		
		
		
		/*
			
		//Log.d(TAG, imgpath);
		
		//recupération de l'image en 1 seul ligne
		imageViewResult.setImageBitmap(BitmapFactory.decodeFile((String) getIntent().getStringExtra("PathToImage")));	
					
		//image logo
		Bitmap logo = BitmapFactory.decodeFile(PathToLogo);
		
		
		
		
		FeatureDetector detector = FeatureDetector.create(FeatureDetector.ORB);
	    DescriptorExtractor descriptor = DescriptorExtractor.create(DescriptorExtractor.ORB);;
	    DescriptorMatcher matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMING);
	  
	    
	    //first image
	    Mat img1 = Highgui.imread(PathToFile);
	    Mat descriptors1 = new Mat();
	    MatOfKeyPoint keypoints1 = new MatOfKeyPoint();
	    detector.detect(img1, keypoints1);
	    descriptor.compute(img1, keypoints1, descriptors1);
	    //second image
	    Mat img2 = Highgui.imread(PathToLogo);
	    Mat descriptors2 = new Mat();
	    MatOfKeyPoint keypoints2 = new MatOfKeyPoint();
	    detector.detect(img2, keypoints2);
	    descriptor.compute(img2, keypoints2, descriptors2);
	    //matcher should include 2 different image's descriptors
	    MatOfDMatch  matches = new MatOfDMatch();             
	    matcher.match(descriptors1,descriptors2,matches);
	    //feature and connection colors
	    Scalar RED = new Scalar(255,0,0);
	    Scalar GREEN = new Scalar(0,255,0);
	    //output image
	    Mat outputImg = new Mat();
	    MatOfByte drawnMatches = new MatOfByte();
	    //this will draw all matches, works fine
	    Features2d.drawMatches(img1, keypoints1, img2, keypoints2, matches, 
	            outputImg, GREEN, RED,  drawnMatches, Features2d.NOT_DRAW_SINGLE_POINTS);
	    int DIST_LIMIT = 100;
	    List<DMatch> matchList = matches.toList();
	    List<DMatch> matches_final = new ArrayList<DMatch>();
	    for(int i=0; i<matchList.size(); i++){
	        if(matchList.get(i).distance <= DIST_LIMIT){
	            matches_final.add(matches.toList().get(i));
	        }
	    }
	    MatOfDMatch matches_final_mat = new MatOfDMatch();
	    matches_final_mat.fromList(matches_final);
	    //Utils.matToBitmap(outputImg, image);
		//imageViewResult.setImageBitmap(image);
	    
	    if (matches_final_mat.rows() >= 400) {
            AfficheToast("match found : " + matches_final_mat.rows());
        } else {
        	AfficheToast("match not found" + matches_final_mat.rows());
        }
	    
	    
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
	
	
	public void AfficheToast(String aff)
	{
		Toast.makeText(getApplicationContext(),aff, 
                Toast.LENGTH_SHORT).show();
		
	}
		
}