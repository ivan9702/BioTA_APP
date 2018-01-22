package com.startek.biota.app.database;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;


import com.google.gson.Gson;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;
import com.startek.biota.app.models.DirtyData;
import com.startek.biota.app.models.EmailLog;
import com.startek.biota.app.models.Fingerprint;
import com.startek.biota.app.models.FingerprintLog;
import com.startek.biota.app.models.Human;
import com.startek.biota.app.models.InternalLog;
import com.startek.biota.app.models.MatchLog;
import com.startek.biota.app.models.Nfc;
import com.startek.biota.app.models.NfcLog;
import com.startek.biota.app.models.RunningLog;
import com.startek.biota.app.models.Setting;
import com.startek.biota.app.models.Team;
import com.startek.biota.app.utils.Converter;

//import net.sqlcipher.database.SQLiteDatabase;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 *
 * reference:
 *  https://github.com/sierpito/demo-ormlite-with-sqlcipher
 *  https://github.com/sierpito/demo-ormlite-with-sqlcipher/blob/master/src/main/java/com/demo/sqlcipher/SQLCipherActivity.java
 *  https://github.com/datatheorem/SQLCipher-Android
 *
 *  http://ormlite.com/javadoc/ormlite-core/doc-files/ormlite_3.html#UpdateBuilder
 */
public class Cache {

    private static final String TAG = "SqliteCacheImpl";
    private Context context;
    private OrmLiteSqliteOpenHelper helper;

    // ----------------------------------------
    // tableList
    // ----------------------------------------

    private static List<Class> tableList;

    static
    {
        tableList = new ArrayList();
        tableList.add(DirtyData.class);
        tableList.add(Fingerprint.class);
        tableList.add(FingerprintLog.class);
        tableList.add(Human.class);
        tableList.add(MatchLog.class);
        tableList.add(Nfc.class);
        tableList.add(NfcLog.class);
        tableList.add(Team.class);
        tableList.add(RunningLog.class);
        tableList.add(EmailLog.class);
        tableList.add(Setting.class);
        tableList.add(InternalLog.class);
    }

    public static List<Class> getTableList()
    {
        return tableList;
    }

    // ----------------------------------------

    public Cache(Context context)
    {
        this.context = context;
    }

    private <D extends Dao<T, ?>, T> D getDao(Class<T> clazz) throws SQLException
    {
        // Dao<User, String> dao = helper.getDao(User.class);

        if(helper == null)
        {
            boolean useCipher = false;

            if(useCipher)
            {
                // Android 6.0 目前無法使用 Cipher
//                SQLiteDatabase.loadLibs(context);
//                helper =  new SQLiteOrmWithCipherHelper(context);
            }
            else
            {
                helper =  new SQLiteOrmHelper(context);
            }
        }

        return helper.getDao(clazz);
    }

    public static String getNextUuid()
    {
        return java.util.UUID.randomUUID().toString().replace("-", "").toUpperCase().trim();
    }

    public List<Fingerprint> queryFingerprint(String humanId)
    {
        try {
            Dao<Fingerprint, Integer> dao = getDao(Fingerprint.class);
            return dao.queryBuilder().where().eq("humanId", humanId).query();
        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<Fingerprint>();
        }
    }

    public List<Nfc> queryNfc(String humanId)
    {
        try {
            Dao<Nfc, Integer> dao = getDao(Nfc.class);
            return dao.queryBuilder().where().eq("humanId", humanId).query();
        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<Nfc>();
        }
    }

    public int deleteFingerprint(String humanId)
    {
        try {
            Dao<Fingerprint, Integer> dao = getDao(Fingerprint.class);
            DeleteBuilder<Fingerprint, Integer> deleteBuilder = dao.deleteBuilder();
            deleteBuilder.where().eq("humanId", humanId);
            return dao.delete(deleteBuilder.prepare());
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public int deleteNfc(String humanId)
    {
        try {
            Dao<Nfc, Integer> dao = getDao(Nfc.class);
            DeleteBuilder<Nfc, Integer> deleteBuilder = dao.deleteBuilder();
            deleteBuilder.where().eq("humanId", humanId);
            return dao.delete(deleteBuilder.prepare());
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public List<Human> queryHuman() {
        try
        {
            Dao<Human, String> daoHuman = getDao(Human.class);
            Dao<Fingerprint, Integer> daoFingerprint = getDao(Fingerprint.class);
            Dao<Nfc, Integer> daoNfc = getDao(Nfc.class);

            List<Human> items = daoHuman.queryForAll();

            for(Human item:items)
            {
                item.is_local = true;
                item.getFingerprints().addAll(queryFingerprint(item.id));
                item.getNfcs().addAll(queryNfc(item.id));
            }

            return items;
        }
        catch (SQLException e) {
            e.printStackTrace();
            Log.e(TAG, e.getMessage());
            return new ArrayList<Human>();
        }
    }

    public int createNfc(Nfc nfc)
    {
        int result = 0;

        try
        {
            Dao<Nfc, Integer> daoNfc = getDao(Nfc.class);
            result += daoNfc.create(nfc);
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }

        return result;
    }

    public int createFingerprint(Fingerprint fingerprint)
    {
        int result = 0;

        try
        {
            Dao<Fingerprint, Integer> daoFingerprint = getDao(Fingerprint.class);
            result += daoFingerprint.create(fingerprint);
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }

        return result;
    }

    private boolean containNfc(List<Nfc> deletedNfcs, Nfc aNfc)
    {
        for(Nfc nfc:deletedNfcs)
        {
            if(nfc.id == aNfc.id)
                return true;
        }
        return false;
    }

    private boolean containFingerprint(List<Fingerprint> deletedFingerprints, Fingerprint aFingerprint)
    {
        for(Fingerprint fingerprint:deletedFingerprints)
        {
            if(fingerprint.id == aFingerprint.id)
                return true;
        }
        return false;
    }

    public int updateHuman(Human human) throws SQLException {
        int result = 0;

        Dao<Human, String> daoHuman = getDao(Human.class);
        result += daoHuman.update(human);

        Dao<Nfc, Integer> daoNfc = getDao(Nfc.class);

        for(Nfc nfc:human.getOriginalNfcs())
        {
            nfc.humanId = human.id;
            result += daoNfc.delete(nfc);
        }

        for(Nfc nfc:human.getNfcs())
        {
            nfc.humanId = human.id;

            Dao.CreateOrUpdateStatus status = daoNfc.createOrUpdate(nfc);
            if(status.isCreated() || status.isUpdated()) result += 1;
        }

        Dao<Fingerprint, Integer> daoFingerprint = getDao(Fingerprint.class);

        for(Fingerprint fingerprint:human.getOriginalFingerprints())
        {
            fingerprint.humanId = human.id;
            result += daoFingerprint.delete(fingerprint);
        }

        for(Fingerprint fingerprint:human.getFingerprints())
        {
            fingerprint.humanId = human.id;

            Dao.CreateOrUpdateStatus status = daoFingerprint.createOrUpdate(fingerprint);
            if(status.isCreated() || status.isUpdated()) result += 1;
        }

        return result;
    }

    public int createHuman(Human human) throws SQLException {
        int result = 0;

        Dao<Human, String> daoHuman = getDao(Human.class);
        human.id = getNextUuid();
        result += daoHuman.create(human);

        Dao<Nfc, Integer> daoNfc = getDao(Nfc.class);
        for(Nfc nfc:human.getNfcs())
        {
            nfc.humanId = human.id;
            result += daoNfc.create(nfc);
        }

        Dao<Fingerprint, Integer> daoFingerprint = getDao(Fingerprint.class);
        for(Fingerprint fingerprint:human.getFingerprints())
        {
            fingerprint.humanId = human.id;
            result += daoFingerprint.create(fingerprint);
        }

        return result;
    }

    public int deleteHuman(Human human) throws SQLException {
        int result = 0;

        Dao<Fingerprint, Integer> daoFingerprint = getDao(Fingerprint.class);
        for(Fingerprint fingerprint:human.getFingerprints())
        {
            result += daoFingerprint.delete(fingerprint);
        }

        Dao<Nfc, Integer> daoNfc = getDao(Nfc.class);
        for(Nfc nfc:human.getNfcs())
        {
            result += daoNfc.delete(nfc);
        }

        Dao<Human, String> daoHuman = getDao(Human.class);
        result += daoHuman.delete(human);

        return result;
    }

    // ----------------------------------------
    // 系統設定
    // ----------------------------------------

    /**
     * 查詢所有系統設定
     */
    public List<Setting> querySettings()
    {
        try
        {
            Dao<Setting, Integer> dao = getDao(Setting.class);

            QueryBuilder<Setting, Integer> queryBuilder = dao.queryBuilder();

            queryBuilder.distinct().selectColumns("signature");

            List<Setting> distinctSettings = dao.query(queryBuilder.prepare());

            List<Setting> result = new ArrayList<Setting>();

            for(Setting setting:distinctSettings)
            {
                result.add(getLastestSetting(setting.signature));
            }

            Comparator<Setting> comparator = new Comparator<Setting>() {
                public int compare(Setting lhs, Setting rhs)
                {
                    return new Integer(lhs.order).compareTo(new Integer(rhs.order));
                }
            };

            Collections.sort(result, comparator);
            //Collections.sort(result, Collections.reverseOrder(comparator));

            return result;
        }
        catch (Exception ex)
        {
            ex.printStackTrace();

            return new ArrayList<Setting>();
        }
    }

    /**
     * 查詢系統設定對應預設值
     */
    public Setting getLastestSetting(String signature)
    {
        boolean ascending = false;
        return getSetting(signature, ascending);
    }

    /**
     * 查詢系統設定對應最新一筆資料
     */
    public Setting getFirstSetting(String signature)
    {
        boolean ascending = true;
        return getSetting(signature, ascending);
    }

    /**
     * 查詢系統設定對應前一次參數
     */
    public Setting getPreviousSetting(Setting setting)
    {
        try
        {
            Dao<Setting, Integer> dao = getDao(Setting.class);

            Setting found = dao.queryBuilder()
                    .orderBy("version", true) // true stand for sorting by asc style
                    .where()
                    .eq("signature", setting.signature).and()
                    .eq("version", setting.version - 1)
                    .queryForFirst();

            return found;
        }
        catch (Exception ex)
        {
            ex.printStackTrace();

            return null;
        }
    }

    private Setting getSetting(String signature, boolean ascending)
    {
        try
        {
            Dao<Setting, Integer> dao = getDao(Setting.class);

            Setting found = dao.queryBuilder()
                    .orderBy("version", ascending)
                    .where()
                    .eq("signature", signature)
                    .queryForFirst();

            return found;
        }
        catch (Exception ex)
        {
            ex.printStackTrace();

            return null;
        }
    }

    /**
     * 儲存系統設定
     */
    public int saveSetting(Setting reference, String newValue) throws SQLException {
        Dao<Setting, Integer> dao = getDao(Setting.class);

        Setting setting = new Setting();
        setting.category = reference.category;
        setting.signature = reference.signature;
        setting.description = reference.description;
        setting.value = newValue;
        setting.version = reference.version + 1;
        setting.editorType = reference.editorType;
        setting.editorValues = reference.editorValues;

        return dao.create(setting);
    }

    public int createSettingIfNotExists(
            int order,
            int category,
            String signature,
            String description,
            String value,
            int version,
            int editorType,
            String editorValues)
    {
        try
        {
            Dao<Setting, Integer> dao = getDao(Setting.class);

            Setting found = dao.queryBuilder()
                    .where()
                    .eq("signature", signature)
                    .queryForFirst();

            if(found != null) return 0;

            Setting setting = new Setting();
            setting.order = order;
            setting.category = category;
            setting.signature = signature;
            setting.description = description;
            setting.value = value;
            setting.version = version;
            setting.editorType = editorType;
            setting.editorValues = editorValues;

            return dao.create(setting);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();

            return 0;
        }
    }

    // ----------------------------------------
    // Email Log
    // ----------------------------------------

    public int createEmailLog(int category, String event, String operator, String description, String result, boolean success)
    {
        try
        {
            Dao<EmailLog, Integer> dao = getDao(EmailLog.class);
            EmailLog r = new EmailLog();
            r.category = category;
            r.date = Calendar.getInstance().getTime();
            r.event = event;
            r.operator = operator;
            r.description = description;
            r.result = result;
            r.success = success;
            return dao.create(r);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();

            return 0;
        }
    }

    public int deleteEmailLogs(List<EmailLog> logs)
    {
        try
        {
            int result = 0;

            Dao<EmailLog, Integer> dao = getDao(EmailLog.class);

            for(EmailLog log:logs)
            {
                result += dao.delete(log);
            }

            return result;
        }
        catch (Exception ex)
        {
            ex.printStackTrace();

            return 0;
        }
    }

    public List<EmailLog> queryEmailLogs()
    {
        try
        {
            Dao<EmailLog, Integer> dao = getDao(EmailLog.class);

            return dao.queryForAll();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();

            return new ArrayList<EmailLog>();
        }
    }

    // ----------------------------------------
    // 運行記錄
    // ----------------------------------------

    public int createRunningLog(Throwable throwable)
    {
        return createRunningLog(
                RunningLog.CATEGORY_ACCESS_CONTROL,
                throwable);
    }

    public int createRunningLog(int category, Throwable throwable)
    {
        String event = throwable.getClass().getSimpleName();
        String operator = "";
        String description = Converter.getStackTrace(throwable);
        String result = Converter.getMessage(throwable);
        boolean success = false;

        return createRunningLog(
                category,
                event,
                operator,
                description,
                result,
                success);
    }

    public int createRunningLog(int category, String event, String operator, String description, String result, boolean success)
    {
        try
        {
            createEmailLog(
                    category,
                    event,
                    operator,
                    description,
                    result,
                    success);

            Dao<RunningLog, Integer> dao = getDao(RunningLog.class);
            RunningLog r = new RunningLog();
            r.category = category;
            r.date = Calendar.getInstance().getTime();
            r.event = event;
            r.operator = operator;
            r.description = description;
            r.result = result;
            r.success = success;
            return dao.create(r);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();

            return 0;
        }
    }

    public int deleteRunningLogs(int categories, String filteredStr)
    {
        try
        {
            Dao<RunningLog, Integer> dao = getDao(RunningLog.class);

            DeleteBuilder<RunningLog, Integer> deleteBuilder = dao.deleteBuilder();

            Where<RunningLog, Integer> where = deleteBuilder.where();

            buildWhere(where, categories, filteredStr);

            return deleteBuilder.delete();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();

            return 0;
        }
    }

    public List<RunningLog> queryRunningLogs(int categories, String filteredStr)
    {
        try
        {
            Dao<RunningLog, Integer> dao = getDao(RunningLog.class);

            QueryBuilder<RunningLog, Integer> queryBuilder = dao.queryBuilder();

            Where<RunningLog, Integer> where = queryBuilder.where();

            buildWhere(where, categories, filteredStr);

            queryBuilder.orderBy("date", false); // desc order

            return dao.query(queryBuilder.prepare());
        }
        catch (Exception ex)
        {
            ex.printStackTrace();

            return new ArrayList<RunningLog>();
        }
    }

    /**
     * 建立查詢的 where 條件
     * http://ormlite.com/javadoc/ormlite-core/com/j256/ormlite/stmt/Where.html
     */
    private void buildWhere(Where<RunningLog, Integer> where, int categories, String filteredStr) throws SQLException {
        List<Integer> categoryList = new ArrayList<Integer>();

        int[] flags = new int[]
                {
                        RunningLog.CATEGORY_USER_IN_OUT,
                        RunningLog.CATEGORY_DATA_MAINTAIN,
                        RunningLog.CATEGORY_SYNC_SERVER,
                        RunningLog.CATEGORY_ACCESS_CONTROL,
                };

        for(int i=0; i<flags.length; i++)
        {
            int flag = flags[i];

            if((categories & flag) == flag) categoryList.add(flag);
        }

        if(!TextUtils.isEmpty(filteredStr))
        {
            where.and(
                    where.in("category", categoryList),
                    where.or(
                            where.like("event", "%" + filteredStr + "%"),
                            where.like("operator", "%" + filteredStr + "%"),
                            where.like("description", "%" + filteredStr + "%"),
                            where.like("result", "%" + filteredStr + "%")
                    )
            );
        }
        else
        {
            where.in("category", categoryList);
        }
    }

    // ----------------------------------------
    // 手動同步資料
    // ----------------------------------------

    public List<DirtyData> queryDirtyData() {

        try
        {
            Dao<DirtyData, Integer> dao = getDao(DirtyData.class);
            DeleteBuilder<DirtyData, Integer> deleteBuilder = dao.deleteBuilder();
            deleteBuilder.where().eq("state", DirtyData.STATE_SUCCESS);
            dao.delete(deleteBuilder.prepare());

            return dao.queryForAll();
        }
        catch (SQLException e) {
            e.printStackTrace();
            Log.e(TAG, e.getMessage());
            return new ArrayList<DirtyData>();
        }
    }

    /**
     *
     * http://stackoverflow.com/questions/5401467/convert-string-into-a-class-object
     */
    public int createDirtyData(int action, final Object obj) {
        try
        {
            Dao<DirtyData, Integer> dao = getDao(DirtyData.class);
            DirtyData d = new DirtyData();

            // ----------------------------------------
            // IMPORTANT
            // ----------------------------------------
            // (1) user Class.forName(dirtyData.className) to get Class object
            // (2) make sure class's package name has been import in SyncDataActivity.java
            // ----------------------------------------

            d.className = obj.getClass().toString();
            d.action = action;
            d.json = new Gson().toJson(obj);
            d.state = DirtyData.STATE_PENDING;
            d.date = Calendar.getInstance().getTime();
            return dao.create(d);
        }
        catch (SQLException e) {
            e.printStackTrace();
            Log.e(TAG, e.getMessage());
            return 0;
        }
    }

    public int deleteDirtyData(Human human) throws SQLException
    {
        int result = 0;
        List<DirtyData> dirtyDataList = queryDirtyData();

        for(DirtyData dirtyData:dirtyDataList)
        {
            Class clazz = Converter.getClass(dirtyData.className);
            if(!clazz.equals(Human.class))continue;

            Human aHuman = new Gson().fromJson(dirtyData.json, Human.class);

            if(!aHuman.id.equals(human.id))continue;

            result += deleteDirtyData(dirtyData);
        }

        return result;
    }

    public int updateDirtyData(DirtyData dirtyData) throws SQLException
    {
        Dao<DirtyData, Integer> dao = getDao(DirtyData.class);
        return dao.update(dirtyData);
    }


    public int deleteDirtyData(DirtyData dirtyData) throws SQLException {
        Dao<DirtyData, Integer> dao = getDao(DirtyData.class);
        return dao.delete(dirtyData);
    }

    // ----------------------------------------
    // MatchLog operation
    // ----------------------------------------

    private Date getLastAccessDatetime(String bind_id) {
        try
        {
            Dao<MatchLog, Integer> dao = getDao(MatchLog.class);

            MatchLog matchLog = dao.queryBuilder()
                    .orderBy("id", false)
                    .where()
                    .eq("bind_id", bind_id)
                    .and()
                    .eq("type", MatchLog.CATEGORY_VERIFY)
                    .and()
                    .eq("is_success", true)
                    .queryForFirst();

            return (matchLog != null) ? matchLog.STime : null;
        }
        catch (Exception ex)
        {
            ex.printStackTrace();

            return null;
        }
    }

    public String getLastAccessDate(String bind_id) {
        String result = Converter.toString(getLastAccessDatetime(bind_id), Converter.DateTimeFormat.YYYYMMdd);
        return TextUtils.isEmpty(result) ? "-" : result;
    }

    public String getLastAccessTime(String bind_id) {
        String result = Converter.toString(getLastAccessDatetime(bind_id), Converter.DateTimeFormat.HHmmss);
        return TextUtils.isEmpty(result) ? "-" : result;
    }



    public int createMatchLog(MatchLog matchLog)
    {
        try
        {
            Dao<MatchLog, Integer> dao = getDao(MatchLog.class);

            return dao.create(matchLog);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();

            return 0;
        }
    }

    public int createInternalLog(InternalLog internalLog)
    {
        try
        {
            Dao<InternalLog, Integer> dao = getDao(InternalLog.class);

            return dao.create(internalLog);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();

            return 0;
        }
    }

    public List<InternalLog> queryInternalLog()
    {
        try
        {
            Dao<InternalLog, Integer> dao = getDao(InternalLog.class);

            return dao.queryForAll();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();

            return new ArrayList<InternalLog>();
        }
    }

    public int deleteInternalLog(List<InternalLog> logs)
    {
        try
        {
            int result = 0;

            Dao<InternalLog, Integer> dao = getDao(InternalLog.class);

            for(InternalLog log:logs)
            {
                result += dao.delete(log);
            }

            return result;
        }
        catch (Exception ex)
        {
            ex.printStackTrace();

            return 0;
        }
    }
}
