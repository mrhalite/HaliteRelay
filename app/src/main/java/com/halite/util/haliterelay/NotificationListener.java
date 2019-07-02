package com.halite.util.haliterelay;

import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;

/**
 * Created by halite on 2017. 10. 13..
 */

public class NotificationListener extends NotificationListenerService {
    @Override
    public void onCreate() {
        super.onCreate();

        // notify를 볼 수 있는 권한이 있는지 체크
        // 권한이 없으면 권한 설정을 띄운다.
        if (!isPermissionAllowed()) {
            Intent intent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }

    // notify 권한 볼 수 있는 권한 있는제 체크
    private boolean isPermissionAllowed() {
        Set<String> notiListenerSet = NotificationManagerCompat.getEnabledListenerPackages(this);
        String myPackageName = getPackageName();

        for(String packageName : notiListenerSet) {
            if(packageName == null) {
                continue;
            }
            if(packageName.equals(myPackageName)) {
                return true;
            }
        }

        return false;
    }

    // notify가 발생하면 불린다
    // 카카오톡의 경우
    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        String pkgName = sbn.getPackageName();

        if (pkgName.equals("com.kakao.talk")) {
//            Log.i("NotificationListener", "[snowdeer] onNotificationPosted() - " + sbn.toString());
//            Log.i("NotificationListener", "[snowdeer] PackageName:" + sbn.getPackageName());
//            Log.i("NotificationListener", "[snowdeer] PostTime:" + sbn.getPostTime());

            Notification notificatin = sbn.getNotification();
            Bundle extras = notificatin.extras;
            String title = extras.getString(Notification.EXTRA_TITLE);
//        int smallIconRes = extras.getInt(Notification.EXTRA_SMALL_ICON);
//        Bitmap largeIcon = ((Bitmap) extras.getParcelable(Notification.EXTRA_LARGE_ICON));
            CharSequence text = extras.getCharSequence(Notification.EXTRA_TEXT);
            CharSequence subText = extras.getCharSequence(Notification.EXTRA_SUB_TEXT);

            if (text == null)
                return;

            Date dt = new Date(sbn.getPostTime());
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String dateStr = sdf.format(dt);

//            Log.i("NotificationListener", "Time:" + dateStr);
//            Log.i("NotificationListener", "Title:" + title);
//            Log.i("NotificationListener", "Text:" + text);
//            Log.i("NotificationListener", "Sub Text:" + subText);

            Context context = getBaseContext();
            Intent intent = new Intent(context, RelayService.class);
            intent.putExtra(RelayService.CMD_NOTIFICATION_RECEIVED, sbn.getPackageName());
            intent.putExtra(RelayService.DATA_NOTIFICATION_DATE, dateStr);
            intent.putExtra(RelayService.DATA_NOTIFICATION_TITLE, title);
            intent.putExtra(RelayService.DATA_NOTIFICATION_TEXT, text);
            intent.putExtra(RelayService.DATA_NOTIFICATION_SUBTEXT, subText);
            context.startService(intent);
        }
    }
}
