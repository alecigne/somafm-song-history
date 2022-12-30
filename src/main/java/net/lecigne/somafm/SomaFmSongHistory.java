package net.lecigne.somafm;

import static net.lecigne.somafm.config.SomaFmConfig.ROOT_CONFIG;
import static net.lecigne.somafm.job.SomaFmSongHistoryJob.ACTION_KEY;
import static net.lecigne.somafm.job.SomaFmSongHistoryJob.BUSINESS_INSTANCE_KEY;
import static net.lecigne.somafm.job.SomaFmSongHistoryJob.CHANNEL_KEY;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigBeanFactory;
import com.typesafe.config.ConfigFactory;
import java.time.ZoneId;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import net.lecigne.somafm.business.BusinessAction;
import net.lecigne.somafm.business.RecentBroadcastBusiness;
import net.lecigne.somafm.config.SomaFmConfig;
import net.lecigne.somafm.job.SomaFmSongHistoryJob;
import net.lecigne.somafm.model.Channel;
import org.flywaydb.core.Flyway;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;

@Slf4j
public class SomaFmSongHistory {

  /**
   * SomaFM's broadcast location.
   */
  public static final ZoneId BROADCAST_LOCATION = ZoneId.of("America/Los_Angeles");

  /**
   * The title given to breaks by SomaFM.
   */
  public static final String BREAK_STATION_ID = "Break / Station ID";

  public static void main(String[] args) throws SchedulerException {
    // Args
    if (args.length != 2) {
      log.error("You must enter the channel's public name as an argument.");
      return;
    }
    var actionName = args[0];
    var channelName = args[1];

    // Load config
    Config config = ConfigFactory.load();
    SomaFmConfig somaFmConfig = ConfigBeanFactory.create(config.getConfig(ROOT_CONFIG), SomaFmConfig.class);

    // Prepare database
    Flyway.configure()
        .dataSource(somaFmConfig.getDbUrl(), somaFmConfig.getDbUser(), somaFmConfig.getDbPassword())
        .load()
        .migrate();

    // Prepare business
    RecentBroadcastBusiness business = RecentBroadcastBusiness.init(somaFmConfig);

    // Prepare scheduler
    SchedulerFactory schedulerFactory = new StdSchedulerFactory();
    Scheduler scheduler = schedulerFactory.getScheduler();
    scheduler.start();
    scheduler.getContext().put(BUSINESS_INSTANCE_KEY, business);

    JobDetail job = newJob(SomaFmSongHistoryJob.class)
        .usingJobData(CHANNEL_KEY, channelName)
        .build();

    Channel channel = Channel.getByPublicName(channelName).orElseThrow();
    job.getJobDataMap().put(CHANNEL_KEY, channel);
    job.getJobDataMap().put(ACTION_KEY, BusinessAction.getValue(actionName));

    int intervalInMinutes = (int) somaFmConfig.getInterval().toMinutes();
    Trigger trigger = newTrigger()
        .startNow()
        .withSchedule(simpleSchedule().withIntervalInMinutes(intervalInMinutes).repeatForever())
        .build();
    scheduler.scheduleJob(job, trigger);
  }

}
