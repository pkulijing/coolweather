package com.example.jingli.coolweather.util;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.example.jingli.coolweather.R;

import java.util.List;

/**
 * Created by jingli on 9/9/15.
 */
public class EditLocationAdapter extends ArrayAdapter<String> {

    private int itemLayoutId;

    public EditLocationAdapter(Context context, int resourceId, List<String> cities) {
        super(context, resourceId, cities);
        itemLayoutId = resourceId;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        String city = getItem(position);
        View view;
        ViewHolder viewHolder;
        if(convertView == null) {
            view = LayoutInflater.from(getContext()).inflate(itemLayoutId, null);
            viewHolder = new ViewHolder();
            viewHolder.cityTextView = (TextView)view.findViewById(R.id.edit_location_item_text);
            viewHolder.cityCheckBox = (CheckBox)view.findViewById(R.id.edit_location_item_checkbox);
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
        CheckBox cityCheckBox;
    }
}
