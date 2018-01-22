package com.startek.biota.app.database;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.support.ConnectionSource;
import android.content.Context;
import com.j256.ormlite.table.TableUtils;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.startek.biota.app.managers.FileManager;
import com.startek.biota.app.models.DirtyData;
import com.startek.biota.app.models.EmailLog;
import com.startek.biota.app.models.Fingerprint;
import com.startek.biota.app.models.FingerprintLog;
import com.startek.biota.app.models.Human;
import com.startek.biota.app.models.MatchLog;
import com.startek.biota.app.models.Nfc;
import com.startek.biota.app.models.NfcLog;
import com.startek.biota.app.models.RunningLog;
import com.startek.biota.app.models.Setting;
import com.startek.biota.app.models.Team;

// TODO: SqlCipher 目前尚未支援 Android 6.0
public class SQLiteOrmWithCipherHelper {}
/*
//
// 參考資料
//  https://github.com/sierpito/demo-ormlite-with-sqlcipher
//  http://ormlite.com/data_types.shtml
// http://www.cnblogs.com/over140/archive/2012/10/22/2733346.html
//
public class SQLiteOrmWithCipherHelper extends OrmLiteSqliteOpenHelper {

    private static final String DATABASE_PASSWORD = "c3RhcnRlay5iaW90YWNsaWVudA=="; // "startek.biotaclient" 's Base64 format
    private static final String DATABASE_NAME = "database.db";
    private static final int DATABASE_VERSION = 3;

    public SQLiteOrmWithCipherHelper(Context context) {
        super(context, FileManager.getDatabasePath(DATABASE_NAME), null, DATABASE_VERSION, DATABASE_PASSWORD);

        // TODO: 測試用，用於重建資料表，未來上線時請移除
//        SQLiteDatabase db = getWritableDatabase(SQLiteOrmWithCipherHelper.DATABASE_PASSWORD);
//        onCreate(db);
    }

    @Override
    public void onCreate(SQLiteDatabase database, ConnectionSource source) {
        createTables(source);
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, ConnectionSource source, int oldVersion, int newVersion) {
        dropTables(source);
        createTables(source);
    }

    private void createTables(ConnectionSource source) {
        try {
            for(Class table:Cache.getTableList())
            {
                // TableUtils.dropTable(source, table, true); // PS: 沒有對應資料表，會導致問題
                TableUtils.createTableIfNotExists(source, table);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void dropTables(ConnectionSource source) {
        try {
            for(Class table:Cache.getTableList())
                TableUtils.dropTable(source, table, true);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
*/