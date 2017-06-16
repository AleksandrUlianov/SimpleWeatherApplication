package edu.poleaxe.simpleweatherapplication.support.internetconnection;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.RemoteException;
import edu.poleaxe.simpleweatherapplication.support.customdialogmanager.Log;

import java.security.AccessControlException;

/**
 * Created by Aleksandr Ulianov (poleaxe) on 10.06.2017.
 */
public class InternetConnectionManager {

    /**
     *
     * @param parentActivity which for to check if any network is established and connected
     * @return boolean trues if there is any network established and connected. false if there is no available networks or all disconnected
     * @throws InternetConnectionException in case parent Activity was not specified
     */
    public boolean IsConnectionEstablished(Activity parentActivity) throws InternetConnectionException {
        if (parentActivity == null) {
            new Log().WriteLog();
            throw new InternetConnectionException("Internal error happened during gaining access to network hardware");
        }

        ConnectivityManager connectivityManager = (ConnectivityManager) parentActivity.getSystemService(Context.CONNECTIVITY_SERVICE);

        return getActiveNetwork(connectivityManager) == null ? false : IsConnected(connectivityManager);

    }

    /**
     *
     * @param connectivityManager of ConnectivityManager to check available networks
     * @return Network available network. Null if there is no available networks
     */
    private Network getActiveNetwork(ConnectivityManager connectivityManager) {
        Network availableNetwork = null;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            availableNetwork = connectivityManager.getActiveNetwork();
        }
        else {
            //TODO handle old SDKs to getActiveNetwork
        }

        return availableNetwork;
    }

    /**
     *
     * @param connectivityManager of ConnectivityManager to check if any network is established
     * @return boolean true is connected, false if not
     */
    private boolean IsConnected(ConnectivityManager connectivityManager){
        NetworkInfo availableNetworkInfo = connectivityManager.getActiveNetworkInfo();

        return availableNetworkInfo.isConnected();
    }

}
