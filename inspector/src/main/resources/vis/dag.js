// Create a new directed graph
var g = new dagreD3.graphlib.Graph().setGraph({});

// States and transitions from RFC 793
var states = {
  CLOSED: {
    description: "represents no connection state at all.",
    style: "fill: #f77"
  },

  LISTEN: {
    description: "represents waiting for a connection request from any " +
                 "remote TCP and port."
  },

  "SYN SENT": {
    description: "represents waiting for a matching connection " +
                 "request after having sent a connection request."
  },

  "SYN RCVD": {
    description: "represents waiting for a confirming connection " +
                 "request acknowledgment after having both received and sent a " +
                 "connection request."
  },


  ESTAB: {
    description: "represents an open connection, data received " +
                 "can be delivered to the user.  The normal state for the data " +
                 "transfer phase of the connection.",
    style: "fill: #7f7"
  },

  "FINWAIT-1": {
    description: "represents waiting for a connection termination " +
                 "request from the remote TCP, or an acknowledgment of the " +
                 "connection termination request previously sent."

  },

  "FINWAIT-2": {
    description: "represents waiting for a connection termination " +
                 "request from the remote TCP."
  },


  "CLOSE WAIT": {
    description: "represents waiting for a connection termination " +
                 "request from the local user."
  },

  CLOSING: {
    description: "represents waiting for a connection termination " +
                 "request acknowledgment from the remote TCP."
  },

  "LAST-ACK": {
    description: "represents waiting for an acknowledgment of the " +
                 "connection termination request previously sent to the remote " +
                 "TCP (which includes an acknowledgment of its connection " +
                 "termination request)."
  },

  "TIME WAIT": {
    description: "represents waiting for enough time to pass to be " +
                 "sure the remote TCP received the acknowledgment of its " +
                 "connection termination request."
  }
};

// Add states to the graph, set labels, and style
Object.keys(states).forEach(function(state) {
  var value = states[state];
  value.label = state;
  value.rx = value.ry = 5;
  g.setNode(state, value);
});
$.getScript("data.js", function(){

});


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

// Center the graph
var initialScale = 0.75;
zoom
  .translate([(svg.attr("width") - g.graph().width * initialScale) / 2, 20])
  .scale(initialScale)
  .event(svg);
svg.attr('height', g.graph().height * initialScale + 40);