package edu.brown.cs.jwu175zcheng12.maps;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

import edu.brown.cs.jwu175zcheng12.csvdataset.StarDataset;
import edu.brown.cs.jwu175zcheng12.csvdataset.MockPersonDataset;
import edu.brown.cs.jwu175zcheng12.mapscommands.CommandMap;
import edu.brown.cs.jwu175zcheng12.mapscommands.CommandNearest;
import edu.brown.cs.jwu175zcheng12.mapscommands.CommandRoute;
import edu.brown.cs.jwu175zcheng12.mapscommands.CommandWays;
import edu.brown.cs.jwu175zcheng12.repl.GeneralREPL;
import edu.brown.cs.jwu175zcheng12.starscommands.CommandNaiveNeighbors;
import edu.brown.cs.jwu175zcheng12.starscommands.CommandMock;
import edu.brown.cs.jwu175zcheng12.starscommands.CommandNaiveRadius;
import edu.brown.cs.jwu175zcheng12.starscommands.CommandNeighbors;
import edu.brown.cs.jwu175zcheng12.starscommands.CommandRadius;
import edu.brown.cs.jwu175zcheng12.starscommands.CommandStars;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import spark.ExceptionHandler;
import spark.Request;
import spark.Response;
import spark.Spark;
import spark.template.freemarker.FreeMarkerEngine;

import freemarker.template.Configuration;

import static edu.brown.cs.jwu175zcheng12.repl.GeneralREPL.printError;

/**
 * The Main class of our project. This is where execution begins.
 */
public final class Main {

  private static final int DEFAULT_PORT = 4567;

  // For various GeneralREPLCommands
  private static final StarDataset STAR_DATASET = new StarDataset();
  private static final MapDatabase MAP_DATABASE = new MapDatabase();
  // For Maps
  private static final CheckinThread CHECKIN_THREAD =
      new CheckinThread("data/maps/userCheckins.sqlite3");

  /**
   * The initial method called when execution begins.
   *
   * @param args An array of command line arguments
   */
  public static void main(String[] args) {
    new Main(args).run();
  }

  private final String[] args;

  private Main(String[] args) {
    this.args = args;
  }

  private void run() {
    // Parse command line arguments
    OptionParser parser = new OptionParser();
    parser.accepts("gui");
    parser.accepts("port").withRequiredArg().ofType(Integer.class)
        .defaultsTo(DEFAULT_PORT);
    OptionSet options = parser.parse(args);

    if (options.has("gui")) {
      runSparkServer((int) options.valueOf("port"));
    }

    // Create new REPL
    GeneralREPL newREPL = new GeneralREPL(
        new InputStreamReader(System.in, StandardCharsets.UTF_8),
        "(\"\\s+\")|(\"\\s+)|(\\s+\")|(\\s+(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$))|(\"$)");

    // Create a new MockPersonDataset
    MockPersonDataset mockPersonDataset = new MockPersonDataset();

    // Add Stars project commands to the REPL
    newREPL.registerCommand(new CommandStars(STAR_DATASET));
    newREPL.registerCommand(new CommandNaiveNeighbors(STAR_DATASET));
    newREPL.registerCommand(new CommandNeighbors(STAR_DATASET));
    newREPL.registerCommand(new CommandNaiveRadius(STAR_DATASET));
    newREPL.registerCommand(new CommandRadius(STAR_DATASET));
    newREPL.registerCommand(new CommandMock(mockPersonDataset));

    // Add Maps project commands to the REPL
    newREPL.registerCommand(new CommandMap(MAP_DATABASE));
    newREPL.registerCommand(new CommandWays(MAP_DATABASE));
    newREPL.registerCommand(new CommandNearest(MAP_DATABASE));
    newREPL.registerCommand(new CommandRoute(MAP_DATABASE));

    // Run the REPL
    newREPL.startREPL();

    // Join thread once REPL is done
    try {
      if (options.has("gui")) {
        CHECKIN_THREAD.join();
      }
    } catch (InterruptedException e) {
      printError("Thread failed to join in Main.java!");
    }
  }

  private static FreeMarkerEngine createEngine() {
    Configuration config = new Configuration();
    File templates = new File("src/main/resources/spark/template/freemarker");
    try {
      config.setDirectoryForTemplateLoading(templates);
    } catch (IOException ioe) {
      System.out.printf("ERROR: Unable use %s for template loading.%n",
          templates);
      System.exit(1);
    }
    return new FreeMarkerEngine(config);
  }

  private void runSparkServer(int port) {
    Spark.port(port);
    Spark.externalStaticFileLocation("src/main/resources/static");

    Spark.options("/*", (request, response) -> {
      String accessControlRequestHeaders = request.headers("Access-Control-Request-Headers");
      if (accessControlRequestHeaders != null) {
        response.header("Access-Control-Allow-Headers", accessControlRequestHeaders);
      }

      String accessControlRequestMethod = request.headers("Access-Control-Request-Method");

      if (accessControlRequestMethod != null) {
        response.header("Access-Control-Allow-Methods", accessControlRequestMethod);
      }

      return "OK";
    });

    Spark.exception(Exception.class, new ExceptionPrinter());

    FreeMarkerEngine freeMarker = createEngine();

    // Start checkin thread
    CHECKIN_THREAD.start();

    // Prepare GUIHandler class
    GUIHandlers myGuiHandlers =
        new GUIHandlers(STAR_DATASET, MAP_DATABASE, CHECKIN_THREAD);

    // Setup Spark Routes
    Spark.before((request, response) -> response.header("Access-Control-Allow-Origin", "*"));
    Spark.get("/stars", new GUIHandlers.StarsFrontPageHandler(), freeMarker);
    Spark.get("/neighbors", myGuiHandlers.new StarsNeighborsHandler(), freeMarker);
    Spark.get("/radius", myGuiHandlers.new StarsRadiusHandler(), freeMarker);
    Spark.post("/nearest-node", myGuiHandlers.new MapNearestNodeHandler());
    Spark.post("/route-coordinates", myGuiHandlers.new MapRouteCoordinatesHandler());
    Spark.post("/route-names", myGuiHandlers.new MapRouteNamesHandler());
    Spark.post("/route-mixed", myGuiHandlers.new MapRouteMixedHandler());
    Spark.post("/ways-and-cells", myGuiHandlers.new MapWaysAndCellsHandler());
    Spark.post("/get-checkins", myGuiHandlers.new GetCheckinsHandler());
    Spark.post("/get-one-users-checkins", myGuiHandlers.new GetOneUsersCheckinsHandler());
  }

  /**
   * Display an error page when an exception occurs in the server.
   */
  private static class ExceptionPrinter implements ExceptionHandler {
    @Override
    public void handle(Exception e, Request req, Response res) {
      res.status(500);
      StringWriter stacktrace = new StringWriter();
      try (PrintWriter pw = new PrintWriter(stacktrace)) {
        pw.println("<pre>");
        e.printStackTrace(pw);
        pw.println("</pre>");
      }
      res.body(stacktrace.toString());
    }
  }
}
