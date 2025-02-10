package com.example.mtrchecker;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.fragment.app.Fragment;

public class MTRMapFragment extends Fragment {
    public MTRMapFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mtr_map, container, false);

        ImageView mapImage = view.findViewById(R.id.mapImage);
        mapImage.setImageResource(R.drawable.mtr_routemap); // Ensure this image exists in res/drawable/

        return view;
    }
}
