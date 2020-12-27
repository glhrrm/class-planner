package br.edu.ifrs.classplanner.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.RequiresApi;

import br.edu.ifrs.classplanner.R;
import br.edu.ifrs.classplanner.activity.MainActivity;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class ReminderService extends JobService {
    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        Intent intent = new Intent(getBaseContext(), MainActivity.class);
        int id = (int) (Math.random() * 1000);
        PendingIntent pendingIntent = PendingIntent
                .getActivity(getBaseContext(), id, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notification = new Notification.Builder(getBaseContext())
                .setContentTitle("Lembrete")
                .setContentText("Você tem aulas ainda não planejadas")
                .setSmallIcon(R.drawable.ic_attention)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent).build();

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        notificationManager.notify(0, notification);

        return true;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        return false;
    }
}