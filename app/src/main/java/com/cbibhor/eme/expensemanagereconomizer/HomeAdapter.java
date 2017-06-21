package com.cbibhor.eme.expensemanagereconomizer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.view.ViewCompat;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by Bibhor Chauhan on 19-04-2017.
 */

public class HomeAdapter extends RecyclerView.Adapter<HomeAdapter.ViewHolder> {

    Context mContext;
    public static OnItemClickListener mItemClickListener;

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        public final LinearLayout placeHolder;
        public final TextView placeName;
        public final CardView cardView;

        public ViewHolder(View itemView) {
            super(itemView);
            cardView = (CardView) itemView;
            placeHolder = (LinearLayout) itemView.findViewById(R.id.mainHolder);
            placeName = (TextView) itemView.findViewById(R.id.placeName);
            placeHolder.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (mItemClickListener != null) {
                mItemClickListener.onItemClick(itemView, getPosition());
            }
        }
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mItemClickListener = mItemClickListener;
    }

    List<HomeMenu> homeMenuList;
    HomeAdapter(Context context, List<HomeMenu> homeMenuList){
        this.homeMenuList=homeMenuList;
        this.mContext=context;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public HomeAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.cardview_home, viewGroup, false);
        HomeAdapter.ViewHolder viewHolder = new HomeAdapter.ViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final HomeAdapter.ViewHolder holder, int i) {
        ViewCompat.setTransitionName(holder.cardView, "cardViewTransition" + i);
        ViewCompat.setTransitionName(holder.placeName, "textTransition" + i);
        HomeMenu homeMenu = homeMenuList.get(i);
        holder.placeName.setText(homeMenu.getName());
    }

    @Override
    public int getItemCount() {
        return homeMenuList.size();
    }
}
