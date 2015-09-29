package com.telecom.androidlogo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.features2d.DMatch;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;
import org.opencv.features2d.KeyPoint;
import org.opencv.highgui.Highgui;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
		
		
		FeatureDetector detector = FeatureDetector.create(FeatureDetector.ORB);
		 DescriptorExtractor descriptor = DescriptorExtractor.create(DescriptorExtractor.ORB);;
		 DescriptorMatcher matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMING);

		 //first image
		 Mat img1 = Highgui.imread(PathToFile, Highgui.IMREAD_GRAYSCALE);
		 Mat descriptors1 = new Mat();
		 MatOfKeyPoint keypoints1 = new MatOfKeyPoint();

		 detector.detect(img1, keypoints1);
		 descriptor.compute(img1, keypoints1, descriptors1);

		 String Serveur = "http://195.154.75.125/descripteurs";

		 List<String> Logos = Getlogos(Serveur);
		 
		 int[] score = new int[Logos.size()];
		 String[] nom = new String[Logos.size()];
		 
		 int j = 0;
		 
		 for(String s : Logos )
		 {
	
			 Mat descriptors2 = ReadDescriptor(Serveur+"/"+s);
			 
			 //matcher should include 2 different image's descriptors
			 MatOfDMatch  matches = new MatOfDMatch();             
			 matcher.match(descriptors1,descriptors2,matches);
			 
			 
			 int DIST_LIMIT = 60;
			 
			 List<DMatch> matchesList = matches.toList();
			 List<DMatch> matches_final= new ArrayList<DMatch>();
			 for(int i=0; i<matchesList.size(); i++)
			 {
			    if(matchesList .get(i).distance <= DIST_LIMIT){
			        matches_final.add(matches.toList().get(i));
			    }
			 }
	
			 MatOfDMatch matches_final_mat = new MatOfDMatch();
			 
			 matches_final_mat.fromList(matches_final);	
			 
			 score [j] = matches_final.size();
			 nom[j] = s.substring(0, s.indexOf(".txt"));
			 
			 j++;
		 }		
		 
		 int[] tempscore = new int[score.length];
		 
		 for(int i = 0;i<score.length;i++)
		 {
			 tempscore[i]=score[i];
		 }
		 		 
		 Arrays.sort(tempscore);
		 
		 String[] tab_final = new String[tempscore.length];
		 
		 int x =0;
		 
		 for(int i = tempscore.length-1;i>=0;i--)
		 {
			 for(int y = 0;y<score.length;y++)
			 {
				 if(tempscore[i] == score[y]) tab_final[x] = nom[y] + " --> ";
			 }
			 
			 tab_final[x] += String.valueOf(tempscore[i]);
			 
			 x++;
		 }
		 
		 
		 AfficheToast( "The winners are :\n" + tab_final[0] + "\n" + tab_final[1] + "\n" + tab_final[2]);
		 
	}
	
	public List<String> Getlogos(String urlToRead) {
		
		List<String> logos = new LinkedList<String>();
		
		String temp = getHTML(urlToRead);
		
		while(temp.contains(".txt"))
		{
			int deb = temp.indexOf(".txt\">") + 6;
			int fin = temp.indexOf("</a>",deb);
			logos.add(temp.substring(deb,fin));
			temp = temp.substring(fin);
		}
		
		return logos;		
	}
	
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
	
	public Mat ReadDescriptor(String PathFile_or_URL)
	{	    
		String Acouper = "";

		if(PathFile_or_URL.contains("http"))
		{			
			if(isOnline())	Acouper = getHTML(PathFile_or_URL).trim().replaceAll("\n", "").replaceAll(" ", "");
			else return null;
		}
		else
		{
			Acouper = ReadXML(PathFile_or_URL).trim().replaceAll("\n", "").replaceAll(" ", "");

			if(Acouper.length() == 0) return null;
		}

		int ligne = CountChar(";", Acouper) + 1;

		if (Acouper.contains("[")) Acouper = Acouper.substring(1);//supprimer le '['

		String[] matrice = Acouper.split(";");	    
		int colonne = CountChar(",", matrice[0]) + 1;

		Mat des = new Mat(ligne,colonne,CvType.CV_8UC1);	  
		String[][] matrices = new String[ligne][colonne];//nombre de ";" +1

		for(int i = 0; i<matrice.length;i++)
		{
			int j = 0;

			for (String s : matrice[i].split(","))
			{
				if(s.contains("]")) s = s.substring(0,s.indexOf("]"));

				matrices[i][j] = s;
				j++;
			}
		}


		for(int row=0;row<ligne;row++)
		{
			for(int col=0;col<colonne;col++)
			{
				des.put(row, col, Double.parseDouble(matrices[row][col]));
			}
		}

		return des;
	}

	public boolean isOnline() {
		ConnectivityManager connMgr = (ConnectivityManager) 
				getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		return (networkInfo != null && networkInfo.isConnected());
	}
	
	public void WriteXMLDescriptor(String PathToXML,List<KeyPoint> des)
	{
		File f = new File (PathToXML);		
		
		try
		{
		    FileWriter fw = new FileWriter (f);

	    	String temp = "";
	    	
		    for (int i = 0; i < des.size(); i++) {
		    	
		    	temp += "<Keypoint>\n";
		    	temp += "\t<angle>"+des.get(i).angle+"</angle>\n";
		    	temp += "\t<response>"+des.get(i).response+"</response>\n";
		    	temp += "\t<size>"+des.get(i).size+"</size>\n";
		    	temp += "</Keypoint>\n";
		    } 
		    
	    	fw.write(temp);
		    fw.close();
		}
		catch (IOException exception)
		{
			Log.e("Descriptor", "Erreur lors de la lecture : " + exception.getMessage());
		}		
	}
	
	public List<KeyPoint> XMLToKeypoint(String PathToXML)
	{
		String AllKey = ReadXML(PathToXML);
		
		List<KeyPoint> retour = new LinkedList<KeyPoint>();

		try{
		
			while(AllKey.contains("<Keypoint>"))
			{
				retour.add(DecriptKeypoint(AllKey));				
				AllKey = AllKey.substring(AllKey.indexOf("</Keypoint>")+11);
			}		
		
		}		
		catch (Exception e){
			Log.e(TAG,e.toString());
		}
		
		return retour;
		
	}
	
	private KeyPoint DecriptKeypoint(String xmlToParse)
	{	
		KeyPoint k = new KeyPoint();
		k.angle = Float.valueOf(LitBaliseXML(xmlToParse,"angle"));
		k.response = Float.valueOf(LitBaliseXML(xmlToParse,"response"));
		k.size = Float.valueOf(LitBaliseXML(xmlToParse,"size"));
		
		return k;		
	}
	
	private String LitBaliseXML(String xmlToParse,String balise)
	{
		return xmlToParse.substring(xmlToParse.indexOf("<"+balise+">")+balise.length()+2,xmlToParse.indexOf("</"+balise+">"));		
	}
	
	
	private String ReadXML(String PathToXML)
	{
		String chaine;
		
	    chaine ="";
	    
	    try{
			InputStream ips=new FileInputStream(PathToXML); 
			InputStreamReader ipsr=new InputStreamReader(ips);
			BufferedReader br=new BufferedReader(ipsr);
			String ligne;
			while ((ligne=br.readLine())!=null){
				chaine+=ligne+"\n";
			}
			br.close(); 
		}		
		catch (Exception e){
			System.out.println(e.toString());
		}
	    
	    return chaine;
	}
	
	
	public void WriteDescriptor(String PathToFile)
	{
		Mat img1 = Highgui.imread(PathToFile, Highgui.IMREAD_GRAYSCALE);
		Mat descriptors1 = new Mat();
		MatOfKeyPoint keypoints1 = new MatOfKeyPoint();
		 
		FeatureDetector detector = FeatureDetector.create(FeatureDetector.ORB);
		DescriptorExtractor descriptor = DescriptorExtractor.create(DescriptorExtractor.ORB);
		detector.detect(img1, keypoints1);
		descriptor.compute(img1, keypoints1, descriptors1);		
		
		WriteDescriptor(PathToFile.substring(0, PathToFile.indexOf("."))+".txt",descriptors1);	
		
		AfficheToast(PathToFile.substring(0, PathToFile.indexOf("."))+".txt --> OK");
	}
	
	
	
	private void WriteDescriptor(String PathFile, Mat Descriptor)
	{		
		File f = new File (PathFile);
	    FileWriter fw;
		try {
			fw = new FileWriter (f);
	    	String temp = Descriptor.dump();		    
	    	fw.write(temp);
		    fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	private int CountChar(String RegEx, String chaine)
	{		
		Matcher match = Pattern.compile(RegEx).matcher(chaine);
		int occurence = 0;
		
		while(match.find()) occurence++;
		
		return occurence;		
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


