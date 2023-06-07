package com.example.avaruussaa_android;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.os.Bundle;
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
        TextView activityView = findViewById(R.id.label_activity_value);
        ImageButton settingsBtn = findViewById(R.id.main_btn_settings);
        Button stationBtn = findViewById((R.id.main_btn_station));

        // Create the observer which updates main_btn_station when the current station changes
        viewModel.getName().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String newName) {
                stationBtn.setText(newName);
            }
        });

        // Create the observer which updates activityView when the activity for current station changes
        viewModel.getActivity().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String newActivity) {
                activityView.setText(newActivity);
            }
        });

        // Create the observer which updates activityView with an error msg if necessary
        viewModel.getError().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String newError) {
                if (newError.length() > 0) {
                    // TODO set activityView text style
                    activityView.setText(newError);
                    activityView.setTextAppearance(R.style.ErrorStyle);
                }
            }
        });



        // Observe the LiveData, passing in this activity as the LifecycleOwner and the observer.
//        viewModel.getStationName().observe(this, nameObserver);


        viewModel.getError().getValue();
        // TODO textView.setTextAppearance(R.style.style_two) < use this change style of activityView

        // Navigate to SettingsActivity when user presses the settings button
        settingsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent startIntent = new Intent(getApplicationContext(), SettingsActivity.class);
                startActivity(startIntent);
            }
        });
    }
}