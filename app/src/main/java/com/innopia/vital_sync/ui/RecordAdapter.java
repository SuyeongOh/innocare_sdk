package com.innopia.vital_sync.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.innopia.vital_sync.R;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class RecordAdapter extends RecyclerView.Adapter<RecordAdapter.ViewHolder> {

    private LayoutInflater mInflater;
    private List<Item> itemList;
    private final List<String> mData = new ArrayList<>();

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
        Item item = itemList.get(position);
        holder.label.setText(item.getName());
        holder.label.setSelected(item.isSelected());
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        Button label;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            label = itemView.findViewById(R.id.record_element_label);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
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
}
