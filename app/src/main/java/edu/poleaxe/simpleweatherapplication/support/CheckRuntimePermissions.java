package edu.poleaxe.simpleweatherapplication.support;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

/**
 * Created by Aleksandr Ulianov (poleaxe) on 14.06.2017.
 */
public class CheckRuntimePermissions extends AsyncTask<Object[], Void, Void> {

    private Activity activityToCheck;
    private String permissionToCheck;

    public boolean CheckForPermission(){
        return (ContextCompat.checkSelfPermission(activityToCheck, permissionToCheck)
                ==
                PackageManager.PERMISSION_GRANTED);
    }

    public void RequestPermission(){
        ActivityCompat.requestPermissions(activityToCheck,
                new String[]{permissionToCheck},1);
    }

    private void RequestPermisstion(){
            (new LogManager()).captureLog(activityToCheck.getApplicationContext(), "permission: " + permissionToCheck + " : NOT granted!");
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(activityToCheck,
                    permissionToCheck)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(activityToCheck,
                        new String[]{permissionToCheck},1);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
    }

    public void SetParametersToRun(Activity activityToCheck, String permissionToCheck){
        this.activityToCheck    = activityToCheck;
        this.permissionToCheck  = permissionToCheck;

    }

    @Override
    protected Void doInBackground(Object[]... objects) throws IllegalArgumentException{
        if (this.activityToCheck == null || this.permissionToCheck == null || this.permissionToCheck.equals("")){
            throw (new IllegalArgumentException("Internal error happened. Impossible to check permission for NULL activity or undefined permission"));
        }
        RequestPermisstion();
        return null;
    }
}
