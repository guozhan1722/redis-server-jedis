/* Copyright Vecima Networks Inc. as an unpublished work. All Rights Reserved.
 *
 * The information contained herein is confidential property of Vecima Networks Inc. The use,
 * copying, transfer or disclosure of such information is prohibited except by express written
 * agreement with Vecima Networks Inc. */

package jedissentinelexample;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisSentinelPool;
import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.jedis.exceptions.JedisException;

import java.io.PrintStream;
import java.util.HashSet;
import java.util.Set;

/**
 * \defgroup java_src
 * @{
 */

/** A basic Jedis Sentinel pool example. Can be used for testing. */

@SuppressWarnings({ "PMD.UseUtilityClass", 
                    "PMD.SystemPrintln",
                    "PMD.AvoidInstantiatingObjectsInLoops" })

class JedisSentinelExample {
  private static final int SENTINEL_PORT = 26379;
  private static final String SENTINEL_MASTER_NAME = "vmaster";
  private static final String SENTINEL_SERVICE_NAME = "redis-sentinel";
  private static final int SET_COMMAND_ARG_NUM = 2;

  private static PrintStream realSystemOut = System.out;
  private static JedisSentinelPool jedisSentinelPool;
  private static Jedis jedis;
  private static Options options;
  
  private static Set<String> getSentinelPoolSet() {
    Set<String> sentinelSet = new HashSet<String>();

    HostAndPort serviceHost = new HostAndPort(SENTINEL_SERVICE_NAME, SENTINEL_PORT);
    String serviceAddr = serviceHost.getHost();

    // The set adding to Sentinel pool's format is "ip_addr:port"
    sentinelSet.add(serviceAddr + ":" + SENTINEL_PORT);

    return sentinelSet;
  }

  /** Set key-value to Redis . */
  private static String setRedisValue(String userKey, String userValue) {
    String reply = null;

    if (userKey == null || userValue == null) {
      System.out.println("Invalid Input commands");
      return reply;
    }

    jedis = null;
    try {
      String addressIp = jedisSentinelPool.getResource().getClient().getHost();
      jedis = new Jedis(addressIp);
    } catch (JedisException e) {
      System.out.println(e);
      System.exit(1);
    }

    if (jedis != null) {
      reply = jedis.set(userKey, userValue);
      jedis.close();
    }

    return reply;
  }

  /** Get key-value from redis. */
  private static String getRedisValue(String userKey) {
    String reply = null;
    jedis = null;

    if (userKey == null) {
      System.out.println("Invalid Input commands");
      return reply;
    }

    try {
      String addressIp = jedisSentinelPool.getResource().getClient().getHost();
      jedis = new Jedis(addressIp);
    } catch (JedisException e) {
      System.out.println(e);
      System.exit(1);
    }
    
    if (jedis != null) {
      reply = jedis.get(userKey);
      jedis.close();
    }
    return reply;
  }

  
  private static Options buildOptions() {
    Options options = new Options();

    options.addOption("h", false, "Print this Message")
           .addOption("g", true, "Get redis key value");

    /* The options just retrieve the first arg by default. 
    we need setArgs to more than 1 for Set argument. */
    Option option = new Option("s", "Set Redis key value");
    option.setArgs(Option.UNLIMITED_VALUES);
    options.addOption(option);
    return options;
  }
  
  private static CommandLine parseCommandline(String... args) throws ParseException {
    options = buildOptions();
    DefaultParser parser = new DefaultParser();

    return parser.parse(options, args);
  }

  public static void main(final String... argv) {

    CommandLine cmd = null;
    try {
      cmd = parseCommandline(argv);
    } catch (ParseException pe) {
      System.out.println(pe.getMessage());
      System.exit(1);
    }
    
    if (argv.length == 0 || cmd.hasOption("h")) {
      HelpFormatter formatter = new HelpFormatter();
      formatter.printHelp("Jedis Sentinel Example",
          "usage: java -jar /usr/share/java/jedis-sentinel-example.jar [options ...] <args>",
          options, "");
      System.exit(1);
    }


    String result = null;

    jedisSentinelPool = null;
    try {
      /*
       * In order to remove the output from sentinel, we have to shut down the output stream
       * temporarily
       */
      System.setOut(null);
      jedisSentinelPool = new JedisSentinelPool(SENTINEL_MASTER_NAME, getSentinelPoolSet());
      /* Resume screen stream output to display the test result */ 
      System.setOut(realSystemOut);

      if (cmd.hasOption("s")) {
        String[] mySet = cmd.getOptionValues("s");
        if (mySet.length == SET_COMMAND_ARG_NUM) {
          String myKey = mySet[0];
          String myVal = mySet[1];
          result = setRedisValue(myKey, myVal);
        }
      } else if (cmd.hasOption("g")) {
        String myKey = cmd.getOptionValue("g");
        result = getRedisValue(myKey);
      }
    } catch (JedisConnectionException jce) {
      System.out.println(jce);
    } finally {
      if (jedisSentinelPool != null) {
        jedisSentinelPool.destroy();
      }
    }
    
    System.out.println(result);
    System.exit(0);
  }
}
/**
 * @}
 */
