import TextBox from './TextBox.js';
import React, {useEffect, useState} from 'react';
import {ClickModifierSelectButton} from "./ClickModifierSelectButton";

function MapRouteCoordinates({
                               requestRouteByCoordinates,
                               nearestNode, setNearestNode,
                               setSrcNode, setDestNode, isRouting,
                               clickModifier, setClickModifier
                             }) {
  const [srcLat, setSrcLat] = useState("")
  const [srcLon, setSrcLon] = useState("")
  const [desLat, setDesLat] = useState("")
  const [desLon, setDesLon] = useState("")

  /**
   * A useEffect hook which sets the srcNode, destNode, and nearestNode as
   * appropriate when the clickModifier or nearestNode are changed.
   */
  useEffect(() => {
    // User does not have nearestNode picked out; ready to click.
    if (nearestNode === null) {
      return
    }
    // User has nearestNode picked out; clicked to...
    if (clickModifier === 1) {
      // ... set source node
      setSrcNode(nearestNode)
      setNearestNode(null)
      // Display appropriate form inputs
      setSrcLat(nearestNode.lat)
      setSrcLon(nearestNode.lon)
    } else if (clickModifier === 2) {
      // ... set destination node
      setDestNode(nearestNode)
      setNearestNode(null)
      // Display appropriate form inputs
      setDesLat(nearestNode.lat)
      setDesLon(nearestNode.lon)
    }
    // Reset clickModifier
    setClickModifier(0)
  }, [clickModifier, setClickModifier, nearestNode, setNearestNode, setSrcNode, setDestNode])

  return (
      <div className="MapRouteFunction">
        <p>
          <span className="orange">function</span>{" "}
          <span className="yellow">find_a_route_by_coords</span>{"() {"}
        </p>
        <form className="indent" onSubmit={(e) =>
            requestRouteByCoordinates(e, srcLat, srcLon, desLat, desLon)}>
          <div id="srcNodeSelectorWrapper">
            <div id="srcNodeSelectorInnerWrapper">
              <div className="noWrap">
                <span> <span className="italics purple">srcLat</span> = </span>
                <TextBox label={"source latitude"} type={"number"} value={srcLat}
                         change={setSrcLat}/>
              </div>
              <div className="noWrap">
                <span> <span className="italics purple">srcLon</span> = </span>
                <TextBox label={"source longitude"} type={"number"} value={srcLon}
                         change={setSrcLon}/>
              </div>
            </div>
            <ClickModifierSelectButton thisNum={1} clickModifier={clickModifier}
                                       setClickModifier={setClickModifier}
                                       nearestNode={nearestNode}/>
          </div>
          <div id="desCoordsWrapper">
            <div id="desCoordsInnerWrapper">
              <div className="noWrap">
                <span> <span className="italics purple">desLat</span> = </span>
                <TextBox label={"destination latitude"} type={"number"} value={desLat}
                         change={setDesLat}/>
              </div>
              <div className="noWrap">
                <span> <span className="italics purple">desLon</span> = </span>
                <TextBox label={"destination longitude"} type={"number"} value={desLon}
                         change={setDesLon}/>
              </div>
            </div>
            <ClickModifierSelectButton thisNum={2} clickModifier={clickModifier}
                                       setClickModifier={setClickModifier}
                                       nearestNode={nearestNode}/>
          </div>
          <div className="buttonInputWrapper hasDataToolTip"
               data-tool-tip={"Submit Route Request"}>
            <input className={isRouting ? "buttonInput inactive" : "buttonInput"}
                   tabIndex="0" type="submit" value="return"/>
          </div>
        </form>
        <p>{"}"}</p>
      </div>
  )
}

export default MapRouteCoordinates;
