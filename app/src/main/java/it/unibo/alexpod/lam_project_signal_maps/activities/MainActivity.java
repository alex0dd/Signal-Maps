package it.unibo.alexpod.lam_project_signal_maps.activities;

import android.Manifest;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.view.ViewPager;
import android.os.Bundle;

import it.unibo.alexpod.lam_project_signal_maps.R;
import it.unibo.alexpod.lam_project_signal_maps.adapters.SectionsPageAdapter;
import it.unibo.alexpod.lam_project_signal_maps.fragments.MapsFragment;
import it.unibo.alexpod.lam_project_signal_maps.permissions.PermissionsRequester;

public class MainActivity extends AppCompatActivity {

    TabLayout navigationTabLayout;

    private ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        PermissionsRequester permissionsRequester = new PermissionsRequester(this, getApplicationContext());
        permissionsRequester.requirePermission(Manifest.permission.ACCESS_COARSE_LOCATION);

        viewPager = findViewById(R.id.container);
        setupViewPager(viewPager);

        navigationTabLayout = findViewById(R.id.navigationTabLayout);
        navigationTabLayout.setupWithViewPager(viewPager);
    }

    private void setupViewPager(ViewPager viewPager){
        SectionsPageAdapter adapter = new SectionsPageAdapter(getSupportFragmentManager());
        adapter.addFragment(new MapsFragment(), getString(R.string.wifi_map_tab_title));
        adapter.addFragment(new MapsFragment(), getString(R.string.umts_map_tab_title));
        adapter.addFragment(new MapsFragment(), getString(R.string.lte_map_tab_title));
        viewPager.setAdapter(adapter);
    }
}
