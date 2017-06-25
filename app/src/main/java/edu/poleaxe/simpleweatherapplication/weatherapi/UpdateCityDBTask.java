package edu.poleaxe.simpleweatherapplication.weatherapi;

import android.app.Activity;
import android.os.AsyncTask;
import edu.poleaxe.simpleweatherapplication.R;
import edu.poleaxe.simpleweatherapplication.customenums.DialogsTypesEnum;
import edu.poleaxe.simpleweatherapplication.dbmanager.DBManager;
import edu.poleaxe.simpleweatherapplication.support.LogManager;
import edu.poleaxe.simpleweatherapplication.support.customdialogmanager.DialogManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by Aleksandr Ulianov (poleaxe) on 24.06.2017.
 */
public class UpdateCityDBTask extends AsyncTask<Void, Integer, Void> {

    private Activity parentActivity;

    public UpdateCityDBTask(Activity parentActivity){
        this.parentActivity = parentActivity;
    }

    /**
     *
     */
    private void UpdateCitiesDB(){
        DBManager dbManager = new DBManager(parentActivity);
        if (dbManager.checkExistanceOfCitiesBase()){
            return;
        }

        //TODO check permissions and connection

        ArrayList<String> citiesToAdd = null;
        try {
            citiesToAdd = getCityList();
        } catch (IOException e) {
            new LogManager().captureLog(parentActivity.getApplicationContext(), e.getMessage());
        }

        if (citiesToAdd == null){
            //TODO parentActivity.callBackToUI("An error occured while updating database of cities");
            return;
        }

        dbManager.UpdateCityListDB(citiesToAdd);

    }

    /**
     * Retrieves list of cities from the webserver
     * @return ArrayList of Strings (each string - one city with its attributes) to be added to DB
     * @throws IOException
     * @throws MalformedURLException
     */
    private ArrayList<String> getCityList() throws IOException {
        ArrayList<String> listOfCitiesToAddToDB = new ArrayList<>();
        String cityLinkURLString = parentActivity.getResources().getString(R.string.citi_list);
        URL cityListURL = new URL(cityLinkURLString);
        HttpURLConnection connection = (HttpURLConnection) cityListURL.openConnection();
        connection.connect();

        if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
            throw new MalformedURLException("Server returned HTTP " + connection.getResponseCode()
                    + " " + connection.getResponseMessage());
        }

        BufferedReader in = new BufferedReader(new InputStreamReader(cityListURL.openStream()));
        String str;
        in.readLine();
        while ((str = in.readLine()) != null) {
            listOfCitiesToAddToDB.add(str);
        }
        in.close();
        connection.disconnect();

        return listOfCitiesToAddToDB;
    }


    @Override
    protected Void doInBackground(Void... voids) {

        UpdateCitiesDB();

        return null;
    }

    @Override
    protected void onPreExecute() {
        new DialogManager().DisplayDialog(DialogsTypesEnum.TOAST, "Updating list of cities...", parentActivity);

    }

    @Override
    protected void onPostExecute(Void aVoid) {
        new DialogManager().DisplayDialog(DialogsTypesEnum.TOAST, "List of cities updated successfully", parentActivity);
    }
}
