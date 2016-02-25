package com.example.logov2;


import android.app.Fragment;
import android.graphics.Bitmap;
import android.os.Bundle;

public class RetainedFragment extends Fragment {

    // data object we want to retain
    private Bitmap image;

    // this method is only called once for this fragment
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // retain this fragment
        setRetainInstance(true);
    }

    public void setData(Bitmap data) {
        this.image = data;
    }

    public Bitmap getData() {
        return image;
    }
}
