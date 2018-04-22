package it.unibo.alexpod.lam_project_signal_maps.permissions;

import android.Manifest;
import android.app.Activity;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import java.util.HashMap;
import java.util.Objects;

/**
 * Created by alexpod on 14/04/18.
 */

public class PermissionsRequester {

    private Activity requestingActivity;
    private Context applicationContext;
    private HashMap<String, Integer> permissionsMap;
    private Integer currentPermissionRequestCode;

    public PermissionsRequester(Activity requestingActivity, Context applicationContext){
        this.requestingActivity = requestingActivity;
        this.applicationContext = applicationContext;
        this.permissionsMap = new HashMap<>();
        this.currentPermissionRequestCode = 0;
    }

    private boolean checkSpecialPermission(String permissionName){
        boolean granted = false;
        AppOpsManager appOps = (AppOpsManager) this.applicationContext.getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(), this.applicationContext.getPackageName());

        if (mode == AppOpsManager.MODE_DEFAULT) {
            granted = (this.applicationContext.checkCallingOrSelfPermission(permissionName) == PackageManager.PERMISSION_GRANTED);
        } else {
            granted = (mode == AppOpsManager.MODE_ALLOWED);
        }
        return granted;
    }

    public void requirePermission(String permissionName){
        //https://stackoverflow.com/questions/36820668/request-permission-on-package-usage-stats
        //if permission is not already into the permissions table
        if(this.permissionsMap.get(permissionName) == null) {
            //then add it
            this.permissionsMap.put(permissionName, currentPermissionRequestCode);
            //increment permissions id
            this.currentPermissionRequestCode++;
        }
        if(Objects.equals(permissionName, Manifest.permission.PACKAGE_USAGE_STATS)){
            if(!checkSpecialPermission(permissionName)){
                this.applicationContext.startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
            }
        }
        else {
            if (ContextCompat.checkSelfPermission(this.requestingActivity,
                    permissionName)
                    != PackageManager.PERMISSION_GRANTED) {

                // Permission is not granted
                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(this.requestingActivity,
                        permissionName)) {

                    // Show an explanation to the user *asynchronously* -- don't block
                    // this thread waiting for the user's response! After the user
                    // sees the explanation, try again to request the permission.

                } else {
                    // No explanation needed; request the permission
                    ActivityCompat.requestPermissions(this.requestingActivity,
                            new String[]{permissionName},
                            this.permissionsMap.get(permissionName));

                    // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                    // app-defined int constant. The callback method gets the
                    // result of the request.
                }
            } else {
                // Permission has already been granted
            }
        }
    }

}
