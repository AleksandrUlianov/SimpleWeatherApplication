package edu.poleaxe.simpleweatherapplication;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import edu.poleaxe.simpleweatherapplication.customenums.ForecastPeriods;
import edu.poleaxe.simpleweatherapplication.customenums.TemperatureDegrees;
import edu.poleaxe.simpleweatherapplication.customenums.UnitMeasurements;
import edu.poleaxe.simpleweatherapplication.dbmanager.DBManager;
import edu.poleaxe.simpleweatherapplication.support.customdialogmanager.DialogManager;
import edu.poleaxe.simpleweatherapplication.customenums.DialogsTypesEnum;
import edu.poleaxe.simpleweatherapplication.support.internetconnection.InternetConnectionException;
import edu.poleaxe.simpleweatherapplication.support.internetconnection.InternetConnectionManager;
import edu.poleaxe.simpleweatherapplication.weatherapi.City;
import edu.poleaxe.simpleweatherapplication.visualcomponents.SuggestedCityEntryAdapter;
import edu.poleaxe.simpleweatherapplication.visualcomponents.WeatherEntryAdapter;
import edu.poleaxe.simpleweatherapplication.weatherapi.ForecastInstance;
import edu.poleaxe.simpleweatherapplication.weatherapi.ForecastProcessor;
import edu.poleaxe.simpleweatherapplication.weatherapi.UpdateCityDBTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 */
public class WeatherCheckActivity extends AppCompatActivity {

    private DialogManager dialogManager = new DialogManager();
    private DBManager dbManager;

    private ArrayList<ForecastInstance> forecastListToDisplay = new ArrayList<>();

    private static TemperatureDegrees   temperatureDegrees  = TemperatureDegrees.CELSIUS;
    private static UnitMeasurements     unitMeasurements    = UnitMeasurements.METRIC;
    private static ForecastPeriods      forecastPeriod      = ForecastPeriods.NOW;

    private City selectedCity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather_check);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        AutoCompleteTextView tvSuggestedCity = (AutoCompleteTextView) findViewById(R.id.tvSuggestedCity);

        SuggestedCityEntryAdapter adapter = new SuggestedCityEntryAdapter(this, R.layout.suggested_city_line, dbManager);
        tvSuggestedCity.setAdapter(adapter);
        tvSuggestedCity.setOnItemClickListener(adapter);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                InitiateUpdateWeather();
            }
        }
        );

        RadioGroup rg = (RadioGroup) findViewById(R.id.rgForecastPeriod);
        rg.check(R.id.rbPeriodDays1);

        rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton rb = (RadioButton) group.findViewById(checkedId);
                if (rb.isChecked()){
                    forecastPeriod = ForecastPeriods.valueOf(rb.getTag().toString());
                    ProcessCheckedRB();
                   //TODO return back weather update on radiobutton switch
                    // InitiateUpdateWeather();
                }
            }
        });

        WeatherEntryAdapter weatherEntryAdapter = new WeatherEntryAdapter(this, forecastListToDisplay);
        ListView forecastListView = (ListView) findViewById(R.id.lvForecastList);
        forecastListView.setAdapter(weatherEntryAdapter);

        SwitchCompat temperatureSwitch = (SwitchCompat) findViewById(R.id.switchTUnits);
        SwitchCompat measurementSwitch = (SwitchCompat) findViewById(R.id.switchMUnits);

        temperatureSwitch.setChecked(false);
        measurementSwitch.setChecked(false);

        temperatureSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Map<String, String> settingToUpdate = new HashMap<>();

                if (v.isSelected()){
                    temperatureDegrees = TemperatureDegrees.CELSIUS;
                }
                else {
                    temperatureDegrees = TemperatureDegrees.FARENHEIT;
                }
                settingToUpdate.put("degreesType",temperatureDegrees.name());
                dbManager.updateAnySettings(settingToUpdate);
            }
        });

        measurementSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Map<String, String> settingToUpdate = new HashMap<>();
                if (v.isSelected()){
                    unitMeasurements = UnitMeasurements.IMPERIAL;

                }
                else {
                    unitMeasurements = UnitMeasurements.METRIC;
                }
                settingToUpdate.put("unitsType",unitMeasurements.name());
                dbManager.updateAnySettings(settingToUpdate);
            }
        });

    }

    @Override
    protected void onStart(){
        super.onStart();
        dbManager = new DBManager(this);
        SetUpApplicationConditions();
    }

    /**
     *
     */
    private void ProcessCheckedRB() {

        Map settingsToChange = new HashMap<String, String>();
        settingsToChange.put("forecastPeriod",forecastPeriod.name());
        dbManager.updateAnySettings(settingsToChange);

    }

    public DBManager getDBManager(){
        return dbManager;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getLayoutInflater().inflate(R.layout.settings_menu,null);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
//        if (id == R.id.select) {
//            return true;
//        }
        return super.onOptionsItemSelected(item);
    }

    /**
     *
     * @return
     */
    boolean CheckConnections(){

        try {
            if ((new InternetConnectionManager()).IsConnectionEstablished(this)){
               // dialogManager.DisplayDialog(DialogsTypesEnum.TOAST, "Network is active", this);
                return true;
            }
            else {
                dialogManager.DisplayDialog(DialogsTypesEnum.TOAST, "Network is INactive", this);
                return false;
            }
        } catch (InternetConnectionException e) {
            new DialogManager().DisplayDialog(DialogsTypesEnum.ALERT, "UI: " + e.getMessage(), this);
            return false;
        }
    }

    /**
     *
     */
    private void SetUpApplicationConditions(){

        boolean isDBAvailable = false;
        try {
            isDBAvailable = dbManager.PrepareDB();
        }
        catch (IllegalAccessError|NullPointerException e){
            dialogManager.DisplayDialog(DialogsTypesEnum.TOAST,"Settings DB not available. Default settings will be used", this);
            return;
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (!isDBAvailable){
            dialogManager.DisplayDialog(DialogsTypesEnum.TOAST,"Settings DB not available. Default settings will be used", this);
        }

        ApplySettingsFromDB();
        UpdateCityDBTask updateCityDBTask = new UpdateCityDBTask(this);
        updateCityDBTask.execute();
    }

    /**
     *
     */
    private void ApplySettingsFromDB() {
        String parameterValue;
        parameterValue      = dbManager.getSettingValue("degreesType");
        temperatureDegrees  = parameterValue == null ? temperatureDegrees : TemperatureDegrees.valueOf(parameterValue);
        switch (temperatureDegrees){
            case FARENHEIT: ((SwitchCompat) findViewById(R.id.switchTUnits)).setChecked(true); break;
            default:
                ((SwitchCompat) findViewById(R.id.switchTUnits)).setChecked(false);
        }
        parameterValue      = dbManager.getSettingValue("unitsType");
        unitMeasurements    = parameterValue == null ? unitMeasurements : UnitMeasurements.valueOf(parameterValue);
        switch (unitMeasurements){
            case IMPERIAL:((SwitchCompat) findViewById(R.id.switchMUnits)).setChecked(true); break;
            default:
                ((SwitchCompat) findViewById(R.id.switchMUnits)).setChecked(false); break;
        }
        parameterValue      = dbManager.getSettingValue("period");
        forecastPeriod      = parameterValue == null ? forecastPeriod : ForecastPeriods.valueOf(parameterValue.toUpperCase());
        selectedCity        = dbManager.getLastCity();
        ((AutoCompleteTextView) findViewById(R.id.tvSuggestedCity)).setText(selectedCity == null ? "" : selectedCity.getLocationName());

    }

    /**
     *
     */
    private void InitiateUpdateWeather(){

        if (selectedCity == null){
            dialogManager.DisplayDialog(DialogsTypesEnum.TOAST, "Please select a city",this);
            return;
        }

        if (!CheckConnections()){
            return;
        }

        ForecastProcessor forecastProcessor = new ForecastProcessor(this);
        forecastProcessor.GetWeatherForecastForSelectedCity(forecastPeriod, selectedCity);
    }

    /**
     *
     * @param forecastInstanceArrayList
     */
    public void UpdateWeather(ArrayList<ForecastInstance> forecastInstanceArrayList){
        forecastListToDisplay.clear();
        forecastListToDisplay.addAll(forecastInstanceArrayList);
        ((WeatherEntryAdapter) ((ListView) findViewById(R.id.lvForecastList)).getAdapter()).notifyDataSetChanged();

    }

    /**
     *
     * @param selectedCity
     */
    public void setSelectedCity(City selectedCity) {
        this.selectedCity = selectedCity;
        Map<String, String> settingToUpdate = new HashMap<>();
        settingToUpdate.put("lastLocation",selectedCity.getLocationID());
        dbManager.updateAnySettings(settingToUpdate);
    }

}
