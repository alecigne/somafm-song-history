package net.lecigne.somafm.model;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Predicate;
import lombok.Getter;

@Getter
public enum Channel {
  BEAT_BLENDER("beatblender", "Beat Blender"),
  BLACK_ROCK_FM("brfm", "Black Rock FM"),
  BOOT_LIQUOR("bootliquor", "Boot Liquor"),
  CHRISTMAS_LOUNGE("christmas", "Christmas Lounge"),
  CHRISTMAS_ROCKS("xmasrocks", "Christmas Rocks!"),
  CLIQHOP_IDM("cliqhop", "cliqhop idm"),
  COVERS("covers", "Covers"),
  DEEP_SPACE_ONE("deepspaceone", "Deep Space One"),
  DEF_CON_RADIO("defcon", "DEF CON Radio"),
  DIGITALIS("digitalis", "Digitalis"),
  DRONE_ZONE("dronezone", "Drone Zone"),
  DUB_STEP_BEYOND("dubstep", "Dub Step Beyond"),
  FLUID("fluid", "Fluid"),
  FOLK_FORWARD("folkfwd", "Folk Forward"),
  GROOVE_SALAD("groovesalad", "Groove Salad"),
  GROOVE_SALAD_CLASSICS("gsclassic", "Groove Salad Classics"),
  HEAVYWEIGHT_REGGAE("reggae", "Heavyweight Reggae"),
  ILLINOIS_STREET_LOUNGE("illstreet", "Illinois Street Lounge"),
  INDIE_POP_ROCKS("indiepop", "Indie Pop Rocks!"),
  JOLLY_OL_SOUL("jollysoul", "Jolly Ol' Soul"),
  LEFT_COAST_70S("seventies", "Left Coast 70s"),
  LUSH("lush", "Lush"),
  METAL_DETECTOR("metal", "Metal Detector"),
  MISSION_CONTROL("missioncontrol", "Mission Control"),
  N5MD_RADIO("n5md", "n5MD Radio"),
  POPTRON("poptron", "PopTron"),
  SECRET_AGENT("secretagent", "Secret Agent"),
  SEVEN_INCH_SOUL("7soul", "Seven Inch Soul"),
  SF_10_33("sf1033", "SF 10-33"),
  SF_POLICE_SCANNER("scanner", "SF Police Scanner"),
  SONIC_UNIVERSE("sonicuniverse", "Sonic Universe"),
  SPACE_STATION_SOMA("spacestation", "Space Station Soma"),
  SUBURBS_OF_GOA("suburbsofgoa", "Suburbs of Goa"),
  SYNPHAERA_RADIO("synphaera", "Synphaera Radio"),
  THE_DARK_ZONE("darkzone", "The Dark Zone"),
  THE_TRIP("thetrip", "The Trip"),
  THISTLE_RADIO("thistle", "ThistleRadio"),
  UNDERGROUND_80S("u80s", "Underground 80s"),
  VAPORWAVES("vaporwaves", "Vaporwaves"),
  XMAS_IN_FRISKO("xmasinfrisko", "Xmas in Frisko"),
  SPECIALS("specials", "Specials");

  private final String internalName;
  private final String publicName;

  Channel(String internalName, String publicName) {
    this.internalName = internalName;
    this.publicName = publicName;
  }

  public static Optional<Channel> getByInternalName(String internalName) {
    return getBy(channel -> channel.internalName.equals(internalName));
  }

  public static Optional<Channel> getByPublicName(String publicName) {
    return getBy(channel -> channel.publicName.equals(publicName));
  }

  private static Optional<Channel> getBy(Predicate<Channel> predicate) {
    return Arrays.stream(Channel.values())
        .filter(predicate)
        .findFirst();
  }

}
