package com.halite.util.haliterelay;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/*
 * SMS 수신 Class
 * 수신되면 MainActivity의 smsReceived()를 호출 한다.
 * Created by halite on 2017. 10. 5..
 */

/*
    AndroidManifest.xml에 다음을 등록 해야 함
        <uses-permission android:name="android.permission.RECEIVE_SMS" />

    References
        1. BroadcastReceiver에서 service로 intent 보내기 ==> https://stackoverflow.com/questions/4420495/sending-an-intent-from-broadcast-receiver-to-a-running-service-in-android
 */

public class SMSReceiver extends BroadcastReceiver {
    public static final String SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(SMS_RECEIVED)) {
            Log.d(this.getClass().getSimpleName(), "SMS Received");

            Bundle bundle = intent.getExtras();

            if (bundle != null) {
                Object[] pdusObj = (Object[])bundle.get("pdus");
                if (pdusObj == null) {
                    Log.d(this.toString(), "pdusObj is null.");
                    return;
                }

                SmsMessage[] messages = new SmsMessage[pdusObj.length];
                for (int i = 0; i < pdusObj.length; i++) {
                    messages[i] = SmsMessage.createFromPdu((byte[])pdusObj[i]);
                }

                StringBuilder sms = new StringBuilder();
                for (SmsMessage smsMessage : messages) {
                    sms.append(smsMessage.getMessageBody());
                }
                String smsStr = sms.toString();

                String cmdStr = "";
                if (smsStr.length() > 6) {
                    cmdStr = smsStr.substring(0, 6);
                }
                Log.d(this.getClass().getSimpleName(), "Command : " + cmdStr);
                if (cmdStr.equals("cmdcmd")) {
                    // command 받았음 전달 할 것
                    Log.d(this.getClass().getSimpleName(), "Forward SMS");
                    String[] cmds = smsStr.split("_");

                    Intent smsIntent = new Intent(context, RelayService.class);
                    smsIntent.putExtra(RelayService.CMD_FORWARD_SMS, "");
                    smsIntent.putExtra(RelayService.DATA_FORWARD_SMS_ADDR, cmds[1]);
                    smsIntent.putExtra(RelayService.DATA_FORWARD_SMS_BODY, cmds[2]);
                    context.startService(smsIntent);
                }
                else {
                    // 단순 메시지 받음
                    Date smsDate = new Date(messages[0].getTimestampMillis());
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String smsDateStr = sdf.format(smsDate);
                    String smsAddr = messages[0].getOriginatingAddress();

                    Intent smsIntent = new Intent(context, RelayService.class);
                    smsIntent.putExtra(RelayService.CMD_SMS_RECEIVED, "");
                    smsIntent.putExtra(RelayService.DATA_SMS_DATE, smsDateStr);
                    smsIntent.putExtra(RelayService.DATA_SMS_ADDR, smsAddr);
                    smsIntent.putExtra(RelayService.DATA_SMS_BODY, sms.toString());
                    context.startService(smsIntent);

                    // Toast.makeText(context, sms.toString(), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}
