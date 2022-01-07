import MapRouteCoordinates from "./MapRouteCoordinates";
import MapRouteNames from "./MapRouteNames";
import MapRouteClear from "./MapRouteClear"
import React, {useState} from "react";
import axios from "axios";

const usingSelectedNodeMessage = "Using Selected Node!"

function MapRouting({
                      nearestNode, setNearestNode,
                      srcNode, destNode,
                      setSrcNode, setDestNode,
                      setWaysAndCells, setRoute,
                      setToast,
                      configObj, displayNone,
                      route
                    }) {
  const [isRouting, setIsRouting] = useState(false)
  const [clickModifier, setClickModifier] = useState(0)

  /**
   * A helper function which actually sends the axios request to the server
   * database, and responds appropriately after.
   *
   * @param url  the url to send the post request to
   * @param body the body to be sent in the post request
   */
  const requestHelper = (url, body) => {
    if (isRouting) {
      return
    }
    setIsRouting(true)
    console.log("%cQuerying server for route.", "color: orange", body)
    axios.post(url, body, configObj)
        .then(response => {
          setIsRouting(false)
          if (response.data.errorMessage) {
            setToast({type: "error", message: "Error: " + response.data.errorMessage})
            console.error(response.data.errorMessage)
            return
          }
          setToast({type: "success", message: "Route found!"})
          setWaysAndCells((prev) => {
            return {ways: {...prev.ways, ...response.data.ways}, cells: prev.cells}
          })
          setRoute(response.data.route)
        })
        .catch(error => {
          setIsRouting(false)
          setToast({type: "error", message: "Error: " + error.message})
          console.log("Error: ", error)
        })
  }

  const requestRouteByMixedHelper = (lat, lon, wayA, wayB, forward) => {
    requestHelper("http://localhost:4567/route-mixed",
        {lat, lon, wayA, wayB, forward})
  }

  /**
   * A function which queries the server database for the route between two
   * points, each specified by their latitude and longitude.
   *
   * Note that this would be somewhat analogous to calling:
   * "route  srcLat  srcLon  desLat  desLon"
   *
   * @param e      the HTML form submission event (used to preventDefault)
   * @param srcLat the latitude of the source point
   * @param srcLon the longitude of the source point
   * @param desLat the latitude of the destination point
   * @param desLon the longitude of the destination point
   */
  const requestRouteByCoordinates = (e, srcLat, srcLon, desLat, desLon) => {
    // Important to prevent reloading the page on click!
    e.preventDefault()
    // Request route by coordinates
    requestHelper("http://localhost:4567/route-coordinates",
        {srcLat, srcLon, desLat, desLon})
  }

  /**
   * A function which queries the server database for the route between two
   * points, each specified as the intersection of two named streets.
   *
   * Note that this would be somewhat analogous to calling:
   * "route  way1  way2  way3  way4"
   *
   * @param e    the HTML form submission event (used to preventDefault)
   * @param way1 the first named street of the source point
   * @param way2 the second named street of the source point
   * @param way3 the first named street of the destination point
   * @param way4 the second named street of the destination point
   */
  const requestRouteByNames = (e, way1, way2, way3, way4) => {
    // Important to prevent reloading the page on click!
    e.preventDefault()
    // If using selected nodes, redirect to other functions
    if (way1 === usingSelectedNodeMessage && way2 === usingSelectedNodeMessage) {
      if (srcNode === null) {
        setToast({
          type: "error", message: "Error: It's a lovely morning in the village, "
              + "and you are a horrible goose. Please select a source node."
        })
        return
      }
      if (way3 === usingSelectedNodeMessage && way4 === usingSelectedNodeMessage) {
        if (destNode === null) {
          setToast({
            type: "error", message: "Error: It's a lovely morning in the village, "
                + "and you are a horrible goose. Please select a destination node."
          })
          return
        }
        requestRouteByCoordinates(e, srcNode.lat, srcNode.lon, destNode.lat, destNode.lon)
        return
      }
      requestRouteByMixedHelper(srcNode.lat, srcNode.lon, way3, way4, true)
      return
    } else if (way3 === usingSelectedNodeMessage && way4 === usingSelectedNodeMessage) {
      if (destNode === null) {
        setToast({
          type: "error", message: "Error: It's a lovely morning in the village, "
              + "and you are a horrible goose. Please select a destination node."
        })
        return
      }
      requestRouteByMixedHelper(destNode.lat, destNode.lon, way1, way2, false)
      return
    }
    // Otherwise, request route by names
    requestHelper("http://localhost:4567/route-names",
        {way1, way2, way3, way4})
  }

  return (
      <div id="MapRoutingContainer" style={{display: displayNone ? "none" : ""}}>
        <MapRouteCoordinates requestRouteByCoordinates={requestRouteByCoordinates}
                             nearestNode={nearestNode} setNearestNode={setNearestNode}
                             setSrcNode={setSrcNode} setDestNode={setDestNode}
                             isRouting={isRouting} clickModifier={clickModifier}
                             setClickModifier={setClickModifier}/>
        <MapRouteNames requestRouteByNames={requestRouteByNames}
                       nearestNode={nearestNode} setNearestNode={setNearestNode}
                       setSrcNode={setSrcNode} setDestNode={setDestNode}
                       isRouting={isRouting} clickModifier={clickModifier}
                       setClickModifier={setClickModifier}
                       usingSelectedNodeMessage={usingSelectedNodeMessage}/>
        <MapRouteClear setRoute={setRoute} setToast={setToast} route={route}/>
      </div>
  )
}

export default MapRouting;