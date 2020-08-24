package com.zebra.jamesswinton.scantocloudwedge;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.material.snackbar.Snackbar;
import com.zebra.jamesswinton.scantocloudwedge.adapters.ScanEventAdapter;
import com.zebra.jamesswinton.scantocloudwedge.data.ScanEvent;
import com.zebra.jamesswinton.scantocloudwedge.databinding.ActivityMainBinding;
import com.zebra.jamesswinton.scantocloudwedge.networking.EndpointApi;
import com.zebra.jamesswinton.scantocloudwedge.networking.RetrofitInstance;
import com.zebra.jamesswinton.scantocloudwedge.settings.SettingsActivity;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

import static com.google.android.material.snackbar.BaseTransientBottomBar.LENGTH_LONG;
import static com.zebra.jamesswinton.scantocloudwedge.data.ScanEvent.PostRequestState.COMPLETE;
import static com.zebra.jamesswinton.scantocloudwedge.data.ScanEvent.PostRequestState.FAILED;
import static com.zebra.jamesswinton.scantocloudwedge.data.ScanEvent.PostRequestState.IN_PROGRESS;

public class MainActivity extends AppCompatActivity implements ScanEventAdapter.OnRetryClickListener {

    // Data Holder
    public static List<ScanEvent> mScanEvents = new ArrayList<>();

    // Preferences
    private SharedPreferences mSharedPreferences;

    // Data Binding
    private ActivityMainBinding mDataBinding;

    // Scan Variables
    private IntentFilter mScanIntentFilter;

    // Networking
    private String mBaseUrl = "";
    private String mEndPoint = "";
    private Retrofit mRetrofit = null;
    private EndpointApi mEndpointApi = null;

    // UI
    private ScanEventAdapter mScanEventAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDataBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        setSupportActionBar(mDataBinding.toolbar);

        // Init Retrofit
        mBaseUrl = mSharedPreferences.getString(getString(R.string.base_url_pref_key), getString(R.string.base_url_default_value));
        mEndPoint = mSharedPreferences.getString(getString(R.string.relative_endpoint_pref_key), getString(R.string.relative_endpoint_default_value));
        mRetrofit = RetrofitInstance.getInstance(mBaseUrl);
        mEndpointApi = mRetrofit.create(EndpointApi.class);

        // Init Recycler View
        mScanEventAdapter = new ScanEventAdapter(this, this);
        mDataBinding.scanDataRecyclerview.setLayoutManager(new LinearLayoutManager(this));
        mDataBinding.scanDataRecyclerview.setAdapter(mScanEventAdapter);
        mDataBinding.scanDataRecyclerview.setItemAnimator(null);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Verify / Update Base Url
        verifyAndUpdateBaseUrlAndEndpoints();

        // Register Scan Receiver
        if (mScanIntentFilter == null) {
            mScanIntentFilter = new IntentFilter();
            mScanIntentFilter.addAction(getString(R.string.scan_action));
        } registerReceiver(mScanReceiver, mScanIntentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Unregister Scan Receiver
        unregisterReceiver(mScanReceiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * DataWedge Broadcast Receiver
     */

    private BroadcastReceiver mScanReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String intentAction = intent.getAction();
            if (intentAction != null && intentAction.equals(getString(R.string.scan_action))) {
                String data = intent.getStringExtra(getString(R.string.datawedge_intent_key_data));
                String symbology = intent.getStringExtra(getString(R.string.datawedge_intent_key_label_type)).substring(11);
                String source = intent.getStringExtra(getString(R.string.datawedge_intent_key_source));
                long timeStamp = System.currentTimeMillis();

                // Create New Record
                ScanEvent scanEvent = new ScanEvent(data, symbology, source, timeStamp, IN_PROGRESS);

                // Update Recycler View
                mScanEvents.add(scanEvent);
                mScanEventAdapter.notifyItemInserted(mScanEvents.indexOf(scanEvent));
                mDataBinding.scanDataRecyclerview.scrollToPosition(mScanEvents.indexOf(scanEvent));

                // Initiate Retrofit Call for this ScanEvent
                sendScanEventToCloud(scanEvent);
            }
        }
    };

    private void sendScanEventToCloud(ScanEvent scanEvent) {
        // Update State
        mEndpointApi.sendScanEventToCloud(mEndPoint, scanEvent).enqueue(new Callback<ScanEvent>() {
            @Override
            public void onResponse(@NonNull Call<ScanEvent> call, @NonNull Response<ScanEvent> response) {
                if (response.isSuccessful()) {
                    updateScanEventStatus(scanEvent, COMPLETE);
                } else {
                    showErrorSnackbar(response);
                    updateScanEventStatus(scanEvent, FAILED);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ScanEvent> call, @NonNull  Throwable t) {
                showFailureSnackbar(t);
                updateScanEventStatus(scanEvent, FAILED);
            }
        });
    }

    private void updateScanEventStatus(ScanEvent scanEvent, ScanEvent.PostRequestState postRequestState) {
        int index = mScanEvents.indexOf(scanEvent);
        scanEvent.setPostRequestState(postRequestState);
        mScanEvents.set(index, scanEvent);
        mScanEventAdapter.notifyItemChanged(index);
    }


    private void showErrorSnackbar(Response<ScanEvent> response) {
        String errorMessage = String.format(getString(R.string.unsuccessful_http_response), response.code(), response.message());
        Snackbar.make(mDataBinding.coordinator, errorMessage, LENGTH_LONG).show();
    }

    private void showFailureSnackbar(Throwable t) {
        String errorMessage = String.format(getString(R.string.request_failed), t.getMessage());
        Snackbar.make(mDataBinding.coordinator, errorMessage, LENGTH_LONG).show();
    }

    /**
     * Retry Callback
     */

    @Override
    public void onRetryClicked(ScanEvent scanEvent) {
        updateScanEventStatus(scanEvent, IN_PROGRESS);
        sendScanEventToCloud(scanEvent);
    }

    /**
     * URL Utilities
     */

    private void verifyAndUpdateBaseUrlAndEndpoints() {
        // Base URL Check
        if (!mBaseUrl.equals(mSharedPreferences.getString(
                getString(R.string.base_url_pref_key),
                getString(R.string.base_url_default_value)))) {
            mBaseUrl = mSharedPreferences.getString(
                    getString(R.string.base_url_pref_key),
                    getString(R.string.base_url_default_value));

            // Re-init Retrofit
            mRetrofit = RetrofitInstance.updateInstance(mBaseUrl);
        }

        // Endpoint Check
        if (!mEndPoint.equals(mSharedPreferences.getString(
                getString(R.string.relative_endpoint_pref_key),
                getString(R.string.relative_endpoint_default_value)))) {
            mEndPoint = mSharedPreferences.getString(
                    getString(R.string.relative_endpoint_pref_key),
                    getString(R.string.relative_endpoint_default_value));
        }
    }
}