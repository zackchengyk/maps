import React, {useEffect, useState} from "react";

export function ClickModifierSelectButton({
                                            thisNum,
                                            clickModifier, setClickModifier,
                                            nearestNode
                                          }) {
  const [classState, setClassState] = useState("")
  const srcDest = thisNum % 2 ? "Source" : "Destination"
  const currentToolTip = nearestNode ?
      "Set To Currently Selected Node" : (clickModifier === thisNum) ?
          "Disable 'Click To Select " + srcDest + " Node'"
          : "Enable 'Click To Select " + srcDest + " Node'"

  /**
   * An onclick function which toggles the clickModifier between 0 and thisNum
   * when the button is pressed. If the clickModifier was some other value, it
   * will be set to thisNum.
   *
   * The clickModifier affects what will happen when the map is clicked.
   */
  const onClickToggleClickModifier = () => {
    (clickModifier === thisNum) ?
        setClickModifier(0) : setClickModifier(thisNum)
  }

  /**
   * A useEffect hook which sets the style of this button as appropriate when
   * the clickModifier is changed.
   */
  useEffect(() => {
    (clickModifier === thisNum) ?
        setClassState("active") : setClassState("")
  }, [thisNum, clickModifier])

  return (
      <button id={thisNum + "ClickModifierSelectButton"}
              className={
                "tabHighlightable ClickModifierSelectButton hasDataToolTip "
                + classState
              }
              aria-label={currentToolTip} data-tool-tip={currentToolTip}
              type="button" tabIndex="0" onClick={onClickToggleClickModifier}>
        <svg className="selectButtonIcon" xmlns="http://www.w3.org/2000/svg"
             viewBox="0 0 6 10">
          <path
              d="M6,5a1,1,0,0,1-.29.71l-4,4A1,1,0,0,1,.29,8.29L3.59,5,.29,1.71A1,1,0,0,1,1.71.29l4,4A1,1,0,0,1,6,5Z"/>
        </svg>
      </button>
  );
}