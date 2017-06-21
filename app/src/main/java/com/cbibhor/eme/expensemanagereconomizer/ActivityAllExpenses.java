package com.cbibhor.eme.expensemanagereconomizer;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import static java.lang.StrictMath.max;

public class ActivityAllExpenses extends AppCompatActivity {

    public static final String EXTRA_PARAM_ID = "place_id";
    private TextView mTitle, noExpense;
    private HomeMenu mPlace;
    ViewPager viewPager;
    TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().requestFeature(android.view.Window.FEATURE_CONTENT_TRANSITIONS);
        getWindow().requestFeature(android.view.Window.FEATURE_ACTIVITY_TRANSITIONS);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_expenses);
        mTitle = (TextView) findViewById(R.id.textView);
        noExpense = (TextView)findViewById(R.id.titleNoExpense);
        int mPositionRef = getIntent().getExtras().getInt(EXTRA_PARAM_ID,0);
        View base = findViewById(R.id.detail_layout);
        ViewCompat.setTransitionName(base, "cardViewTransition" + mPositionRef);
        ViewCompat.setTransitionName(mTitle, "textTransition" + mPositionRef);
        mPlace = HomeMenuData.setHomeMenuList().get(mPositionRef);
        mTitle.setText(mPlace.name);
        noExpense.setVisibility(View.INVISIBLE);
        viewPager = (ViewPager)findViewById(R.id.viewpager);
        tabLayout = (TabLayout)findViewById(R.id.tabs);
        addFragments();
        tabLayout.setupWithViewPager(viewPager);
    }

    private void addFragments(){
        fragmentPagerAdapter adapter = new fragmentPagerAdapter(getSupportFragmentManager(),this);
        SQLiteDatabase mydb=null;
        String path = "/data/data/com.cbibhor.eme.expensemanagereconomizer/databases/Main_Database";
        mydb = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READWRITE);
        Cursor c = mydb.rawQuery("SELECT DISTINCT smsMM, smsYY FROM tableTransactions ORDER BY smsDate ASC", null);
        int colMM = c.getColumnIndex("smsMM");
        int colYY = c.getColumnIndex("smsYY");
        c.moveToLast();
        if(c.getCount()==0) noExpense.setVisibility(View.VISIBLE);
        if(c.getCount()<=4) tabLayout.setTabMode(TabLayout.MODE_FIXED);
        else if(c.getCount()>4) tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
        if(c.getCount()>0){
            do{
                String month = c.getString(colMM);
                String year = c.getString(colYY);
                adapter.addFragmentTitle(month,year);
            }while (c.moveToPrevious());
        }
        mydb.close();
        c.close();
        viewPager.setAdapter(adapter);
    }
}
