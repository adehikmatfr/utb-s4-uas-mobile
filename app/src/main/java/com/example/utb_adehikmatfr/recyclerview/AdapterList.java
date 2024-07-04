package com.example.utb_adehikmatfr.recyclerview;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.utb_adehikmatfr.R;

import java.util.List;

public class AdapterList extends RecyclerView.Adapter<AdapterList.ViewHolder> {
    private List<ItemList> itemList;

    public AdapterList(List<ItemList> itemLists) {
        this.itemList = itemLists;
    }

    public List<ItemList> getItemLists() {
        return itemList;
    }

    public void setItemLists(List<ItemList> itemLists) {
        this.itemList = itemLists;
    }

    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(ItemList item);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_data, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ItemList item = itemList.get(position);
        holder.title.setText(item.getTitle());
        holder.description.setText(item.getDescription());
        Glide.with(holder.thumbnailView.getContext()).load(item.getThumbnailUrl()).into(holder.thumbnailView);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null) {
                    listener.onItemClick(item);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView thumbnailView;
        public TextView title, description;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            thumbnailView = itemView.findViewById(R.id.thumbnail_image);
            title = itemView.findViewById(R.id.title);
            description = itemView.findViewById(R.id.description);
        }
    }

    public void setOnItemClickListener(OnItemClickListener listener){
            this.listener = listener;
    }
}
