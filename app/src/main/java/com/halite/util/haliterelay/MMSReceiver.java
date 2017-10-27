package com.halite.util.haliterelay;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.SmsMessage;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/*
    susemi99.kr/664 참조
 */

public class MMSReceiver extends BroadcastReceiver {
    public static final String MMS_RECEIVED_1 = "android.intent.action.DATA_SMS_RECEIVED";
    public static final String MMS_RECEIVED_2 = "android.provider.Telephony.WAP_PUSH_RECEIVED";
    public static final String MMS_DATA_TYPE = "application/vnd.wap.mms-message";

    private Context _context;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(this.getClass().getSimpleName(), intent.getAction().toString());

        _context = context;

        Runnable runn = new Runnable() {
            @Override
            public void run() {
                parseMMS();
            }
        };
        Handler handler = new Handler();
        handler.postDelayed(runn, 6000);
    }

    private void parseMMS()
    {
        ContentResolver contentResolver = _context.getContentResolver();
        final String[] projection = new String[] { "_id" };
        Uri uri = Uri.parse("content://mms");
        Cursor cursor = contentResolver.query(uri, projection, null, null, "_id desc limit 1");

        if (cursor.getCount() == 0)
        {
            cursor.close();
            return;
        }

        cursor.moveToFirst();
        String id = cursor.getString(cursor.getColumnIndex("_id"));
        cursor.close();

        String date = parseDate(id);
        String number = parseNumber(id);
        String msg = parseMessage(id);
        Log.d(this.getClass().getSimpleName(), "mms | " + number + "|" + msg);

        Intent mmsIntent = new Intent(_context, RelayService.class);
        mmsIntent.putExtra(RelayService.CMD_MMS_RECEIVED, "");
        mmsIntent.putExtra(RelayService.DATA_MMS_DATE, date);
        mmsIntent.putExtra(RelayService.DATA_MMS_ADDR, number);
        mmsIntent.putExtra(RelayService.DATA_MMS_BODY, msg);
        _context.startService(mmsIntent);
    }

    private String parseDate(String $id) {
        Cursor cursor = _context.getContentResolver().query(Uri.parse("content://mms"), null, null, null, null);
        int count = cursor.getCount();
        if (count > 0) {
            cursor.moveToFirst();
            long timestamp = cursor.getLong(2) * 1000;
            Date date = new Date(timestamp);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String dateStr = sdf.format(date);

            return dateStr;
        }

        return null;
    }

    private String parseNumber(String $id)
    {
        String result = null;

        Uri uri = Uri.parse(MessageFormat.format("content://mms/{0}/addr", $id));
        String[] projection = new String[] { "address" };
        String selection = "msg_id = ? and type = 137";// type=137은 발신자
        String[] selectionArgs = new String[] { $id };

        Cursor cursor = _context.getContentResolver().query(uri, projection, selection, selectionArgs, "_id asc limit 1");

        if (cursor.getCount() == 0)
        {
            cursor.close();
            return result;
        }

        cursor.moveToFirst();
        result = cursor.getString(cursor.getColumnIndex("address"));
        cursor.close();

        return result;
    }

    private String parseMessage(String $id)
    {
        String result = null;

        // 조회에 조건을 넣게되면 가장 마지막 한두개의 mms를 가져오지 않는다.
        Cursor cursor = _context.getContentResolver().query(Uri.parse("content://mms/part"), new String[] { "mid", "_id", "ct", "_data", "text" }, null, null, null);

        Log.d(this.getClass().getSimpleName(), "mms 메시지 갯수 : " + cursor.getCount() + "|");
        if (cursor.getCount() == 0)
        {
            cursor.close();
            return result;
        }

        cursor.moveToFirst();
        while (!cursor.isAfterLast())
        {
            String mid = cursor.getString(cursor.getColumnIndex("mid"));
            if ($id.equals(mid))
            {
                String partId = cursor.getString(cursor.getColumnIndex("_id"));
                String type = cursor.getString(cursor.getColumnIndex("ct"));
                if ("text/plain".equals(type))
                {
                    String data = cursor.getString(cursor.getColumnIndex("_data"));

                    if (TextUtils.isEmpty(data))
                        result = cursor.getString(cursor.getColumnIndex("text"));
                    else
                        result = parseMessageWithPartId(partId);
                }
            }
            cursor.moveToNext();
        }
        cursor.close();

        return result;
    }


    private String parseMessageWithPartId(String $id)
    {
        Uri partURI = Uri.parse("content://mms/part/" + $id);
        InputStream is = null;
        StringBuilder sb = new StringBuilder();
        try
        {
            is = _context.getContentResolver().openInputStream(partURI);
            if (is != null)
            {
                InputStreamReader isr = new InputStreamReader(is, "UTF-8");
                BufferedReader reader = new BufferedReader(isr);
                String temp = reader.readLine();
                while (!TextUtils.isEmpty(temp))
                {
                    sb.append(temp);
                    temp = reader.readLine();
                }
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (is != null)
            {
                try
                {
                    is.close();
                }
                catch (IOException e)
                {
                }
            }
        }
        return sb.toString();
    }
}
