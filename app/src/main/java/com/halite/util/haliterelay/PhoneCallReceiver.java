package com.halite.util.haliterelay;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;

public class PhoneCallReceiver extends BroadcastReceiver {
    public static final String PHONECALL_RECEIVED = "android.intent.action.PHONE_STATE";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Bundle bundle = intent.getExtras();

        if (action.equals(PHONECALL_RECEIVED)) {
            String state = bundle.getString(TelephonyManager.EXTRA_STATE);
            Log.d(this.getClass().getSimpleName(), "Phone Call Received : " + state);
            // 전확 오면 ringing 상태이다가 전화가 끊어지면 idle 상태로 감
            if (state.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
                String incomingNumber = bundle.getString(TelephonyManager.EXTRA_INCOMING_NUMBER);
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String dateStr = sdf.format(new Date());

                Intent phoneCallIntent = new Intent(context, RelayService.class);
                phoneCallIntent.putExtra(RelayService.CMD_PHONECALL_RECEIVED, "");
                phoneCallIntent.putExtra(RelayService.DATA_PHONECALL_DATE, dateStr);
                phoneCallIntent.putExtra(RelayService.DATA_PHONECALL_INCOMINGNUMBER, incomingNumber);
                context.startService(phoneCallIntent);
            }
        }
    }
}
