package com.cbibhor.eme.expensemanagereconomizer;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static java.lang.StrictMath.max;

/**
 * Created by Bibhor Chauhan on 10-05-2017.
 */

public class DatabaseHelper {
    static SQLiteDatabase mydb = null;

    public void writetoDB(Bundle bundle){
        String sender, smsDay, smsDD, smsMM, smsYY, finalDate;
        int amount;
        String path = "/data/data/com.cbibhor.eme.expensemanagereconomizer/databases/Main_Database";
        mydb = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READWRITE);
        sender = bundle.getString("sender");
        amount = bundle.getInt("amount");
        smsDD = bundle.getString("date");
        smsMM = bundle.getString("month");
        smsYY = bundle.getString("year");
        smsDay = getDay(smsDD,smsMM,smsYY);
        finalDate = formatDate(smsDD+"-"+smsMM+"-"+smsYY);
        sender = sender.replaceAll("'", " ");
        mydb.execSQL("INSERT INTO tableTransactions (tag, amount, smsDay, smsDD, smsMM, smsYY, smsDate) VALUES(" +
                "'" + sender + "','" + amount + "','" + smsDay + "','" + smsDD + "','" + smsMM + "','" + smsYY + "','" + finalDate + "')");
        mydb.close();
    }

    public void updateDB(Integer id, Bundle bundle){
        String sender, smsDay, smsDD, smsMM, smsYY, finalDate;
        int amount;
        String path = "/data/data/com.cbibhor.eme.expensemanagereconomizer/databases/Main_Database";
        mydb = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READWRITE);
        sender = bundle.getString("sender");
        amount = bundle.getInt("amount");
        smsDD = bundle.getString("date");
        smsMM = bundle.getString("month");
        smsYY = bundle.getString("year");
        smsDay = getDay(smsDD,smsMM,smsYY);
        finalDate = formatDate(smsDD+"-"+smsMM+"-"+smsYY);
        sender = sender.replaceAll("'", " ");
        mydb.execSQL("UPDATE tableTransactions " +
                "SET tag = '" + sender + "', amount = " + amount + ", smsDay = '" + smsDay + "', smsDD = '" + smsDD + "', " +
                        "smsMM = '" + smsMM + "', smsYY = '" + smsYY + "', smsDate = '" + finalDate + "' " +
                        "WHERE id = " + id);
        mydb.close();
    }

    public void deletefromDB(int Id){
        String path = "/data/data/com.cbibhor.eme.expensemanagereconomizer/databases/Main_Database";
        mydb = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READWRITE);
        mydb.execSQL("DELETE FROM tableTransactions WHERE id = " + Id);
        mydb.close();
    }

    public Integer amountSpent(String begin, String end){
        Integer amtSpnt = 0;
        begin = formatDate(begin);
        end = formatDate(end);
        String path = "/data/data/com.cbibhor.eme.expensemanagereconomizer/databases/Main_Database";
        mydb = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READWRITE);
        Cursor c = mydb.rawQuery("SELECT SUM(amount) FROM tableTransactions WHERE smsDate BETWEEN '"+begin+"' AND '"+end+"'", null);
        c.moveToFirst();
        if(c.getCount()>0) amtSpnt = max(0,c.getInt(0));
        mydb.close();
        c.close();
        return amtSpnt;
    }

    private String getDay(String dd, String MM, String yy){
        String input_date = dd + "-" + MM + "-" + yy;
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");
        Date myDate = null;
        try {
            myDate = dateFormat.parse(input_date);

        } catch (ParseException e) {
            e.printStackTrace();
        }
        DateFormat df = new SimpleDateFormat("EE");
        String finalDay = df.format(myDate);
        return finalDay;
    }

    public String formatDate(String input_date){
        SimpleDateFormat inputdateFormat = new SimpleDateFormat("dd-MMM-yyyy");
        Date myDate = null;
        try {
            myDate = inputdateFormat.parse(input_date);

        } catch (ParseException e) {
            e.printStackTrace();
        }
        DateFormat outputDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String finalDate = outputDateFormat.format(myDate);
        return finalDate;
    }
}
