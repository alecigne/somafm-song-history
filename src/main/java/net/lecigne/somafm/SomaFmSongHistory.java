package net.lecigne.somafm;

import static net.lecigne.somafm.config.SomaFmConfig.ROOT_CONFIG;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigBeanFactory;
import com.typesafe.config.ConfigFactory;
import java.time.ZoneId;
import lombok.extern.slf4j.Slf4j;
import net.lecigne.somafm.business.RecentBroadcastBusiness;
import net.lecigne.somafm.config.SomaFmConfig;
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
    // Load config
    Config config = ConfigFactory.load();
    SomaFmConfig somaFmConfig = ConfigBeanFactory.create(config.getConfig(ROOT_CONFIG), SomaFmConfig.class);

    // Prepare database
    Flyway.configure()
        .dataSource(somaFmConfig.getDbUrl(), somaFmConfig.getDbUser(), somaFmConfig.getDbPassword())
        .load()
        .migrate();

    // Prepare business
//    RecentBroadcastBusiness business = RecentBroadcastBusiness.init(somaFmConfig);

    SchedulerFactory schedFact = new StdSchedulerFactory();

    Scheduler sched = schedFact.getScheduler();

    sched.start();

    // define the job and tie it to our HelloJob class
    JobDetail job = newJob(RecentBroadcastBusiness.class)
        .withIdentity("myJob", "group1")
        .build();

    // Trigger the job to run now, and then every 40 seconds
    Trigger trigger = newTrigger()
        .withIdentity("myTrigger", "group1")
        .startNow()
        .withSchedule(simpleSchedule()
            .withIntervalInSeconds(40)
            .repeatForever())
        .build();

    // Tell quartz to schedule the job using our trigger
    sched.scheduleJob(job, trigger);
  }

}
