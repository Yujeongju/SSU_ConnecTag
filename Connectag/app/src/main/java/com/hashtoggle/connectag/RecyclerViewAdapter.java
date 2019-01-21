package com.hashtoggle.connectag;

import android.content.Context;
import android.content.Intent;
import android.provider.DocumentsContract;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.MyViewHoler> {

    private Context mContext;
    private List<Post> mData;

    public RecyclerViewAdapter(Context mContext, List<Post> mData){
        this.mContext = mContext;
        this.mData = mData;
    }

    @Override
    public MyViewHoler onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        View view;
        LayoutInflater mInflater = LayoutInflater.from(mContext);
        view = mInflater.inflate(R.layout.card_view_item, viewGroup, false);

        return new MyViewHoler(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHoler myViewHoler, int i) {
        myViewHoler.card_keyword.setText(String.valueOf(mData.get(i).getCard_keyword()));
        myViewHoler.card_link.setText(String.valueOf(mData.get(i).getCard_link()));
        myViewHoler.card_cnt.setText(String.valueOf(mData.get(i).getCard_cnt()));
        myViewHoler.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


            }
        });

    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public static class MyViewHoler extends RecyclerView.ViewHolder{

        TextView card_keyword;
        TextView card_link;
        TextView card_cnt;
        CardView cardView ;

        public MyViewHoler(@NonNull View itemView) {
            super(itemView);
            card_keyword = (TextView)itemView.findViewById(R.id.keyword);
            card_link = (TextView)itemView.findViewById(R.id.link);
            card_cnt = (TextView)itemView.findViewById(R.id.count);

            cardView = (CardView)itemView.findViewById(R.id.card_view);
        }
    }

}
