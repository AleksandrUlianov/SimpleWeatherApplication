package edu.poleaxe.simpleweatherapplication.visualcomponents;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import edu.poleaxe.simpleweatherapplication.R;
import edu.poleaxe.simpleweatherapplication.weatherapi.ForecastInstance;

import java.util.ArrayList;

/**
 * Created by Aleksandr Ulianov (poleaxe) on 17.06.2017.
 */
public class WeatherEntryAdapter extends BaseAdapter{
    private Context context;
    private ArrayList<ForecastInstance> forecastList;

    public WeatherEntryAdapter(Context context, ArrayList<ForecastInstance> forecastList){
        this.context = context;
        this.forecastList = forecastList;
    }

    @Override
    public int getCount() {
        return forecastList.size();
    }

    @Override
    public ForecastInstance getItem(int position) {
        if (position > getCount()){return null;}
        return forecastList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null){
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(
                    R.layout.forecast_line, null);
        }

        TextView forecastDateTime       = (TextView) convertView.findViewById(R.id.tvForecastDayTime);
        TextView forecastPhenomena      = (TextView) convertView.findViewById(R.id.tvWeatherPhenomena);
        TextView forecastPrecipitation  = (TextView) convertView.findViewById(R.id.tvPrecipitation);
        TextView forecastHumidity       = (TextView) convertView.findViewById(R.id.tvHumidity);
        TextView forecastUVIndex        = (TextView) convertView.findViewById(R.id.tvUVIndex);
        TextView forecastTemperature    = (TextView) convertView.findViewById(R.id.tvTemperature);
        TextView forecastPressure       = (TextView) convertView.findViewById(R.id.tvPressure);
        TextView forecastVisibility     = (TextView) convertView.findViewById(R.id.tvVisibility);

        ForecastInstance instanceToDisplay = getItem(position);

        if (instanceToDisplay == null)
        {
            forecastDateTime.setText("N/A");
            forecastPhenomena.setText("N/A");
            forecastPrecipitation.setText("N/A");
            forecastHumidity.setText("N/A");
            forecastUVIndex.setText("N/A");
            forecastTemperature.setText("N/A");
            forecastPressure.setText("N/A");
            forecastVisibility.setText("N/A");
        }
        else{
            forecastDateTime.setText(instanceToDisplay.getForecastDateTime());
            forecastPhenomena.setText(instanceToDisplay.getForecastPhenomena());
            forecastPrecipitation.setText(instanceToDisplay.getForecastPrecipitation());
            forecastHumidity.setText(instanceToDisplay.getForecastHumidity());
            forecastUVIndex.setText(instanceToDisplay.getForecastUVIndex());
            forecastTemperature.setText(instanceToDisplay.getForecastTemperature());
            forecastPressure.setText(instanceToDisplay.getForecastPressure());
            forecastVisibility.setText(instanceToDisplay.getForecastVisibility());
        }

        return convertView;
    }
}
