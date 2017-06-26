package edu.poleaxe.simpleweatherapplication.support;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;

/**class to capture any log that will be needed
 * Created by Aleksandr Ulianov (poleaxe) on 14.06.2017.
 */
public class LogManager {

    private ApplicationInfo applicationInfo = null;

    private String getApplicationName(Context context) {
        PackageManager packageManager = context.getPackageManager();
        try {
            applicationInfo = packageManager.getApplicationInfo(context.getApplicationInfo().packageName, 0);
        } catch (final PackageManager.NameNotFoundException e) {
            //not a really good solution. idea was to use ACRA library to inform developer about an error happened
            e.printStackTrace();
        }
        return (String) (applicationInfo != null ? packageManager.getApplicationLabel(applicationInfo) : "Unknown");
    }

    public void captureLog(Context context, String messageToLog){
        if (context == null){return;}
        Log.d(getApplicationName(context), messageToLog);

    }
}
