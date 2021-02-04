package br.edu.ifrs.classplanner.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import br.edu.ifrs.classplanner.R;
import br.edu.ifrs.classplanner.activity.MainActivity;
import br.edu.ifrs.classplanner.helper.Helper;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class ReminderService extends JobService {

    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        Intent intent = new Intent(getBaseContext(), MainActivity.class);
        PendingIntent pendingIntent = PendingIntent
                .getActivity(getBaseContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notification = new NotificationCompat.Builder(this, "reminder")
                .setContentTitle("Lembrete")
                .setContentText("Você tem aulas ainda não planejadas")
                .setSmallIcon(R.drawable.ic_attention)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build();

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        notificationManager.notify(0, notification);

        return true;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        return false;
    }

    public int generateJobId(String date, String time) {
        LocalDateTime localDateTime = Helper.parseDateTime(date + time);
        long millis = ZonedDateTime.of(localDateTime, ZoneId.systemDefault()).toInstant().toEpochMilli();
        return (int) (millis % Integer.MAX_VALUE);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void cancelJob(Context context, int jobId) {
        JobScheduler scheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        if (scheduler.getPendingJob(jobId) != null) {
            scheduler.cancel(jobId);
        }
    }
}