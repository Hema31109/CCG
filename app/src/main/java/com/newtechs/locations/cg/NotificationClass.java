package com.newtechs.locations.cg;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.newtechs.locations.cg.activities.CaseListActivity;

/**
 * Created by Niru.R on 05-10-2018.
 */

public class NotificationClass {
    private static void addNotification(Context context,String title,String content,int flag) {
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.mipmap.ic_launcher_circle_arrow)
                        .setContentTitle(title)
                        .setContentText(content);

        Intent notificationIntent = new Intent(context, CaseListActivity.class);
        notificationIntent.putExtra("flag",flag);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(contentIntent);

        // Add as notification
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(0, builder.build());
    }
}
