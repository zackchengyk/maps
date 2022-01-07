<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="utf-8">
  <meta name="Description" content="CS32 Spring 2021 Stars 3 Submission.">
  <title>${title}</title>
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <!-- In real-world webapps, css is usually minified and
       concatenated. Here, separate normalize from our code, and
       avoid minification for clarity. -->
  <link rel="stylesheet" href="main.css">
</head>
<body>
${content}
<!-- Again, we're serving up the unminified source for clarity. -->
</body>
<!-- See http://html5boilerplate.com/ for a good place to start
     dealing with real world issues like old browsers.  -->
<script>
    const x = document.getElementById("x")
    const y = document.getElementById("y")
    const z = document.getElementById("z")
    const xyzParent = x.parentElement
    const star = document.getElementById("star")
    const starParent = star.parentElement

    x.addEventListener("input", disableStarInput)
    y.addEventListener("input", disableStarInput)
    z.addEventListener("input", disableStarInput)
    star.addEventListener("input", disableXYZInput)

    function disableXYZInput() {
        x.disabled = true
        y.disabled = true
        z.disabled = true
        xyzParent.firstElementChild.innerHTML =
            "// Click here to search by coordinates instead"
        xyzParent.firstElementChild.tabIndex = 0
        xyzParent.classList.add("commentedOut")
        xyzParent.addEventListener("click", swapToXYZInput)
        xyzParent.firstElementChild.addEventListener("keydown",
            swapToXYZInputOnKeyDown)
    }

    function swapToXYZInput() {
        console.log("Hello")
        xyzParent.removeEventListener("click", swapToXYZInput)
        xyzParent.firstElementChild.removeEventListener("keydown",
            swapToXYZInputOnKeyDown)
        x.disabled = false
        y.disabled = false
        z.disabled = false
        xyzParent.firstElementChild.innerHTML = "Coordinates "
        xyzParent.firstElementChild.removeAttribute("tabIndex")
        xyzParent.classList.remove("commentedOut")
        disableStarInput()
    }

    function swapToXYZInputOnKeyDown(e) {
        if (e.key === "Enter" || e.key === " ") {
            e.stopPropagation();
            e.preventDefault();
            swapToXYZInput();
            setTimeout(() => x.focus(), 0)
        }
    }

    function disableStarInput() {
        star.disabled = true
        starParent.firstElementChild.innerHTML =
            "// Click here to search by star instead"
        starParent.firstElementChild.tabIndex = 0
        starParent.classList.add("commentedOut")
        starParent.addEventListener("click", swapToStarInput)
        starParent.firstElementChild.addEventListener("keydown",
            swapToStarInputOnKeyDown)
    }

    function swapToStarInput() {
        starParent.removeEventListener("click", swapToStarInput)
        starParent.firstElementChild.removeEventListener("keydown",
            swapToStarInputOnKeyDown)
        star.disabled = false
        starParent.firstElementChild.innerHTML = "Star "
        starParent.firstElementChild.removeAttribute("tabIndex")
        starParent.classList.remove("commentedOut")
        disableXYZInput()
    }

    function swapToStarInputOnKeyDown(e) {
        if (e.key === "Enter" || e.key === " ") {
            e.stopPropagation();
            e.preventDefault();
            swapToStarInput();
            setTimeout(() => star.focus(), 0)
        }
    }
</script>
</html>