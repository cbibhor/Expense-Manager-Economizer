package com.cbibhor.eme.expensemanagereconomizer;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static java.lang.StrictMath.max;

public class ActivitySetLimit extends AppCompatActivity {

    public static final String EXTRA_PARAM_ID = "place_id";
    private TextView mTitle, mSubmit, mAmount;
    private HomeMenu mPlace;
    private FileHelper obj = new FileHelper();
    private String begDate="", begMonth="", begYear="", endDate="", endMonth="", endYear="", curAmount="";
    private String formattedDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().requestFeature(android.view.Window.FEATURE_CONTENT_TRANSITIONS);
        getWindow().requestFeature(android.view.Window.FEATURE_ACTIVITY_TRANSITIONS);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_set_limit);
        mTitle = (TextView) findViewById(R.id.textView);
        mAmount = (TextView) findViewById(R.id.amtLimit);
        mSubmit = (TextView) findViewById(R.id.submitLimit);
        int mPositionRef = getIntent().getExtras().getInt(EXTRA_PARAM_ID, 0);
        View base = findViewById(R.id.detail_layout);
        ViewCompat.setTransitionName(base, "cardViewTransition" + mPositionRef);
        ViewCompat.setTransitionName(mTitle, "textTransition" + mPositionRef);
        mPlace = HomeMenuData.setHomeMenuList().get(mPositionRef);
        mTitle.setText(mPlace.name);
        final Spinner beginday, beginMonth, beginYear, endDaySpinner, endMonthSpinner, endYearSpinner;
        beginday = (Spinner)findViewById(R.id.beginDay);
        beginMonth = (Spinner)findViewById(R.id.beginMonth);
        beginYear = (Spinner)findViewById(R.id.beginYear);
        endDaySpinner = (Spinner)findViewById(R.id.endDay);
        endMonthSpinner = (Spinner)findViewById(R.id.endMonth);
        endYearSpinner = (Spinner)findViewById(R.id.endYear);
        getSystemDate();
        final Date todaysDate = parseDate(formattedDate);
        List<String> arrString = ReadFile();
        if(arrString.get(0).equalsIgnoreCase("true")){
            Integer spent = Integer.parseInt(arrString.get(1));
            Integer safe = Integer.parseInt(arrString.get(2));
            String startDate = arrString.get(3);
            String endDateC = arrString.get(4);
            begDate = startDate.substring(0,2);
            begMonth = startDate.substring(3,6);
            begYear = startDate.substring(7,11);
            endDate = endDateC.substring(0,2);
            endMonth = endDateC.substring(3,6);
            endYear = endDateC.substring(7,11);
            curAmount = Integer.toString(safe);
        }
        addItemsToBeginDateSpinner(beginday);
        addItemsToBeginMonthSpinner(beginMonth);
        addItemsToBeginYearSpinner(beginYear);
        addItemsToEndDateSpinner(endDaySpinner);
        addItemsToEndMonthSpinner(endMonthSpinner);
        addItemsToEndYearSpinner(endYearSpinner);
        mSubmit.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Date finalbeginDate = parseDate(begDate+"-"+begMonth+"-"+begYear);
                Date finalendDate = parseDate(endDate+"-"+endMonth+"-"+endYear);
                if(mAmount.getText().toString().isEmpty())
                    mAmount.setError("Enter Amount");
                else if(finalendDate.compareTo(finalbeginDate)<=0 || finalendDate.compareTo(todaysDate)<=0)
                    Toast.makeText(ActivitySetLimit.this,"Set Begin and End Dates",Toast.LENGTH_SHORT).show();
                else{
                    String finalAmount = mAmount.getText().toString();
                    DatabaseHelper dbobj = new DatabaseHelper();
                    Integer curSpent = dbobj.amountSpent(begDate+"-"+begMonth+"-"+begYear, endDate+"-"+endMonth+"-"+endYear);
                    Integer finnalAmt = Integer.parseInt(finalAmount);
                    String finalString = "true" + System.getProperty("line.separator") +
                                    curSpent + System.getProperty("line.separator") +
                                    Integer.toString(finnalAmt) + System.getProperty("line.separator") +
                                    begDate+"-"+begMonth+"-"+begYear + System.getProperty("line.separator") +
                                    endDate+"-"+endMonth+"-"+endYear;
                    obj.saveToFile(finalString);
                    Toast.makeText(ActivitySetLimit.this,"Limit Set",Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        });
        mAmount.setText(curAmount);
    }

    public static List<String> ReadFile(){
        final String fileName = "limit.txt";
        final String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/.ExpenseManagerFiles/";
        final String TAG = FileHelper.class.getName();
        String line = null;
        List<String> arrString = new ArrayList<>();
        try {
            FileInputStream fileInputStream = new FileInputStream (path+fileName);
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            while((line = bufferedReader.readLine()) != null){
                arrString.add(line);
            }
            fileInputStream.close();
            bufferedReader.close();
        }
        catch(FileNotFoundException ex) {
            Log.d(TAG, ex.getMessage());
        }
        catch(IOException ex) {
            Log.d(TAG, ex.getMessage());
        }
        return arrString;
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

    private void getSystemDate(){
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
        formattedDate = df.format(c.getTime());
        begDate = formattedDate.substring(0,2);
        begMonth = formattedDate.substring(3,6);
        begYear = formattedDate.substring(7,11);
        endDate=begDate; endMonth=begMonth; endYear=begYear;
    }

    private void addItemsToBeginDateSpinner(Spinner dateSpinner){
        final List<String> list = new ArrayList<>();
        Integer pos=0;
        for(int i=1; i<=9; i++) {
            if(begDate.equalsIgnoreCase("0"+Integer.toString(i)))
                pos=i;
            list.add("0" + Integer.toString(i));
        }
        for(int i=10; i<=31; i++) {
            if(begDate.equalsIgnoreCase(Integer.toString(i)))
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
                begDate = list.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void addItemsToBeginMonthSpinner(Spinner monthSpinner){
        int pos=0;
        final String[] list = {"Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"};
        for(int i=0; i<list.length; i++)
            if(begMonth.equalsIgnoreCase(list[i]))
                pos=i;
        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, list);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        monthSpinner.setAdapter(adapter);
        monthSpinner.setSelection(pos);
        monthSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                begMonth = list[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void addItemsToBeginYearSpinner(Spinner yearSpinner){
        int pos=0;
        final String[] list = {"2016","2017","2018","2019","2020"};
        for(int i=0; i<list.length; i++)
            if(begYear.equalsIgnoreCase(list[i]))
                pos=i;
        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, list);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        yearSpinner.setAdapter(adapter);
        yearSpinner.setSelection(pos);
        yearSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                begYear = list[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void addItemsToEndDateSpinner(Spinner dateSpinner){
        final List<String> list = new ArrayList<>();
        Integer pos=0;
        for(int i=1; i<=9; i++) {
            if(endDate.equalsIgnoreCase("0"+Integer.toString(i)))
                pos=i;
            list.add("0" + Integer.toString(i));
        }
        for(int i=10; i<=31; i++) {
            if(endDate.equalsIgnoreCase(Integer.toString(i)))
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
                endDate = list.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void addItemsToEndMonthSpinner(Spinner monthSpinner){
        int pos=0;
        final String[] list = {"Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"};
        for(int i=0; i<list.length; i++)
            if(endMonth.equalsIgnoreCase(list[i]))
                pos=i;
        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, list);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        monthSpinner.setAdapter(adapter);
        monthSpinner.setSelection(pos);
        monthSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                endMonth = list[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void addItemsToEndYearSpinner(Spinner yearSpinner){
        int pos=0;
        final String[] list = {"2016","2017","2018","2019","2020"};
        for(int i=0; i<list.length; i++)
            if(endYear.equalsIgnoreCase(list[i]))
                pos=i;
        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, list);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        yearSpinner.setAdapter(adapter);
        yearSpinner.setSelection(pos);
        yearSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                endYear = list[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }
}
