package com.example.avaruussaa_android;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageButton;

import java.util.ArrayList;

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

        // Finish activity i.e. go back to main activity when user presses the back arrow button on top of the screen.
        ImageButton backBtn = findViewById(R.id.stations_btn_back);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}