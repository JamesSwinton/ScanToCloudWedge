package com.zebra.jamesswinton.scantocloudwedge.settings;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;
import androidx.databinding.DataBindingUtil;

import android.os.Bundle;
import android.view.MenuItem;

import com.zebra.jamesswinton.scantocloudwedge.R;
import com.zebra.jamesswinton.scantocloudwedge.databinding.ActivitySettingsBinding;

import java.util.Objects;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivitySettingsBinding dataBinding = DataBindingUtil.setContentView(this,
                R.layout.activity_settings);
        setSupportActionBar(dataBinding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Show Fragment
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_holder, new SettingsFragment())
                .addToBackStack("SETTINGS")
                .commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        NavUtils.navigateUpFromSameTask(this);
    }
}