package com.cbibhor.eme.expensemanagereconomizer;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static java.lang.StrictMath.max;

/**
 * Created by Bibhor Chauhan on 18-04-2017.
 */

public class RVAdapterTransactions extends RecyclerView.Adapter<RVAdapterTransactions.ViewHolder> {

    String curDate="", curMonth="", curYear="";
    MainActivity inst = MainActivity.instance();
    FileHelper objFileHelper = new FileHelper();
    DatabaseHelper dbHelper = new DatabaseHelper();

    public static class ViewHolder extends RecyclerView.ViewHolder{
        TextView address;
        TextView body;
        TextView smsDay;
        TextView smsDate;
        CardView cardView;
        ImageView mDelete;
        ImageView mEdit;

        ViewHolder(View itemView){
            super(itemView);
            address = (TextView)itemView.findViewById(R.id.smsaddress);
            body = (TextView)itemView.findViewById(R.id.smsbody);
            smsDay = (TextView) itemView.findViewById(R.id.smsDay);
            smsDate = (TextView) itemView.findViewById(R.id.smsDate);
            cardView = (CardView) itemView.findViewById(R.id.trans_card);
            mDelete = (ImageView) itemView.findViewById(R.id.ic_delete);
            mEdit = (ImageView) itemView.findViewById(R.id.ic_edit);
        }
    }

    List<classTransaction> transactionList;
    Context context;

    RVAdapterTransactions(List<classTransaction> transactionList, Context context){
        this.transactionList = transactionList;
        this.context = context;
    }


    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public RVAdapterTransactions.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.card_layout_transactions, viewGroup, false);
        RVAdapterTransactions.ViewHolder viewHolder = new RVAdapterTransactions.ViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final RVAdapterTransactions.ViewHolder holder, final int i) {
        String mDay, mDD, mMM, mYY, strfinal;
        mDay = transactionList.get(i).getSmsDay();
        mDD = transactionList.get(i).getSmsDD();
        mMM = transactionList.get(i).getSmsMM();
        mYY = transactionList.get(i).getSmsYY();
        strfinal = mDD+" "+mMM+", "+mYY;
        holder.address.setText(transactionList.get(i).getAddress());
        holder.body.setText(context.getResources().getString(R.string.rupeeSymbol)+" "+transactionList.get(i).getBody().toString());
        holder.smsDay.setText(mDay);
        holder.smsDate.setText(strfinal);
        final Integer mId = transactionList.get(i).getId();
        String mAdd = transactionList.get(i).getAddress();
        holder.mDelete.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                deleteEntry(mId, i);
            }
        });
        holder.mEdit.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                editEntry(mId, i);
            }
        });
    }

    @Override
    public int getItemCount() {
        return transactionList.size();
    }

    private void deleteEntry(final int mId, int position){
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(context);
        View mView = LayoutInflater.from(context).inflate(R.layout.delete_transaction, null);
        TextView mYes = (TextView) mView.findViewById(R.id.delete_yes);
        TextView mNo = (TextView) mView.findViewById(R.id.delete_no);
        mBuilder.setView(mView);
        final AlertDialog dialog = mBuilder.create();
        curDate = transactionList.get(position).getSmsDD();
        curMonth = transactionList.get(position).getSmsMM();
        curYear = transactionList.get(position).getSmsYY();

        mYes.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                DatabaseHelper obj = new DatabaseHelper();
                obj.deletefromDB(mId);
                updateLimitFile();
                inst.notifyUpdate();
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

    private void editEntry(final int mId, int position){
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(context);
        View mView = LayoutInflater.from(context).inflate(R.layout.add_new_transaction, null);
        final TextView mTitle = (TextView) mView.findViewById(R.id.pop_up_title);
        final EditText mTag = (EditText) mView.findViewById(R.id.edittag);
        final Spinner dateSpinner = (Spinner) mView.findViewById(R.id.spinnerDate);
        final Spinner monthSpinner = (Spinner) mView.findViewById(R.id.spinnerMonth);
        final Spinner yearSpinner = (Spinner) mView.findViewById(R.id.spinnerYear);
        final EditText mAmount = (EditText) mView.findViewById(R.id.editamount);
        mTitle.setText("Edit Expense");
        mTag.setText(transactionList.get(position).getAddress());
        mAmount.setText(transactionList.get(position).getBody().toString());
        curDate = transactionList.get(position).getSmsDD();
        curMonth = transactionList.get(position).getSmsMM();
        curYear = transactionList.get(position).getSmsYY();
        addItemsOnDateSpinner(dateSpinner);
        addItemsOnMonthSpinner(monthSpinner);
        addItemsOnYearSpinner(yearSpinner);
        TextView mCancel = (TextView) mView.findViewById(R.id.btn_cancel);
        TextView mDone = (TextView) mView.findViewById(R.id.btn_done);
        mBuilder.setView(mView);
        final AlertDialog dialog = mBuilder.create();

        mCancel.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        mDone.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(mTag.getText().toString().isEmpty())
                    mTag.setError("Required");
                if(mAmount.getText().toString().isEmpty())
                    mAmount.setError("Required");
                else{
                    DatabaseHelper obj = new DatabaseHelper();
                    Bundle bundle = new Bundle();
                    String strTag, strAmt;
                    strTag = mTag.getText().toString();
                    strAmt = mAmount.getText().toString();
                    int amt = Integer.parseInt(strAmt);
                    bundle.putString("sender",strTag);
                    bundle.putInt("amount",amt);
                    bundle.putString("date",curDate);
                    bundle.putString("month",curMonth);
                    bundle.putString("year",curYear);
                    obj.updateDB(mId,bundle);
                    updateLimitFile();
                    inst.notifyUpdate();
                    dialog.dismiss();
                }
            }
        });

        dialog.show();
    }

    private void updateLimitFile(){
        List<String> arrString;
        arrString = objFileHelper.ReadFile();
        Date setDate = parseDate(curDate+"-"+curMonth+"-"+curYear);
        if(arrString.get(0).equalsIgnoreCase("true")){
            String totalLimit = arrString.get(2);
            String startDate = arrString.get(3);
            String endDate = arrString.get(4);
            if(parseDate(endDate).compareTo(setDate)>=0 && setDate.compareTo(parseDate(startDate))>=0){
                Integer newAmount = dbHelper.amountSpent(startDate, endDate);
                String finalString = "true" + System.getProperty("line.separator") +
                        Integer.toString(newAmount) + System.getProperty("line.separator") +
                        totalLimit + System.getProperty("line.separator") +
                        startDate + System.getProperty("line.separator") +
                        endDate;
                objFileHelper.saveToFile(finalString);
                inst.setSpendLimit();
            }
        }
    }

    private Date parseDate(String input_date){
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");
        Date myDate = null;
        try {
            myDate = dateFormat.parse(input_date);

        } catch (ParseException e) {
            e.printStackTrace();
        }
        return myDate;
    }

    private void addItemsOnDateSpinner(Spinner dateSpinner){
        List<String> list = new ArrayList<>();
        Integer pos=0;
        for(int i=1; i<=9; i++) {
            if(curDate.equalsIgnoreCase("0"+Integer.toString(i)))
                pos=i;
            list.add("0" + Integer.toString(i));
        }
        for(int i=10; i<=31; i++) {
            if(curDate.equalsIgnoreCase(Integer.toString(i)))
                pos=i;
            list.add(Integer.toString(i));
        }
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dateSpinner.setAdapter(dataAdapter);
        dateSpinner.setSelection(pos-1);
        dateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void addItemsOnMonthSpinner(Spinner monthSpinner){
        int pos=0;
        String[] list = {"Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"};
        for(int i=0; i<list.length; i++)
            if(curMonth.equalsIgnoreCase(list[i]))
                pos=i;
        ArrayAdapter adapter = new ArrayAdapter(context, android.R.layout.simple_spinner_item, list);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        monthSpinner.setAdapter(adapter);
        monthSpinner.setSelection(pos);
        monthSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void addItemsOnYearSpinner(Spinner yearSpinner){
        int pos=0;
        String[] list = {"2016","2017","2018","2019","2020"};
        for(int i=0; i<list.length; i++)
            if(curYear.equalsIgnoreCase(list[i]))
                pos=i;
        ArrayAdapter adapter = new ArrayAdapter(context, android.R.layout.simple_spinner_item, list);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        yearSpinner.setAdapter(adapter);
        yearSpinner.setSelection(pos);
        yearSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }
}
