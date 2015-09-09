package com.example.jingli.coolweather.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import com.example.jingli.coolweather.R;
import com.example.jingli.coolweather.util.EditLocationAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EditLocationActivity extends AppCompatActivity {


    private static final int ADD_CITY = 1;
    private EditLocationAdapter adapter;

    private List<String> citiesList = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_location);

        ListView editLocationList = (ListView) findViewById(R.id.edit_location_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.edit_location_toolbar);
        toolbar.setTitle(getString(R.string.edit_list));
        setSupportActionBar(toolbar);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String citiesString = prefs.getString("all_cities", "");
        String[] cities = citiesString.split(",");

        Collections.addAll(citiesList, cities);
        adapter = new EditLocationAdapter(EditLocationActivity.this, R.layout.edit_location_item, citiesList);
        editLocationList.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_edit_location, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        switch (item.getItemId()) {
            case R.id.add_city:
                Intent chooseAreaIntent = new Intent(EditLocationActivity.this, ChooseAreaActivity.class);
                chooseAreaIntent.putExtra("from_edit_location", true);
                startActivityForResult(chooseAreaIntent, ADD_CITY);
                break;
            case R.id.action_settings:
                return true;
            default:
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        switch (requestCode) {
            case ADD_CITY:
                if(resultCode == RESULT_OK) {
                    String newCityName = intent.getStringExtra("new_city_name");
                    if(!citiesList.contains(newCityName)) {
                        citiesList.add(newCityName);
                        adapter.notifyDataSetChanged();
                    }
                }
                break;
            default:
        }
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_OK, new Intent());
        finish();
    }


}
