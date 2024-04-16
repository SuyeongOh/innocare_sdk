package com.vitalsync.vital_sync.ui;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.vitalsync.vital_sync.R;

import java.util.HashMap;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class GtAdapter extends RecyclerView.Adapter<GtAdapter.ViewHolder> {

    private List<String> mData;
    private final HashMap<String, String> mDataMap = new HashMap<>();
    private LayoutInflater mInflater;
    public GtAdapter(Context context, List<String> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.layout_gt_element, parent, false);
        EditText inputView = view.findViewById(R.id.view_input_gt);
        inputView.setText("0");
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String label = mData.get(position);
        TextView textView = holder.textView;
        textView.setText(String.format("%s", label));
        holder.inputView.setHint(String.format("input %s result", label));

        holder.inputView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String label = textView.getText().toString();

                if(mDataMap.containsKey(label)){
                    mDataMap.replace(label, s.toString());
                }else{
                    mDataMap.put(textView.getText().toString(), s.toString());
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    // Getter 메서드 추가
    public List<String> getData() {
        return mData;
    }

    public HashMap<String, String> getDataMap(){ return mDataMap; }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView;
        EditText inputView;
        ViewHolder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.view_input_gt_label);
            inputView = itemView.findViewById(R.id.view_input_gt);
        }

        public String getLabel(){
            return textView.getText().toString();
        }

        public String getInput(){
            return inputView.getText().toString();
        }
    }
}

