function MapRouteClear({setRoute, setToast, route}) {

  const clearRoute = (e) => {
    e.preventDefault()
    if (route !== null) {
      setRoute(null)
      setToast({type: "success", message: "Route cleared!"})
    }
  }

  return (
      <div className="MapRouteFunction">
        <div>
          <p>
            <span className="orange">function</span>{" "}
            <span className="yellow">clear_route</span>{"() {"}
          </p>
          <form className="indent"
                onSubmit={(e) => clearRoute(e)}>
            <div className="buttonInputWrapper hasDataToolTip"
                 data-tool-tip={"Clear Route"}>
              <input className="buttonInput" tabIndex="0"
                     type="submit" value="return"/>
            </div>
          </form>
          <p>{"}"}</p>
        </div>
      </div>
  )
}

export default MapRouteClear;