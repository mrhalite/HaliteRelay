package com.halite.util.haliterelay;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootCompleteReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            // RelayService 실행
            Intent service = new Intent(context, RelayService.class);
            context.startService(service);

            // NotifyLister 실행
            service = new Intent(context, NotificationListener.class);
            context.startService(service);
        }
    }
}
