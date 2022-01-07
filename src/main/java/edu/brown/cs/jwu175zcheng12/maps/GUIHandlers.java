package edu.brown.cs.jwu175zcheng12.maps;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import edu.brown.cs.jwu175zcheng12.csvdataset.StarDataset;
import edu.brown.cs.jwu175zcheng12.repl.GeneralPair;
import edu.brown.cs.jwu175zcheng12.stars.Star;
import org.json.JSONObject;
import spark.ModelAndView;
import spark.QueryParamsMap;
import spark.Request;
import spark.Response;
import spark.Route;
import spark.TemplateViewRoute;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static edu.brown.cs.jwu175zcheng12.repl.GeneralREPL.printInfo;

/**
 * A class which contains all the route handlers needed for our Main's GUI.
 */
final class GUIHandlers {

  private final StarDataset starDataset;
  private final MapDatabase mapDatabase;
  private final CheckinThread checkinThread;

  // For GUI things
  private static final DecimalFormat DP_3 = new DecimalFormat("#.###");
  private static final String TABLE_HEADER = "<tr>"
      + "<th>Star Id</th><th>Name</th><th>Distance</th>"
      + "<th>x</th><th>y</th><th>z</th></tr>";
  private static final Gson GSON = new Gson();
  private static final BigDecimal INTERVAL = new BigDecimal("0.01");

  /**
   * The constructor for this class.
   *
   * @param starDataset   the starDataset we want the route handlers to use
   * @param mapDatabase   the mapDatabase we want the route handlers to use
   * @param checkinThread the checkinThread we want the route handlers to use
   */
  GUIHandlers(
      StarDataset starDataset,
      MapDatabase mapDatabase,
      CheckinThread checkinThread) {
    this.starDataset = starDataset;
    this.mapDatabase = mapDatabase;
    this.checkinThread = checkinThread;
  }

  /**
   * Handles requests for a nearest node.
   */
  class MapNearestNodeHandler implements Route {
    @Override
    public Object handle(Request request, Response response) throws Exception {
      printInfo("===== Request to MapNearestNodeHandler received.");
      // Begin extracting request information
      JSONObject data = new JSONObject(request.body());
      double lat = data.getDouble("lat");
      double lon = data.getDouble("lon");
      Double[] coordinates = mapDatabase.getNearestNode(lat, lon).getCoordinates();
      return GSON.toJson(ImmutableMap.of("lat", coordinates[0],
          "lon", coordinates[1]));
    }
  }

  /**
   * Handles requests for a route.
   */
  class MapRouteCoordinatesHandler implements Route {
    @Override
    public Object handle(Request request, Response response) throws Exception {
      printInfo("===== Request to MapRouteCoordinatesHandler received.");
      // Begin extracting request information
      JSONObject data = new JSONObject(request.body());
      double sLat = data.getDouble("srcLat");
      double sLon = data.getDouble("srcLon");
      double dLat = data.getDouble("desLat");
      double dLon = data.getDouble("desLon");
      printInfo("Raw coordinates: " + sLat + ", " + sLon + ", "
          + sLat + ", " + sLon);
      // Get start and end nodes
      Node start = mapDatabase.getNearestNode(sLat, sLon);
      Node end = mapDatabase.getNearestNode(dLat, dLon);
      printInfo("Corresponding to Nodes: " + start + ", " + end);
      if (start == null || end == null) {
        return badRouteResponse("There are no Nodes in this database!");
      }
      // Pass start and end nodes to helper
      return getAndFormatWaysForRoute(start, end);
    }
  }

  /**
   * Handles requests for a route by way names.
   */
  class MapRouteNamesHandler implements Route {
    @Override
    public Object handle(Request request, Response response) throws Exception {
      printInfo("===== Request to MapRouteNamesHandler received.");
      // Begin extracting request information
      JSONObject data = new JSONObject(request.body());
      String way1Name = data.getString("way1");
      String way2Name = data.getString("way2");
      String way3Name = data.getString("way3");
      String way4Name = data.getString("way4");
      printInfo("Raw way names: " + way1Name + ", " + way2Name
          + ", " + way3Name + ", " + way4Name);
      // Get start and end nodes
      Node start = mapDatabase.getIntersectionNode(way1Name, way2Name);
      Node end = mapDatabase.getIntersectionNode(way3Name, way4Name);
      printInfo("Corresponding to Nodes: " + start + ", " + end);
      if (start == null || end == null) {
        return badRouteResponse("At least one street + cross-street pair "
            + "does not intersect!");
      }
      // Pass start and end nodes to helper
      return getAndFormatWaysForRoute(start, end);
    }
  }

  /**
   * Handles requests for a route by a mix of way names and coordinates.
   */
  class MapRouteMixedHandler implements Route {
    @Override
    public Object handle(Request request, Response response) throws Exception {
      printInfo("===== Request to MapRouteMixedHandler received.");
      // Begin extracting request information
      JSONObject data = new JSONObject(request.body());
      double lat = data.getDouble("lat");
      double lon = data.getDouble("lon");
      String wayAName = data.getString("wayA");
      String wayBName = data.getString("wayB");
      boolean forward = data.getBoolean("forward");
      printInfo("Raw information: " + lat + ", " + lon
          + ", " + wayAName + ", " + wayBName + ", forward? = " + forward);
      // Get start and end nodes
      Node coordNode = mapDatabase.getNearestNode(lat, lon);
      Node waysNode = mapDatabase.getIntersectionNode(wayAName, wayBName);
      printInfo("Corresponding to Nodes: " + coordNode + ", " + waysNode);
      if (coordNode == null) {
        return badRouteResponse("There are no Nodes in this database!");
      }
      if (waysNode == null) {
        return badRouteResponse("The given street + cross-street pair "
            + "does not intersect!");
      }
      // Pass start and end nodes to helper
      if (forward) {
        return getAndFormatWaysForRoute(coordNode, waysNode);
      } else {
        return getAndFormatWaysForRoute(waysNode, coordNode);
      }
    }
  }

  /**
   * Handles requests for ways and ways in cells, given a bounding box.
   */
  class MapWaysAndCellsHandler implements Route {
    @Override
    public Object handle(Request request, Response response) throws Exception {
      printInfo("===== Request to MapWaysAndCellsHandler received.");
      // Data extraction
      JSONObject data = new JSONObject(request.body());
      BigDecimal topLeftLat = new BigDecimal(data.getString("topLeftLat"));
      BigDecimal topLeftLon = new BigDecimal(data.getString("topLeftLon"));
      BigDecimal botRightLat = new BigDecimal(data.getString("botRightLat"));
      BigDecimal botRightLon = new BigDecimal(data.getString("botRightLon"));
      printInfo("Ways-And-Cells inputs: " + topLeftLat
          + ", " + topLeftLon + ", " + botRightLat + ", " + botRightLon);
      // Getting results
      GeneralPair<Map<String, Object>, Map<String, Map<String, List<String>>>>
          answer = mapDatabase.getWaysAndCellsForGUI(topLeftLat, topLeftLon,
          botRightLat.subtract(INTERVAL), botRightLon.add(INTERVAL));
      Map<String, Object> variables =
          ImmutableMap.of("ways", ImmutableMap.copyOf(answer.getFirst()),
              "cells", ImmutableMap.copyOf(answer.getSecond()));
      printInfo("Sending response");
      return GSON.toJson(variables);
    }
  }

  /**
   * Handles requests for getting checkins.
   */
  class GetCheckinsHandler implements Route {
    @Override
    public Object handle(Request request, Response response) throws Exception {
      //printInfo("===== Request to GetCheckinsHandler received.");
      // Getting results
      Map<Double, UserCheckin> recentCheckinsMap = checkinThread.getLatestCheckins();
      // Make into list
      List<Object> recentCheckinsList = new ArrayList<>(recentCheckinsMap.size());
      for (Map.Entry<Double, UserCheckin> entry : recentCheckinsMap.entrySet()) {
        Map<String, Object> checkinInfo = ImmutableMap.<String, Object>builder()
            .put("id", entry.getValue().getId())
            .put("name", entry.getValue().getName())
            .put("timestamp", entry.getValue().getTimestamp())
            .put("latitude", entry.getValue().getLat())
            .put("longitude", entry.getValue().getLon())
            .build();
        recentCheckinsList.add(checkinInfo);
      }
      return GSON.toJson(
          ImmutableMap.of("checkins", recentCheckinsList)
      );
    }
  }

  /**
   * Handles requests for getting the checkins for a single user.
   */
  class GetOneUsersCheckinsHandler implements Route {
    @Override
    public Object handle(Request request, Response response) throws Exception {
      printInfo("===== Request to GetOneUsersCheckinsHandler received.");
      // Begin extracting request information
      JSONObject data = new JSONObject(request.body());
      int id = data.getInt("id");
      // Getting results
      return GSON.toJson(ImmutableMap.of("checkins",
          checkinThread.getOneUsersCheckinsHandlerForGUI(id))
      );
    }
  }

  /**
   * Handle GET requests for the front page of our Stars website.
   */
  static class StarsFrontPageHandler implements TemplateViewRoute {
    @Override
    public ModelAndView handle(Request req, Response res) {
      Map<String, Object> variables = ImmutableMap.of(
          "title", "Stars: Query The Database",
          "output", "",
          "outputColor", "green");
      return new ModelAndView(variables, "queryNeighbors.ftl");
    }
  }

  /**
   * Handle GET requests for Stars' neighbors function.
   */
  class StarsNeighborsHandler implements TemplateViewRoute {
    public ModelAndView handle(Request req, Response res) {

      // Extracting information
      QueryParamsMap qm = req.queryMap();
      String starName = qm.value("starName");
      String neighborsStr = qm.value("neighbors");
      String xStr = qm.value("x");
      String yStr = qm.value("y");
      String zStr = qm.value("z");
      Star star = starDataset.getStarByName(starName);
      boolean isUsingStar = starName != null && !starName.equals("");

      // New entrant?
      if (starName == null && neighborsStr == null && xStr == null
          && yStr == null && zStr == null) {
        return makeOutput("", "");
      }

      // Parsing Information
      int neighbors;
      Double x = null;
      Double y = null;
      Double z = null;
      if (neighborsStr == null || neighborsStr.equals("")) {
        return makeOutput("red", "Missing no. of neighbors.");
      }
      try {
        neighbors = Integer.parseInt(neighborsStr);
      } catch (NumberFormatException error) {
        return makeOutput("red", "Bad no. of neighbors.");
      }
      if (!isUsingStar) {
        if (xStr == null || yStr == null || zStr == null
            || xStr.equals("") || yStr.equals("") || zStr.equals("")) {
          return makeOutput("red", "Missing coordinates for neighbors command.");
        }
        try {
          x = Double.parseDouble(xStr);
          y = Double.parseDouble(yStr);
          z = Double.parseDouble(zStr);
        } catch (NumberFormatException error) {
          return makeOutput("red", "Bad coordinates for neighbors command.");
        }
      } else if (star == null) {
        return makeOutput("red", "Star not found.");
      }

      // Get results
      List<Star> results;
      if (isUsingStar) {
        results = starDataset.neighbors(neighbors, star);
      } else {
        results = starDataset.neighbors(neighbors, x, y, z);
      }

      // Check that results is not empty
      if (results.size() == 0) {
        return makeOutput("red", "No stars found.");
      }

      // Create output string
      StringBuilder sb = new StringBuilder();
      sb.append("Neighbouring Stars, In Descending Order Of Proximity:");
      generateTable(sb, results, star, x, y, z);
      return makeOutput("orange", sb.toString());
    }

    private ModelAndView makeOutput(String outputColor, String output) {
      Map<String, String> variables = ImmutableMap.of(
          "title", "Stars: Neighbors Results",
          "output", output,
          "outputColor", outputColor);
      return new ModelAndView(variables, "queryNeighbors.ftl");
    }
  }

  /**
   * Handle GET requests for Stars' radius function.
   */
  class StarsRadiusHandler implements TemplateViewRoute {
    public ModelAndView handle(Request req, Response res) {

      // Extracting information
      QueryParamsMap qm = req.queryMap();
      String starName = qm.value("starName");
      String radiusStr = qm.value("radius");
      String xStr = qm.value("x");
      String yStr = qm.value("y");
      String zStr = qm.value("z");

      // New entrant?
      if (starName == null && radiusStr == null && xStr == null
          && yStr == null && zStr == null) {
        return makeOutput("", "");
      }

      Star star = starDataset.getStarByName(starName);
      boolean isUsingStar = starName != null && !starName.equals("");

      // Parsing Information
      double radius;
      Double x = null;
      Double y = null;
      Double z = null;
      if (radiusStr == null || radiusStr.equals("")) {
        return makeOutput("red", "Missing radius.");
      }
      try {
        radius = Double.parseDouble(radiusStr);
      } catch (NumberFormatException error) {
        return makeOutput("red", "Bad radius.");
      }
      if (!isUsingStar) {
        if (xStr == null || yStr == null || zStr == null
            || xStr.equals("") || yStr.equals("") || zStr.equals("")) {
          return makeOutput("red", "Missing coordinates for radius command.");
        }
        try {
          x = Double.parseDouble(xStr);
          y = Double.parseDouble(yStr);
          z = Double.parseDouble(zStr);
        } catch (NumberFormatException error) {
          return makeOutput("red", "Bad coordinates for radius command.");
        }
      } else if (star == null) {
        return makeOutput("red", "Star not found.");
      }

      // Get results
      List<Star> results;
      if (isUsingStar) {
        results = starDataset.radius(radius, star);
      } else {
        results = starDataset.radius(radius, x, y, z);
      }

      // Check that results is not empty
      if (results.size() == 0) {
        return makeOutput("red", "No stars found.");
      }

      // Create output string
      StringBuilder sb = new StringBuilder();
      sb.append("Stars Within Radius, In Descending Order Of Proximity:");
      generateTable(sb, results, star, x, y, z);
      return makeOutput("orange", sb.toString());
    }

    private ModelAndView makeOutput(String outputColor, String output) {
      Map<String, String> variables = ImmutableMap.of(
          "title", "Stars: Radius Results",
          "output", output,
          "outputColor", outputColor);
      return new ModelAndView(variables, "queryRadius.ftl");
    }
  }

  /**
   * A helper function which gets the route between two Nodes, then formats a
   * JSON to send to the frontend GUI in response to a route query.
   *
   * @param start The starting node in the route query
   * @param end   The ending node in the route query
   * @return a JSON object of the format:
   * {ways: {wayID1: {way1Info}, wayID2: {way2Info}, ... },
   * route: [wayID1, ... ]}
   */
  private static Object getAndFormatWaysForRoute(Node start, Node end) {
    if (start.equals(end)) {
      return badRouteResponse("Same starting and ending Node!");
    }
    List<Way> pathWays = start.aStarPath(end);
    if (pathWays == null) {
      return badRouteResponse("No route found; Nodes are not connected!");
    }
    List<String> pathWayIds = new ArrayList<>(pathWays.size());
    Map<String, Object> waysIntermediate = new HashMap<>();
    for (Way w : pathWays) {
      pathWayIds.add(w.getWayId());
      Double[] wayStart = w.getStart().getCoordinates();
      Double[] wayEnd = w.getEnd().getCoordinates();
      Map<String, Object> wayInfo = ImmutableMap.<String, Object>builder()
          .put("startLat", wayStart[0])
          .put("startLon", wayStart[1])
          .put("endLat", wayEnd[0])
          .put("endLon", wayEnd[1])
          .put("type", w.getType())
          .put("name", w.getName())
          .build();
      waysIntermediate.put(w.getWayId(), wayInfo);
    }
    Map<String, Object> variables = ImmutableMap.of(
        "ways", ImmutableMap.copyOf(waysIntermediate),
        "route", pathWayIds);
    printInfo("Sending good route response");
    return GSON.toJson(variables);
  }

  /**
   * A helper function which formats a JSON to send to the frontend GUI in
   * response to a route query which failed.
   *
   * @param errorMessage a String, the error message to be printed
   * @return a JSON object of the format:
   * {errorMessage: errorMessage, ways: {}, route: []}
   */
  private static Object badRouteResponse(String errorMessage) {
    printInfo("Sending bad route response: \"" + errorMessage + "\"");
    return GSON.toJson(ImmutableMap.of(
        "errorMessage", errorMessage,
        "ways", Collections.emptyMap(),
        "route", Collections.emptyList()));
  }

  /**
   * A function which populates a StringBuilder using a list of stars. It will
   * only get called by the GUI stuff, so ignore SpotBugs' report that this
   * is an uncalled method.
   *
   * @param sb      a StringBuilder to be appended to
   * @param results a list of Stars returned by neighbors / radius
   * @param star    a reference Star, if any
   * @param x       a reference x-coordinate, if any
   * @param y       a reference y-coordinate, if any
   * @param z       a reference z-coordinate, if any
   */
  private static void generateTable(StringBuilder sb, List<Star> results,
                                    Star star, Double x, Double y, Double z) {
    sb.append("<table>");
    sb.append(TABLE_HEADER);
    for (Star resultStar : results) {
      sb.append("<tr><td>");
      sb.append(resultStar.getStarId());
      sb.append("</td><td class=\"name\">");
      sb.append(resultStar.getProperName());
      sb.append("</td><td>");
      if (star != null) {
        sb.append(DP_3.format(resultStar.getDistanceFrom(star)));
      } else {
        sb.append(DP_3.format(resultStar.getDistanceFrom(x, y, z)));
      }
      sb.append("</td><td>");
      sb.append(DP_3.format(resultStar.getX()));
      sb.append("</td><td>");
      sb.append(DP_3.format(resultStar.getY()));
      sb.append("</td><td>");
      sb.append(DP_3.format(resultStar.getZ()));
      sb.append("</td></tr>");
    }
    sb.append("</table>");
  }
}
