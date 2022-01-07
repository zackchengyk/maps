import React, {useEffect, useState} from 'react';
import axios from "axios";

const maxCheckins = 30
const requestLatestIntervalMs = 1000;

/**
 * A helper function which turns a Unix timestamp into a JSX expression
 *
 * @param timestamp  a Unix timestamp
 * @param col        the color of the timestamp
 * @returns JSX expression
 */
const timestampToJSX = (timestamp, col = "orange") => {
  // Code taken from https://stackoverflow.com/questions/847185
  const date = new Date(Math.round(timestamp) * 1000);
  // Hours part from the timestamp
  const hours = date.getHours();
  // Minutes part from the timestamp
  const minutes = "0" + date.getMinutes();
  // Seconds part from the timestamp
  const seconds = "0" + date.getSeconds();
  // Will display time in 10:30:23 format
  return (
      <React.Fragment>
        <span className={col}>{(col === "orange" ? "@" : "") + hours + ":"
        + minutes.substr(-2) + ":" + seconds.substr(-2)}</span>
      </React.Fragment>
  )
}

function MapClients({setToast, setNumLines, configObj, displayNone}) {
  const [isRequestingLatest, setIsRequestingLatest] = useState(false)
  const [checkinsList, setCheckinsList] = useState([])
  const [isMaxed, setIsMaxed] = useState(false)

  const [isRequestingOneUsers, setIsRequestingOneUsers] = useState(false)
  const [requestedUsersData, setRequestedUsersData] = useState(
      {timestamp: null, list: []})

  const requestLatestCheckins = () => {
    if (isRequestingLatest) {
      return
    }
    setIsRequestingLatest(true)
    axios.post("http://localhost:4567/get-checkins", {}, configObj)
        .then(response => {
          setIsRequestingLatest(false)
          if (response.data.errorMessage) {
            setToast({type: "error", message: "Error: " + response.data.errorMessage})
            return
          }
          setCheckinsList((prev) =>
              [...response.data.checkins.sort((a, b) => b.timestamp - a.timestamp),
                  ...prev.slice(0, maxCheckins - response.data.checkins.length)])
        })
        .catch(error => {
          setIsRequestingLatest(false)
          setToast({type: "error", message: "Error: " + error.message})
          console.log("Error: ", error)
        })
  }

  const requestOneUsersCheckins = (id, timestamp) => {
    if (isRequestingOneUsers) {
      return
    }
    setIsRequestingOneUsers(true)
    axios.post("http://localhost:4567/get-one-users-checkins", {id}, configObj)
        .then(response => {
          setIsRequestingOneUsers(false)
          if (response.data.errorMessage) {
            setToast({type: "error", message: "Error: " + response.data.errorMessage})
            console.error(response.data.errorMessage)
            return
          }
          setRequestedUsersData({
            timestamp: timestamp,
            list: response.data.checkins.sort((a, b) => b.timestamp - a.timestamp)
          })
        })
        .catch(error => {
          setIsRequestingOneUsers(false)
          setToast({type: "error", message: "Error: " + error.message})
          console.log("Error: ", error)
        })
  }

  useEffect(() => {
    const calculatedLength = Math.min(checkinsList.length, maxCheckins)
    const isEqualToMaxCheckins = calculatedLength === maxCheckins
    let oneUsersLength = 0
    if (checkinsList.filter(ele => ele.timestamp === requestedUsersData.timestamp).length) {
      oneUsersLength += requestedUsersData.list.length + 1
    }
    setNumLines(2 * calculatedLength + oneUsersLength + 1 - isEqualToMaxCheckins * 1)
    setIsMaxed(isEqualToMaxCheckins)
  }, [checkinsList, requestedUsersData, setNumLines])

  useEffect(() => {
    const interval = setInterval(() => {
      requestLatestCheckins();
    }, requestLatestIntervalMs);
    return () => clearInterval(interval)
  })

  const oneUsersDataToJSX = (data) => {
    return (
        <React.Fragment>
          <span className={"expander"} tabIndex={0}
                onClick={onClickClearOneUsersCheckins}
                onKeyDown={(e) =>
                    e.key === "Enter" ? onClickClearOneUsersCheckins() : null}>
            {"{\n"}
          </span>
          {data.list.map((ele, j) =>
              <React.Fragment key={j}>
                <span className="doubleIndent">{timestampToJSX(ele.timestamp, "purple")}</span>
                <span>{": ["}</span>
                <span className="lightblue">{ele.latitude.toString().slice(0, 9)}</span>
                <span className="orange">{","}</span>
                <span className="lightblue">{ele.longitude.toString().slice(0, 10)}</span>
                <span>{"]"}</span>
                <span className="orange">{","}</span>
                <span>{"\n"}</span>
              </React.Fragment>)}
          <span className={"expander"} tabIndex={0}
                onClick={onClickClearOneUsersCheckins}
                onKeyDown={(e) =>
                    e.key === "Enter" ? onClickClearOneUsersCheckins() : null}>
            {"}"}
          </span>
        </React.Fragment>
    )
  }

  const onClickGetOneUsersCheckins = (ele) => {
    requestOneUsersCheckins(ele.id, ele.timestamp)
  }

  const onClickClearOneUsersCheckins = () => {
    setRequestedUsersData({timestamp: null, list: []})
  }

  return (
      <div id="MapClientsContainer" style={{display: displayNone ? "none" : ""}}>
        <ul id="MapClientsList">
          {checkinsList.map((ele, i) =>
              <li className={"MapClientsContent"} key={i}>
                {timestampToJSX(ele.timestamp)}
                <span>{" "}</span>
                <span className="yellow">{ele.name.replaceAll(" ", "") + " "}</span>
                <span>{"(\n"}</span>
                <span className="indent">{"checkedInAt"}</span>
                <span>{" = "}</span>
                <span className="">{"["}</span>
                <span className="lightblue">{ele.latitude.toString().slice(0, 9)}</span>
                <span className="orange">{", "}</span>
                <span className="lightblue">{ele.longitude.toString().slice(0, 10)}</span>
                <span className="">{"]"}</span>
                <span className="orange">{""}</span>
                <span>{") "}</span>
                {ele.timestamp === requestedUsersData.timestamp &&
                oneUsersDataToJSX(requestedUsersData)}
                {ele.timestamp !== requestedUsersData.timestamp &&
                <span id={"replaceMe" + i} className="expander gray" tabIndex={0}
                      onClick={() => onClickGetOneUsersCheckins(ele)}
                      onKeyDown={(e) =>
                          e.key === "Enter" ? onClickGetOneUsersCheckins(ele) : null}>
                      {"{...}"}</span>
                }
              </li>
          )}

          {
            isMaxed && <li className={"MapClientsContent"}>
<span className="gray">{"... Reached view limit (" + maxCheckins
+ " entries)"}</span>
            </li>
          }
        </ul>
      </div>
  )
}

export default MapClients;