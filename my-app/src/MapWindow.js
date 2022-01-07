import React, {useState} from 'react';
import MapCanvas from "./MapCanvas";
import axios from "axios";
import MapWindowTab from "./MapWindowTab";
import MapRouting from "./MapRouting";
import MapClients from "./MapClients";
import {ToastBox} from "./ToastBox";

const config = {
  headers: {
    "Content-Type": "application/json",
    'Access-Control-Allow-Origin': '*',
  }
}

/**
 * A helper function for deepMergeWaysAndCells
 * @param currWays   the current map of way Ids to way objects
 * @param currCell   the current cell
 * @param importList the list of way Ids to be imported into the current cell
 */
const cellsDividerHelper = (currWays, currCell, importList) => {
  for (let i = 0; i < importList.length; i++) {
    const wayId = importList[i]
    if (typeof currWays[wayId].type === "undefined"
        || currWays[wayId].type === "unclassified"
        || currWays[wayId].type === "") {
      currCell.nonRoads.push(wayId)
    } else {
      currCell.roads.push(wayId)
    }
  }
}

/**
 * A function which takes an object containing new ways and cells and merges it
 * into another object containing existing ways and cells, then returns a new
 * object containing a union of the old and new ways and cells.
 *
 * @param x old ways and cells
 * @param y new ways and cells
 * @returns union of old and new ways and cells
 */
const deepMergeWaysAndCells = (x, y) => {
  const z = {ways: {}, cells: {}}
  z.ways = {...x.ways, ...y.ways}
  // Copy from x
  for (const [k1, v1] of Object.entries(x.cells)) {
    z.cells[k1] = {}
    for (const [k2, v2] of Object.entries(v1)) {
      z.cells[k1][k2] = v2
    }
  }
  // Import from y
  for (const [k1, v1] of Object.entries(y.cells)) {
    if (typeof z.cells[k1] === "undefined") {
      z.cells[k1] = {}
    }
    for (const [k2, v2] of Object.entries(v1)) {
      if (!z.cells[k1][k2]) {
        z.cells[k1][k2] = {roads: [], nonRoads: []}
      }
      cellsDividerHelper(z.ways, z.cells[k1][k2], v2)
    }
  }
  return z
}

function MapWindow() {
  // Important information
  const [toast, setToast] = useState(null)
  const [nearestNode, setNearestNode] = useState(null)
  const [srcNode, setSrcNode] = useState(null)
  const [destNode, setDestNode] = useState(null)
  const [route, setRoute] = useState(null)
  const [waysAndCells, setWaysAndCells] = useState(
      {ways: {}, cells: {}})

  // Miscellanea
  const [activeTab, setActiveTab] = useState("maps3.cs32")
  const [numLines, setNumLines] = useState(1)
  const [isQuerying, setIsQuerying] = useState(false)

  /**
   * A function which queries the server database for the ways and cells in a
   * rectangular region. topLeftLat and topLeftLon refer to the coordinates of
   * the top-left cell, and botRightLat and botRightLon refer to the coordinates
   * of the bottom-right cell.
   *
   * Note that this query includes both the top-left and the bottom-right cell;
   * this would be somewhat analogous to calling:
   * "ways  topLeftLat  topLeftLon  botRightLat-interval  botRightLon+interval"
   *
   * @param topLeftLat  the latitude of the top-left cell's top-left point
   * @param topLeftLon  the longitude of the top-left cell's top-left point
   * @param botRightLat the latitude of the bottom-right cell's top-left point
   * @param botRightLon the longitude of the bottom-right cell's top-left point
   */
  const requestWaysAndCells = (topLeftLat, topLeftLon, botRightLat, botRightLon) => {
    if (isQuerying) {
      return
    }
    setIsQuerying(true)
    const body = {topLeftLat, topLeftLon, botRightLat, botRightLon}
    axios.post("http://localhost:4567/ways-and-cells", body, config)
        .then(response => {
          setIsQuerying(false)
          setToast({type: "success", message: "Loaded map section!"})
          if (response.data.newMap) {
            setWaysAndCells(deepMergeWaysAndCells({ways: {}, cells: {}}, response.data))
          } else {
            setWaysAndCells((prev) => deepMergeWaysAndCells(prev, response.data))
          }
        })
        .catch(error => {
          setIsQuerying(false)
          setToast({type: "error", message: "Error: " + error.message})
          console.log("Error: ", error)
        })
  }

  /**
   * A function which queries the server database for the nearest traversable
   * node to a given query point. If a node is found, nearestNode is set to it.
   * If setModifier is supplied, the found node is instead stored in srcNode or
   * destNode.
   *
   * Note that this would be somewhat analogous to calling:
   * "nearest  lat  lon"
   *
   * @param lat         the latitude of the query point
   * @param lon         the longitude of the query point
   * @param setModifier
   */
  const requestNearestNode = (lat, lon, setModifier = 0) => {
    if (isQuerying) {
      return
    }
    setIsQuerying(true)
    const body = {lat: lat, lon: lon}
    axios.post("http://localhost:4567/nearest-node", body, config)
        .then(response => {
          setIsQuerying(false)
          setToast({
            type: "success",
            message: "Nearest node found! ["
                + response.data.lat.toString().slice(0, 9) + ", "
                + response.data.lon.toString().slice(0, 10) + "]"
          })
          switch (setModifier) {
            case 0:
              setNearestNode(response.data)
              break
            case 1:
            case 3:
              setSrcNode(response.data)
              break
            case 2:
            case 4:
              setDestNode(response.data)
              break
            default:
              return
          }
        })
        .catch(error => {
          setIsQuerying(false)
          setToast({type: "error", message: "Error: " + error.message})
          console.log("Error: ", error)
        })
  }

  return (
      <div id="MapWindow" className="shadow">
        <div id="tabsWrapper">
          <MapWindowTab name={"maps3.cs32"} activeTab={activeTab} setActiveTab={setActiveTab}/>
          <MapWindowTab name={"maps4.cs32"} activeTab={activeTab} setActiveTab={setActiveTab}/>
        </div>
        <div id="MapWindowInnerWrapper">
          <div id="MapWindowLeft" style={{overflowY: (activeTab === "maps4.cs32") ? "scroll" : ""}}>
            <div id="MapWindowLeftInner">
              <div id="MapWindowLeftLineColumn">
                <p>
                  {[...Array((activeTab === "maps3.cs32") ? 20 : numLines)]
                      .map((_, i) =>
                          <React.Fragment key={i}>{i + 1}<br/></React.Fragment>
                      )}
                </p>
              </div>
              <div id="MapWindowLeftRouting">
                <MapRouting nearestNode={nearestNode} setNearestNode={setNearestNode}
                            srcNode={srcNode} destNode={destNode}
                            setSrcNode={setSrcNode} setDestNode={setDestNode}
                            setWaysAndCells={setWaysAndCells} setRoute={setRoute}
                            setToast={setToast} configObj={config}
                            displayNone={activeTab !== "maps3.cs32"}
                            route={route}/>
                <MapClients setToast={setToast} configObj={config}
                            setNumLines={setNumLines}
                            displayNone={activeTab !== "maps4.cs32"}/>
              </div>
            </div>
          </div>
          <div id="MapWindowRight">
            <MapCanvas route={route}
                       waysAndCells={waysAndCells}
                       requestWaysAndCells={requestWaysAndCells}
                       nearestNode={nearestNode}
                       requestNearestNode={requestNearestNode}
                       srcNode={srcNode} destNode={destNode}/>
          </div>
        </div>
        <ToastBox toast={toast}/>
      </div>
  )
}

export default MapWindow;
