package com.example.jingli.coolweather.util;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.jingli.coolweather.R;


public class DailyForecastViewHolder extends RecyclerView.ViewHolder {

    TextView dateText;
    ImageView condImage_d, condImage_n;
    TextView tempText;
    public DailyForecastViewHolder(View itemView) {
        super(itemView);
        dateText = (TextView) itemView.findViewById(R.id.date_text);
        condImage_d = (ImageView) itemView.findViewById(R.id.cond_d_image);
        condImage_n = (ImageView) itemView.findViewById(R.id.cond_n_image);
        tempText = (TextView) itemView.findViewById(R.id.temp_text);
    }

}
