package com.vitalsync.vital_sync.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.vitalsync.vital_sync.R;
import com.vitalsync.vital_sync.service.login.UserInfo;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class UserListAdapter extends RecyclerView.Adapter<UserListAdapter.UserViewHolder> {

    private List<UserInfo> userList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(UserInfo user);
    }

    public UserListAdapter(List<UserInfo> userList, OnItemClickListener listener) {
        this.userList = userList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        UserInfo user = userList.get(position);
        holder.bind(user, listener);
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView btnUser;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            btnUser = itemView.findViewById(R.id.btnUser);
        }

        public void bind(final UserInfo user, final OnItemClickListener listener) {
            btnUser.setText(user.getUserId());
            btnUser.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(user);
                }
            });
        }
    }
}
