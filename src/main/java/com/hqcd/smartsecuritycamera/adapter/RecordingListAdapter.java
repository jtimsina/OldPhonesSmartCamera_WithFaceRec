package com.hqcd.smartsecuritycamera.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hqcd.smartsecuritycamera.R;

import java.util.ArrayList;

public class RecordingListAdapter extends RecyclerView.Adapter<RecordingListAdapter.ViewHolder>{

    private Context context;
    private ArrayList<String> recordings;

    public RecordingListAdapter(Context context, ArrayList<String> recordings) {
        this.context = context;
        this.recordings = recordings;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recording_list_item, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.recording.setText(recordings.get(position));
        System.out.println("a: " + recordings.get(position));
    }

    @Override
    public int getItemCount() {
        return recordings.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        TextView recording;
        public ViewHolder(View itemView){
            super(itemView);
            recording = itemView.findViewById(R.id.recording_name);
        }

    }
}
