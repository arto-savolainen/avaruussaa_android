package com.example.avaruussaa_android.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageButton;

import com.example.avaruussaa_android.R;
import com.example.avaruussaa_android.data.models.Station;
import com.example.avaruussaa_android.data.StationsData;
import com.example.avaruussaa_android.adapters.StationsGridAdapter;

import java.util.ArrayList;

// This activity displays the names of all available weather stations in a GridView. When the user clicks
// on a station name the adapter calls StationsData to set app state and finishes the activity, returning
// to the main screen which now displays the data of the selected station.
public class StationsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stations);

        // Get list of station names to put in the GridView.
        GridView stationsGrid = findViewById(R.id.stations_gridview);
        ArrayList<Station> stationsList = new ArrayList<>(StationsData.getDefaultStationsList(this));

        // Create adapter which transforms the list to TextViews and set it as the GridView's adapter.
        StationsGridAdapter adapter = new StationsGridAdapter(this, stationsList);
        stationsGrid.setAdapter(adapter);

        // Finish activity, i.e. go back to main activity when the user presses the back arrow button.
        ImageButton backBtn = findViewById(R.id.stations_btn_back);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}