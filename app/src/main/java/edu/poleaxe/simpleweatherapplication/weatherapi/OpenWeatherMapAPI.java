package edu.poleaxe.simpleweatherapplication.weatherapi;

import android.app.Activity;
import android.content.Context;
import edu.poleaxe.simpleweatherapplication.R;
import edu.poleaxe.simpleweatherapplication.support.LogManager;
import edu.poleaxe.simpleweatherapplication.support.customdialogmanager.DialogManager;
import edu.poleaxe.simpleweatherapplication.support.customdialogmanager.DialogsTypesEnum;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by Aleksandr Ulianov (poleaxe) on 14.06.2017.
 */
public class OpenWeatherMapAPI implements Runnable {

    /**
     * Retrieves list of cities from the webserver
     * @param context context where was initiated
     * @return ArrayList of Strings (each string - one city with its attributes) to be added to DB
     * @throws IOException
     * @throws MalformedURLException
     */
    private ArrayList<String> getCityList(Context context) throws IOException, MalformedURLException {
        ArrayList<String> listOfCitiesToAddToDB = new ArrayList<>();
        String cityLinkURLString = context.getResources().getString(R.string.citi_list);
        URL cityListURL = new URL(cityLinkURLString);
        HttpURLConnection connection = (HttpURLConnection) cityListURL.openConnection();
        connection.connect();

        if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
            throw new MalformedURLException("Server returned HTTP " + connection.getResponseCode()
                    + " " + connection.getResponseMessage());
        }

        BufferedReader in = new BufferedReader(new InputStreamReader(cityListURL.openStream()));
        String str;
        while ((str = in.readLine()) != null) {
            listOfCitiesToAddToDB.add(str);
        }
        in.close();
        connection.disconnect();

        return listOfCitiesToAddToDB;
    }

    public void UpdateCitiesDB(Context context){
        ArrayList<String> citiesToAdd = null;
        try {
            citiesToAdd = getCityList(context);
        } catch (IOException e) {
            new LogManager().captureLog(context, e.getMessage());
        }

        if (citiesToAdd == null){
            new DialogManager().DisplayDialog(DialogsTypesEnum.TOAST, "An error occured while updating database of cities", (Activity) context);
            return;
        }
        for (String lineToExecute : citiesToAdd
             ) {
            String[] parsedLine = lineToExecute.split(" ");
            for (String dataValue : parsedLine
                 ) {
                if (dataValue.trim().equals("")){
                    continue;
                }

                new DialogManager().DisplayDialog(DialogsTypesEnum.TOAST,dataValue.trim(), (Activity )context);

            }

            break;
        }

    }

    @Override
    public void run() {

    }
}
