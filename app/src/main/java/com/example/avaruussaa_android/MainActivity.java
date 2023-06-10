package com.example.avaruussaa_android;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final MainModel viewModel = new ViewModelProvider(this).get(MainModel.class);
        TextView activityView = findViewById(R.id.main_tv_activity_value);
        ImageButton settingsBtn = findViewById(R.id.main_btn_settings);
        Button stationBtn = findViewById((R.id.main_btn_station));

        // Create the observer which updates main_btn_station Button when the current station changes
        viewModel.getName().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String newName) {
                stationBtn.setText(newName);
            }
        });

        // Create the observer which updates main_tv_activity_value TextView when the activity for current station changes
        viewModel.getActivity().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String newActivity) {
                activityView.setText(newActivity);

                if (!newActivity.contains(getResources().getString(R.string.main_loading_text))) {
                    activityView.setTextAppearance(R.style.activity_big);
                }
            }
        });

        // Create the observer which updates main_tv_activity_value TextView with an error msg if necessary
        viewModel.getError().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String newError) {
                if (newError.length() > 0) {
                    activityView.setText(newError);
                    activityView.setTextAppearance(R.style.error_grey);
                }
            }
        });

        // Navigate to SettingsActivity when user presses the settings button
        settingsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent startIntent = new Intent(getApplicationContext(), SettingsActivity.class);
                startActivity(startIntent);
            }
        });

        // Navigate to StationsActivity when user presses the station button
        stationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent startIntent = new Intent(getApplicationContext(), StationsActivity.class);
                startActivity(startIntent);
            }
        });
    }
}