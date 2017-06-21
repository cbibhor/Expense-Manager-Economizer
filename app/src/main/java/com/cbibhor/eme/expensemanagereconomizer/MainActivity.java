package com.cbibhor.eme.expensemanagereconomizer;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.ArcShape;
import android.media.Image;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.util.Pair;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static java.lang.StrictMath.max;


public class MainActivity extends AppCompatActivity {

    public static final String BROADCAST = "android.status_broadcast.MainEME";
    List<classTransaction> transactionsList;
    List<HomeMenu> homeMenuList;
    RVAdapterTransactions trans_adapter;
    RecyclerView trans_recyclerView, opt_recyclerView;
    TextView amountSpent, amountSafe, txtSpent, txtSafe, helpText, textNoExpense;
    LinearLayout bars, midDetails;
    View barlineTop, barlineBottom, barlineLeft, barlineRight;
    String curDate="", curMonth="", curYear="";
    UtilityMain objUtility = new UtilityMain();
    FileHelper objFileHelper = new FileHelper();
    DatabaseHelper dbHelper = new DatabaseHelper();
    private static MainActivity inst;
    Runnable run;

    public static MainActivity instance(){
        return inst;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().requestFeature(android.view.Window.FEATURE_CONTENT_TRANSITIONS);
        getWindow().requestFeature(android.view.Window.FEATURE_ACTIVITY_TRANSITIONS);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        txtSpent = (TextView)findViewById(R.id.txtSpent);
        txtSafe = (TextView)findViewById(R.id.txtSafe);
        bars = (LinearLayout)findViewById(R.id.bars);
        amountSpent = (TextView) findViewById(R.id.amtSpent);
        amountSafe = (TextView) findViewById(R.id.amtSafe);
        barlineTop = (View)findViewById(R.id.barlineTop);
        barlineBottom = (View)findViewById(R.id.barlineBottom);
        barlineLeft = (View)findViewById(R.id.barlineLeft);
        barlineRight = (View)findViewById(R.id.barlineRight);
        midDetails = (LinearLayout)findViewById(R.id.midDetails);
        helpText = (TextView) findViewById(R.id.helpText);
        trans_recyclerView = (RecyclerView) findViewById(R.id.trecycler_view);
        textNoExpense = (TextView)findViewById(R.id.textNoExpenses);
        ImageView mAbout = (ImageView) findViewById(R.id.ic_about);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        boolean isFirst = prefs.getBoolean("isFirst", false);
        if(!isFirst){
            SharedPreferences.Editor mEdit = prefs.edit();
            mEdit.putBoolean("isFirst", true);
            mEdit.commit();
            objUtility.createDatabase(MainActivity.this);
            objUtility.scanInbox(MainActivity.this);
            //objUtility.getOffers(MainActivity.this);
            String initialise = "false" + System.getProperty("line.separator") +
                                "0" + System.getProperty("line.separator") +
                                "0" + System.getProperty("line.separator") +
                                "null" + System.getProperty("line.separator") +
                                "null";
            objFileHelper.saveToFile(initialise);
        }

        setSpendLimit();
        setOptionsCards();
        setTransactionCards();
        run = new Runnable() {
            @Override
            public void run() {
                transactionsList.clear();
                setTransactionCards();
                trans_adapter.notifyDataSetChanged();
            }
        };

        mAbout.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                showAbout();
            }
        });
    }

    private void hideViews(){
        txtSpent.setVisibility(View.INVISIBLE);
        txtSafe.setVisibility(View.INVISIBLE);
        bars.setVisibility(View.INVISIBLE);
        amountSpent.setVisibility(View.INVISIBLE);
        amountSafe.setVisibility(View.INVISIBLE);
        barlineTop.setVisibility(View.INVISIBLE);
        barlineBottom.setVisibility(View.INVISIBLE);
        barlineLeft.setVisibility(View.INVISIBLE);
        barlineRight.setVisibility(View.INVISIBLE);
        midDetails.setVisibility(View.INVISIBLE);
        helpText.setVisibility(View.VISIBLE);
    }

    private void showViews(){
        txtSpent.setVisibility(View.VISIBLE);
        txtSafe.setVisibility(View.VISIBLE);
        bars.setVisibility(View.VISIBLE);
        amountSpent.setVisibility(View.VISIBLE);
        amountSafe.setVisibility(View.VISIBLE);
        barlineTop.setVisibility(View.VISIBLE);
        barlineBottom.setVisibility(View.VISIBLE);
        barlineLeft.setVisibility(View.VISIBLE);
        barlineRight.setVisibility(View.VISIBLE);
        midDetails.setVisibility(View.VISIBLE);
        helpText.setVisibility(View.INVISIBLE);
    }

    private void notifyVisible(){
        Intent intent = new Intent();
        intent.setAction(BROADCAST);
        Bundle extra = new Bundle();
        extra.putString("status","true");
        intent.putExtras(extra);
        sendBroadcast(intent);
    }

    private void notifyNotVisible(){
        Intent intent = new Intent();
        intent.setAction(BROADCAST);
        Bundle extra = new Bundle();
        extra.putString("status","false");
        intent.putExtras(extra);
        sendBroadcast(intent);
    }

    @Override
    protected void onPause() {
        super.onPause();
        notifyNotVisible();
    }

    @Override
    protected void onResume() {
        super.onResume();
        notifyVisible();
        runOnUiThread(run);
        setSpendLimit();
    }

    @Override
    protected void onStart() {
        super.onStart();
        inst=this;
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        notifyNotVisible();
    }

    public void notifyUpdate(){
        runOnUiThread(run);
    }

    public void setSpendLimit(){
        long daysLeft=0;
        List<String> arrString;
        arrString = objFileHelper.ReadFile();
        if(arrString.get(0).equalsIgnoreCase("true")){
            showViews();
            Integer left=0;
            Integer spent = Integer.parseInt(arrString.get(1));
            Integer safe = Integer.parseInt(arrString.get(2));
            String startDate = arrString.get(3);
            String endDate = arrString.get(4);
            String todaysDate = getSystemDate();
            if(parseDate(endDate).compareTo(parseDate(todaysDate)) > 0){
                long diff = parseDate(endDate).getTime() - parseDate(todaysDate).getTime();
                daysLeft = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
            }
            TextView limitBeginDate = (TextView) findViewById(R.id.limitBeginDate);
            TextView limitEndDate = (TextView) findViewById(R.id.limitEndDate);
            TextView numDays = (TextView) findViewById(R.id.numDays);
            LinearLayout bar_spend = (LinearLayout) findViewById(R.id.bar_spend);
            LinearLayout bar_safe = (LinearLayout) findViewById(R.id.bar_safe);
            LinearLayout.LayoutParams params1 = (LinearLayout.LayoutParams) bar_spend.getLayoutParams();
            LinearLayout.LayoutParams params2 = (LinearLayout.LayoutParams) bar_safe.getLayoutParams();
            if(spent==0) {
                left=safe;
                params1.weight = 0.001f;
                params2.weight = 0.999f;
            }
            else if(spent>=safe){
                left=0;
                params1.weight = 0.999f;
                params2.weight = 0.001f;
            }
            else {
                left=safe-spent;
                params1.weight = (float) spent / safe;
                params2.weight = (float) 1 - params1.weight;
            }
            amountSpent.setText(getResources().getString(R.string.rupeeSymbol) + arrString.get(1));
            amountSafe.setText(getResources().getString(R.string.rupeeSymbol) + Integer.toString(left));
            limitBeginDate.setText(startDate);
            limitEndDate.setText(endDate);
            numDays.setText(Long.toString(daysLeft));
        }
        else{
            hideViews();
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

    private String getSystemDate(){
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
        String formattedDate = df.format(c.getTime());
        return formattedDate;
    }

    private void setOptionsCards(){
        opt_recyclerView = (RecyclerView)findViewById(R.id.orecycler_view);
        RecyclerView.LayoutManager opt_layoutManager = new GridLayoutManager(this,4);
        opt_recyclerView.setLayoutManager(opt_layoutManager);
        HomeMenuData homeMenuData = new HomeMenuData();
        homeMenuList = homeMenuData.setHomeMenuList();
        HomeAdapter opt_adapter = new HomeAdapter(this,homeMenuList);
        opt_recyclerView.setAdapter(opt_adapter);
        opt_adapter.setOnItemClickListener(onItemClickListener);
    }

    private void setTransactionCards(){
        transactionsList = objUtility.readDatabase();
        RecyclerView.LayoutManager trans_layoutManager = new GridLayoutManager(this, 2);
        trans_recyclerView.setLayoutManager(trans_layoutManager);
        trans_adapter = new RVAdapterTransactions(transactionsList, MainActivity.this);
        trans_recyclerView.setAdapter(trans_adapter);
        if(transactionsList.isEmpty()){
            textNoExpense.setVisibility(View.VISIBLE);
        }
        else{
            textNoExpense.setVisibility(View.INVISIBLE);
        }
    }

    HomeAdapter.OnItemClickListener onItemClickListener = new HomeAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(View v, int position) {
            Intent intent;
            TextView textView = (TextView) v.findViewById(R.id.placeName);
            Pair<View, String> pair3 = Pair.create((View) textView, ViewCompat.getTransitionName(textView));
            ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(MainActivity.this, pair3);
            if (position == 0) {
                intent = new Intent(MainActivity.this, ActivityOffers.class);
                intent.putExtra(ActivityOffers.EXTRA_PARAM_ID, position);
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    ActivityCompat.startActivity(MainActivity.this, intent, options.toBundle());
                } else {
                    startActivity(intent);
                }
            }
            else if (position == 1) {
                intent = new Intent(MainActivity.this, ActivitySetLimit.class);
                intent.putExtra(ActivitySetLimit.EXTRA_PARAM_ID, position);
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    ActivityCompat.startActivity(MainActivity.this, intent, options.toBundle());
                } else {
                    startActivity(intent);
                }
            }
            else if (position == 2) {
                intent = new Intent(MainActivity.this, ActivityAllExpenses.class);
                intent.putExtra(ActivityAllExpenses.EXTRA_PARAM_ID, position);
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    ActivityCompat.startActivity(MainActivity.this, intent, options.toBundle());
                } else {
                    startActivity(intent);
                }
            }
            else if (position==3){
                showNewTransactionBox();
            }
        }
    };

    private void showNewTransactionBox(){
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
        String formattedDate = df.format(c.getTime());
        curDate = formattedDate.substring(0,2);
        curMonth = formattedDate.substring(3,6);
        curYear = formattedDate.substring(7,11);
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(MainActivity.this);
        View mView = getLayoutInflater().inflate(R.layout.add_new_transaction, null);
        final EditText mTag = (EditText) mView.findViewById(R.id.edittag);
        final Spinner dateSpinner = (Spinner) mView.findViewById(R.id.spinnerDate);
        final Spinner monthSpinner = (Spinner) mView.findViewById(R.id.spinnerMonth);
        final Spinner yearSpinner = (Spinner) mView.findViewById(R.id.spinnerYear);
        final EditText mAmount = (EditText) mView.findViewById(R.id.editamount);
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
                    dbHelper.writetoDB(bundle);
                    Date setDate = parseDate(curDate+"-"+curMonth+"-"+curYear);
                    List<String> arrString;
                    arrString = objFileHelper.ReadFile();
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
                            setSpendLimit();
                        }
                    }
                    notifyUpdate();
                    dialog.dismiss();
                }
            }
        });

        dialog.show();
    }

    private void addItemsOnDateSpinner(Spinner dateSpinner){
        final List<String> list = new ArrayList<>();
        Integer pos=0;
        for(int i=1; i<=9; i++) {
            if(curDate.equalsIgnoreCase(Integer.toString(i)))
                pos=i;
            list.add("0" + Integer.toString(i));
        }
        for(int i=10; i<=31; i++) {
            if(curDate.equalsIgnoreCase(Integer.toString(i)))
                pos=i;
            list.add(Integer.toString(i));
        }
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dateSpinner.setAdapter(dataAdapter);
        dateSpinner.setSelection(pos-1);
        dateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                curDate = list.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void addItemsOnMonthSpinner(Spinner monthSpinner){
        int pos=0;
        final String[] list = {"Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"};
        for(int i=0; i<list.length; i++)
            if(curMonth.equalsIgnoreCase(list[i]))
                pos=i;
        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, list);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        monthSpinner.setAdapter(adapter);
        monthSpinner.setSelection(pos);
        monthSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                curMonth = list[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void addItemsOnYearSpinner(Spinner yearSpinner){
        int pos=0;
        final String[] list = {"2016","2017","2018","2019","2020"};
        for(int i=0; i<list.length; i++)
            if(curYear.equalsIgnoreCase(list[i]))
                pos=i;
        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, list);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        yearSpinner.setAdapter(adapter);
        yearSpinner.setSelection(pos);
        yearSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                curYear = list[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void showAbout(){
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(MainActivity.this);
        View mView = getLayoutInflater().inflate(R.layout.layout_about, null);
        mBuilder.setView(mView);
        final AlertDialog dialog = mBuilder.create();
        dialog.show();
    }

}
