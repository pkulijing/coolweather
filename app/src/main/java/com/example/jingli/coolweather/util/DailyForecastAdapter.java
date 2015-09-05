package com.example.jingli.coolweather.util;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.jingli.coolweather.R;
import com.example.jingli.coolweather.model.Weather;

import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by jingli on 9/6/15.
 */
public class DailyForecastAdapter extends RecyclerView.Adapter<DailyForecastViewHolder>{

    private List<Weather.DailyForecast> dailyForecasts;

    public DailyForecastAdapter(List<Weather.DailyForecast> dailyForecasts) {
        this.dailyForecasts = dailyForecasts;
    }

    @Override
    public int getItemCount() {
        if (dailyForecasts == null)
            return 0;
        return dailyForecasts.size();
    }

    @Override
    public DailyForecastViewHolder onCreateViewHolder(ViewGroup root, int i) {
        View itemView = LayoutInflater.from(root.getContext()).inflate(R.layout.daily_forecast_layout, root, false);
        return new DailyForecastViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(DailyForecastViewHolder viewHolder, int i) {
        Weather.DailyForecast dailyForecast = dailyForecasts.get(i);
        viewHolder.dateText.setText(dateToShow(dailyForecast.date, i));
        viewHolder.tempText.setText(dailyForecast.minTemp + "° ~ " + dailyForecast.maxTemp + "°");
        int drawableId = MyApplication.getContext().getResources().getIdentifier(
                "w" + dailyForecast.code_d,
                "drawable",
                MyApplication.getContext().getPackageName());
        viewHolder.condImage_d.setImageResource(drawableId);
        drawableId = MyApplication.getContext().getResources().getIdentifier(
                "w" + dailyForecast.code_n,
                "drawable",
                MyApplication.getContext().getPackageName());
        viewHolder.condImage_n.setImageResource(drawableId);

    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    private static String dateToShow(String date, int i) {
        if(i == 0) {
            return MyApplication.getContext().getResources().getString(R.string.today);
        } else if (i == 1) {
            return MyApplication.getContext().getResources().getString(R.string.tomorrow);
        }
        Pattern pattern = Pattern.compile("\\d\\d\\d\\d-(\\d\\d-\\d\\d)");
        Matcher matcher = pattern.matcher(date);
        if(matcher.find()){
            //Log.d("MyLog", matcher.group(1));
            return matcher.group(1);
        } else {
            throw new RuntimeException(date + " is not a valid date value. Should be yyyy-mm-dd");
        }
    }
}
