package com.startek.biota.app.config;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.startek.biota.app.global.Global;
import com.startek.biota.app.models.Setting;
import com.startek.biota.app.utils.Converter;
import com.startek.biota.app.utils.MobileStatus;
import com.startek.biota.app.utils.RuntimeVariables;
import com.startek.biota.app.utils.StrUtils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Z215 on 2016/03/28.
 */
public class Config {

    private static final String TAG = "Config";

    private Context context;
    private DefaultConfig defaultConfig;

    public Config(Context context)
    {
        this.context = context;
        createDefaultSettingsIfNotExists();
    }

    public DefaultConfig getDefaultConfig()
    {
        if(defaultConfig == null)
        {
            defaultConfig = DefaultConfig.getInstance();
        }
        return defaultConfig;
    }

    private void createDefaultSettingsIfNotExists()
    {
        Field[] fields = getDefaultConfig().getClass().getDeclaredFields();

        for(Field field:fields)
        {
            field.setAccessible(true);

            String fieldName = field.getName();

            if(fieldName.equals("instance"))continue;

            try {
                Setting setting = (Setting)field.get(getDefaultConfig());

                int order = setting.order;
                int category = setting.category;
                String signature = setting.signature;
                String description = setting.description;
                String value = setting.value;
                int version = setting.version;
                int editorType = setting.editorType;
                String editorValues = setting.editorValues;

                Global.getCache().createSettingIfNotExists(
                    order,
                    category,
                    signature,
                    description,
                    value,
                    version,
                    editorType,
                    editorValues);

            } catch (Exception e) {
                e.printStackTrace();

                String error = Converter.getMessage(e);
                if(!TextUtils.isEmpty(error))
                    Log.e(TAG, error);
            }
        }
    }

    private String getSettingValueString(Setting defaultSetting)
    {
        Setting setting = Global.getCache().getLastestSetting(defaultSetting.signature);
        String result = setting == null ? defaultSetting.value : setting.value;

        result = getRuntimeValue(result);

        return result;
    }

    private int getSettingValueInteger(Setting defaultSetting)
    {
        try
        {
            return Integer.parseInt(getSettingValueString(defaultSetting));
        }
        catch (Exception e1)
        {
            e1.printStackTrace();

            try
            {
                return Integer.parseInt(defaultSetting.value);
            }
            catch (Exception e2)
            {
                e2.printStackTrace();

                return 4; // MaxEmployeeIdLength: 4, MaxNfcCount: 4
            }
        }
    }

    public String getBaseUrl()
    {
        return getSettingValueString(getDefaultConfig().BaseUrl);
    }

    public String getWsHuman()
    {
        return getSettingValueString(getDefaultConfig().WsHuman);
    }

    public String getWsNfc()
    {
        return getSettingValueString(getDefaultConfig().WsNfc);
    }

    public String getWsFingerprint()
    {
        return getSettingValueString(getDefaultConfig().WsFingerprint);
    }

    public String getWsAnnouncement()
    {
        return getSettingValueString(getDefaultConfig().WsAnnouncement);
    }

    public String getWsReminder()
    {

        return getSettingValueString(getDefaultConfig().WsReminder);
    }

    public String getWsApDevice() {

        return getSettingValueString(getDefaultConfig().WsApDevice);
    }

    /**
     * 手動輸入工號最大長度
     */
    public int getMaxEmployeeIdLength() {
        return getSettingValueInteger(getDefaultConfig().MaxEmployeeIdLength);
    }

    /**
     * 每個人最多允許設定幾張 NFC 卡
     */
    public int getMaxNfcCount()
    {
        return getSettingValueInteger(getDefaultConfig().MaxNfcCount);
    }

    public int getVerifyTimeout()
    {
        return getSettingValueInteger(getDefaultConfig().VerifyTimeout);
    }
    public int getEnrollTimeout()
    {
        return getSettingValueInteger(getDefaultConfig().EnrollTimeout);
    }
    public int getVerifyScoreLimit()
    {
        return getSettingValueInteger(getDefaultConfig().VerifyScoreLimit);
    }

    /**
     * 是否連線 SERVER
     * @return
     */
    public boolean useBiotaServer() {

        // 沒有定義SERVER網址，就不連SERVER
        if(TextUtils.isEmpty(getBaseUrl())) return false;

        // 沒有網路連線，就不連SERVER
        if(!MobileStatus.isNetworkEnabled()) return false;

        // 20160621 允許強制使用離線版本
        boolean result = Boolean.parseBoolean(getSettingValueString(getDefaultConfig().UseBiotaServer));

        return result;
    }

    /**
     * 是否隱藏 API 錯誤
     * @return
     */
    public boolean hideApiError() {
        return Boolean.parseBoolean(getSettingValueString(getDefaultConfig().HideApiError));
    }

    public String getEmailServer() {
        return getSettingValueString(getDefaultConfig().EmailServer);
    }
    public int getEmailPort()
    {
        return getSettingValueInteger(getDefaultConfig().EmailPort);
    }
    public String getEmailAccount() {
        return getSettingValueString(getDefaultConfig().EmailAccount);
    }
    public String getEmailPassword() {
        return getSettingValueString(getDefaultConfig().EmailPassword);
    }
    public String getEmailSSL() {
        return getSettingValueString(getDefaultConfig().EmailSSL);
    }

    public String getEmailTarget() {

        return getSettingValueString(getDefaultConfig().EmailTarget);
    }

    public String getEmailSubject() {

        return getSettingValueString(getDefaultConfig().EmailSubject);
    }

    public String getEmailBody() {

        return getSettingValueString(getDefaultConfig().EmailBody);
    }

    public List<Calendar> getEmailTimes() {

        try
        {
            List<Calendar> result = new ArrayList<Calendar>();

            String[] strTimes = getSettingValueString(getDefaultConfig().EmailTimes).split(",", -1);

            Calendar now = Calendar.getInstance();

            for(String strTime:strTimes)
            {
                Calendar ref = Converter.toCalendar(strTime.trim(), Converter.DateTimeFormat.HHmm);

                if(ref != null)
                {
                    Calendar cal = Calendar.getInstance();

                    cal.set(Calendar.YEAR, now.get(Calendar.YEAR));
                    cal.set(Calendar.MONTH, now.get(Calendar.MONTH));
                    cal.set(Calendar.DAY_OF_MONTH, now.get(Calendar.DAY_OF_MONTH));

                    cal.set(Calendar.HOUR_OF_DAY, ref.get(Calendar.HOUR_OF_DAY));
                    cal.set(Calendar.MINUTE, ref.get(Calendar.MINUTE));

                    cal.set(Calendar.SECOND, 0);
                    cal.set(Calendar.MILLISECOND, 0);

                    // http://oldgrayduck.blogspot.tw/2012/10/androidalarmmanager.html
                    if(cal.compareTo(now) != 1)
                    {
                        cal.add(Calendar.DATE, 1);
                    }

                    result.add(cal);
                }
            }

            return result;
        }
        catch (Exception ex)
        {
            ex.printStackTrace();

            return new ArrayList<Calendar>();
        }
    }

    public String getEmailAttachFileFormat() {

        return getSettingValueString(getDefaultConfig().EmailAttachFileFormat);
    }

    public boolean getNfcAlert() {
        return StrUtils.equals("Y", getSettingValueString(getDefaultConfig().NfcAlert));
    }

    public boolean getCreateManagerIfNotExist() {
        return StrUtils.equals("Y", getSettingValueString(getDefaultConfig().CreateManagerIfNotExist));
    }

    public String getWsFingerprintDevice() {
        return getSettingValueString(getDefaultConfig().WsFingerprintDevice);
    }

    public String getWsMatchLog() {
        return getSettingValueString(getDefaultConfig().WsMatchLog);
    }

    private String getRuntimeValue(String input)
    {
        String result = input;

        RuntimeVariables runtimeVariables = new RuntimeVariables();

        // \{\w+:\w+\}|\{\w+\}

//        String regex = "\\{\\w+\\}";
        String regex = "\\{\\w+\\}|\\{\\w+:\\w+\\}";

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);

        while(matcher.find()) {

            String token = input.substring(matcher.start(), matcher.end());

            String variable = token.replace("{", "").replace("}", "");

            String[] tokens = variable.split(":");

            if(tokens.length == 0)continue;

            String methodName = tokens[0];

            List<Class> parameterTypes = new ArrayList<Class>();
            List<String> args = new ArrayList<String>();

            if(tokens.length ==2)
            {
                String[] params = tokens[1].split(",");

                for(int i=0; i<params.length; i++)
                {
                    parameterTypes.add(String.class);
                    args.add(params[i]);
                }
            }

            java.lang.reflect.Method method = null;

            try
            {
                method = runtimeVariables.getClass().getMethod(
                        methodName,
                        parameterTypes.toArray(new Class[parameterTypes.size()]));
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
            }

            if(method == null)continue;

            String value = null;
            try {
                value = (String) method.invoke(runtimeVariables, (Object[])args.toArray(new String[args.size()]));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }

            if(value == null)continue;

            result = result.replace(token, value);
        }

        return result;
    }
}
