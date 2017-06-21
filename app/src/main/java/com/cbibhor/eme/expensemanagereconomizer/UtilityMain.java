package com.cbibhor.eme.expensemanagereconomizer;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by Bibhor Chauhan on 03-05-2017.
 */

public class UtilityMain {

    static SQLiteDatabase mydb = null;
    List<classTransaction> transactionsList;
    FilterUtility objFilter = new FilterUtility();
    DatabaseHelper objDbHelper = new DatabaseHelper();

    public void createDatabase(Context context){
        mydb = context.openOrCreateDatabase("Main_Database", Context.MODE_PRIVATE ,null);
        mydb.execSQL("CREATE TABLE IF NOT EXISTS tableTransactions (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "tag VARCHAR," +
                "amount INTEGER," +
                "smsDay VARCHAR," +
                "smsDD VARCHAR," +
                "smsMM VARCHAR," +
                "smsYY VARCHAR," +
                "smsDate DATE);");
        mydb.execSQL("CREATE TABLE IF NOT EXISTS tableOffers (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "tag VARCHAR," +
                "offertext VARCHAR);");
        mydb.close();
    }

    public List<classTransaction> readDatabase(){
        transactionsList = new ArrayList<>();
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
            c = mydb.rawQuery("SELECT * FROM tableTransactions ORDER BY smsDate ASC", null);
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
                    transactionsList.add(smsData);
                }while (c.moveToPrevious());
            }
        }
        c.close();
        mydb.close();
        return transactionsList;
    }

    public void scanInbox(Context context){
        String mSender="";
        String mMessage="";
        String sender = "";
        Integer amount = 0;
        String msgDate = "";
        String smsDay = "";
        String smsDD="";
        String smsMM="";
        String smsYY="";
        String strfinal="";
        String path = "/data/data/com.cbibhor.eme.expensemanagereconomizer/databases/Main_Database";
        File dbfile = new File(path);
        if (!dbfile.exists()) {
            mydb = context.openOrCreateDatabase("Main_Database", Context.MODE_PRIVATE ,null);
            mydb.execSQL("CREATE TABLE IF NOT EXISTS tableTransactions (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "tag VARCHAR," +
                    "amount INTEGER," +
                    "smsDay VARCHAR," +
                    "smsDD VARCHAR," +
                    "smsMM VARCHAR," +
                    "smsYY VARCHAR," +
                    "smsDate DATE);");
            mydb.execSQL("CREATE TABLE IF NOT EXISTS tableOffers (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "tag VARCHAR," +
                    "offertext VARCHAR);");
            mydb.close();
        }
        mydb = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READWRITE);
        Uri uri = Uri.parse("content://sms/inbox");
        Cursor c = context.getContentResolver().query(uri, null, null ,null,null);
        if(c.moveToLast()) {
            for(int i=0; i < c.getCount(); i++) {
                mMessage = c.getString(c.getColumnIndexOrThrow("body")).toString();
                mSender = c.getString(c.getColumnIndexOrThrow("address")).toString();
                String date =  c.getString(c.getColumnIndex("date"));
                Long timestamp = Long.parseLong(date);
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(timestamp);
                Date finaldate = calendar.getTime();
                msgDate = finaldate.toString();
                smsDay = msgDate.substring(0,3);
                smsMM = msgDate.substring(4,7);
                smsDD = msgDate.substring(8,10);
                smsYY = msgDate.substring(24,28);
                mSender = mSender.replaceAll("'", " ");
                mMessage = mMessage.replaceAll("'", " ");
                strfinal = objDbHelper.formatDate(smsDD+"-"+smsMM+"-"+smsYY);
                Bundle bundle = objFilter.foundSender(mSender, mMessage);
                if(bundle != null) {
                    sender = bundle.getString("sender");
                    amount = bundle.getInt("message");
                    mydb.execSQL("INSERT INTO tableTransactions (tag, amount, smsDay, smsDD, smsMM, smsYY, smsDate) VALUES(" +
                            "'" + sender + "'," + amount + ",'" + smsDay + "','" + smsDD + "','" + smsMM + "','" + smsYY + "','" + strfinal + "')");
                }
                else{
                    Bundle bundleOffers = objFilter.isOffer(mSender, mMessage);
                    if(bundleOffers != null) {
                        String tSender = bundleOffers.getString("sender");
                        mydb.execSQL("INSERT INTO tableOffers (tag, offertext) VALUES(" +
                                "'" + tSender + "','" + mMessage + "')");
                    }
                }
                c.moveToPrevious();
            }
        }
        mydb.close();
        c.close();
    }
}
