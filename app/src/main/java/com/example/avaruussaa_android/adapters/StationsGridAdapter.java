package com.example.avaruussaa_android.adapters;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.avaruussaa_android.R;
import com.example.avaruussaa_android.data.models.Station;
import com.example.avaruussaa_android.data.StationsData;

import java.util.ArrayList;

// This class is an ArrayAdapter for the GridView in StationsActivity. It takes a list of Stations
// and inflates it to a GridView consisting of TextViews which display the names of the stations.
// Clicking on a grid element calls StationsData.setCurrentStation() and finishes StationsActivity,
// returning the user to the main screen which now shows the data of the selected station.
// Code adapted from chaitanyamunje @ https://www.geeksforgeeks.org/gridview-in-android-with-example/
public class StationsGridAdapter extends ArrayAdapter<Station> {
    public StationsGridAdapter(@NonNull Context context, ArrayList<Station> stationsList) {
        super(context, 0, stationsList);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        View listItemView = convertView;
        if (listItemView == null) {
            // Layout Inflater inflates each item to be displayed in GridView.
            listItemView = LayoutInflater.from(getContext()).inflate(R.layout.stations_grid_item, parent, false);
        }

        // The dataset is a list of stations; get the station associated with the current element,
        // get the TextView of the element, and set its text as the station's name.
        Station station = getItem(position);
        TextView stationTv = listItemView.findViewById(R.id.grid_tv_element);
        stationTv.setText(station.name());

        // Set OnClick listener for the current grid element.
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