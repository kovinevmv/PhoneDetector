/**
 * AnyChart is lightweight robust charting library with great API and Docs, that works with your stack and has tons of chart types and features.
 *
 * Theme: darkGlamour
 * Version: 2.0.0 (2019-04-26)
 * License: https://www.anychart.com/buy/
 * Contact: sales@anychart.com
 * Copyright: AnyChart.com 2019. All rights reserved.
 */
(function() {
  "use strict";

  function a() {
    return window.anychart.color.setOpacity(this.sourceColor, 0.6, !0);
  }
  function b() {
    return window.anychart.color.darken(this.sourceColor);
  }
  function c() {
    return window.anychart.color.lighten(this.sourceColor);
  }
  var e = {
    palette: {
      type: "distinct",
      items: "#D81B60 #ce93d8 #ab47bc #d81b60 #880e4f #ffd600 #ff6e40 #03a9f4 #5e35b1 #1976d2".split(
        " "
      )
    },
    defaultOrdinalColorScale: {
      autoColors: function(d) {
        return window.anychart.color.blendedHueProgression(
          "#D81B60",
          "#d81b60",
          d
        );
      }
    },
    defaultLinearColorScale: { colors: ["#D81B60", "#d81b60"] },
    defaultFontSettings: {
      fontFamily: '"Source Sans Pro", sans-serif',
      fontSize: 13,
      fontColor: "#d7cacc"
    },
    defaultBackground: {
      fill: "#303030",
      stroke: "#192125",
      cornerType: "round",
      corners: 0
    },
    defaultAxis: {
      stroke: "#655B66",
      title: { fontSize: 15 },
      ticks: { stroke: "#655B66" },
      minorTicks: { stroke: "#46474F" }
    },
    defaultGridSettings: { stroke: "#655B66" },
    defaultMinorGridSettings: { stroke: "#46474F" },
    defaultSeparator: { fill: "#84707C" },
    defaultTooltip: {
      background: { fill: "#303030 0.9", stroke: "2 #192125", corners: 3 },
      fontSize: 13,
      title: { align: "center", fontSize: 15 },
      padding: { top: 10, right: 15, bottom: 10, left: 15 },
      separator: { margin: { top: 10, right: 10, bottom: 10, left: 10 } }
    },
    defaultColorRange: {
      stroke: "#455a64",
      ticks: { stroke: "#455a64", position: "outside", length: 7, enabled: !0 },
      minorTicks: {
        stroke: "#455a64",
        position: "outside",
        length: 5,
        enabled: !0
      },
      marker: {
        padding: { top: 3, right: 3, bottom: 3, left: 3 },
        fill: "#d7cacc"
      }
    },
    defaultScroller: {
      fill: "#5d5d5d",
      selectedFill: "#455a64",
      thumbs: {
        fill: "#546e7a",
        stroke: "#5d5d5d",
        hovered: { fill: "#78909c", stroke: "#455a64" }
      }
    },
    defaultLegend: { fontSize: 13 },
    chart: {
      defaultSeriesSettings: {
        base: {
          selected: {
            stroke: "1.5 #fafafa",
            markers: { stroke: "1.5 #fafafa" }
          }
        },
        lineLike: { selected: { stroke: "3 #fafafa" } },
        areaLike: { selected: { stroke: "3 #fafafa" } },
        marker: { selected: { stroke: "1.5 #fafafa" } },
        candlestick: {
          normal: {
            risingFill: "#D81B60",
            risingStroke: "#D81B60",
            fallingFill: "#d81b60",
            fallingStroke: "#d81b60"
          },
          hovered: {
            risingFill: c,
            risingStroke: b,
            fallingFill: c,
            fallingStroke: b
          },
          selected: {
            risingStroke: "3 #D81B60",
            fallingStroke: "3 #d81b60",
            risingFill: "#333333 0.85",
            fallingFill: "#333333 0.85"
          }
        },
        ohlc: {
          normal: { risingStroke: "#D81B60", fallingStroke: "#d81b60" },
          hovered: { risingStroke: b, fallingStroke: b },
          selected: { risingStroke: "3 #D81B60", fallingStroke: "3 #d81b60" }
        }
      },
      title: { fontSize: 17 },
      padding: { top: 20, right: 25, bottom: 15, left: 15 }
    },
    cartesianBase: {
      defaultSeriesSettings: {
        box: {
          selected: {
            medianStroke: "#fafafa",
            stemStroke: "#fafafa",
            whiskerStroke: "#fafafa",
            outlierMarkers: {
              enabled: null,
              size: 4,
              fill: "#fafafa",
              stroke: "#fafafa"
            }
          }
        }
      }
    },
    pieFunnelPyramidBase: {
      normal: { labels: { fontColor: null } },
      selected: { stroke: "1.5 #fafafa" },
      connectorStroke: "#84707C",
      outsideLabels: { autoColor: "#d7cacc" },
      insideLabels: { autoColor: "#5d5d5d" }
    },
    map: {
      unboundRegions: { enabled: !0, fill: "#5d5d5d", stroke: "#455a64" },
      defaultSeriesSettings: {
        base: {
          normal: { stroke: c, labels: { fontColor: "#212121" } },
          hovered: { fill: "#bdbdbd" },
          selected: { fill: "3 #fafafa" }
        },
        connector: {
          normal: { markers: { stroke: "1.5 #5d5d5d" } },
          hovered: { markers: { stroke: "1.5 #5d5d5d" } },
          selected: {
            stroke: "1.5 #fafafa",
            markers: { fill: "#fafafa", stroke: "1.5 #5d5d5d" }
          }
        },
        marker: { normal: { labels: { fontColor: "#d7cacc" } } }
      }
    },
    sparkline: {
      padding: 0,
      background: { stroke: "#303030" },
      defaultSeriesSettings: {
        area: { stroke: "1.5 #D81B60", fill: "#D81B60 0.5" },
        column: { fill: "#D81B60", negativeFill: "#d81b60" },
        line: { stroke: "1.5 #D81B60" },
        winLoss: { fill: "#D81B60", negativeFill: "#d81b60" }
      }
    },
    bullet: {
      background: { stroke: "#303030" },
      defaultMarkerSettings: { fill: "#D81B60", stroke: "2 #D81B60" },
      padding: { top: 5, right: 10, bottom: 5, left: 10 },
      margin: { top: 0, right: 0, bottom: 0, left: 0 },
      rangePalette: {
        items: ["#4D6570", "#445963", "#3B4D56", "#34444C", "#2D3B42"]
      }
    },
    heatMap: {
      normal: { stroke: "1 #303030", labels: { fontColor: "#212121" } },
      hovered: { stroke: "1.5 #303030" },
      selected: { stroke: "2 #fafafa", labels: { fontColor: "#fafafa" } }
    },
    treeMap: {
      normal: {
        headers: {
          background: { enabled: !0, fill: "#5d5d5d", stroke: "#455a64" }
        },
        labels: { fontColor: "#212121" },
        stroke: "#455a64"
      },
      hovered: {
        headers: {
          fontColor: "#d7cacc",
          background: { fill: "#455a64", stroke: "#455a64" }
        }
      },
      selected: { labels: { fontColor: "#fafafa" }, stroke: "2 #eceff1" }
    },
    stock: {
      padding: [20, 30, 20, 60],
      defaultPlotSettings: {
        xAxis: { background: { fill: "#655B66 0.3", stroke: "#655B66" } }
      },
      scroller: {
        fill: "none",
        selectedFill: "#655B66 0.3",
        outlineStroke: "#655B66",
        defaultSeriesSettings: {
          base: { selected: { stroke: a, fill: a } },
          lineLike: { selected: { stroke: a } },
          areaLike: { selected: { stroke: a, fill: a } },
          marker: { selected: { stroke: a } },
          candlestick: {
            normal: {
              risingFill: "#999 0.6",
              risingStroke: "#999 0.6",
              fallingFill: "#999 0.6",
              fallingStroke: "#999 0.6"
            },
            selected: {
              risingStroke: a,
              fallingStroke: a,
              risingFill: a,
              fallingFill: a
            }
          },
          ohlc: {
            normal: { risingStroke: "#999 0.6", fallingStroke: "#999 0.6" },
            selected: { risingStroke: a, fallingStroke: a }
          }
        }
      }
    }
  };
  window.anychart = window.anychart || {};
  window.anychart.themes = window.anychart.themes || {};
  window.anychart.themes.darkGlamour = e;
})();