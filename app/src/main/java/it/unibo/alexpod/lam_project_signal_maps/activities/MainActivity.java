package it.unibo.alexpod.lam_project_signal_maps.activities;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.util.Arrays;

import it.unibo.alexpod.lam_project_signal_maps.R;
import it.unibo.alexpod.lam_project_signal_maps.enums.SignalType;
import it.unibo.alexpod.lam_project_signal_maps.fragments.MapsFragment;
import it.unibo.alexpod.lam_project_signal_maps.permissions.PermissionsRequester;
import it.unibo.alexpod.lam_project_signal_maps.services.GPSLocationService;

public class MainActivity extends AppCompatActivity {

    private Toolbar mainToolbar;
    private Spinner mainToolbarSignalTypeSpinner;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;


    private MapsFragment mapsFragment;
    private SignalType currentSignalType = SignalType.Wifi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /* Initialize UI variables */
        navigationView = findViewById(R.id.mainNavView);
        drawerLayout = findViewById(R.id.mainDrawerLayout);
        mainToolbar = findViewById(R.id.mainToolbar);
        mainToolbarSignalTypeSpinner = findViewById(R.id.mainToolbarSignalTypeSpinner);

        /* Require all permissions */
        requestAllPermissions();

        /* Declare used variables */
        mapsFragment = new MapsFragment();

        /* Initialize UI components */
        initializeToolbar();
        initializeSignalTypeSpinner();
        initializeNavigationView();

        // Set the main screen fragment as maps fragment
        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_container, mapsFragment)
                .commit();

        // Start the gps data gathering service
        startService(new Intent(getApplicationContext(), GPSLocationService.class));
        // TODO: add a visualization of sampled data in a list or a chart
    }

    private void initializeNavigationView() {
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment fragment = null;
                // Hide everything auxiliary to the next by default
                mainToolbarSignalTypeSpinner.setVisibility(View.INVISIBLE);
                // Select which fragment was chosen
                if(item.getItemId() == R.id.nav_map){
                    fragment = new MapsFragment();
                    mapsFragment = (MapsFragment) fragment;
                    mainToolbarSignalTypeSpinner.setVisibility(View.VISIBLE);
                }
                else if(item.getItemId() == R.id.nav_settings){
                    Intent settingsIntent = new Intent();
                    settingsIntent.setComponent(new ComponentName(getApplicationContext(), SettingsActivity.class));
                    startActivity(settingsIntent);
                }
                // Set item as selected to persist highlight
                item.setChecked(true);
                // Close drawer when item is tapped
                drawerLayout.closeDrawers();

                // If no unknown fragments were selected
                if(fragment != null) {
                    // Swap UI fragments
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, fragment)
                            .commit();
                }
                return true;
            }
        });
    }

    void initializeToolbar() {
        // Remove title from toolbar
        mainToolbar.setTitle("");
        setSupportActionBar(mainToolbar);
        // Add menu icon
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu_white_24dp);
        }
    }

    private void initializeSignalTypeSpinner() {
        ArrayAdapter<String> mainToolbarSignalTypeSpinnerMenuAdapter = new ArrayAdapter<String>(
                MainActivity.this,
                R.layout.custom_spinner_text_item
        );

        /* Add strings to the adaptor */
        mainToolbarSignalTypeSpinnerMenuAdapter.add(getString(R.string.wifi_map_tab_title));
        mainToolbarSignalTypeSpinnerMenuAdapter.add(getString(R.string.umts_map_tab_title));
        mainToolbarSignalTypeSpinnerMenuAdapter.add(getString(R.string.lte_map_tab_title));
        mainToolbarSignalTypeSpinnerMenuAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mainToolbarSignalTypeSpinner.setAdapter(mainToolbarSignalTypeSpinnerMenuAdapter);

        /* Declare event handlers */
        mainToolbarSignalTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // store the current signal type for refresh usage
                currentSignalType = SignalType.values()[position];
                mapsFragment.setSignalType(currentSignalType);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void requestAllPermissions() {
        PermissionsRequester permissionsRequester = new PermissionsRequester(
                this,
                getApplicationContext()
        );
        permissionsRequester.requirePermissions(Arrays.asList(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.CHANGE_WIFI_STATE
        ));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Opens navigation menu on menu icon click
        switch (item.getItemId()) {
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
                return true;
            case R.id.action_refresh:
                // refresh the signal
                mapsFragment.setSignalType(currentSignalType);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(this.mapsFragment != null) {
            // attempt to set user location enabled on map
            this.mapsFragment.setUserLocationEnabled();
        }
    }
}
