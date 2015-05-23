// Create a new directed graph
var g = new dagreD3.graphlib.Graph().setGraph({});

// States and transitions from RFC 793
var states = {
// !-- REPLACE WITH NODES --!

};

// Add states to the graph, set labels, and style
Object.keys(states).forEach(function(state) {
  var value = states[state];
  value.label = state;
  value.rx = value.ry = 5;
  g.setNode(state, value);
});

// Set up the edges
// !-- REPLACE WITH EDGES --!


// Create the renderer
var render = new dagreD3.render();

// Set up an SVG group so that we can translate the final graph.
var svg = d3.select("svg"),
    inner = svg.append("g");

// Set up zoom support
var zoom = d3.behavior.zoom().on("zoom", function() {
    inner.attr("transform", "translate(" + d3.event.translate + ")" +
                                "scale(" + d3.event.scale + ")");
  });
svg.call(zoom);

// Simple function to style the tooltip for the given node.
var styleTooltip = function(name, description) {
  return "<p class='name'>" + name + "</p><p class='description'>" + description + "</p>";
};

// Run the renderer. This is what draws the final graph.
render(inner, g);

inner.selectAll("g.node")
  .attr("title", function(v) { return styleTooltip(v, g.node(v).description) })
  .each(function(v) { $(this).tipsy({ gravity: "w", opacity: 1, html: true }); });

inner.selectAll("g.node")
  .on("click", function(n){
        var url = n + "-report.html";
        $(location).attr('href', url);
        window.location = url;
    });

inner.selectAll("g.node")
    .on('mouseover', function(d){
            d3.select(this).select('rect').style("fill", "white");
                    })
        .on('mouseout', function(d){
          d3.select(this).select('rect').style("fill", g.node(d).basefill);
        });

// Center the graph
var initialScale = 0.75;
zoom
  .translate([(svg.attr("width") - g.graph().width * initialScale) / 2, 20])
  .scale(initialScale)
  .event(svg);
svg.attr('height', g.graph().height * initialScale + 40);