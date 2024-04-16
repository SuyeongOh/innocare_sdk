package com.vitalsync.vital_sync.ui;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.vitalsync.vital_sync.R;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class RecordAdapter extends RecyclerView.Adapter<RecordAdapter.ViewHolder> {

    private LayoutInflater mInflater;
    private List<Item> itemList;
    private final List<String> mData = new ArrayList<>();

    private RecordDataLoadListener listener;

    public RecordAdapter(Context context, List<String> data) {
        mInflater = LayoutInflater.from(context);
        itemList = new ArrayList<Item>();
        for(String label : data){
            itemList.add(new Item(label));
        }
        mData.addAll(data);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.layout_record_element, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Log.d("vital", "record position : " + position);
        Item item = itemList.get(position);
        holder.label.setText(item.getName());
        holder.label.setSelected(item.isSelected());
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public void setLoadListener(RecordDataLoadListener listener) {
        this.listener = listener;
    }

    public void clearAllSelect(){
        for(int i = 0; i < itemList.size(); i++) {
            if(itemList.get(i).selected){
                itemList.get(i).selected = false;
                notifyItemChanged(i);
            }
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        Button label;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            label = itemView.findViewById(R.id.record_element_label);
            label.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onLoad(label);
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        // 기존에 선택된 아이템 선택 해제
                        for (Item item : itemList) {
                            item.setSelected(false);
                        }
                        // 현재 클릭된 아이템 선택
                        itemList.get(position).setSelected(true);
                        notifyDataSetChanged();
                    }
                }
            });
        }

        public Button getButton(){
            return label;
        }
    }

    public class Item {
        private String data;
        private boolean selected;

        public Item(String data) {
            this.data = data;
            this.selected = false;
        }

        public String getName() {
            return data;
        }

        public boolean isSelected() {
            return selected;
        }

        public void setSelected(boolean selected) {
            this.selected = selected;
        }
    }

    public interface RecordDataLoadListener{
        void onLoad(Button btn);
    }
}
