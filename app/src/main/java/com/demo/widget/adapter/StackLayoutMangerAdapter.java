package com.demo.widget.adapter;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseViewHolder;
import com.demo.widget.R;

import java.util.Random;

/**
 * @author wenshi
 * @github
 * @Description
 * @since 2019/6/28
 */
public class StackLayoutMangerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    String[] titles;
    OnItemClickListener clickListener;

    public StackLayoutMangerAdapter(String[] titles, OnItemClickListener listener) {
        this.titles = titles;
        this.clickListener = listener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_stack_card,
                parent, false);
        view.setBackgroundColor(Color.argb(255,
                new Random().nextInt(255),
                new Random().nextInt(255),
                new Random().nextInt(255)));
        BaseViewHolder holder = new BaseViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, final int position) {
        ((TextView) viewHolder.itemView.findViewById(R.id.tv)).setText("" + titles[position]);
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (clickListener != null) {
                    clickListener.onClick(v, position);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return titles.length;
    }

    public interface OnItemClickListener {
        void onClick(View view, int position);
    }

}
