package com.startek.biota.app.utils;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import au.com.bytecode.opencsv.CSVWriter;

/**
 * Created by Z215 on 2016/04/13.
 */
public class MyCsvWriter {

    public interface CsvLine
    {
        List<String> getCsvEntries();
    }

    /**
     * http://stackoverflow.com/questions/4632501/android-http://smasap.evaair.com/SMASWEBAP/PRE/PRED0160.aspx?d=1460521049467-csv-file-from-table-values
     */
    public <T extends MyCsvWriter.CsvLine> void write2File(String filepath, List<T> lines) throws IOException {

        CSVWriter writer = new CSVWriter(new FileWriter(filepath), ',');

        for(CsvLine line:lines)
        {
            List<String> listEntries = line.getCsvEntries();

            // 20160621 移除所有換行符號
            listEntries = removeBreakLine(listEntries);

            String[] entries = listEntries.toArray(new String[listEntries.size()]);
            writer.writeNext(entries);
        }
        writer.close();
    }

    private List<String> removeBreakLine(List<String> listEntries)
    {
        List<String> result = new ArrayList<String>();

        for(String listEntry:listEntries)
        {
            /**
             * 20160705 Norman, fix following bug
             *
             * Stack Trace : Java.lang.NullPointerExceptionat
             *
             * com.startek.biota.app.utils.MyCsvWriter.removeBreakLine(MyCsvWriter.java:46)
             *
             * at com.startek.biota.app.utils.MyCsvWriter.write2File(MyCsvWriter.java:32)
             * at com.startek.biota.app.jobs.EmailJob.createAttachment(EmailJob.java:232)
             * at com.startek.biota.app.jobs.EmailJob.sendEmailLog(EmailJob.java:82)
             * at com.startek.biota.app.jobs.EmailJob.run(EmailJob.run(EmailJob.java:63)
             * at java.lang.Thread.run(Thread.java:841)
             *
             */
            if(listEntry == null) listEntry = "";

            result.add(StrUtils.removeBreakLine(listEntry.trim()));
        }

        return result;
    }

}
