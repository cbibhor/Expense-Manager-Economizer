package com.cbibhor.eme.expensemanagereconomizer;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Bibhor Chauhan on 13-05-2017.
 */

public class PageFragment extends android.support.v4.app.Fragment {
    public static final String ARG_PAGE = "ARG_PAGE";
    private int mPage;
    List<classTransaction> monthlyList;
    static List<String> tabList = new ArrayList<>();
    RecyclerView recyclerView;
    FragmentCardAdapter trans_adapter;
    private static PageFragment inst;

    public static PageFragment instance(){
        return inst;
    }

    public static PageFragment newInstance(int page, List<String> tabTitles) {
        tabList = tabTitles;
        Bundle args = new Bundle();
        args.putInt(ARG_PAGE, page);
        PageFragment fragment = new PageFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPage = getArguments().getInt(ARG_PAGE);
        inst=this;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        inst=this;
        View view = inflater.inflate(R.layout.fragment_monthly, container, false);
        recyclerView = (RecyclerView) view.findViewById(R.id.expenses_recycler_view);
        setUpAdapter();
        return view;
    }

    private void setUpAdapter(){
        getMonthlyList();
        RecyclerView.LayoutManager trans_layoutManager = new GridLayoutManager(getActivity(), 2);
        recyclerView.setLayoutManager(trans_layoutManager);
        trans_adapter = new FragmentCardAdapter(monthlyList, getActivity());
        recyclerView.setAdapter(trans_adapter);
    }

    private void getMonthlyList(){
        monthlyList = new ArrayList<>();
        SQLiteDatabase mydb=null;
        String path = "/data/data/com.cbibhor.eme.expensemanagereconomizer/databases/Main_Database";
        mydb = SQLiteDatabase.openOrCreateDatabase(path, null);
        mydb.execSQL("CREATE TABLE IF NOT EXISTS tableTransactions (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "tag VARCHAR," +
                "amount INTEGER," +
                "smsDay VARCHAR," +
                "smsDD VARCHAR," +
                "smsMM VARCHAR," +
                "smsYY VARCHAR," +
                "smsDate DATE);");
        Cursor c = null;
        if(mydb != null) {
            c = mydb.rawQuery("SELECT * FROM tableTransactions WHERE smsMM = '"+tabList.get(mPage).substring(0,3)+"' AND smsYY = '"+tabList.get(mPage).substring(5,9)+"'", null);
            int colID = c.getColumnIndex("id");
            int colSender = c.getColumnIndex("tag");
            int colMsg = c.getColumnIndex("amount");
            int colDay = c.getColumnIndex("smsDay");
            int colDD = c.getColumnIndex("smsDD");
            int colMM = c.getColumnIndex("smsMM");
            int colYY = c.getColumnIndex("smsYY");
            c.moveToLast();
            Integer mID=0;
            String sender = "";
            Integer amt=0;
            String smsDay = "";
            String smsDD="";
            String smsMM="";
            String smsYY="";
            if(c.getCount() > 0){
                do{
                    classTransaction smsData = new classTransaction();
                    mID = c.getInt(colID);
                    sender = c.getString(colSender);
                    amt = c.getInt(colMsg);
                    smsDay = c.getString(colDay);
                    smsDD = c.getString(colDD);
                    smsMM = c.getString(colMM);
                    smsYY = c.getString(colYY);
                    smsData.setId(mID);
                    smsData.setAddress(sender);
                    smsData.setBody(amt);
                    smsData.setSmsDay(smsDay);
                    smsData.setSmsDD(smsDD);
                    smsData.setSmsMM(smsMM);
                    smsData.setSmsYY(smsYY);
                    monthlyList.add(smsData);
                }while (c.moveToPrevious());
            }
        }
        c.close();
        mydb.close();
    }

    public void resetPage(){
        monthlyList.clear();
        getMonthlyList();
        setUpAdapter();
        trans_adapter.notifyDataSetChanged();
    }
}
