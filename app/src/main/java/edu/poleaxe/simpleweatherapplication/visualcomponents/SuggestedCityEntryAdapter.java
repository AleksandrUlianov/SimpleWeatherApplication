package edu.poleaxe.simpleweatherapplication.visualcomponents;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.CursorAdapter;
import android.widget.TextView;
import edu.poleaxe.simpleweatherapplication.R;
import edu.poleaxe.simpleweatherapplication.WeatherCheckActivity;
import edu.poleaxe.simpleweatherapplication.dbmanager.DBManager;
import edu.poleaxe.simpleweatherapplication.weatherapi.City;

/**
 * Created by Aleksandr Ulianov (poleaxe) on 21.06.2017.
 */
public class SuggestedCityEntryAdapter extends CursorAdapter
implements AdapterView.OnItemClickListener{
    private  DBManager dbManager;
    private Context context;
    private int layout_to_inflate;

    public SuggestedCityEntryAdapter(Context context, int layout_to_inflate, DBManager dbManager) {
        super(context, null, false);
        this.layout_to_inflate  = layout_to_inflate;
        this.dbManager          = dbManager;
        this.context            = context;
    }

    @Override
    public String convertToString(Cursor resultSet){
        return dbManager.cityToFromCursor(resultSet).getLocationName();
    }

    @Override
    public Cursor runQueryOnBackgroundThread(CharSequence constraint) {
        if (dbManager == null){
            dbManager = ((WeatherCheckActivity) context).getDBManager();
        }
        return dbManager.getCursorOverSuggestedCities((String) constraint);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        final LayoutInflater inflater = LayoutInflater.from(context);

        return inflater.inflate(layout_to_inflate,
                parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        City cityToSuggest = dbManager.cityToFromCursor(cursor);
        TextView tvSuggestedCityName    = (TextView) view.findViewById(R.id.tvSuggestedCityName);
        TextView tvSuggestedCityCountry = (TextView) view.findViewById(R.id.tvSuggestedCityCountry);

        tvSuggestedCityName.setText(cityToSuggest.getLocationName());
        tvSuggestedCityCountry.setText(cityToSuggest.getLocationCountryCode());

    }

    @Override
    public void onItemClick(AdapterView<?> listView, View view, int position, long id){
        Cursor resulSet = (Cursor) listView.getItemAtPosition(position);
        if (resulSet != null) {
            City selectedCity = dbManager.cityToFromCursor(resulSet);
            WeatherCheckActivity parentActivity = (WeatherCheckActivity) context;
            parentActivity.setSelectedCity(selectedCity);

            ((AutoCompleteTextView) parentActivity.findViewById(R.id.tvSuggestedCity)).setText(selectedCity.getLocationName());


        }
    }
}
