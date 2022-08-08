package com.app.sportsrecord;

import android.content.Context;

import androidx.annotation.WorkerThread;

import com.opencsv.CSVWriter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

public class PersistentCounter {
    private Context mContext;
    private static String fileName = "number.csv";
    private String filePath;
    public PersistentCounter(Context context){
        this.mContext = context;
        filePath = mContext.getFilesDir() + "/" + fileName;
    }

    @WorkerThread
    public int getCount() throws Exception{
        File numberFile = new File(filePath);
        if(numberFile.exists()) {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(filePath));
            String line = bufferedReader.readLine();
            return Integer.parseInt(line.replace("\"",""));
        } else {
            return 0;
        }
    }

    @WorkerThread
    public void increaseCounter() throws Exception{
        int count = getCount();
        CSVWriter numberFileWriter = new CSVWriter(new FileWriter(filePath, false));
        numberFileWriter.writeNext(new String[]{(count+1) + ""});
        numberFileWriter.close();
    }
}
