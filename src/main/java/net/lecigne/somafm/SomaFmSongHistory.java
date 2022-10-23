package net.lecigne.somafm;

import static net.lecigne.somafm.config.Configuration.ROOT_CONFIG;
import static net.lecigne.somafm.model.Channel.DRONE_ZONE;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigBeanFactory;
import com.typesafe.config.ConfigFactory;
import java.time.ZoneId;
import net.lecigne.somafm.business.RecentBroadcastBusiness;
import net.lecigne.somafm.config.Configuration;

public class SomaFmSongHistory {

  /**
   * SomaFM's broadcast location.
   */
  public static final ZoneId BROADCAST_LOCATION = ZoneId.of("America/Los_Angeles");

  /**
   * The title given to breaks by SomaFM.
   */
  public static final String BREAK_STATION_ID = "Break / Station ID";

  public static void main(String[] args) {
    Config config = ConfigFactory.load();
    Configuration configuration = ConfigBeanFactory.create(config.getConfig(ROOT_CONFIG), Configuration.class);
    String defaultChannel = args.length != 0 ? args[0] : DRONE_ZONE.getPublicName();
    RecentBroadcastBusiness business = RecentBroadcastBusiness.init(configuration);
    business.displayRecentBroadcasts(defaultChannel);
  }

}
