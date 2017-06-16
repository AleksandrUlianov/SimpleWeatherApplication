package edu.poleaxe.simpleweatherapplication.support;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;

/**
 * Created by Aleksandr Ulianov (poleaxe) on 14.06.2017.
 */
public class LogManager {

    private PackageManager packageManager = null;
    private ApplicationInfo applicationInfo = null;

    private String getApplicationName(Context context) {
        packageManager = context.getPackageManager();
        try {
            applicationInfo = packageManager.getApplicationInfo(context.getApplicationInfo().packageName, 0);
        } catch (final PackageManager.NameNotFoundException e) {
            //TODO catch error
        }
        return (String) (applicationInfo != null ? packageManager.getApplicationLabel(applicationInfo) : "Unknown");
    }

    public void captureLog(Context context, String messageToLog){
        Log.d(getApplicationName(context), messageToLog);

    }
}
