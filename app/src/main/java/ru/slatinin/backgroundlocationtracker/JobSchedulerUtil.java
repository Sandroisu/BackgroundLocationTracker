package ru.slatinin.backgroundlocationtracker;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;

import java.util.concurrent.TimeUnit;

public class JobSchedulerUtil {
    private final static int LOCATION_UPDATE_ID = 1411;

    public static void scheduleUpdateJob(Context app){
        JobInfo jobInfo = new JobInfo.Builder(LOCATION_UPDATE_ID,
                new ComponentName(app, LocationUpdateJobService.class))
                .setPeriodic(TimeUnit.MINUTES.toMillis(15))
                .setBackoffCriteria(TimeUnit.MINUTES.toMillis(8),
                        JobInfo.BACKOFF_POLICY_LINEAR)
                .build();
        JobScheduler scheduler = (JobScheduler) app.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        scheduler.schedule(jobInfo);
    }

}
