package com.example.avaruussaa_android;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.ArrayList;

// Code adapted from https://www.geeksforgeeks.org/gridview-in-android-with-example/
public class StationsGridAdapter extends ArrayAdapter<Station> {
//    private Context context;
    public StationsGridAdapter(@NonNull Context context, ArrayList<Station> stationsList) {
        super(context, 0, stationsList);
//        this.context = context;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        View listItemView = convertView;
        if (listItemView == null) {
            // Layout Inflater inflates each item to be displayed in GridView.
            listItemView = LayoutInflater.from(getContext()).inflate(R.layout.stations_grid_item, parent, false);
        }

        Station station = getItem(position);
        TextView stationTv = listItemView.findViewById(R.id.grid_tv_element);
        stationTv.setText(station.name());

        stationTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String stationName = (String) ((TextView) v).getText();
                Log.d("stationstag", "onClick: stationName: " + stationName);
                StationsData.setCurrentStation(v.getContext(), stationName);
                ((Activity) v.getContext()).finish();
            }
        });

        return listItemView;
    }
}