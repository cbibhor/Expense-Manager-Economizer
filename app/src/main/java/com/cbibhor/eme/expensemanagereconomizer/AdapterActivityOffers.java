package com.cbibhor.eme.expensemanagereconomizer;

import android.content.Context;
import android.content.Intent;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

/**
 * Created by Bibhor Chauhan on 04-05-2017.
 */

public class AdapterActivityOffers extends RecyclerView.Adapter<AdapterActivityOffers.ViewHolder> {

    public static class ViewHolder extends RecyclerView.ViewHolder{
        TextView mTitle, mBody;
        ImageView mDelete;
        CardView mCard;

        public ViewHolder(View itemView) {
            super(itemView);
            mTitle = (TextView)itemView.findViewById(R.id.senderTitle);
            mBody = (TextView)itemView.findViewById(R.id.offerBody);
            mDelete = (ImageView)itemView.findViewById(R.id.ic_delete);
            mCard = (CardView) itemView.findViewById(R.id.new_card);
        }
    }

    List<classOffers> offersList;
    Context context;

    AdapterActivityOffers(List<classOffers> offersList, Context context){
        this.offersList = offersList;
        this.context = context;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public AdapterActivityOffers.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.card_layout_offers, viewGroup, false);
        AdapterActivityOffers.ViewHolder viewHolder = new AdapterActivityOffers.ViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        holder.mTitle.setText(offersList.get(position).getSender());
        holder.mBody.setText(offersList.get(position).getOffer());
        holder.mDelete.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                deleteEntry(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return offersList.size();
    }

    private void deleteEntry(final int pos){
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(context);
        View mView = LayoutInflater.from(context).inflate(R.layout.delete_transaction, null);
        TextView mYes = (TextView) mView.findViewById(R.id.delete_yes);
        TextView mNo = (TextView) mView.findViewById(R.id.delete_no);
        mBuilder.setView(mView);
        final AlertDialog dialog = mBuilder.create();

        mYes.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                SQLiteDatabase mydb = null;
                String path = "/data/data/com.cbibhor.eme.expensemanagereconomizer/databases/Main_Database";
                mydb = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READWRITE);
                mydb.execSQL("DELETE FROM tableOffers WHERE id = " + offersList.get(pos).getId());
                mydb.close();
                ActivityOffers inst = ActivityOffers.instance();
                inst.resetView();
                dialog.dismiss();
            }
        });

        mNo.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }
}
