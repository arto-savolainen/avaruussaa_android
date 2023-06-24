package com.example.avaruussaa_android;

import android.Manifest;
import android.os.Build;
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
                uncheckNotificationSwitchAndShowToast();
            }
        });

        // Triggered when the user attempts to enable notifications while permission is not granted.
        // Unchecks the setting, sets the appropriate icon and summary text, and shows an informative toast.
        private void uncheckNotificationSwitchAndShowToast() {
            SwitchPreferenceCompat notificationSwitch = findPreference("notifications");

            if (notificationSwitch != null) {
                uncheckNotificationSwitch(notificationSwitch);
                showPermissionToast();
            }
        }

        // Unchecks the notification setting and sets summary text to indicate that permission has been denied.
        private void uncheckNotificationSwitch(SwitchPreferenceCompat notificationSwitch) {
            notificationSwitch.setChecked(false);
            setNotificationIcon(notificationSwitch, false);
            notificationSwitch.setSummaryOff(R.string.settings_notifications_summary_no_permission);
        }

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

        // When user changes the Notifications setting set the SwitchPreferenceCompat icon and summary text as appropriate.
        // On Android 13 also ask for notification permission if the user set the switch to checked and the permission has not been granted.
        // On Android 12 and older, if permission has been revoked, show a toast informing the user they must grant permission to enable notifications.
        private void setNotificationSwitchListener(@NonNull SwitchPreferenceCompat notificationSwitch) {
            notificationSwitch.setOnPreferenceChangeListener((preference, newValue) -> {
                Log.d(TAG, "setNotificationSwitchListener: newValue: " + newValue + " areNotificationsEnabled(): " + NotificationManagerCompat.from(getContext()).areNotificationsEnabled());
                Boolean newValueBoolean = (Boolean) newValue;

                if (newValueBoolean && getContext() != null && !NotificationManagerCompat.from(getContext()).areNotificationsEnabled()) {
                    Log.d(TAG, "onCreate: CAN NOT USE NOTIFICATIONS, ASKING PERMISSIONS");

                    // Notification permissions are apparently new to API 33 / Android 13.
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
                    }
                    else {
                        uncheckNotificationSwitchAndShowToast();
                        return false;
                    }
                }
                else if (!newValueBoolean && getContext() != null && NotificationManagerCompat.from(getContext()).areNotificationsEnabled()) {
                    notificationSwitch.setSummaryOff(R.string.settings_notifications_summary_off);
                }
                else if (!newValueBoolean && getContext() != null && !NotificationManagerCompat.from(getContext()).areNotificationsEnabled()) {
                    notificationSwitch.setSummaryOff(R.string.settings_notifications_summary_no_permission);
                }

                setNotificationIcon(notificationSwitch, newValueBoolean);
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
                uncheckNotificationSwitch(notificationSwitch);
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
                setNotificationIcon(notificationSwitch, notificationSwitch.isChecked());
                checkPermission(notificationSwitch);
                setNotificationSwitchListener(notificationSwitch);
            }

            if (brightnessBar != null) {
                setBrightnessBarListener(brightnessBar);
            }
        }

        // When implemented this function is intended to change the opacity of the background gradient drawable.
        // The gradient would have to be changed from being set via the background attribute to an imageView,
        // since there doesn't appear to be a way to gradually change the tint of a background drawable.
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