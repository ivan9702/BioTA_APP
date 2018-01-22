package com.startek.biota.app.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.startek.biota.app.managers.FileManager;

import java.sql.SQLException;

//
// 參考資料
//  https://github.com/sierpito/demo-ormlite-with-sqlcipher
//  http://ormlite.com/data_types.shtml
//
public class SQLiteOrmHelper extends OrmLiteSqliteOpenHelper {

    private static final String DATABASE_PASSWORD = "c3RhcnRlay5iaW90YWNsaWVudA=="; // "startek.biotaclient" 's Base64 format
    private static final String DATABASE_NAME = "database.db";
    private static final int DATABASE_VERSION = 3;

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

    public SQLiteOrmHelper(Context context) {
        super(context, FileManager.getDatabasePath(DATABASE_NAME), null, DATABASE_VERSION);
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
}
