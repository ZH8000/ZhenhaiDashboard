/**
 *  Create a function that colud draw a bar chart.
 *
 *  This function will return another function that could draw a pie chart
 *  with siganature of function (selection, dataSet, onClickCallback), where
 *  selection, dataSet is required and onClickCallback is optional.
 *
 *  onClickCallback should have the signature of function(data), where data
 *  will be the data from clicked bar.
 *
 *  The bar chart will bind to the node that selected by selection parameter,
 *  which should be a valid d3 / jquery selector.
 *
 *  The options parameter will control the basic attribute of this chart, and
 *  must have the following attribute:
 *
 *   - options.totalHeight
 *   - options.barWidth
 *   - options.barPadding
 *   - options.bottomMargin
 *   - options.topMargin
 *   - options.extractValue
 *   - options.extractName
 *   - options.levelA
 *   - options.levelB
 *   - options.levelC
 *
 */
function barChart(options) {

  function assertInt(variable, message) {
    if (typeof variable !== "number") {
      var errorMessage = message || "Assertion failed";
      if (typeof Error !== "undefined") {
        throw new Error(errorMessage);
      } else {
        throw message;
      }
    }
  }        

  assertInt(options.totalHeight, "No totalHeight attribute");
  assertInt(options.barWidth, "No barWidth attribute");
  assertInt(options.barPadding, "No barPadding attribute");
  assertInt(options.bottomMargin, "No bottomMargin attribute");
  assertInt(options.topMargin, "No topMargin attribute");

  var draw = function (selection, dataSet) {

    function getXOffset(index) {
      return index * options.barWidth + index * options.barPadding;
    }

    function getDataValue(data) { 
      return options.extractValue(data); 
    }

    function calculateLineYPosition(data) {
      return options.totalHeight - scalar(data) - options.bottomMargin-10;
    }

    function calculateRectYPosition(data) {
      return options.totalHeight - scalar(getDataValue(data)) - options.bottomMargin-10;
    }

    function calculateTopLabelYPosition(data) {
      return options.totalHeight - scalar(getDataValue(data)) - 25;
    }

    var totalWidth = getXOffset(dataSet.length);
    var minimalY = options.bottomMargin;
    var maximalY = options.totalHeight - 
  		 (options.bottomMargin + options.topMargin + 10);

    var domainMax = d3.max(dataSet, options.extractValue)

    if (options.levelA && (parseInt(options.levelA) / 12) > domainMax) {
      domainMax = parseInt(options.levelA) / 12
    }

    var scalar = d3.scale.linear().
                    domain([0, domainMax]).
                    range([minimalY, maximalY - 80]);

    var chart = d3.select(selection).
                   attr("width", totalWidth + 20).
                   attr("height", options.totalHeight + 50)

    // Remove all existing content, so we could draw different
    // bar chart on same HTML element.
    chart.selectAll("g").remove();

    var bar = chart.selectAll("g").data(dataSet).enter().
                    append("g")

    // Bar rectangular
    bar.append("rect").
        attr("width", options.barWidth - 5).
        attr("height", function(d) { return scalar(getDataValue(d)) }).
        attr("x", function(d, i) { return getXOffset(i) + 10 }).
        attr("y", calculateRectYPosition).
        attr("onclick", function(d) { 
          if (d.link) {
            return "window.location.href='" + d.link + "'" 
          } else {
            return "void(0);"
          }
        })

    // The top label, which is the actually data value
    bar.append("text").
        attr("x", function(d, i) { return getXOffset(i) }).
        attr("y", calculateTopLabelYPosition).
        attr("dy", "-15px").
        attr("dx", "30px").
        text(options.extractValue)

    // The bottom label, the name of current data
    bar.append("text").
        attr("x", function(d, i) { return getXOffset(i) }).
        attr("y", options.totalHeight-10).
        attr("dx", "35px").
        text(options.extractName)

    if (options.levelA && options.levelA > 0) {
      var lineY = calculateLineYPosition(parseInt(options.levelA) / 12);
      chart.append("rect").attr("x", 0).attr("y", lineY).attr("width", totalWidth).attr("height", 1).attr("style", "fill:rgb(255,0,255);stroke:rgb(255,0,255)")
    }

    if (options.levelB && options.levelB > 0) {
      var lineY = calculateLineYPosition(parseInt(options.levelB) / 12);
      chart.append("rect").attr("x", 0).attr("y", lineY).attr("width", totalWidth).attr("height", 1).attr("style", "fill:rgb(255,0,0);stroke:rgb(255,0,0)")
    }

    if (options.levelC && options.levelC > 0) {
      var lineY = calculateLineYPosition(parseInt(options.levelC) / 12);
      chart.append("rect").attr("x", 0).attr("y", lineY).attr("width", totalWidth).attr("height", 1).attr("style", "fill:rgb(0,255,0);stroke:rgb(0,255,0)")
    }
  }

  return draw;
}


function pieChart(options) {

  function assertInt(variable, message) {
    if (typeof variable !== "number") {
      var errorMessage = message || "Assertion failed";
      if (typeof Error !== "undefined") {
        throw new Error(errorMessage);
      } else {
        throw message;
      }
    }
  }        

  assertInt(options.width, "No width attribute");
  assertInt(options.height, "No height attribute");
  assertInt(options.radius, "No radius");
  var threshold = options.threshold >= 0 ? options.threshold : 0.5;

  var draw = function (selection, data) {

    function calculateArcCenter(data) {
      data.innerRadius = 0;
      data.outerRadius = options.radius;
      return "translate(" + arc.centroid(data) + ")";
    }

    var total = 0;

    for (var i = 0; i < data.length; i++) {
      total += data[i].value;
    }

    function getPercentage(sliceValue) {
      return ((sliceValue / total) * 100).toFixed(2) + "%";
    }

    var color = d3.scale.category20c();

    var pieData = d3.layout.pie().value(options.extractValue)(data);


    var pieChart = d3.select(selection)
        .attr("width", options.width)
        .attr("height", options.height)
        .append("g")
        .attr("transform", "translate(" + options.radius + "," + options.radius + ")")

    var arc = d3.svg.arc().outerRadius(options.radius);

    var arcs = pieChart.selectAll("g.slice")
        .data(pieData)
        .enter()
        .append("g")
        .attr("class", "slice");

    arcs.append("path")
      .attr("fill", function(d, i) { return color(i); } )
      .attr("d", arc)
      .attr("class", "popup")
      .attr("id", function(d, i) { return "pieBlock" + i; } )
      .attr("title", function(d, i) { 
        var percentage = getPercentage(d.value)
        var title = options.extractName(d.data);
        return "[" + title + "] " + percentage; 
      })
      .attr("onclick", function(d) { 
        if (d.data.link) {
          return "window.location.href='" + d.data.link + "'" 
        } else {
          return "void(0);"
        }
      })

    arcs.append("text")
        .attr("transform", calculateArcCenter)
        .attr("text-anchor", "middle")
        .attr("id", function(d, i) { return "pieText" + i; })
        .text(function(d) { 
          if (d.endAngle - d.startAngle >= threshold) {
            return options.extractName(d.data); 
          } {
            return "";
          }
        });

  }

  return draw;
}

function formatDate(date) {

  function padding(orig) {
    switch (orig.toString().length) {
      case 0: return "00";
      case 1: return "0" + orig;
      default: return orig;
    }
  }

  return date.getFullYear() + "-" + 
         padding(date.getMonth()) + "-" + 
         padding(date.getDate()) + " " +
         padding(date.getHours()) + ":" + 
         padding(date.getMinutes()) + ":" + 
         padding(date.getSeconds());
}

