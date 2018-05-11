package it.unibo.alexpod.lam_project_signal_maps.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;

import it.unibo.alexpod.lam_project_signal_maps.R;
import it.unibo.alexpod.lam_project_signal_maps.fragments.SettingsFragment;

public class SettingsActivity extends AppCompatActivity {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fragment settingsFragment = new SettingsFragment();
        setTheme(R.style.settingsTheme);
        // Display the fragment as the main content.
        getSupportFragmentManager().beginTransaction()
                .replace(android.R.id.content, settingsFragment)
                .commit();

    }
}
