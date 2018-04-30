package it.unibo.alexpod.lam_project_signal_maps.activities;

import android.Manifest;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import it.unibo.alexpod.lam_project_signal_maps.R;
import it.unibo.alexpod.lam_project_signal_maps.fragments.MapsFragment;
import it.unibo.alexpod.lam_project_signal_maps.permissions.PermissionsRequester;
import it.unibo.alexpod.lam_project_signal_maps.services.GPSLocationService;

public class MainActivity extends AppCompatActivity {

    Toolbar mainToolbar;
    Spinner mainToolbarSpinner;

    private MapsFragment mapsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*
        * Initialize UI
        * */
        mainToolbar = findViewById(R.id.mainToolbar);
        mainToolbarSpinner = findViewById(R.id.mainToolbarSpinner);
        // Remove title from toolbar
        mainToolbar.setTitle("");

        PermissionsRequester permissionsRequester = new PermissionsRequester(
                this,
                getApplicationContext()
        );
        permissionsRequester.requirePermission(Manifest.permission.ACCESS_COARSE_LOCATION);
        permissionsRequester.requirePermission(Manifest.permission.ACCESS_FINE_LOCATION);

        ArrayAdapter<String> mainToolbarSpinnerMenuAdapter = new ArrayAdapter<String>(
                MainActivity.this,
                R.layout.custom_spinner_text_item
        );
        mainToolbarSpinnerMenuAdapter.add(getString(R.string.wifi_map_tab_title));
        mainToolbarSpinnerMenuAdapter.add(getString(R.string.umts_map_tab_title));
        mainToolbarSpinnerMenuAdapter.add(getString(R.string.lte_map_tab_title));
        mainToolbarSpinnerMenuAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mainToolbarSpinner.setAdapter(mainToolbarSpinnerMenuAdapter);

        /*
        * Declare used variables
        * */
        mapsFragment = new MapsFragment();


        /*
        * Declare event handlers
        * */
        mainToolbarSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mapsFragment.setSignalType(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // start the gps data gathering service
        startService(new Intent(getApplicationContext(), GPSLocationService.class));
    }
}
