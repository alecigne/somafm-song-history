package net.lecigne.somafm.job;

import net.lecigne.somafm.business.BusinessAction;
import net.lecigne.somafm.business.RecentBroadcastBusiness;
import net.lecigne.somafm.model.Channel;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.SchedulerContext;
import org.quartz.SchedulerException;

public class SomaFmSongHistoryJob implements Job {

  public static final String BUSINESS_INSTANCE_KEY = "business";
  public static final String ACTION_KEY = "action";
  public static final String CHANNEL_KEY = "channel";

  @Override
  public void execute(JobExecutionContext jobExecutionContext) {
    SchedulerContext schedulerContext = null;
    try {
      schedulerContext = jobExecutionContext.getScheduler().getContext();
    } catch (SchedulerException e) {
      e.printStackTrace();
    }
    var business = (RecentBroadcastBusiness) schedulerContext.get(BUSINESS_INSTANCE_KEY);
    JobDataMap jobDataMap = jobExecutionContext.getJobDetail().getJobDataMap();
    var action = (BusinessAction) jobDataMap.get(ACTION_KEY);
    var channel = (Channel) jobDataMap.get(CHANNEL_KEY);
    business.handleRecentBroadcasts(action, channel);
  }

}
