package com.vitalsync.vital_sync.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.vitalsync.vital_sync.R;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ProfileAdapter extends RecyclerView.Adapter<ProfileAdapter.ViewHolder> {
    private LayoutInflater mInflater;
    private ArrayList<String> mData;
    public ProfileAdapter(Context context, ArrayList<String> data){
        mInflater = LayoutInflater.from(context);
        mData = data;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.layout_profile_element, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

    }


    @Override
    public int getItemCount() {
        return 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView titleView;
        EditText inputView;
        ViewHolder(View itemView) {
            super(itemView);
            titleView = itemView.findViewById(R.id.element_title);
            inputView = itemView.findViewById(R.id.element_content);
        }

        public String getLabel(){
            return titleView.getText().toString();
        }

        public String getInput(){
            return inputView.getText().toString();
        }
    }
}
