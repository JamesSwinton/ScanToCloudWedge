package com.zebra.jamesswinton.scantocloudwedge.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.zebra.jamesswinton.scantocloudwedge.MainActivity;
import com.zebra.jamesswinton.scantocloudwedge.R;
import com.zebra.jamesswinton.scantocloudwedge.data.ScanEvent;
import com.zebra.jamesswinton.scantocloudwedge.databinding.AdapterScanDataBinding;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class ScanEventAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    // ViewHolders
    private static final int EMPTY_VIEW_HOLDER = 0;
    private static final int SCAN_VIEW_HOLDER = 1;

    // Data
    private Context mContext;
    private OnRetryClickListener mOnRetryClickListener;

    public ScanEventAdapter(Context context, OnRetryClickListener onRetryClickListener) {
        this.mContext = context;
        this.mOnRetryClickListener = onRetryClickListener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == SCAN_VIEW_HOLDER) {
            return new ScanEventViewHolder(DataBindingUtil.inflate(LayoutInflater.from(
                    parent.getContext()), R.layout.adapter_scan_data, parent,
                    false));
        } else {
            return new EmptyViewHolder(LayoutInflater.from(
                    parent.getContext()).inflate(R.layout.adapter_no_data, parent,
                    false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        if (viewHolder instanceof ScanEventViewHolder) {
            // Get Current ScanEvent
            ScanEvent scanEvent = MainActivity.mScanEvents.get(position);
            ScanEventViewHolder vh = (ScanEventViewHolder) viewHolder;

            // Populate
            vh.mScanDataBinding.data.setText(String.format(mContext.getString(R.string.data_holder), scanEvent.getDataString()));
            vh.mScanDataBinding.source.setText(String.format(mContext.getString(R.string.source_holder), scanEvent.getSource()));
            vh.mScanDataBinding.symbology.setText(String.format(mContext.getString(R.string.symbology_holder), scanEvent.getLabelType()));
            vh.mScanDataBinding.timestamp.setText(getDate(scanEvent.getTimeStamp()));

            // Handle Post Status
            switch (scanEvent.getPostRequestState()) {
                case COMPLETE:
                    vh.mScanDataBinding.postStatus.setText("Post Complete");
                    vh.mScanDataBinding.statusIcon.setImageResource(R.drawable.ic_success);
                    break;
                case IN_PROGRESS:
                    vh.mScanDataBinding.postStatus.setText("Post In Progress...");
                    Glide.with(mContext)
                            .asGif()
                            .load(R.drawable.uploading)
                            .into(vh.mScanDataBinding.statusIcon);
                    break;
                case FAILED:
                    vh.mScanDataBinding.postStatus.setText("Post Failed");
                    vh.mScanDataBinding.statusIcon.setImageResource(R.drawable.ic_error);
                    vh.mScanDataBinding.statusIcon.setOnClickListener(view ->
                            mOnRetryClickListener.onRetryClicked(scanEvent));
                    break;
            }
        }
    }

    @Override
    public int getItemCount() {
        return MainActivity.mScanEvents == null || MainActivity.mScanEvents.isEmpty() ? 1 : MainActivity.mScanEvents.size();
    }

    @Override
    public int getItemViewType(int position) {
        return MainActivity.mScanEvents == null || MainActivity.mScanEvents.isEmpty() ? EMPTY_VIEW_HOLDER : SCAN_VIEW_HOLDER;
    }

    /**
     * ViewHolders
     */

    private static class ScanEventViewHolder extends RecyclerView.ViewHolder {
        public AdapterScanDataBinding mScanDataBinding;
        public ScanEventViewHolder(@NonNull AdapterScanDataBinding viewBinding) {
            super(viewBinding.getRoot());
            mScanDataBinding = viewBinding;
        }
    }

    private static class EmptyViewHolder extends RecyclerView.ViewHolder {
        public EmptyViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    /**
     * Utilities
     */

    public static String getDate(long milliSeconds) {
        SimpleDateFormat formatter = new SimpleDateFormat("hh:mm:ss.SSS", Locale.getDefault());
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        return formatter.format(calendar.getTime());
    }

    /**
     * Click Listener Callback
     */

    public interface OnRetryClickListener {
        void onRetryClicked(ScanEvent scanEvent);
    }
}
