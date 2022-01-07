import React from "react";

function MapWindowTab(props) {

  let classes = "tab lightgreen"
  if (props.activeTab === props.name) {
    classes += " active"
  }

  return (
      <button id={props.name + "Tab"} className={classes} onClick={() => props.setActiveTab(props.name)}>
        {props.name}
      </button>
  )
}

export default MapWindowTab;
