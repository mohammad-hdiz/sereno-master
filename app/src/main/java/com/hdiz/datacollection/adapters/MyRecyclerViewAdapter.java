package com.hdiz.datacollection.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.hdiz.datacollection.R;

import java.util.ArrayList;
import java.util.List;

public class MyRecyclerViewAdapter extends RecyclerView.Adapter<MyRecyclerViewAdapter.ViewHolder> {

    private ArrayList<String> mData;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    RecyclerViewDataPass recyclerViewDataPass;


    public interface RecyclerViewDataPass {
        public void pass(ArrayList<String> dataList);
    }
    // data is passed into the constructor
    public MyRecyclerViewAdapter(Context context, ArrayList<String> data, RecyclerViewDataPass recyclerViewDataPass) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
        this.recyclerViewDataPass = recyclerViewDataPass;
    }

    // inflates the row layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.prescription_content, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String prescription = mData.get(position);
        holder.myTextView.setText(prescription);
    }

    // total number of rows
    @Override
    public int getItemCount() {
        return mData.size();
    }


    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView myTextView;
        ImageView myImageView;

        ViewHolder(View itemView) {
            super(itemView);
            myTextView = itemView.findViewById(R.id.prescript);
            myImageView = itemView.findViewById(R.id.remove_pres);
            myImageView.setOnClickListener(this);
            itemView.setOnClickListener(this);

          }

        @Override
        public void onClick(View view) {
            if(view.equals(myImageView)){
                removeAt(getPosition());
                recyclerViewDataPass.pass(mData);
                Log.d("mDataSize",mData.size()+"");
            }else if (mClickListener != null) {
                mClickListener.onItemClick(view, getPosition());
            }
        }
    }

    // convenience method for getting data at click position
    String getItem(int id) {
        return mData.get(id);
    }

    public void removeAt(int position) {
        mData.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, mData.size());
    }

    // allows clicks events to be caught
    void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }

    public void addItem(final String newItem, final int pos) {
        if (mData == null) mData = new ArrayList();
        if(!newItem.equals("Please Select ...")){
            if(!mData.contains(newItem)) {
                mData.add(newItem);
                notifyItemInserted(mData.size() - 1);
                notifyDataSetChanged();
                recyclerViewDataPass.pass(mData);
            }
        }
    }
}
