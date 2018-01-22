package com.startek.biota.app.jobs;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.startek.biota.app.R;
import com.startek.biota.app.global.Global;
import com.startek.biota.app.managers.FileManager;
import com.startek.biota.app.models.EmailLog;
import com.startek.biota.app.models.InternalLog;
import com.startek.biota.app.models.RunningLog;
import com.startek.biota.app.utils.Converter;
import com.startek.biota.app.utils.MobileStatus;
import com.startek.biota.app.utils.MyCsvWriter;

import java.io.File;
import java.util.Calendar;
import java.util.List;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

/**
 * https://support.mailpoet.com/knowledgebase/default-ports-numbers-smtp-pop-imap/
 * http://cn.waterlin.org/Java/JavaMail.html
 * http://www.tutorialspoint.com/javamail_api/javamail_api_send_email_with_attachment.htm
 * https://github.com/tschut/android-javamail-tutorial/
 */
public class EmailJob implements Runnable
{
    public static final int ACTION_EMAILLOG = 1;
    public static final int ACTION_INTERNALLOG = 2;

    private static final String TAG = "EmailJob";
    private static final String operator = "EmailJob";
    private Context context;
    private int action;

    public EmailJob(Context context, int action)
    {
        this.context = context;
        this.action = action;
    }

    @Override
    public void run()
    {
        switch (action)
        {
            case ACTION_EMAILLOG:
                sendEmailLog();
                break;

            case ACTION_INTERNALLOG:
                sendInternalLog();
                break;
        }
    }

    private void sendEmailLog()
    {
        String filepath = null;

        try
        {
            Session session = createSessionObject();

            List<EmailLog> logs = Global.getCache().queryEmailLogs();
            String detail = String.format(context.getString(R.string.email_detail), logs.size());
            filepath = createAttachment(logs);

            String email = Global.getConfig().getEmailTarget();

            if(TextUtils.isEmpty(email))
                throw new Exception(getString(R.string.email_is_empty));

            if(!isValidEmail(email))
                throw new Exception(String.format(getString(R.string.email_is_invalid), email));

            String subject = Global.getConfig().getEmailSubject() + detail;
//            String body = Global.getConfig().getEmailBody();
//            String body = Global.getConfig().getEmailBody() + String.format("(Form %s)", fromClass.getSimpleName());
            String body = Global.getConfig().getEmailBody() + String.format("(Form %s - %s)", MobileStatus.getDeviceName(), MobileStatus.getDeviceId());

            Message message = createMessage(session, email, subject, body, filepath);

            // 要避免 android.os.NetworkOnMainThreadException
            // handlerThread = new HandlerThread("EmailServiceThread");
            // handlerThread.setPriority(Thread.MIN_PRIORITY);
            // handlerThread.start();
            // handler = new Handler(handlerThread.getLooper());
            // handler.post(new EmailJob(context));
            Transport.send(message); // must be run in non-ui thread

            Global.getCache().deleteEmailLogs(logs);

            Global.getCache().createRunningLog(
                    RunningLog.CATEGORY_SYNC_SERVER,
                    getString(R.string.runninglog_event_exportdata),
                    operator,
                    String.format(getString(R.string.runninglog_event_exportdata_success), detail),
                    getString(R.string.result_success),
                    true);
            Log.d(TAG, "發送EMAIL成功");
        }
        catch (Exception e)
        {
            e.printStackTrace();

            String error = Converter.getMessage(e);

            Global.getCache().createRunningLog(
                    RunningLog.CATEGORY_SYNC_SERVER,
                    getString(R.string.runninglog_event_exportdata),
                    operator,
                    String.format(getString(R.string.runninglog_event_exportdata_failure), error, Converter.getStackTrace(e)),
                    getString(R.string.result_failure),
                    false);
            Log.e(TAG, String.format("發送EMAIL失敗，原因：%s，StackTrace：%s", error, Converter.getStackTrace(e)));
        }
        finally {
            if(filepath != null)
                FileManager.delete(filepath);
        }
    }

    private void sendInternalLog()
    {
        String filepath = null;

        try
        {
            Session session = createSessionObject();

            List<InternalLog> logs = Global.getCache().queryInternalLog();
            String detail = String.format(context.getString(R.string.email_detail), logs.size());
            filepath = createAttachment(logs);

            String email = Global.getConfig().getEmailTarget();

            if(TextUtils.isEmpty(email))
                throw new Exception(getString(R.string.email_is_empty));

            if(!isValidEmail(email))
                throw new Exception(String.format(getString(R.string.email_is_invalid), email));

            String subject = "BioTaClient InternalLog";
            String body = "";

            Message message = createMessage(session, email, subject, body, filepath);

            // 要避免 android.os.NetworkOnMainThreadException
            // handlerThread = new HandlerThread("EmailServiceThread");
            // handlerThread.setPriority(Thread.MIN_PRIORITY);
            // handlerThread.start();
            // handler = new Handler(handlerThread.getLooper());
            // handler.post(new EmailJob(context));
            Transport.send(message); // must be run in non-ui thread

            Global.getCache().deleteInternalLog(logs);

//            Global.getCache().createRunningLog(
//                    RunningLog.CATEGORY_SYNC_SERVER,
//                    getString(R.string.runninglog_event_exportdata),
//                    operator,
//                    String.format(getString(R.string.runninglog_event_exportdata_success), detail),
//                    getString(R.string.result_success),
//                    true);

            Log.d(TAG, "發送EMAIL(InternalLog)成功");
        }
        catch (Exception e)
        {
            e.printStackTrace();

//            Global.getCache().createRunningLog(
//                    RunningLog.CATEGORY_SYNC_SERVER,
//                    getString(R.string.runninglog_event_exportdata),
//                    operator,
//                    String.format(getString(R.string.runninglog_event_exportdata_failure), e.getMessage()),
//                    getString(R.string.result_failure),
//                    false);

            Log.e(TAG, String.format("發送EMAIL(InternalLog)失敗，原因：%s", e.getMessage()));
        }
        finally {
            if(filepath != null)
                FileManager.delete(filepath);
        }
    }

    public final static boolean isValidEmail(CharSequence target) {
        if (target == null) {
            return false;
        } else {
            String[] emails = target.toString().split(",");
            for(String email:emails)
            {
                if(!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches())
                    return false;
            }
            return true;
        }
    }

    private String getString(int resId)
    {
        return context == null ? "" : context.getString(resId);
    }

    private <T extends MyCsvWriter.CsvLine> String createAttachment(List<T> logs) throws Exception {

        if(logs == null || logs.size() == 0)return null;

        String fileFormat = Global.getConfig().getEmailAttachFileFormat();

        if(fileFormat.equals("csv"))
        {
            String filepath = FileManager.getCsvPath(Converter.toString(Calendar.getInstance().getTime(), Converter.DateTimeFormat.FileName) + ".csv");

            MyCsvWriter writer = new MyCsvWriter();

            writer.write2File(filepath, logs);

            return filepath;
        }

        throw new Exception(String.format("無法處理檔案格式: %s", fileFormat));
    }

    private Session createSessionObject() {

        final String username = Global.getConfig().getEmailAccount();
        final String password = Global.getConfig().getEmailPassword();
        final String host = Global.getConfig().getEmailServer();
        final boolean ssl = Boolean.parseBoolean(Global.getConfig().getEmailSSL());
        final int port = Global.getConfig().getEmailPort();

        Properties properties = new Properties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", Boolean.toString(ssl));
        properties.put("mail.smtp.host", host);
        properties.put("mail.smtp.port", Integer.toString(port));

        return Session.getInstance(properties, new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });
    }

    private Message createMessage(
            Session session,
            String email,
            String subject,
            String body,
            String filepath) throws Exception {

        Message message = new MimeMessage(session);

        String mailFrom = Global.getConfig().getEmailAccount();

        message.setFrom(new InternetAddress(mailFrom));
        // http://stackoverflow.com/questions/13854037/send-mail-to-multiple-recipients-in-java
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email));

        message.setSubject(subject);

        Multipart multipart = new MimeMultipart();

        // Set text message part
        BodyPart messageBodyPart = new MimeBodyPart();
        messageBodyPart.setText(body);
        multipart.addBodyPart(messageBodyPart);

        if(!TextUtils.isEmpty(filepath))
        {
            // Part two is attachment
            messageBodyPart = new MimeBodyPart();
            DataSource source = new FileDataSource(filepath);
            messageBodyPart.setDataHandler(new DataHandler(source));

            File file = new File(filepath);
            String fileName = file.getName();
            messageBodyPart.setFileName(fileName);

            multipart.addBodyPart(messageBodyPart);
        }

        // Send the complete message parts
        message.setContent(multipart);

        return message;
    }



}
