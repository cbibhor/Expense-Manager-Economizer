package com.cbibhor.eme.expensemanagereconomizer;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Bibhor Chauhan on 09-05-2017.
 */

public class FileHelper {
    final public static String fileName = "limit.txt";
    final public static String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/.ExpenseManagerFiles/";
    final public static String TAG = FileHelper.class.getName();

    public static List<String> ReadFile(){
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

    public static boolean saveToFile(String data){
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
