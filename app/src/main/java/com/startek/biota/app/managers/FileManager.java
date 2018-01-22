package com.startek.biota.app.managers;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.startek.biota.app.global.Global;
import com.startek.biota.app.models.Human;
import com.startek.biota.app.utils.Converter;
import com.startek.biota.app.utils.RuntimeVariables;

/**
 * 檔案相關操作
 */
public class FileManager {

    public static boolean exists(String filepath)
    {
        File file = new File(filepath);
        return file.exists();
    }

    public static void copy(String src, String dst) throws IOException {
        InputStream in = new FileInputStream(src);
        OutputStream out = new FileOutputStream(dst);

        // Transfer bytes from in to out
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
    }

    public static boolean delete(String filepath)
    {
        if(exists(filepath))
        {
            try
            {
                File file = new File(filepath);
                return file.delete();
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
                return false;
            }
        }

        return false;
    }

    public static String getEnrollBmpPath(int fingerBtnId, int scanTime)
    {
        String filename = String.format("enroll_%s_%d.bmp", Converter.fingerBtnIdToEnglish(fingerBtnId), scanTime);
        return getPath(DIR_TMP, filename);
    }

    public static String getEnrollDatFilename(int fingerBtnId) {
        String filename = String.format("enroll_%s.dat", Converter.fingerBtnIdToEnglish(fingerBtnId));
        return getPath(DIR_TMP, filename);
    }

    public static String getVerifyBmpPath() {
        String filename = String.format("verify.bmp");
        return getPath(DIR_TMP, filename);
    }

    public static String getBmpPath(Human human, int fingerBtnId, int scanTime)
    {
        String filename = String.format("%s_%s_%d.bmp",
                human.bind_id,
                Converter.fingerBtnIdToEnglish(fingerBtnId),
                scanTime);
        return getPath(DIR_BMP, filename);
    }

    public static String getDatPath(Human human, int fingerBtnId)
    {
        String filename = String.format("%s_%s.dat",
                human.bind_id,
                Converter.fingerBtnIdToEnglish(fingerBtnId));
        return getPath(DIR_DAT, filename);
    }

    public static String getCsvPath(String filename) {
        return getPath(DIR_TMP, filename);
    }

    public static String getDatabasePath(String fileName) {
        return getPath(fileName);
    }

    private static final String DIR_BASE = "BioTaClient";
    private static final String DIR_TMP = "tmp";
    private static final String DIR_BMP = "bmp";
    private static final String DIR_DAT = "dat";

    private static String getDir(String filepath)
    {
        File file = new File(filepath);

        return file.getParent();
    }

    private static String getSDPath()
    {
//        boolean saveIntoSdCard = Global.saveIntoSdCard();

        boolean saveIntoSdCard = false;

        boolean sdCardExist = Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);   //判斷sd記憶卡是否存在

        if(saveIntoSdCard && sdCardExist)
        {
//            return Environment.getExternalStorageDirectory().getPath();//得到根目錄 -> 回傳 /storage/emulated/0 不知道怎麼用
            String result = "/mnt/sdcard";

            return new File(result, DIR_BASE).getPath();
        }
        else
        {
            return Global.getContext().getFilesDir().getPath();
        }
    }

    private static String getPath(String... paths)
    {
        String filepath = getSDPath();

        for(String path:paths)
        {
            filepath = new File(filepath, path).getPath();
        }

        createDirIfNotExists(getDir(filepath));

        return filepath;
    }

    private static boolean createDirIfNotExists(String dir)
    {
        boolean ret = true;
        File file = new File(dir);
        if (!file.exists()) {
            if (!file.mkdirs()) {
                ret = false;
            }
        }
        return ret;
    }
}
