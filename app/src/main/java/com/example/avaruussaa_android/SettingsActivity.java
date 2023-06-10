package com.example.avaruussaa_android;

import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;

public class SettingsActivity extends AppCompatActivity {
    public static final int THRESHOLD_MAX_LENGTH = 5;

    // Android preferences automatically implement the logic of writing settings changes to SharedPreferences.
    // So I didn't bother with a view model for this view.
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
        // Sets an EditTextPreference's inputType to a decimal number.
        private void setInputTypeToNumber(@NonNull EditTextPreference preference) {
            preference.setOnBindEditTextListener(new EditTextPreference.OnBindEditTextListener() {
                @Override
                public void onBindEditText(@NonNull EditText editText) {
                    editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                }
            });
        }

        // Overrides onPreferenceChange of an EditTextPreference to truncate user input numbers to five digits.
        private void setValidatorFunction(@NonNull EditTextPreference preference) {
            preference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(@NonNull Preference p, Object newValue) {
                    String newValueString = (String) newValue;
                    // Remove trailing zeroes and extra zeroes from the front
                    newValueString = Utils.removeExtraZeroes(newValueString);

                    // Cut off numbers that are longer than 5 digits including decimal point
                    if (newValueString.length() > THRESHOLD_MAX_LENGTH) {
                        newValueString = newValueString.substring(0, THRESHOLD_MAX_LENGTH);
                    }

                    // Set the truncated string as the value of EditTextPreference and return false to discard the original input.
                    preference.setText(newValueString);
                    return false;
                }
            });
        }

        // When user changes the Notifications setting set the preference icon as appropriate.
        private void setIconChangerFunction(@NonNull SwitchPreferenceCompat preference) {
            preference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(@NonNull Preference p, Object newValue) {
                    Boolean newValueBoolean = (Boolean) newValue;
                    setNotificationIcon(preference, newValueBoolean);

                    return true;
                }
            });
        }

        private void setNotificationIcon(SwitchPreferenceCompat preference, Boolean value) {
            if (value) {
                preference.setIcon(R.drawable.baseline_notifications_active_24);
            }
            else {
                preference.setIcon(R.drawable.baseline_notifications_off_24);
            }
        }

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);

            EditTextPreference thresholdPreference = findPreference("threshold");
            EditTextPreference intervalPreference = findPreference("interval");
            SwitchPreferenceCompat notificationSwitch = findPreference("notifications");
            Boolean notificationValue = getPreferenceManager().getSharedPreferences().getBoolean("notifications", true);

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
                setNotificationIcon(notificationSwitch, notificationValue);
                setIconChangerFunction(notificationSwitch);
            }
        }
    }
}