package com.example.jingli.coolweather.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.example.jingli.coolweather.R;

import java.util.List;

public class EditLocationAdapter extends ArrayAdapter<String> {

    private int resourceId;
    private List<String> cities;

    public EditLocationAdapter(Context context, int resourceId, List<String> cities) {
        super(context, resourceId, cities);
        this.resourceId = resourceId;
        this.cities = cities;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final String city = getItem(position);
        View view;
        final ViewHolder viewHolder;
        if(convertView == null) {
            view = LayoutInflater.from(getContext()).inflate(resourceId, null);
            viewHolder = new ViewHolder();
            viewHolder.cityTextView = (TextView)view.findViewById(R.id.edit_location_item_text);
            viewHolder.deleteCityButton = (Button)view.findViewById(R.id.delete_city);
            viewHolder.deleteCityButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    cities.remove(city);
                    SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(
                            MyApplication.getContext()).edit();
                    StringBuilder citiesBuilder = new StringBuilder();
                    for(String city : cities) {
                        if(cities.indexOf(city) != 0) {
                            citiesBuilder.append(",");
                        }
                        citiesBuilder.append(city);
                    }
                    editor.putString("all_cities", citiesBuilder.toString());
                    editor.apply();
                    notifyDataSetChanged();
                }
            });
            view.setTag(viewHolder);
        } else {
            view = convertView;
            viewHolder = (ViewHolder)view.getTag();
        }
        viewHolder.cityTextView.setText(city);
        return view;
    }

    class ViewHolder {
        TextView cityTextView;
        Button deleteCityButton;
    }
}
