package com.example.mtrchecker;


import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import java.util.Locale;

public class SettingsFragment extends Fragment {
    private static final String PREFS_NAME = "settings_prefs";
    private static final String DARK_MODE_KEY = "dark_mode";
    private static final String LANGUAGE_KEY = "lang";

    public SettingsFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        Switch darkModeSwitch = view.findViewById(R.id.darkModeSwitch);
        Switch languageSwitch = view.findViewById(R.id.languageSwitch);

        SharedPreferences prefs = requireContext().getSharedPreferences(PREFS_NAME, 0);
        boolean darkModeEnabled = prefs.getBoolean(DARK_MODE_KEY, false);
        boolean useChinese = prefs.getString(LANGUAGE_KEY, "en").equals("zh-HK");

        darkModeSwitch.setChecked(darkModeEnabled);
        languageSwitch.setChecked(useChinese);

        // Dark Mode Toggle
        darkModeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean(DARK_MODE_KEY, isChecked).apply();
            AppCompatDelegate.setDefaultNightMode(
                    isChecked ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
            );
        });

        // Language Toggle
        languageSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            String langCode = isChecked ? "zh-HK" : "en";
            prefs.edit().putString(LANGUAGE_KEY, langCode).apply();
            setLocale(langCode);
            requireActivity().recreate(); // restart to apply
        });

        return view;
    }

    private void setLocale(String langCode) {
        Locale locale = Locale.forLanguageTag(langCode);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.setLocale(locale);
        requireContext().getResources().updateConfiguration(config, requireContext().getResources().getDisplayMetrics());
    }
}
