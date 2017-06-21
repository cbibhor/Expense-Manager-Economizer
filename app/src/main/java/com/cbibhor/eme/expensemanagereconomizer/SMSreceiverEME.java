package com.cbibhor.eme.expensemanagereconomizer;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.NotificationCompat;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.concurrent.TimeUnit;

import static java.lang.StrictMath.max;

/**
 * Created by Bibhor Chauhan on 26-04-2017.
 */

public class SMSreceiverEME extends BroadcastReceiver {
    MainActivity inst = MainActivity.instance();
    static SQLiteDatabase mydb=null;
    public static boolean isVisible;
    private classFilter objFilter = new classFilter();
    private classFileHelper objFile = new classFileHelper();
    private Integer nSafe, nAmount;
    private Long nDays;
    private String nSender="";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        switch (action){
            case "android.status_broadcast.MainEME":
                actionSTATUS(context, intent);
                break;
            case "android.provider.Telephony.SMS_RECEIVED":
                actionSMS(context, intent);
                break;
        }
    }

    private void actionSTATUS(Context context, Intent intent){
        Bundle bundle = intent.getExtras();
        if(bundle!=null){
            String status = bundle.getString("status");
            if(status.equalsIgnoreCase("true")) isVisible = true;
            else    isVisible = false;
        }
    }

    private void actionSMS(Context context, Intent intent){
        Bundle smsbundle = intent.getExtras();
        SmsMessage[] messages = null;
        String strSender = "";
        String strMessage = "";
        String msgDate="";
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
        if(smsbundle!=null){
            Object [] pdus = (Object[]) smsbundle.get("pdus");
            messages = new SmsMessage[pdus.length];
            for (int i = 0; i < messages.length; i++)
            {
                messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                strSender = messages[i].getOriginatingAddress();
                strMessage = messages[i].getMessageBody();
                Long timestamp = messages[i].getTimestampMillis();
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(timestamp);
                Date finaldate = calendar.getTime();
                msgDate = finaldate.toString();
                smsDay = msgDate.substring(0,3);
                smsMM = msgDate.substring(4,7);
                smsDD = msgDate.substring(8,10);
                smsYY = msgDate.substring(24,28);
            }
            strSender = strSender.replaceAll("'", " ");
            strMessage = strMessage.replaceAll("'", " ");
            strfinal = formatDate(smsDD+"-"+smsMM+"-"+smsYY);
            Bundle bundle = objFilter.foundSender(strSender, strMessage);
            if(bundle != null) {
                String sender = bundle.getString("sender");
                Integer amount = bundle.getInt("message");
                mydb.execSQL("INSERT INTO tableTransactions (tag, amount, smsDay, smsDD, smsMM, smsYY, smsDate) VALUES(" +
                        "'" + sender + "'," + amount + ",'" + smsDay + "','" + smsDD + "','" + smsMM + "','" + smsYY + "','" + strfinal + "')");
                nSender=sender; nAmount=amount; nSafe=0; nDays=0L;
                Date setDate = parseDate(smsDD+"-"+smsMM+"-"+smsYY);
                List<String> arrString;
                arrString = objFile.ReadFile();
                if(arrString.get(0).equalsIgnoreCase("true")){
                    String totalLimit = arrString.get(2);
                    String startDate = arrString.get(3);
                    String endDate = arrString.get(4);
                    if(parseDate(endDate).compareTo(setDate)>=0 && setDate.compareTo(parseDate(startDate))>=0){
                        Integer newAmount = getAmountSpent(startDate, endDate);
                        String finalString = "true" + System.getProperty("line.separator") +
                                Integer.toString(newAmount) + System.getProperty("line.separator") +
                                totalLimit + System.getProperty("line.separator") +
                                startDate + System.getProperty("line.separator") +
                                endDate;
                        objFile.saveToFile(finalString);
                        long diff = parseDate(endDate).getTime() - setDate.getTime();
                        nDays = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
                        nSafe = Integer.parseInt(totalLimit) - newAmount;
                    }
                    else if(parseDate(endDate).compareTo(setDate)<0 || setDate.compareTo(parseDate(startDate))<0){
                    }
                }
                if(isVisible){
                    inst.setSpendLimit();
                    inst.notifyUpdate();
                }
                else showNotification(context);
            }
            else{
                Bundle bundleOffers = objFilter.isOffer(strSender, strMessage);
                if(bundleOffers != null) {
                    String tSender = bundleOffers.getString("sender");
                    mydb.execSQL("INSERT INTO tableOffers (tag, offertext) VALUES(" +
                            "'" + tSender + "','" + strMessage + "')");
                }
            }
        }
        mydb.close();
    }

    private String formatDate(String input_date){
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

    private Integer getAmountSpent(String begin, String end){
        Integer amtSpnt = 0;
        begin = formatDate(begin);
        end = formatDate(end);
        Cursor c = mydb.rawQuery("SELECT SUM(amount) FROM tableTransactions WHERE smsDate BETWEEN '"+begin+"' AND '"+end+"'", null);
        c.moveToFirst();
        if(c.getCount()>0) amtSpnt = max(0,c.getInt(0));
        c.close();
        return amtSpnt;
    }

    private void showNotification(Context context){
        String nString = nSender + ": " + context.getResources().getString(R.string.rupeeSymbol) + Integer.toString(nAmount) +
                        " | " + "Safe to Spend: " + context.getResources().getString(R.string.rupeeSymbol) + Integer.toString(nSafe) +
                        " | " + Long.toString(nDays) + " days left";
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context,0,intent,PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context);
        mBuilder.setContentIntent(pendingIntent);
        mBuilder.setSmallIcon(R.mipmap.ic_launcher);
        mBuilder.setContentTitle("Expense Manager & Economizer");
        mBuilder.setContentText(nString);
        mBuilder.setAutoCancel(true);
        mBuilder.setSound(alarmSound);
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(001,mBuilder.build());
    }

    public class classFileHelper{
        final public static String fileName = "limit.txt";
        final public String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/.ExpenseManagerFiles/";
        final public String TAG = FileHelper.class.getName();

        public List<String> ReadFile(){
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

        public boolean saveToFile(String data){
            try {
                new File(path).mkdir();
                File file = new File(path+fileName);
                if (!file.exists()) {
                    file.createNewFile();
                }
                FileOutputStream fileOutputStream = new FileOutputStream(file,false);
                fileOutputStream.write((data + System.getProperty("line.separator")).getBytes());
                fileOutputStream.close();
                return true;
            }
            catch(FileNotFoundException ex) {
                Log.d(TAG, ex.getMessage());
            }
            catch(IOException ex) {
                Log.d(TAG, ex.getMessage());
            }
            return  false;
        }
    }

    public class classFilter{
        public String[] senderDict = {"AMAZON","Amazon","PAYTM","Paytm","WAYSMS","FCHRGE","ATMSBI"};
        public String[] keywordsAmazon = {"INR"};
        public String[] keywordsPaytm = {"Paid Rs.","Paid","paid","payment of"};
        public String[] keywordsFreecharge = {"paid"};
        public String[] keywordsRecharge = {"Recharge","recharge","Rchrg","successful"};
        public String[] keywordsATMSBI = {"Thank you for using","purchase","on POS","at","txn#"};
        public String[] keywordsATMSBIRecharge = {"BSNL","bsnl","Bsnl","IDEA","idea","Idea",
                "AIRTEL","airtel","Airtel","VODAFONE","vodafone","Vodafone",
                "DOCOMO","dococmo","Docomo"};
        public String[] extractionKeywords = {"INR","Rs.","Rs"};
        public String[] offersSenderDict = {"WAYSMS","DOMINO","MYNTRA","PANTLS","BIGBZR","BMSHOW","BFCTRY","LNKART","OLACBS","PZAHUT"};
        public String[] offerSenderNames = {"WAYSMS","DOMINOS","MYNTRA","PANTALOONS","BIG BAZAR","BOOK MY SHOW","BRAND FACTORY","LENSKART","OLA CABS","PIZZA HUT"};
        public String[] keywordsOffers = {"Discounts","discounts","DISCOUNTS","Discount","discount","DISCOUNT",
                "Offers","offers","OFFERS","Offer","offer","OFFER",
                "Sale","sale","SALE","Off","off","OFF",
                "Cashback","cashback","CASHBACK"};
        public String[] INkeyowrds = {"Amazon"};

        boolean isRecharge;

        int [] pre_kmp(String pat){
            int m=pat.length();
            int lps[] = new int[m];
            int i=1;
            int len=0;
            lps[0]=0;
            while(i<m){
                if(pat.charAt(i)==pat.charAt(len)) {
                    len++; lps[i] = len; i++;
                }
                else{
                    if(len!=0)	len=lps[len-1];
                    else {
                        lps[i] = 0;
                        i++;
                    }
                }
            }
            return lps;
        }

        private Vector kmp(String pat, String txt){
            int lps[] = pre_kmp(pat);
            Vector index = new Vector(10);
            int i=0, j=0, k=0;
            int n=txt.length(), m=pat.length();
            while(i<n){
                if(txt.charAt(i)==pat.charAt(j)) {
                    i++;
                    j++;
                }
                if(j==m){
                    index.addElement(i-j);
                    //return true;
                    k++;
                    j=lps[j-1];
                }
                else if(i<n && txt.charAt(i)!=pat.charAt(j)){
                    if(j!=0)	j=lps[j-1];
                    else	i++;
                }
            }
            //return false;
            return index;
        }

        public Bundle foundSender(String sender, String message){
            Vector senderindex;
            isRecharge = false;
            for(int i=0; i<senderDict.length; i++) {
                senderindex = kmp(senderDict[i], sender);
                if (senderindex.size() != 0){
                    if(i==0 || i==1)        return utilityAmazon(i,message);
                    else if(i==2 || i==3)   return utilityPaytm(i,message);
                    //else if(i==4)           return utilityPaytm(i,message);
                    else if(i==5)           return utilityFreecharge(i,message);
                    else if(i==6)           return utilityATMSBI(i,message);
                }
            }
            if(!isRecharge) return utilityRecharge(message);
            return null;
        }

        private Bundle utilityAmazon(int i, String message){
            int chk,amount=0;
            Bundle bundle = new Bundle();
            chk = checkKeywordsInMessage(keywordsAmazon,message);
            if(chk!=-1)
                amount = extractAmount(extractionKeywords, 0, message);
            if(amount!=0){
                bundle.putString("sender", senderDict[i]);
                bundle.putInt("message", amount);
                return bundle;
            }
            return null;
        }

        private Bundle utilityPaytm(int i, String message){
            int chk, amount=0;
            Bundle bundle = new Bundle();
            chk = checkKeywordsInMessage(keywordsPaytm,message);
            if(chk!=-1)
                amount = extractAmount(extractionKeywords, 1, message);
                    /*else{
                        if(checkMultipleKeywords(keywordsRecharge[0],keywordsRecharge[1],message))
                            amount = extractAmount(extractionKeywords, 1, message);
                    }*/
            if(amount!=0){
                bundle.putString("sender", senderDict[i]);
                bundle.putInt("message", amount);
                return bundle;
            }
            return null;
        }

        private Bundle utilityFreecharge(int i, String message){
            int chk, amount=0;
            Bundle bundle = new Bundle();
            chk = checkKeywordsInMessage(keywordsFreecharge,message);
            if(chk!=-1)
                amount = extractAmount(extractionKeywords,2,message);
                    /*else{
                        if(checkMultipleKeywords(keywordsRecharge[0],keywordsRecharge[1],message))
                            amount = extractAmount(extractionKeywords,2,message);
                    }*/
            if(amount!=0){
                bundle.putString("sender", senderDict[i]);
                bundle.putInt("message", amount);
                return bundle;
            }
            return null;
        }

        private Bundle utilityATMSBI(int i, String message){
            int amount=0;
            Bundle bundle = new Bundle();
            if(checkMultipleKeywords(keywordsATMSBI[0],keywordsATMSBI[1],message)){
                Vector mIndex, atIndex, txnIndex, sIndex;
                String mSender="";
                mIndex = kmp(keywordsATMSBI[2],message);
                atIndex = kmp(keywordsATMSBI[3],message);
                txnIndex = kmp(keywordsATMSBI[4],message);
                mSender = message.substring((Integer)atIndex.elementAt(0)+3, (Integer)txnIndex.elementAt(0));
                for(int k=0; k<keywordsATMSBIRecharge.length; k++){
                    sIndex = kmp(keywordsATMSBIRecharge[k],mSender);
                    if(sIndex.size()!=0){
                        isRecharge = true;
                        mSender = mSender + "Recharge";
                        break;
                    }
                }
                amount = extractAmount(extractionKeywords,2,message);
                if(amount!=0){
                    bundle.putString("sender", mSender);
                    bundle.putInt("message", amount);
                    return bundle;
                }
            }
            return null;
        }

        private Bundle utilityRecharge(String message){
            int amount = 0;
            Bundle bundle = new Bundle();
            if(checkMultipleKeywords(keywordsRecharge[0],keywordsRecharge[3],message)
                    || checkMultipleKeywords(keywordsRecharge[1],keywordsRecharge[3],message)
                    || checkMultipleKeywords(keywordsRecharge[2],keywordsRecharge[3],message)) {
                amount = extractAmount(extractionKeywords, 1, message);
                if(amount==0)
                    amount = extractAmount(extractionKeywords, 2, message);
            }
            if (amount != 0) {
                bundle.putString("sender", "Recharge");
                bundle.putInt("message", amount);
                return bundle;
            }
            return null;
        }

        private Integer checkKeywordsInMessage(String[] keywords, String message){
            Vector mIndex;
            for(int k=0; k<keywords.length; k++){
                mIndex = kmp(keywords[k], message);
                if(mIndex.size()!=0)
                    return k;
            }
            return -1;
        }

        private boolean checkMultipleKeywords(String keyword1, String keyword2, String message){
            Vector mIndex1, mIndex2;
            mIndex1 = kmp(keyword1, message);
            mIndex2 = kmp(keyword2, message);
            if(mIndex1.size()!=0 && mIndex2.size()!=0)  return true;
            return false;
        }

        private Integer extractAmount(String[] keyword, Integer chk, String message){
            Vector index;
            Integer amount=0;
            index = kmp(keyword[chk], message);
            if(index.size() != 0){
                //for(int k=0; k<index.size(); k++){
                int start = (Integer) index.elementAt(0) + keyword[chk].length();
                char ch = message.charAt(start);
                while(!Character.isDigit(ch)){
                    start++;
                    ch = message.charAt(start);
                }
                while(Character.isDigit(ch)){
                    amount = amount*10 + Character.getNumericValue(ch);
                    start++;
                    ch = message.charAt(start);
                }
                //if(amount!=0)   return amount;
                //}
            }
            return amount;
        }

        public Bundle isOffer(String sender, String message){
            Bundle offerBundle;
            Vector sIndex, mIndex;
            for(int i=0; i<offersSenderDict.length; i++){
                sIndex = kmp(offersSenderDict[i], sender);
                if(sIndex.size() != 0){
                    if(i==offersSenderDict.length-1) {
                        offerBundle = new Bundle();
                        offerBundle.putString("sender",offerSenderNames[i]);
                        return offerBundle;
                    }
                    for(int j=0; j<keywordsOffers.length; j++){
                        mIndex = kmp(keywordsOffers[j], message);
                        if(mIndex.size() != 0){
                            offerBundle = new Bundle();
                            offerBundle.putString("sender",offerSenderNames[i]);
                            return offerBundle;
                        }
                        else{
                            offerBundle = checkInKeywords(message);
                            if(offerBundle!=null) return offerBundle;
                        }
                    }
                }
            }
            return null;
        }

        private Bundle checkInKeywords(String message){
            Bundle chkbundle = new Bundle();
            Vector mIndex;
            for(int i=0; i<INkeyowrds.length; i++){
                mIndex = kmp(INkeyowrds[i], message);
                if(mIndex.size()!=0){
                    if(i==0)    chkbundle.putString("sender","AMAZON");
                    return chkbundle;
                }
            }
            return null;
        }
    }
}
