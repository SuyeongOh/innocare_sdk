package com.innopia.vital_sync.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.innopia.vital_sync.R;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class SimpleRecyclerViewAdapter extends RecyclerView.Adapter<SimpleRecyclerViewAdapter.SimpleRecyclerViewHolder> {
    private float[] mData;

    public SimpleRecyclerViewAdapter(float[] array){
        mData = array;
    }
    @NonNull
    @Override
    public SimpleRecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new SimpleRecyclerViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_simple_recycler_adapter, parent, false)
        );
    }

    @Override
    public void onBindViewHolder(@NonNull SimpleRecyclerViewHolder holder, int position) {
        holder.textView.setText(String.format("(%d : %f)\t\t",position + 1, mData[position]));
    }

    @Override
    public int getItemCount() {
        return mData.length;
    }

    public static class SimpleRecyclerViewHolder extends RecyclerView.ViewHolder {
        TextView textView;
        public SimpleRecyclerViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.recycle_textview);
        }
    }
}
