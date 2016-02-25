package com.example.logov2;

import java.io.File;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import android.app.Application;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.media.ExifInterface;
import android.graphics.Matrix;
import android.net.Uri;
import android.provider.MediaStore;

public class ImageUtility extends Application {
	
	final static String TAG = "ImageUtility";

	public static String getPath(Context ctx, Uri uri) {
		String valR = null;

		String[] projection = { MediaStore.Images.Media.DATA };
		Cursor cursor = ctx.getContentResolver().query(uri, projection, null, null, null);
		if (cursor != null) {
			cursor.moveToFirst();
			valR = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
		}
		cursor.close();
		return valR;
	}

	public static int getCameraPhotoOrientation(Context ctx, Uri imagePath) {
		int rotate = 0;
		try {
			File imageFile = new File(getPath(ctx, imagePath));

			ExifInterface exif = new ExifInterface(imageFile.getAbsolutePath());
			int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

			switch (orientation) {
			case ExifInterface.ORIENTATION_ROTATE_90:
				rotate = 90;
				break;
			case ExifInterface.ORIENTATION_ROTATE_180:
				rotate = 180;
				break;
			case ExifInterface.ORIENTATION_ROTATE_270:
				rotate = 270;
				break;
			default:
				break;
			}
		} catch (Exception e) {
			// ERROR
		}
		return rotate;
	}

	public static Bitmap cropCenterBitmap(Bitmap src) {
		int offsetX, offsetY, destWidth, destHeight;
		// Largeur > Hauteur
		if (src.getWidth() > src.getHeight()) {
			offsetX = (src.getWidth() / 2) - (src.getHeight() / 2);
			offsetY = 0;
			destWidth = destHeight = src.getHeight();

			// Hauteur > Largeur
		} else {
			offsetY = (src.getHeight() / 2) - (src.getWidth() / 2);
			offsetX = 0;
			destWidth = destHeight = src.getWidth();
		}
		return Bitmap.createBitmap(src, offsetX, offsetY, destWidth, destHeight);
	}

	public static Bitmap cropCenterBitmap(Bitmap src, int squareEdge) {
		// TODO Use of squareEdge for max Square Edge of the return Bitmap

		int offsetX, offsetY, destWidth, destHeight;
		// Largeur > Hauteur
		if (src.getWidth() > src.getHeight()) {
			offsetX = (src.getWidth() / 2) - (src.getHeight() / 2);
			offsetY = 0;
			destWidth = destHeight = src.getHeight();

			// Hauteur > Largeur
		} else {
			offsetY = (src.getHeight() / 2) - (src.getWidth() / 2);
			offsetX = 0;
			destWidth = destHeight = src.getWidth();
		}
		return Bitmap.createBitmap(src, offsetX, offsetY, destWidth, destHeight);
	}

	public static Bitmap RotateBitmap(Bitmap src, float angle) {
		Matrix matrix = new Matrix();
		matrix.postRotate(angle);
		return Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight(), matrix, true);
	}

	public static Uri getDrawableUri(Context ctx, int drawable) {
		Uri imageUri = Uri.parse(
				ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + ctx.getResources().getResourcePackageName(drawable)
						+ '/' + ctx.getResources().getResourceTypeName(drawable) + '/'
						+ ctx.getResources().getResourceEntryName(drawable));
		return imageUri;
	}

	public static Bitmap getDrawableBitmap(Context ctx, int logo) {
		Uri drawableUri = ImageUtility.getDrawableUri(ctx, logo);
		try {
			return MediaStore.Images.Media.getBitmap(ctx.getContentResolver(), drawableUri);
		} catch (Exception e) {
			// Error
			return null;
		}
	}

	public static Bitmap convertToGrayscale(Bitmap src) {
		Bitmap valR = Bitmap.createBitmap(src.getWidth(), src.getHeight(), Config.RGB_565);
		// Creation of a Mat
		Mat mat_IMG_SELECTED = new Mat(src.getHeight(), src.getWidth(), CvType.CV_8UC3);
		// Conversion of the Bitmap to a Mat
		Utils.bitmapToMat(src, mat_IMG_SELECTED);
		// Convert the image mat in colors to grayscale
		Imgproc.cvtColor(mat_IMG_SELECTED, mat_IMG_SELECTED, Imgproc.COLOR_RGB2GRAY);
		// Recreate a grayscale bitmap from the mat
		Utils.matToBitmap(mat_IMG_SELECTED, valR);
		return valR;
	}

	public static Mat convertToGrayscaleMat(Bitmap src) {
		// Creation of a Mat
		Mat mat_IMG_SELECTED = new Mat(src.getHeight(), src.getWidth(), CvType.CV_8UC3);
		// Conversion of the Bitmap to a Mat
		Utils.bitmapToMat(convertToGrayscale(src), mat_IMG_SELECTED);
		return mat_IMG_SELECTED;
	}
}
