package com.halite.util.haliterelay;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.BaseColumns;
import android.provider.ContactsContract;
import android.support.v4.app.NotificationManagerCompat;
import android.telephony.SmsManager;
import android.text.Html;
import android.util.Log;
import android.widget.Toast;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Set;

import static java.net.URLEncoder.*;

public class RelayService extends Service {
    public static final String CMD_LOAD_SETTINGS = "CMD_LOAD_SETTINGS";
    public static final String CMD_SMS_RECEIVED = "CMD_SMS_RECEIVED";
    public static final String DATA_SMS_DATE = "DATA_SMS_DATE";
    public static final String DATA_SMS_ADDR = "DATA_SMS_ADDR";
    public static final String DATA_SMS_BODY = "DATA_SMS_BODY";
    public static final String CMD_PHONECALL_RECEIVED = "CMD_PHONECALL_RECEIVED";
    public static final String DATA_PHONECALL_DATE = "DATA_PHONECALL_DATE";
    public static final String DATA_PHONECALL_INCOMINGNUMBER = "DATA_PHONECALL_INCOMINGNUMBER";
    public static final String CMD_FORWARD_SMS = "CMD_FORWARD_SMS";
    public static final String DATA_FORWARD_SMS_ADDR = "DATA_FORWARD_SMS_ADDR";
    public static final String DATA_FORWARD_SMS_BODY = "DATA_FORWARD_SMS_BODY";
    public static final String CMD_NOTIFICATION_RECEIVED = "CMD_NOTIFICATION_RECEIVED";
    public static final String DATA_NOTIFICATION_DATE = "DATA_NOTIFICATION_DATE";
    public static final String DATA_NOTIFICATION_TITLE = "DATA_NOTIFICATION_TITLE";
    public static final String DATA_NOTIFICATION_TEXT = "DATA_NOTIFICATION_TEXT";
    public static final String DATA_NOTIFICATION_SUBTEXT = "DATA_NOTIFICATION_SUBTEXT";
    public static final String CMD_MMS_RECEIVED = "CMD_MMS_RECEIVED";
    public static final String DATA_MMS_DATE = "DATA_MMS_DATE";
    public static final String DATA_MMS_ADDR = "DATA_MMS_ADDR";
    public static final String DATA_MMS_BODY = "DATA_MMS_BODY";

    private BroadcastReceiver _smsReceiver = null;
    private BroadcastReceiver _mmsReceiver = null;
    private BroadcastReceiver _phoneCallReceiver = null;

    private Config _config;

    public RelayService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // notification service를 실행
        Intent intent = new Intent(getBaseContext(), NotificationListener.class);
        startService(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.getStringExtra(CMD_LOAD_SETTINGS) != null) {
            // load settings
            Log.d(this.getClass().getSimpleName(), "Load Settings");
            _config = _loadSettings();
        }
        else if (intent.getStringExtra(CMD_FORWARD_SMS) != null) {
            // forward sms
            Log.d(this.getClass().getSimpleName(), "Forward SMS");
            _forwardSms(intent);
        }
        else if (intent.getStringExtra(CMD_SMS_RECEIVED) != null) {
            // sms received
            Log.d(this.getClass().getSimpleName(), "SMS Received");
            _smsReceived(intent);
            return super.onStartCommand(intent, flags, startId);
        }
        else if (intent.getStringExtra(CMD_PHONECALL_RECEIVED) != null) {
            // phone call received
            Log.d(this.getClass().getSimpleName(), "Phone Call Received");
            _phoneCallReceived(intent);
        }
        else if (intent.getStringExtra(CMD_NOTIFICATION_RECEIVED) != null) {
            // notification received
            Log.d(this.getClass().getSimpleName(), "Notification Received");
            _notificationReceived(intent);
        }
        else if (intent.getStringExtra(CMD_MMS_RECEIVED) != null) {
            // notification received
            Log.d(this.getClass().getSimpleName(), "MMS Received");
            _mmsReceived(intent);
        }
        else {
            Toast.makeText(this, "Service Started", Toast.LENGTH_SHORT).show();
            _config = _loadSettings();
            _registerReceivers();
        }
        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Toast.makeText(this, "Service Destroy", Toast.LENGTH_SHORT).show();
        _unregisterReceivers();

        Intent intent = new Intent(getBaseContext(), NotificationListener.class);
        stopService(intent);
    }

    //----------------------------------------------------------------------------------------------
    // Settings
    //----------------------------------------------------------------------------------------------

    private Config _loadSettings() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this); //getPreferences(MODE_PRIVATE);
        Config cfg = new Config();

        cfg.phoneNumber = sp.getString(Config.PHONE_NUMBER, "");
        cfg.phoneCall = sp.getBoolean(Config.PHONE_CALL, false);
        cfg.sms = sp.getBoolean(Config.SMS, false);
        cfg.email = sp.getBoolean(Config.EMAIL, false);
        cfg.kakoTalk = sp.getBoolean(Config.KAKAOTALK, false);

        return cfg;
    }

    //----------------------------------------------------------------------------------------------
    // BroadcastReceiver Register/Unregister
    //----------------------------------------------------------------------------------------------

    // receiver를 등록 한다
    private void _registerReceivers() {
        // sms receiver 등록
        _smsReceiver = new SMSReceiver();
        IntentFilter srif = new IntentFilter(SMSReceiver.SMS_RECEIVED);
        registerReceiver(_smsReceiver, srif);

        // mms receiver 등록
        _mmsReceiver = new MMSReceiver();
        IntentFilter mrif_1 = new IntentFilter(MMSReceiver.MMS_RECEIVED_1);
        IntentFilter mrif_2 = IntentFilter.create(MMSReceiver.MMS_RECEIVED_2, MMSReceiver.MMS_DATA_TYPE);
        registerReceiver(_mmsReceiver, mrif_1);
        registerReceiver(_mmsReceiver, mrif_2);

        // phone call receiver 등록
        _phoneCallReceiver = new PhoneCallReceiver();
        IntentFilter pcif = new IntentFilter(PhoneCallReceiver.PHONECALL_RECEIVED);
        registerReceiver(_phoneCallReceiver, pcif);
    }

    // receiver를 해제 한다.
    // 해제 하지 않으면 memory leak이나 crash의 원인이 되기도 한다.
    private void _unregisterReceivers() {
        if (_smsReceiver != null) {
            unregisterReceiver(_smsReceiver);
            _smsReceiver = null;
        }

        if (_mmsReceiver != null) {
            unregisterReceiver(_mmsReceiver);
            _mmsReceiver = null;
        }

        if (_phoneCallReceiver != null) {
            unregisterReceiver(_phoneCallReceiver);
            _phoneCallReceiver = null;
        }
    }

    //----------------------------------------------------------------------------------------------
    // SMS Receiving
    //----------------------------------------------------------------------------------------------

    private void _smsReceived(Intent intent) {
        if (_config.sms) {
            String smsDate = intent.getStringExtra(DATA_SMS_DATE);
            String smsAddr = intent.getStringExtra(DATA_SMS_ADDR);
            String smsBody = intent.getStringExtra(DATA_SMS_BODY);

            String smsStr = "문자 받음\n";
            smsStr += "수신 시간 : " + smsDate + "\n";
            smsStr += "수신 번호 : " + smsAddr + "\n";
            smsStr += _getContact(smsAddr) + "\n";
            smsStr += "문자 내용 : " + smsBody;

            _sendSMS(_config.phoneNumber, smsStr);
        }
    }

    private void _mmsReceived(Intent intent) {
        if (_config.sms) {
            String mmsDate = intent.getStringExtra(DATA_MMS_DATE);
            String mmsAddr = intent.getStringExtra(DATA_MMS_ADDR);
            String mmsBody = intent.getStringExtra(DATA_MMS_BODY);

            String smsStr = "MMS 받음\n";
            smsStr += "수신 시간 : " + mmsDate + "\n";
            smsStr += "수신 번호 : " + mmsAddr + "\n";
            smsStr += _getContact(mmsAddr) + "\n";
            smsStr += "문자 내용 : " + mmsBody;

            _sendSMS(_config.phoneNumber, smsStr);
        }
    }

    //----------------------------------------------------------------------------------------------
    // SMS Sending
    //----------------------------------------------------------------------------------------------

    private void _sendSMS(String smsAddr, String smsContents) {
        if (smsAddr.length() == 0) {
            Log.d(this.getClass().getSimpleName(), "No phone number");
            return;
        }

        SmsManager sm = SmsManager.getDefault();
        if (sm == null) {
            Log.d(this.getClass().getSimpleName(), "Null SmsManager");
            return;
        }

        Log.d(this.getClass().getSimpleName(), "Send SMS : " + smsContents);
        ArrayList<String> parts = sm.divideMessage(smsContents);
        sm.sendMultipartTextMessage(smsAddr, null, parts, null, null);

        Toast.makeText(getBaseContext(), smsContents, Toast.LENGTH_SHORT).show();
    }

    //----------------------------------------------------------------------------------------------
    // Phone Call Receiving
    //----------------------------------------------------------------------------------------------
    private void _phoneCallReceived(Intent intent) {
        if (_config.phoneCall) {
            String dateStr = intent.getStringExtra(DATA_PHONECALL_DATE);
            String incomingNumber = intent.getStringExtra(DATA_PHONECALL_INCOMINGNUMBER);

            String contactStr = _getContact(incomingNumber);
            String smsStr = "전화 왔음\n" +
                    "수신 시간 : " + dateStr + "\n" +
                    "수신 번호 : " + incomingNumber + "\n";
            if (contactStr.length() > 0) {
                smsStr += contactStr;
            }

            _sendSMS(_config.phoneNumber, smsStr);
        }
    }

    //----------------------------------------------------------------------------------------------
    // Search Contacts
    //----------------------------------------------------------------------------------------------
    private String _getContact(String incomingNumber) {
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(incomingNumber));
        String contactStr = "";

        ContentResolver contentResolver = getContentResolver();
        Cursor contactLookup = contentResolver.query(uri, new String[] {BaseColumns._ID, ContactsContract.PhoneLookup.DISPLAY_NAME}, null, null, null);
        try {
            if (contactLookup != null && contactLookup.getCount() > 0) {
                contactLookup.moveToNext();

                String contactId = contactLookup.getString(contactLookup.getColumnIndex(ContactsContract.Data._ID));
                String contactName = contactLookup.getString(contactLookup.getColumnIndex(ContactsContract.Data.DISPLAY_NAME));

                contactStr = "수신 이름 : " + contactName;

                Cursor cc = contentResolver.query(
                        ContactsContract.Data.CONTENT_URI,
                        null,
                        ContactsContract.Data.CONTACT_ID + "=? AND " + ContactsContract.Data.MIMETYPE + "=?",
                        new String[]{contactId, ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE},
                        null
                );
                try {
                    cc.moveToNext();

                    String contactCompany = cc.getString(cc.getColumnIndex(ContactsContract.CommonDataKinds.Organization.DATA));
                    String contactTitle = cc.getString(cc.getColumnIndex(ContactsContract.CommonDataKinds.Organization.TITLE));

                    contactStr += "\n";
                    contactStr += "수신 회사 : " + contactCompany + "\n";
                    contactStr += "수신 직책 : " + contactTitle;
                } finally {
                    if (cc != null) {
                        cc.close();
                    }
                }
            }
        } finally {
            if (contactLookup != null) {
                contactLookup.close();
            }
        }

        Log.d(this.getClass().getSimpleName(), "Contact : " + contactStr);
        return contactStr;
    }

    //----------------------------------------------------------------------------------------------
    // Forward SMS
    //----------------------------------------------------------------------------------------------
    private void _forwardSms(Intent intent) {
        String smsAddr = "";
        try {
            smsAddr = intent.getStringExtra(DATA_FORWARD_SMS_ADDR);
        }
        catch (Exception e){
            Log.d(this.getClass().getSimpleName(), e.getMessage());
        }

        String smsBody = "";
        try {
            smsBody = intent.getStringExtra(DATA_FORWARD_SMS_BODY);
        }
        catch (Exception e) {
            Log.d(this.getClass().getSimpleName(), e.getMessage());
        }

        _sendSMS(smsAddr, smsBody);
    }

    //----------------------------------------------------------------------------------------------
    // Notification Received
    //----------------------------------------------------------------------------------------------
    private void _notificationReceived(Intent intent) {
        if (_config.kakoTalk) {
            String pkgName = intent.getStringExtra(CMD_NOTIFICATION_RECEIVED);
            String dateStr = intent.getStringExtra(DATA_NOTIFICATION_DATE);
            String titleStr = intent.getStringExtra(DATA_NOTIFICATION_TITLE);
            String textStr = intent.getStringExtra(DATA_NOTIFICATION_TEXT);
            String subTextStr = intent.getStringExtra(DATA_NOTIFICATION_SUBTEXT);
            String smsBody = "";

            if (pkgName.equals("com.kakao.talk")) {
                // 카카오톡
                smsBody += "카카오톡 받음\n";
            }

            smsBody += "받은 시간 : " + dateStr + "\n";
            smsBody += "제목 : " + titleStr + "\n";
            smsBody += "내용 : " + textStr;
            //smsBody += "SubText : " + subTextStr;

            _sendSMS(_config.phoneNumber, smsBody);
        }
    }
}
