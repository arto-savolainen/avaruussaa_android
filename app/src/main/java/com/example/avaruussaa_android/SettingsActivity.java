package com.example.avaruussaa_android;

import android.Manifest;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationManagerCompat;
import androidx.preference.EditTextPreference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SeekBarPreference;
import androidx.preference.SwitchPreferenceCompat;

// Android Preferences automatically implement the logic of saving settings changes to SharedPreferences.
// So I didn't have to bother with a view model for this view.
public class SettingsActivity extends AppCompatActivity {
    private static final String TAG = "settingstag";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, new SettingsFragment())
                .commit();
        }
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        // Finish activity, i.e. go back to main activity when user presses the back arrow button on top of the screen.
        ImageButton backBtn = findViewById(R.id.settings_btn_back);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (!isGranted) {
                SwitchPreferenceCompat notificationSwitch = findPreference("notifications");
                Log.d(TAG, "onActivityResult: PERMISSION DENIED, notificationSwitch: " + notificationSwitch);
                if (notificationSwitch != null) {
                    notificationSwitch.setChecked(false);
                    showPermissionToast();
                }
            }
        });

        private void showPermissionToast() {
            Toast.makeText(getContext(), getResources().getString(R.string.settings_toast_permission), Toast.LENGTH_LONG).show();
        }

        // Sets an EditTextPreference's inputType to a decimal number.
        private void setInputTypeToNumber(@NonNull EditTextPreference preference) {
            preference.setOnBindEditTextListener(editText -> editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL));
        }

        // Overrides onPreferenceChange of an EditTextPreference to truncate user input numbers to five digits.
        private void setValidatorFunction(@NonNull EditTextPreference preference) {
            preference.setOnPreferenceChangeListener((p, newValue) -> {
                String newValueString = (String) newValue;
                // Remove leading and trailing zeroes from the input and truncate it.
                newValueString = Utils.formatNumberString(newValueString, getResources().getInteger(R.integer.decimal_numeral_max_length));

                // Set the truncated string as the value of EditTextPreference and return false to discard the original input.
                preference.setText(newValueString);
                return false;
            });
        }

        // When user changes the Notifications setting set the SwitchPreferenceCompat icon as appropriate.
        // Also ask for notification permission if it has not been granted and the setting was set to true.
        private void setNotificationSwitchListener(@NonNull SwitchPreferenceCompat notificationSwitch) {
            notificationSwitch.setOnPreferenceChangeListener((preference, newValue) -> {
                Boolean newValueBoolean = (Boolean) newValue;
                setNotificationIcon(notificationSwitch, newValueBoolean);

                if (newValueBoolean && getContext() != null && !NotificationManagerCompat.from(getContext()).areNotificationsEnabled()) {
                    Log.d(TAG, "onCreate: CAN NOT USE NOTIFICATIONS ASKING PERMISSIONS");
                    if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                        Log.d(TAG, "onCreate: we should Show Request Permission Rationale");
                    }

                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
                }
                else if (!newValueBoolean && getContext() != null && NotificationManagerCompat.from(getContext()).areNotificationsEnabled()) {
                    notificationSwitch.setSummaryOff(R.string.settings_notifications_summary_off);
                }

                return true;
            });
        }

        // Set the Notifications setting's icon according to its value when creating the view.
        private void setNotificationIcon(SwitchPreferenceCompat preference, Boolean value) {
            if (value) {
                preference.setIcon(R.drawable.baseline_notifications_active_24);
            } else {
                preference.setIcon(R.drawable.baseline_notifications_off_24);
            }
        }

        // Checks if notification permission is not granted, in which case sets checked to false and informs the user,
        // via the summary text, that they need to grant the permission to enable notifications.
        private void checkPermission(SwitchPreferenceCompat notificationSwitch) {
            if (getContext() != null && !NotificationManagerCompat.from(getContext()).areNotificationsEnabled()) {
                notificationSwitch.setChecked(false);
                notificationSwitch.setSummaryOff(R.string.settings_notifications_summary_no_permission);
            }
            else if (getContext() != null && NotificationManagerCompat.from(getContext()).areNotificationsEnabled()) {
                notificationSwitch.setSummaryOff(R.string.settings_notifications_summary_off);
            }
        }

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);

            EditTextPreference thresholdPreference = findPreference("threshold");
            EditTextPreference intervalPreference = findPreference("interval");
            SwitchPreferenceCompat notificationSwitch = findPreference("notifications");
            SeekBarPreference brightnessBar = findPreference("brightness");
            Boolean notificationEnabled = new AppSettings().getNotificationsEnabled();

            // Set notification threshold and interval preferences to only accept decimal numerals for user input.
            // Additionally add user input validation on preference change.
            if (thresholdPreference != null) {
                setInputTypeToNumber(thresholdPreference);
                setValidatorFunction(thresholdPreference);
            }

            if (intervalPreference != null) {
                setInputTypeToNumber(intervalPreference);
                setValidatorFunction(intervalPreference);
            }

            // Set the notifications icon according to the value of the setting, then set onPreferenceChange callback.
            if (notificationSwitch != null) {
                setNotificationIcon(notificationSwitch, notificationEnabled);
                checkPermission(notificationSwitch);
                setNotificationSwitchListener(notificationSwitch);
            }

            if (brightnessBar != null) {
                setBrightnessBarListener(brightnessBar);
            }
        }

        private void setBrightnessBarListener(SeekBarPreference brightnessBar) {
//            brightnessBar.setOnPreferenceChangeListener((preference, newValue) -> {
////                View settingsView = getActivity().findViewById(android.R.id.content).getRootView();
//                View settingsView = getActivity().findViewById(android.R.id.content);
//
//                return true;
//            });
        }
    }
}