<#assign content>
  <div class="centralize">
    <div id="queryWindow" class="shadow">
      <div id="tabsWrapper">
        <a id="neighborsTab" class="tab lightgreen" href="/neighbors">neighbors.cs32</a>
        <a id="radiusTab" class="tab lightgreen active" href="/radius">radius.cs32</a>
      </div>
      <div id="queryWindowInnerWrapper">
        <div id="queryWindowLeft">
          <p>1<br/>2<br/>3<br/>4<br/>5<br/>6<br/>7<br/>8<br/>9<br/>10<br/>11</p>
        </div>
        <div id="queryWindowRight">
          <div id="content">
            <h1 class="green">
              /**
              <br/>&nbsp;* Stars 3: use this to search for all stars within
              <br/>&nbsp;*&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
              &nbsp;&nbsp;some radius of a given point or star.
              <br/>&nbsp;*/
            </h1>
            <br/>

            <p>
              <span class="orange">function</span>
              <span class="yellow">find_stars_in_radius</span>() {
            </p>
            <form method="GET" action="/radius">
              <label for="radius"> Radius </label>
              <input id="radius" class="textInput shift10ch" type="number"
                     name="radius" required min="0" step="any"
                     autocomplete="off">
              <br/>

              <fieldset>
                <span>Coordinates</span>
                <label id="forX" for="x">x</label>&nbsp;
                <input id="x" class="textInput" type="number"
                       name="x" required step="any" autocomplete="off">
                <label for="y">y</label>
                <input id="y" class="textInput" type="number"
                       name="y" required step="any" autocomplete="off">
                <label for="z">z</label>
                <input id="z" class="textInput" type="number"
                       name="z" required step="any" autocomplete="off">
              </fieldset>

              <label>
                <span>Star</span>
                <input id="star" class="textInput wide shift5ch" type="text"
                       name="starName" required autocomplete="off">
              </label>
              <br/>

              <span>&nbsp&nbsp</span><input class="buttonInput" type="submit" tabindex="0">
            </form>
            <p>}</p>

            <div class="output ${outputColor}">${output}</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</#assign>
<#include "main.ftl">