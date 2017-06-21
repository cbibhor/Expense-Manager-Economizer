package com.cbibhor.eme.expensemanagereconomizer;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ActivityOffers extends AppCompatActivity {

    public static final String EXTRA_PARAM_ID = "place_id";
    private TextView mTitle, textNoOffers;
    private HomeMenu mPlace;
    RecyclerView recyclerView;
    List<classOffers> offersList;
    static SQLiteDatabase mydb = null;
    AdapterActivityOffers adapterActivityOffers;
    private static ActivityOffers inst;

    public static ActivityOffers instance(){return inst;}

    @Override
    protected void onStart() {
        super.onStart();
        inst = this;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().requestFeature(android.view.Window.FEATURE_CONTENT_TRANSITIONS);
        getWindow().requestFeature(android.view.Window.FEATURE_ACTIVITY_TRANSITIONS);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offers);
        recyclerView = (RecyclerView) findViewById(R.id.offers_recycler_view);
        textNoOffers = (TextView)findViewById(R.id.textNoOffers);
        mTitle = (TextView) findViewById(R.id.textView);
        int mPositionRef = getIntent().getExtras().getInt(EXTRA_PARAM_ID,0);
        View base = findViewById(R.id.detail_layout);
        ViewCompat.setTransitionName(base, "cardViewTransition" + mPositionRef);
        ViewCompat.setTransitionName(mTitle, "textTransition" + mPositionRef);
        mPlace = HomeMenuData.setHomeMenuList().get(mPositionRef);
        mTitle.setText(mPlace.name);
        setUpList();
        setUpAdapter();
    }

    private void setUpAdapter() {
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(this,2);
        recyclerView.setLayoutManager(layoutManager);
        adapterActivityOffers = new AdapterActivityOffers(offersList, ActivityOffers.this);
        recyclerView.setAdapter(adapterActivityOffers);
        if(offersList.isEmpty()){
            textNoOffers.setVisibility(View.VISIBLE);
        }
        else{
            textNoOffers.setVisibility(View.INVISIBLE);
        }
    }

    private void setUpList(){
        offersList = new ArrayList<>();
        Integer mId;
        String mSender="";
        String mMessage="";
        String path = "/data/data/com.cbibhor.eme.expensemanagereconomizer/databases/Main_Database";
        mydb = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READWRITE);
        mydb.execSQL("CREATE TABLE IF NOT EXISTS tableOffers (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "tag VARCHAR," +
                "offertext VARCHAR);");
        Cursor c = null;
        if(mydb!=null){
            c = mydb.rawQuery("SELECT * FROM tableOffers", null);
            int colId = c.getColumnIndex("id");
            int colSender = c.getColumnIndex("tag");
            int colOffer = c.getColumnIndex("offertext");
            c.moveToLast();
            if(c.getCount()>0){
                do{
                    classOffers obj = new classOffers();
                    mId = c.getInt(colId);
                    mSender = c.getString(colSender);
                    mMessage = c.getString(colOffer);
                    obj.setId(mId);
                    obj.setSender(mSender);
                    obj.setOffer(mMessage);
                    offersList.add(obj);
                }while (c.moveToPrevious());
            }
        }
        c.close();
        mydb.close();
    }

    public void resetView(){
        offersList.clear();
        setUpList();
        setUpAdapter();
        adapterActivityOffers.notifyDataSetChanged();
    }
}
