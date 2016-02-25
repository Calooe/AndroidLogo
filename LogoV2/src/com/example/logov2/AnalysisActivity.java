package com.example.logov2;

import android.app.Activity;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Scanner;

import org.bytedeco.javacpp.Pointer;
import org.bytedeco.javacpp.opencv_core;
import org.json.JSONArray;


import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacpp.Pointer;
import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_core.CvFileStorage;
import org.bytedeco.javacpp.opencv_core.CvMemStorage;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_features2d;
import org.bytedeco.javacpp.opencv_ml.CvSVM;
import org.bytedeco.javacpp.opencv_nonfree.SIFT;
import org.bytedeco.javacpp.*;
import org.json.JSONException;
import org.json.JSONObject;


public class AnalysisActivity extends Activity {

	ImageView imageViewResult;

	String url = "http://www-rech.telecom-lille.fr/nonfreesift/";

	String PathToXML = "";
	

    private opencv_core.Mat MatVocabulary;
    
  
    
    //create SIFT feature point extracter // default parameters ""opencv2/features2d/features2d.hpp""
    final opencv_nonfree.SIFT detector = new opencv_nonfree.SIFT(0, 3, 0.04, 10, 1.6);
    //create a matcher with FlannBased Euclidien distance (possible also with BruteForce-Hamming)
    final opencv_features2d.FlannBasedMatcher matcher = new opencv_features2d.FlannBasedMatcher();
  //create BoF (or BoW) descriptor extractor
    final opencv_features2d.BOWImgDescriptorExtractor bowide = new opencv_features2d.BOWImgDescriptorExtractor(detector.asDescriptorExtractor(), matcher);

	
	
	private static final String TAG = "Analysis_Activity"; // pour les log debug

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_analysis);		

		imageViewResult = (ImageView) findViewById(R.id.imageViewResult);
		
		PathToXML = getApplicationContext().getFilesDir().getPath() + "/";

		String ResultJSON = getHTML(url + "index.json");

		try {

			JSONObject json = new JSONObject(ResultJSON);

			ArrayList<String> brandname = new ArrayList<String>();
			ArrayList<String> classifier = new ArrayList<String>();

			JSONArray arraybrands = json.getJSONArray("brands");

			String Vocabulaire = "" + json.get(json.names().getString(1));
			
			WriteXMLDescriptor(Vocabulaire, getHTML(url+Vocabulaire));
			
			
			opencv_core.CvFileStorage storage = opencv_core.CvFileStorage.open(getApplicationContext().getFilesDir().getPath() + "/vocabulary", null, 0);
            Pointer p = opencv_core.cvReadByName(storage, null, "vocabulary", opencv_core.cvAttrList());
            opencv_core.CvMat cvMat = new opencv_core.CvMat(p);
            MatVocabulary = new opencv_core.Mat(cvMat);
            opencv_core.cvReleaseFileStorage(storage);

            // set the dictionnary with the matVocabulary created
           bowide.setVocabulary(MatVocabulary);
            Log.d("matvocabulary", String.valueOf(MatVocabulary.rows()));

			
			
			

			
			for (int i = 0; i < arraybrands.length(); i++) {

				JSONObject row = arraybrands.getJSONObject(i);

				String br = row.getString("brandname");
				String clas = getHTML(url + "classifiers/" + row.getString("classifier"));
				
				AfficheToast("Br : " + br);
				AfficheToast("Clas : " + clas);

				brandname.add(br);
				classifier.add(clas);

				WriteXMLDescriptor(br + ".xml", clas);

			}

			// "http://www-rech.telecom-lille.fr/nonfreesift/classifiers/";

			/*
			 * //AfficheToast(Br);
			 * 
			 * JSONObject Brands = new JSONObject(Br);
			 * 
			 * String Voca = "" + json.get(json.names().getString(1));
			 * 
			 * Voca = getHTML(url+Voca);
			 * 
			 * //String[] classi = new String[Brands.length()];
			 * 
			 * 
			 * ArrayList<String> listdata = new ArrayList<String>();
			 * 
			 * if (Brands != null) { for (int i=0;i<Brands.length();i++){
			 * listdata.add(Brands.get();
			 * AfficheToast(listdata.get(i).toString()); } }
			 * 
			 * 
			 * 
			 * AfficheToast(Brands.toString());
			 * 
			 * AfficheToast(Brands.get("classifier").toString());
			 * 
			 * for(int i = 0; i<Brands.length();i++) { classi[i] =
			 * Brands.get("classifier").toString();
			 * 
			 * AfficheToast("i = " + i + " nom = " + classi[i]); }
			 */

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			AfficheToast(e.getMessage());
			e.printStackTrace();
		}

		// Log.d(TAG, test);

		// AfficheToast(test);

	}

	@SuppressWarnings("resource")
	public String getHTML(String urlToRead) {

		String out = "";

		try {
			out = new Scanner(new URL(urlToRead).openStream(), "UTF-8").useDelimiter("\\A").next();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return out;

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

	public void WriteXMLDescriptor(String name, String data) {
							
		File f = new File(PathToXML + name);
		
		AfficheToast(PathToXML);

		try {
			FileWriter fw = new FileWriter(f);
			fw.write(data);
			fw.close();
		} catch (IOException exception) {
			AfficheToast("Erreur lors de la lecture : " + exception.getMessage());
		}
	}

	public void AfficheToast(String aff) {
		Toast.makeText(getApplicationContext(), aff, Toast.LENGTH_SHORT).show();

	}

}