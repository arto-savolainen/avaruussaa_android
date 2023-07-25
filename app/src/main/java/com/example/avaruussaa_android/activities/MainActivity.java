package com.example.avaruussaa_android.activities;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.Manifest;

import com.example.avaruussaa_android.viewmodels.MainModel;
import com.example.avaruussaa_android.utils.Notifier;
import com.example.avaruussaa_android.R;

// This is the main / home screen of the app. It displays the magnetic activity of the currently selected weather station,
// and a rough estimate for the probability of northern lights occurring (none/low/high). Contains buttons which allow
// navigation to the settings screen (SettingsActivity) and the station selection screen (StationsActivity).
public class MainActivity extends AppCompatActivity {
    private static final String TAG = "maintag";
    // Register the permissions callback, which handles the user's response to the system permissions
    // dialog. Save the return value, an instance of ActivityResultLauncher, as an instance variable.
    // This doesn't work on older APIs but there notifications are enabled by default anyway, I think.
    private final ActivityResultLauncher<String> requestPermissionLauncher =
        registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            // No need for callback actions in this activity. SettingsActivity responds to permission changes.
        });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(TAG, "onCreate: MAINACTIVITY onCreate");

        // Register the view model for observing LiveData and add it as a Lifecycle observer.
        final MainModel viewModel = new ViewModelProvider(this).get(MainModel.class);
        getLifecycle().addObserver(viewModel);
        // Register Notifier to receive events for MainActivity lifecycle changes to determine when to send notifications.
        getLifecycle().addObserver(new Notifier());

        TextView activityView = findViewById(R.id.main_tv_activity_value);
        TextView probabilityView = findViewById(R.id.main_tv_probability);
        TextView timerView = findViewById(R.id.main_tv_timer);
        ImageButton settingsBtn = findViewById(R.id.main_btn_settings);
        Button stationBtn = findViewById(R.id.main_btn_station);
        ImageView backgroundImg = findViewById(R.id.main_iv_background);

        // From Android 13 and up we need to ask for permission to send notifications.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
        }

        // Create the observer which updates the station button text when the current station changes.
        viewModel.getName().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String newName) {
                stationBtn.setText(newName);
            }
        });

        // Create the observer which updates main_tv_activity_value when the activity for current station changes.
        viewModel.getActivity().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String newActivity) {
                activityView.setText(newActivity);

                // If the text is not the "loading" message or an error message, set its style to activity_big.
                if (!newActivity.contains(getString(R.string.main_loading_text))) {
                    activityView.setTextAppearance(R.style.activity_big);

                    // Color text according to the strength of magnetic activity.
                    try {
                        double activityDouble = Double.parseDouble(newActivity);
                        if (activityDouble < 0.3) {
                            probabilityView.setText(R.string.main_probability_text_quiet);
                            probabilityView.setTextAppearance(R.style.probability_quiet);
                            activityView.setTextColor(getColor(R.color.activity_blue));
                        } else if (activityDouble >= 0.3 && activityDouble < 0.4) {
                            probabilityView.setText(R.string.main_probability_text_low);
                            probabilityView.setTextAppearance(R.style.probability_low);
                            activityView.setTextColor(getColor(R.color.aurora_yellow));
                        } else if (activityDouble >= 0.4) {
                            probabilityView.setText(R.string.main_probability_text_high);
                            probabilityView.setTextAppearance(R.style.probability_high);
                            activityView.setTextColor(getColor(R.color.aurora_red));
                        }
                    } catch (NumberFormatException e) {
                        Log.e(TAG, "Parsing newActivity to double failed! EXCEPTION: " + e);
                    }
                }
            }
        });

        // Create the observer which updates main_tv_activity_value with an error msg if necessary.
        viewModel.getError().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String newError) {
                // If the error field of the current station contains a message, set its style to error_grey.
                if (newError.length() > 0) {
                    activityView.setText(newError);
                    activityView.setTextAppearance(R.style.error_grey);
                    probabilityView.setText(R.string.main_probability_text_quiet);
                    probabilityView.setTextAppearance(R.style.probability_quiet);
                }
            }
        });

        // Changes the opacity of the view's background image according to the "brightness" preference.
        viewModel.getBrightness().observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer newBrightness) {
                // Brightness is set via a SeekBarPreference and is between 0 and 100. Convert it to 0-255 for setImageAlpha().
                backgroundImg.setImageAlpha((int) Math.round(newBrightness * 2.55));
            }
        });

        // Observes LiveData in the view model to update the "Next update in mm:ss" text every second.
        viewModel.getTimerString().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String newTimerString) {
                timerView.setText(getString(R.string.main_timer_text, newTimerString));
            }
        });

        // Navigate to SettingsActivity when user presses the settings button.
        settingsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent startIntent = new Intent(getApplicationContext(), SettingsActivity.class);
                startActivity(startIntent);
            }
        });

        // Navigate to StationsActivity when user presses the station button.
        stationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent startIntent = new Intent(getApplicationContext(), StationsActivity.class);
                startActivity(startIntent);
            }
        });
    }
}