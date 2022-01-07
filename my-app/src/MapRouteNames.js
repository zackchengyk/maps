import TextBox from './TextBox.js';
import React, {useEffect, useState} from 'react';
import {ClickModifierSelectButton} from "./ClickModifierSelectButton";

function MapRouteNames({
                         requestRouteByNames,
                         nearestNode, setNearestNode,
                         setSrcNode, setDestNode, isRouting,
                         clickModifier, setClickModifier,
                         usingSelectedNodeMessage
                       }) {
  const [way1, setWay1Name] = useState("")
  const [way2, setWay2Name] = useState("")
  const [way3, setWay3Name] = useState("")
  const [way4, setWay4Name] = useState("")

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
    if (clickModifier === 3) {
      // ... set source node
      setSrcNode(nearestNode)
      setNearestNode(null)
      // Display appropriate form inputs
      setWay1Name(usingSelectedNodeMessage)
      setWay2Name(usingSelectedNodeMessage)
    } else if (clickModifier === 4) {
      // ... set destination node
      setDestNode(nearestNode)
      setNearestNode(null)
      // Display appropriate form inputs
      setWay3Name(usingSelectedNodeMessage)
      setWay4Name(usingSelectedNodeMessage)
    }
    // Reset clickModifier
    setClickModifier(0)
  }, [usingSelectedNodeMessage, clickModifier, setClickModifier, nearestNode, setNearestNode, setSrcNode, setDestNode])

  return (
      <div className="MapRouteFunction">
        <div>
          <p>
            <span className="orange">function</span>{" "}
            <span className="yellow">find_a_route_by_ways</span>{"() {"}
          </p>
          <form className="indent"
                onSubmit={(e) =>
                    requestRouteByNames(e, way1, way2, way3, way4)}>
            <div id="srcNodeSelectorWrapper">
              <div id="srcNodeSelectorInnerWrapper">
                <div className="noWrap">
                  <span> <span className="italics purple">way_S1</span>{" = "}</span>
                  <TextBox label={"name of street 1"} type={"String"} value={way1}
                           addClass={"longer"} change={setWay1Name}/>
                </div>
                <div className="noWrap">
                  <span> <span className="italics purple">way_C1</span>{" = "}</span>
                  <TextBox label={"name of cross-street 1"} type={"String"} value={way2}
                           addClass={"longer"} change={setWay2Name}/>
                </div>
              </div>
              <ClickModifierSelectButton thisNum={3} clickModifier={clickModifier}
                                         setClickModifier={setClickModifier}
                                         nearestNode={nearestNode}/>
            </div>
            <div id="srcNodeSelectorWrapper">
              <div id="srcNodeSelectorInnerWrapper">
                <div className="noWrap">
                  <span> <span className="italics purple">way_S2</span>{" = "}</span>
                  <TextBox label={"name of street 2"} type={"String"} value={way3}
                           addClass={"longer"} change={setWay3Name}/>
                </div>
                <div className="noWrap">
                  <span> <span className="italics purple">way_C2</span>{" = "}</span>
                  <TextBox label={"name of cross-street 2"} type={"String"} value={way4}
                           addClass={"longer"} change={setWay4Name}/>
                </div>
              </div>
              <ClickModifierSelectButton thisNum={4} clickModifier={clickModifier}
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
      </div>
  )
}

export default MapRouteNames;
