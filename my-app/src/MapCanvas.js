import React, {useEffect, useRef, useState, useCallback} from 'react';

// For the sake of your sanity while reading this file, hit cmd + shift + minus
// It has been formatted to be most readable when code-folded

// Canvas properties
const fakeDim = 1200, realDim = 600
const step = 0.01, stepInv = 100
const fakeRealScale = fakeDim / realDim
const panRenderDelay = 100
const zoomRenderDelay = 50
const deadZonePx = 4
const zoomInRatio = 10 / 9
const zoomOutRatio = 1 / zoomInRatio

// Style: Map Cells
const canvasBackgroundColor = "rgb(49, 51, 53)"
const notYetLoadedCellColor = "rgb(47, 48, 49)"
// Style: Roads and Non-Roads
const roadScale = 2 / 1000 / 75
const nonRoadScale = 1.5 / 1000 / 75
const roadColor = "rgb(128,128,128)"
const nonRoadColor = "rgb(95,97,99)"
// Style: Route
const routeScale = 4 / 1000 / 75
const routeMinPx = 6
const routeColor = "rgb(111,173,255)"
const routeDash1Scale = 2 / 10000
const routeDash2Scale = 1.5 / 10000
// Style: Source and Destination Markers
const srcColor = "rgb(255,100,171)"
const desColor = "rgb(124,255,89)"
// Style: Circles
const circleRad = Math.floor(fakeDim * 0.009)
const circleStrokeWidth = Math.floor(fakeDim * 0.004)
const circleColor = "rgb(248,189,70)"
// Style: Funky Pointer
const pointerRad = 1.5 * circleRad
const pointerYCenter = 2 * pointerRad
const pointerXInter = pointerRad / pointerYCenter * Math.sqrt((pointerYCenter + pointerRad) * (pointerYCenter - pointerRad))
const pointerYInter = pointerYCenter - pointerRad * pointerRad / pointerYCenter
const pointerAng = Math.atan2((pointerYInter - pointerYCenter), pointerXInter)
// Style: Funky Flag
const flagY1 = 2 * circleRad
const flagY3 = flagY1 + 3 * circleRad
const flagY2 = (flagY1 + flagY3) / 2
const flagX1 = 3 * circleRad
const flagX2 = flagX1 + circleRad
const flagXCircle = 1.65 * circleRad
const flagSq = circleRad

// Small Helper Functions For Rounding
const roundUpLat = (lat) => Math.ceil(lat * stepInv) / stepInv
const roundDownLon = (lon) => Math.floor(lon * stepInv) / stepInv
const roundCent = (num) => Math.round(num * stepInv) / stepInv

// Medium Helper Functions For Drawing
const drawCircleAt = (ctx, x, y, color = circleColor, type = "") => {
  ctx.beginPath()
  ctx.lineWidth = circleStrokeWidth
  ctx.strokeStyle = color
  switch (type) {
    case "filled":
      ctx.arc(x, y, circleRad, 0, 2 * Math.PI)
      ctx.fillStyle = color
      ctx.fill()
      break
    case "target":
      ctx.arc(x, y, circleRad * 1.5, 0, 2 * Math.PI)
      ctx.stroke()
      ctx.fillStyle = color
      ctx.fill()
      ctx.closePath()
      ctx.beginPath()
      ctx.arc(x, y, circleRad, 0, 2 * Math.PI)
      ctx.strokeStyle = canvasBackgroundColor
      break
    default:
      ctx.arc(x, y, circleRad, 0, 2 * Math.PI)
  }
  ctx.stroke()
  ctx.closePath()
}
const drawPointerAt = (ctx, x, y, color) => {
  ctx.beginPath()
  ctx.fillStyle = color
  ctx.moveTo(x + pointerXInter, y - pointerYInter)
  ctx.lineTo(x, y)
  ctx.lineTo(x - pointerXInter, y - pointerYInter)
  ctx.arc(x, y - pointerYCenter, pointerRad, Math.PI + pointerAng, -pointerAng)
  ctx.fill()
  ctx.lineWidth = circleStrokeWidth
  ctx.strokeStyle = color
  ctx.stroke()
  ctx.closePath()
  ctx.beginPath()
  ctx.strokeStyle = canvasBackgroundColor
  ctx.arc(x, y - pointerYCenter, circleRad, 0, 2 * Math.PI)
  ctx.stroke()
  ctx.closePath()
}
const drawFlagAt = (ctx, x, y, color) => {
  ctx.beginPath()
  ctx.moveTo(x, y)
  ctx.lineTo(x, y - flagY3)
  ctx.lineTo(x + flagX2, y - flagY3)
  ctx.lineTo(x + flagX1, y - flagY2)
  ctx.lineTo(x + flagX2, y - flagY1)
  ctx.lineTo(x, y - flagY1)
  ctx.fillStyle = color
  ctx.fill()
  ctx.lineWidth = circleStrokeWidth
  ctx.strokeStyle = color
  ctx.stroke()
  ctx.closePath()
  ctx.beginPath()
  ctx.strokeStyle = canvasBackgroundColor
  ctx.moveTo(x + flagXCircle - flagSq, y - flagY2)
  ctx.lineTo(x + flagXCircle + flagSq, y - flagY2)
  ctx.moveTo(x + flagXCircle, y - flagY2 - flagSq)
  ctx.lineTo(x + flagXCircle, y - flagY2 + flagSq)
  ctx.stroke()
  ctx.closePath()
}

// Actual Component
function MapCanvas({
                     route,
                     waysAndCells, requestWaysAndCells,
                     nearestNode, requestNearestNode,
                     srcNode, destNode
                   }) {

  // States: Important
  const cRef = useRef(null)
  const [panInfo, setPanInfo] = useState(null)
  const [cInfo, setCInfo] = useState({
    topLeftLat: 41.829267,
    topLeftLon: -71.410482,
    botRightLat: 41.812701,
    botRightLon: -71.387777,
  })
  const ways = waysAndCells.ways
  const cells = waysAndCells.cells
  // States: Throttling, etc
  const [isLoadingWAC, setIsLoadingWAC] = useState(false)
  const [isAllowingPanRender, setIsAllowingPanRender] = useState(true)
  const [isAllowingZoomRender, setIsAllowingZoomRender] = useState(true)

  // Small Helper Functions For Conversions
  const latScale = useCallback(() => fakeDim / (cInfo.topLeftLat - cInfo.botRightLat), [cInfo])
  const lonScale = useCallback(() => fakeDim / (cInfo.botRightLon - cInfo.topLeftLon), [cInfo])
  const routeWidth = useCallback(() => Math.max(routeScale * latScale(), routeMinPx), [latScale])
  const roadWidth = useCallback(() => roadScale * latScale(), [latScale])
  const nonRoadWidth = useCallback(() => nonRoadScale * latScale(), [latScale])
  const latToY = useCallback((lat) => (-lat + cInfo.topLeftLat) * latScale(), [cInfo, latScale])
  const lonToX = useCallback((lon) => (lon - cInfo.topLeftLon) * lonScale(), [cInfo, lonScale])
  const yToLat = useCallback((y) => cInfo.topLeftLat - y / latScale(), [cInfo, latScale])
  const xToLon = useCallback((x) => cInfo.topLeftLon + x / lonScale(), [cInfo, lonScale])

  // Medium Helper Functions For Drawing
  const drawWay = useCallback((ctx, wayId) => {
    const way = ways[wayId]
    if (typeof way === "undefined") {
      return
    }
    const startX = lonToX(way["startLon"])
    const startY = latToY(way["startLat"])
    const endX = lonToX(way["endLon"])
    const endY = latToY(way["endLat"])
    ctx.moveTo(startX, startY)
    ctx.lineTo(endX, endY)
  }, [ways, latToY, lonToX])
  const drawContinuousWay = useCallback((ctx, wayId) => {
    const endX = lonToX(ways[wayId].endLon)
    const endY = latToY(ways[wayId].endLat)
    ctx.lineTo(endX, endY)
  }, [ways, latToY, lonToX])

  // Main Drawing Functions
  const drawRoute = useCallback((ctx, route) => {
    if (route === null || route.length === 0) {
      return
    }
    const firstWay = ways[route[0]]
    const lastWay = ways[route[route.length - 1]]
    ctx.beginPath()
    ctx.setLineDash([routeDash1Scale * latScale(), routeDash2Scale * latScale()]);
    ctx.lineWidth = routeWidth()
    ctx.lineCap = "butt"
    ctx.strokeStyle = routeColor
    ctx.moveTo(lonToX(firstWay.startLon), latToY(firstWay.startLat))
    for (let i = 0; i < route.length; i++) {
      const wayId = route[i]
      drawContinuousWay(ctx, wayId)
    }
    ctx.stroke()
    ctx.setLineDash([]);
    ctx.lineCap = "round"
    ctx.closePath()
    drawCircleAt(ctx, lonToX(firstWay.startLon), latToY(firstWay.startLat), routeColor, "filled")
    drawCircleAt(ctx, lonToX(lastWay.endLon), latToY(lastWay.endLat), routeColor, "target")
  }, [drawContinuousWay, latToY, lonToX, ways, latScale, routeWidth])
  const drawMap = useCallback((ctx, cells) => {
    const topLeftCellLat = roundUpLat(cInfo.topLeftLat)
    const topLeftCellLon = roundDownLon(cInfo.topLeftLon)
    const botRightCellLat = roundUpLat(cInfo.botRightLat)
    const botRightCellLon = roundDownLon(cInfo.botRightLon)
    let missingCells = []
    // Iterate over all cells involved in current screen
    for (let lat = topLeftCellLat; lat >= botRightCellLat; lat = roundCent(lat - step)) {
      if (typeof cells[lat] === "undefined") {
        for (let lon = topLeftCellLon; lon <= botRightCellLon; lon = roundCent(lon + step)) {
          missingCells.push({lat: lat, lon: lon})
        }
        continue
      }
      for (let lon = topLeftCellLon; lon <= botRightCellLon; lon = roundCent(lon + step)) {
        const wayIdsInCell = cells[lat][lon]
        if (typeof wayIdsInCell === "undefined") {
          missingCells.push({lat: lat, lon: lon})
          continue
        }
        // Draw non-roads
        if (latScale() > 60000) {
          ctx.beginPath()
          ctx.lineJoin = "round"
          ctx.lineCap = "round"
          ctx.lineWidth = nonRoadWidth()
          ctx.strokeStyle = nonRoadColor
          for (let i = 0; i < wayIdsInCell.nonRoads.length; i++) {
            drawWay(ctx, wayIdsInCell.nonRoads[i])
          }
          ctx.stroke()
          ctx.closePath()
        }
        // Draw roads
        ctx.beginPath()
        ctx.lineWidth = roadWidth()
        ctx.strokeStyle = roadColor
        for (let i = 0; i < wayIdsInCell.roads.length; i++) {
          drawWay(ctx, wayIdsInCell.roads[i])
        }
        ctx.stroke()
        ctx.closePath()
      }
    }
    // If any cells are missing, tell backend what the cells are
    if (missingCells.length > 0) {
      ctx.fillStyle = notYetLoadedCellColor;
      let lowLat = Number.POSITIVE_INFINITY, lowLon = Number.POSITIVE_INFINITY
      let highLat = Number.NEGATIVE_INFINITY, highLon = Number.NEGATIVE_INFINITY
      for (let i = 0; i < missingCells.length; i++) {
        const lat = missingCells[i].lat
        const lon = missingCells[i].lon
        if (lat < lowLat) {
          lowLat = lat
        }
        if (lat > highLat) {
          highLat = lat
        }
        if (lon < lowLon) {
          lowLon = lon
        }
        if (lon > highLon) {
          highLon = lon
        }
        ctx.fillRect(lonToX(lon), latToY(lat), step * lonScale(), step * latScale())
      }
      if (isLoadingWAC || panInfo !== null
          || lowLat === Number.POSITIVE_INFINITY
          || lowLon === Number.POSITIVE_INFINITY
          || highLat === Number.NEGATIVE_INFINITY
          || highLon === Number.NEGATIVE_INFINITY) {
        return
      }
      console.log("%cLoading " + missingCells.length + " cells.", "color: tomato")
      setIsLoadingWAC(true)
      ctx.filter = "blur(100)" // Does not work with Safari
      requestWaysAndCells(highLat, lowLon, lowLat, highLon)
      return
    }
    setIsLoadingWAC(false)
  }, [
    cInfo, drawWay, isLoadingWAC, requestWaysAndCells,
    latScale, lonScale, latToY, lonToX,
    roadWidth, nonRoadWidth, panInfo
  ])

  // Main useEffect Hook
  useEffect(() => {
    // console.log("%cuseEffect function called.", "color: yellowgreen")
    const ctx = cRef.current.getContext("2d")
    // Redraw the map
    ctx.clearRect(0, 0, fakeDim, fakeDim)
    ctx.lineJoin = "round"
    ctx.lineCap = "round"
    drawMap(ctx, cells)
    // Draw in the various selected points
    drawRoute(ctx, route)
    if (srcNode !== null) {
      drawPointerAt(ctx, lonToX(srcNode.lon), latToY(srcNode.lat), srcColor)
    }
    if (destNode !== null) {
      drawFlagAt(ctx, lonToX(destNode.lon), latToY(destNode.lat), desColor)
    }
    if (nearestNode !== null) {
      drawCircleAt(ctx, lonToX(nearestNode.lon), latToY(nearestNode.lat))
    }
  }, [
    route, cells,
    lonToX, latToY, nearestNode,
    drawRoute, drawMap,
    srcNode, destNode
  ])

  // Listeners
  const onMouseDownFunction = (e) => {
    if (isLoadingWAC) {
      console.log("%cPlease wait until the map is finished loading!", "color: tomato")
      return
    }
    const x = e.pageX - cRef.current.getBoundingClientRect().left
    const y = e.pageY - cRef.current.getBoundingClientRect().top
    const lon = xToLon(x * fakeRealScale)
    const lat = yToLat(y * fakeRealScale)
    // Initialize panInfo
    setPanInfo([lat, lon, x, y, cInfo])
  }
  const onMouseMoveFunction = (e) => {
    if (panInfo === null) {
      return
    }
    if (isLoadingWAC) {
      console.log("%cPlease wait until the map is finished loading!", "color: tomato")
      return
    }
    cRef.current.style.cursor = "grabbing"
    const x = e.pageX - cRef.current.getBoundingClientRect().left
    const y = e.pageY - cRef.current.getBoundingClientRect().top
    // Skip if in click dead zone
    if (Math.abs(x - panInfo[2]) < deadZonePx &&
        Math.abs(y - panInfo[3]) < deadZonePx) {
      return
    }
    // If pan render is allowed, change cInfo
    if (isAllowingPanRender) {
      setIsAllowingPanRender(false)
      const up = (y - panInfo[3]) * 2 / latScale()
      const right = (panInfo[2] - x) * 2 / lonScale()
      setTimeout(() => setIsAllowingPanRender(true), panRenderDelay)
      setCInfo({
        topLeftLat: panInfo[4].topLeftLat + up,
        topLeftLon: panInfo[4].topLeftLon + right,
        botRightLat: panInfo[4].botRightLat + up,
        botRightLon: panInfo[4].botRightLon + right,
      })
    }
  }
  const onMouseUpOrLeaveFunction = (e) => {
    if (panInfo === null) {
      return
    }
    if (isLoadingWAC) {
      console.log("%cPlease wait until the map is finished loading!", "color: tomato")
      return
    }
    cRef.current.style.cursor = ""
    const x = e.pageX - cRef.current.getBoundingClientRect().left
    const y = e.pageY - cRef.current.getBoundingClientRect().top
    // Request nearest node if in click dead zone
    if (Math.abs(x - panInfo[2]) < deadZonePx &&
        Math.abs(y - panInfo[3]) < deadZonePx) {
      requestNearestNode(panInfo[0], panInfo[1])
    }
    // Reset panInfo
    setPanInfo(null)
  }
  const onScrollFunction = (e) => {
    if (isLoadingWAC) {
      console.log("%cPlease wait until the map is finished loading!", "color: tomato")
      return
    }
    if (e.deltaY === 0) {
      return
    }
    if (isAllowingZoomRender) {
      setIsAllowingZoomRender(false)
      setTimeout(() => setIsAllowingZoomRender(true), zoomRenderDelay)
      const zoomRate = e.deltaY > 0 ? zoomInRatio : zoomOutRatio
      setCInfo((prev) => {
        return {
          topLeftLat: zoomRate * prev.topLeftLat + (1 - zoomRate) * prev.botRightLat,
          topLeftLon: zoomRate * prev.topLeftLon + (1 - zoomRate) * prev.botRightLon,
          botRightLat: zoomRate * prev.botRightLat + (1 - zoomRate) * prev.topLeftLat,
          botRightLon: zoomRate * prev.botRightLon + (1 - zoomRate) * prev.topLeftLon,
        }
      })
    }
  }
  const onKeyDownFunction = (e) => {
    keyboardPan(e)
    keyboardZoom(e)
  }
  const keyboardPan = (e) => {
    if (isLoadingWAC) {
      console.log("%cPlease wait until the map is finished loading!", "color: tomato")
      return
    }
    let up = 0, right = 0
    if (e.key === "ArrowRight") {
      right = 0.25 * fakeDim / lonScale()
    } else if (e.key === "ArrowLeft") {
      right = -0.25 * fakeDim / lonScale()
    } else if (e.key === "ArrowUp") {
      up = 0.25 * fakeDim / latScale()
    } else if (e.key === "ArrowDown") {
      up = -0.25 * fakeDim / latScale()
    }
    setCInfo((prev) => {
      return {
        topLeftLat: prev.topLeftLat + up,
        topLeftLon: prev.topLeftLon + right,
        botRightLat: prev.botRightLat + up,
        botRightLon: prev.botRightLon + right,
      }
    })
  }
  const keyboardZoom = (e) => {
    if (isLoadingWAC) {
      console.log("%cPlease wait until the map is finished loading!", "color: tomato")
      return
    }
    if (e.key === "[") {
      setCInfo((prev) => {
        return {
          topLeftLat: 1.1 * prev.topLeftLat - 0.1 * prev.botRightLat,
          topLeftLon: 1.1 * prev.topLeftLon - 0.1 * prev.botRightLon,
          botRightLat: 1.1 * prev.botRightLat - 0.1 * prev.topLeftLat,
          botRightLon: 1.1 * prev.botRightLon - 0.1 * prev.topLeftLon,
        }
      })
    } else if (e.key === "]") {
      setCInfo((prev) => {
        return {
          topLeftLat: 0.9 * prev.topLeftLat + 0.1 * prev.botRightLat,
          topLeftLon: 0.9 * prev.topLeftLon + 0.1 * prev.botRightLon,
          botRightLat: 0.9 * prev.botRightLat + 0.1 * prev.topLeftLat,
          botRightLon: 0.9 * prev.botRightLon + 0.1 * prev.topLeftLon,
        }
      })
    }
  }

  return (
      <div id="MapCanvasWrapper">
        <canvas id="MapCanvas" ref={cRef} tabIndex={0}
                height={fakeDim + "px"} width={fakeDim + "px"}
                onMouseDown={onMouseDownFunction}
                onMouseMove={onMouseMoveFunction}
                onMouseUp={onMouseUpOrLeaveFunction}
                onMouseLeave={onMouseUpOrLeaveFunction}
                onWheel={onScrollFunction}
                onKeyDown={onKeyDownFunction}/>
        {isLoadingWAC && <div id="MapCanvasLoading"/>}
      </div>
  )
}

export default MapCanvas;
