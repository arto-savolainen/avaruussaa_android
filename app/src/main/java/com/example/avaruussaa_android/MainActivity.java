package com.example.avaruussaa_android;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationManagerCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.Manifest;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "maintag";
    // Register the permissions callback, which handles the user's response to the system permissions
    // dialog. Save the return value, an instance of ActivityResultLauncher, as an instance variable.
    // This doesn't work on older APIs but who cares. Notifications are enabled by default anyway, afaik.
    private final ActivityResultLauncher<String> requestPermissionLauncher =
        registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            // No need for callback actions in this activity. SettingsActivity responds to permission changes.
        });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Register Notifier to receive events for MainActivity lifecycle changes to determine when to send notifications.
        getLifecycle().addObserver(new Notifier());

        final MainModel viewModel = new ViewModelProvider(this).get(MainModel.class);
        TextView activityView = findViewById(R.id.main_tv_activity_value);
        TextView probabilityView = findViewById(R.id.main_tv_probability);
        ImageButton settingsBtn = findViewById(R.id.main_btn_settings);
        Button stationBtn = findViewById(R.id.main_btn_station);
        ImageView backgroundImg = findViewById(R.id.main_iv_background);

        if (NotificationManagerCompat.from(this).areNotificationsEnabled()) {
            Log.d(TAG, "onCreate: CAN USE NOTIFICATIONS! WOOT!");

        } else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
            Log.d(TAG, "onCreate: SHOULD SHOW REQUEST PERMISSION RATIONALE! WHATEVER LOL");
            // If this method returns true, show an educational UI to the user. In this UI, describe why the feature that the user wants to enable needs a particular permission.
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
        } else {
            // You can directly ask for the permission.
            // The registered ActivityResultCallback gets the result of this request.
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
        }

        // Create the observer which updates main_btn_station Button when the current station changes.
        viewModel.getName().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String newName) {
                stationBtn.setText(newName);
            }
        });

        // Create the observer which updates main_tv_activity_value TextView when the activity for current station changes.
        viewModel.getActivity().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String newActivity) {
                activityView.setText(newActivity);

                // If the text is not the "loading" message or an error message, set its style to activity_big.
                if (!newActivity.contains(getResources().getString(R.string.main_loading_text))) {
                    activityView.setTextAppearance(R.style.activity_big);

                    // Color text according to the strength of magnetic activity.
                    try {
                        double activityDouble = Double.parseDouble(newActivity);
                        if (activityDouble < 0.3) {
                            probabilityView.setText(R.string.main_probability_text_quiet);
                            probabilityView.setTextAppearance(R.style.probability_quiet);
                        } else if (activityDouble >= 0.3 && activityDouble < 0.5) {
                            probabilityView.setText(R.string.main_probability_text_low);
                            probabilityView.setTextAppearance(R.style.probability_low);
                            activityView.setTextColor(getColor(R.color.aurora_yellow));
                        } else if (activityDouble >= 0.5) {
                            probabilityView.setText(R.string.main_probability_text_high);
                            probabilityView.setTextAppearance(R.style.probability_high);
                            activityView.setTextColor(getColor(R.color.aurora_red));
                        }
                    } catch (NumberFormatException e) {
                        Log.e(TAG, "Parsing newActivity to double failed! EXCEPTION: " + e);
                        e.printStackTrace();
                    }
                }
            }
        });

        // Create the observer which updates main_tv_activity_value TextView with an error msg if necessary.
        viewModel.getError().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String newError) {
                // If the error field of the current station contains a message, set its style to error_grey.
                if (newError.length() > 0) {
                    activityView.setText(newError);
                    activityView.setTextAppearance(R.style.error_grey);
                }
            }
        });

        viewModel.getBrightness().observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer newBrightness) {
                // Brightness is set via a SeekBarPreference and is between 0 and 100. Convert it to 0-255 for setImageAlpha().
                backgroundImg.setImageAlpha((int) Math.round(newBrightness * 2.55));
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