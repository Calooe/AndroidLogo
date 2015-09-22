package com.telecom.androidlogo;

import org.opencv.android.OpenCVLoader;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

public class MainActivity extends Activity implements View.OnClickListener{

	Button buttonCapture;
	Button buttonGallery;
	Button buttonAnalysis;
	ImageView imageview;
	Bitmap image;

	String imgPath;
	
	final int IMAGE_CAPTURE = 1;
	final int IMAGE_GALLERY = 2;
	
	private RetainedFragment dataFragment;
	 
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);	

		
		buttonCapture = (Button) findViewById(R.id.buttonCapture);
		buttonGallery = (Button) findViewById(R.id.buttonGallery);
		buttonAnalysis = (Button) findViewById(R.id.buttonAnalysis);
		
		
		imageview = (ImageView) findViewById(R.id.imageView);
		
		buttonCapture.setOnClickListener(this);
		buttonGallery.setOnClickListener(this);
		buttonAnalysis.setOnClickListener(this);	
		
		
		FragmentManager fm = getFragmentManager();
        dataFragment = (RetainedFragment) fm.findFragmentByTag("data");
        
        // create the fragment and data the first time
        if (dataFragment == null) {
            // add the fragment
            dataFragment = new RetainedFragment();
            fm.beginTransaction().add(dataFragment, "data").commit();
            // load the data from the web
            dataFragment.setData(image);	
        }
        else
        {
        	image = dataFragment.getData();
        	imageview.setImageBitmap(image);
        }        					
	}
	
	public void onClick(View v) {
		
		if(v == buttonCapture)
		{
			startCatpureActivity();
		}
		else if (v == buttonGallery)
		{
			GoGallery();
		}
		else if (v == buttonAnalysis)
		{
			startAnalysis();
		}
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
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
	
	
	 @Override
	    public void onDestroy() {
	        super.onDestroy();
	        // store the data in the fragment
	        dataFragment.setData(image);
	    }

	
	protected void startCatpureActivity()
	{
		Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		startActivityForResult(captureIntent,IMAGE_CAPTURE);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{		
		super.onActivityResult(requestCode, resultCode, data);
		
		if(requestCode==IMAGE_CAPTURE && resultCode == RESULT_OK)
		{
			Bundle extra = data.getExtras();
			Bitmap image = (Bitmap) extra.get("data");
			
			imageview.setImageBitmap(image);
		}
		else if(requestCode==IMAGE_GALLERY && resultCode == RESULT_OK)
		{
			//the returned picture URI
			Uri pickedUri = data.getData();
			
			String[] medData = { MediaStore.Images.Media.DATA };			
			Cursor picCursor = getContentResolver().query(pickedUri, medData, null, null, null);
			
			
			if(picCursor!=null)
			{
			    //get the path string
			    int index = picCursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
			    picCursor.moveToFirst();
			    imgPath = picCursor.getString(index);
			}
			else
			imgPath = pickedUri.getPath();			

			//get the file as a bitmap
			image = BitmapFactory.decodeFile(imgPath);
			
			imageview.setImageBitmap(image);
			
		}
	}
	
	protected void GoGallery()
	{
		
		Intent galleryIntent = new Intent();
		galleryIntent.setType("image/*");
		galleryIntent.setAction(Intent.ACTION_PICK);
		//we will handle the returned data in onActivityResult
		startActivityForResult(galleryIntent, IMAGE_GALLERY);
	}
	
	protected void startAnalysis()
	{
		Intent AnalyseIntent = new Intent(this,AnalysisActivity.class);
		AnalyseIntent.putExtra("PathToImage", imgPath);
		startActivity(AnalyseIntent);
		
	}
	
}
