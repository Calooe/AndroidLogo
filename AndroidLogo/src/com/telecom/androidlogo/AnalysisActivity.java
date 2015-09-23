package com.telecom.androidlogo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;
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
		String XMLPath = "/storage/emulated/0/DCIM/xml/kfc.xml";
		
		int MIN_MATCH_THRESHOLD = 300;
		int MAX_MATCH_THRESHOLD = 500;
				
		    //Mat refMat = Highgui.imread(PathToLogo, Highgui.IMREAD_GRAYSCALE);
		    Mat srcMat = Highgui.imread(PathToFile, Highgui.IMREAD_GRAYSCALE);

		    MatOfDMatch matches = new MatOfDMatch();

		    LinkedList<DMatch> listOfGoodMatches = new LinkedList<DMatch>();
		    MatOfKeyPoint srcKeyPoints = new MatOfKeyPoint();

		    
		    
		    //Mat kfcDescriptors = new Mat();	    
		    Mat kfcDescriptors = ReadDescriptor("/storage/emulated/0/DCIM/xml/kfc.txt");
		    
		    Mat srcDescriptors = new Mat();


		    FeatureDetector orbFeatureDetector = FeatureDetector.create(FeatureDetector.ORB);
		    orbFeatureDetector.detect(srcMat, srcKeyPoints);

		    DescriptorExtractor descriptorExtractor = DescriptorExtractor.create(DescriptorExtractor.ORB);
		    descriptorExtractor.compute(srcMat, srcKeyPoints, srcDescriptors);	    
		    
		    //ecriture d'un descripteur
		    //WriteDescriptor("/storage/emulated/0/DCIM/xml/edf.txt", refDescriptors);
		    
		    DescriptorMatcher matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMING);
		    matcher.match(kfcDescriptors, srcDescriptors, matches);
		    	        
		
		    double max_dist = 0;
		    double min_dist = 100;
		    List<DMatch> matchesList = matches.toList();

		    for (int i = 0; i < kfcDescriptors.rows(); i++) {
		        Double distance = (double) matchesList.get(i).distance;
		        if (distance < min_dist) min_dist = distance;
		        if (distance > max_dist) max_dist = distance;
		    }

		    for (int i = 0; i < kfcDescriptors.rows(); i++) {
		        if (matchesList.get(i).distance < 3 * min_dist) {
		            listOfGoodMatches.add(matchesList.get(i));
		        }
		    }
		        
		    
		    //List<KeyPoint> refObjectListKeypoints = XMLToKeypoint(XMLPath);
		    
		    //WriteXMLDescriptor(XMLPath,refObjectListKeypoints); 
		    
		    AfficheToast("KFC = " + listOfGoodMatches.size());
		    
		    
		    
		    
		    //*********************************************************************************
		    
		    //2eme essai
		    
		    Mat edfDescriptors = ReadDescriptor("/storage/emulated/0/DCIM/xml/edf.txt");		    
		    MatOfDMatch matches2 = new MatOfDMatch();

		    LinkedList<DMatch> listOfGoodMatches2 = new LinkedList<DMatch>();

		    DescriptorMatcher matcher2 = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMING);		    
		    matcher2.match(edfDescriptors, srcDescriptors, matches2);
		    	        
		
		    max_dist = 0;
		    min_dist = 100;
		    List<DMatch> matchesList2 = matches2.toList();

		    for (int i = 0; i < edfDescriptors.rows(); i++) {
		        Double distance = (double) matchesList2.get(i).distance;
		        if (distance < min_dist) min_dist = distance;
		        if (distance > max_dist) max_dist = distance;
		    }

		    listOfGoodMatches2.clear();
		    
		    for (int i = 0; i < edfDescriptors.rows(); i++) {
		        if (matchesList2.get(i).distance < 3 * min_dist) {
		            listOfGoodMatches2.add(matchesList2.get(i));
		        }
		    }
		        		    
		    AfficheToast("EDF = " + listOfGoodMatches2.size());  
		    
		    
		    //***************** Fin 2eme essai  **************************************
		    
		    
		    
		    
		    /*
		    
		    String result;
		    
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

		    Mat outputImage = new Mat();*/

		    //Features2d.drawMatches(refMat, refKeypoints, srcMat, srcKeyPoints, goodMatches, outputImage);

		    //Bitmap bitmap = Bitmap.createBitmap(outputImage.cols(), outputImage.rows(), Bitmap.Config.ARGB_8888);

		    //Utils.matToBitmap(outputImage, bitmap);

		    //imageViewResult.setImageBitmap(bitmap);
		    
		    
			//-- prendre les coins de l'objet de image "train" (l'objet à "detecter" )  

		    
		  /*  for(MatOfPoint point : contours) {
		        MatOfPoint2f newPoint = new MatOfPoint2f(point.toArray());
		        newContours.add(newPoint);
		    }
		    
		    
		    
		    List<MatOfPoint2f> obj_corners = new ArrayList<MatOfPoint2f>();

		    MatOfPoint2f a = new MatOfPoint2f();
		    a.create(0,0,Point);
		    
		    MatOfPoint2f b = new MatOfPoint2f();
		    b.create(rows, cols, type);
		    
		    MatOfPoint2f c = new MatOfPoint2f();
		    c.create(rows, cols, type);
		    
		    MatOfPoint2f d = new MatOfPoint2f();
		    d.create(rows, cols, type);
		    
		    
		    
		    
			obj_corners.add(MatOfPoint2f.create(0, 0)); 
			obj_corners.add(MatOfPoint2f(refMat.cols(), 0));
			obj_corners.add(MatOfPoint2f(refMat.cols(), refMat.rows()));
			obj_corners.add(MatOfPoint2f(0, refMat.rows()));
			std::vector<Point2f> scene_corners(4);
			perspectiveTransform(obj_corners, scene_corners, H);

			//-- Dessiner les lignes entre les coins (l'objet mappé dans la scène image "query")  
			Point2f offset((float)img_object.cols, 0);
			line(img_matches, scene_corners[0] + offset, scene_corners[1] + offset, Scalar(0, 255, 0), 4);
			line(img_matches, scene_corners[1] + offset, scene_corners[2] + offset, Scalar(0, 255, 0), 4);
			line(img_matches, scene_corners[2] + offset, scene_corners[3] + offset, Scalar(0, 255, 0), 4);
			line(img_matches, scene_corners[3] + offset, scene_corners[0] + offset, Scalar(0, 255, 0), 4);
		    
		    */
		    
		
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
	
	
	public void WriteDescriptor(String PathFile, Mat Descriptor)
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
	
	public Mat ReadDescriptor(String PathFile)
	{	    
	    String Acouper = ReadXML(PathFile).trim().replaceAll("\n", "").replaceAll(" ", "");
	    
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


