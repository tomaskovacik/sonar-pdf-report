/* global React */
window.registerExtension("sonar-pdf-report/report_page", function (options) {
  var projectKey = options.component.key;
  var base = window.location.origin;

  var containerEl = document.createElement("div");

  function downloadUrl(type) {
    return base + "/api/pdfreport/get?project=" + encodeURIComponent(projectKey) + "&content_type=" + type;
  }

  var btnStyle = {
    display: "inline-block",
    padding: "8px 20px",
    background: "#236a97",
    color: "#fff",
    borderRadius: "4px",
    textDecoration: "none",
    marginRight: "12px",
    fontSize: "14px",
  };

  function render(info) {
    var buttons = [];

    if (info.pdf) {
      buttons.push(
        React.createElement("a", { key: "pdf", href: downloadUrl("pdf"), target: "_blank", style: btnStyle }, "Download PDF Report")
      );
    }
    if (info.html) {
      buttons.push(
        React.createElement("a", { key: "html", href: downloadUrl("html"), target: "_blank", style: btnStyle }, "Download HTML Report")
      );
    }

    var content;
    if (buttons.length === 0) {
      content = React.createElement(
        "p",
        { style: { color: "#666" } },
        "No reports available yet. Run an analysis to generate a report."
      );
    } else {
      content = React.createElement("div", null, buttons);
    }

    return React.createElement(
      "div",
      { style: { padding: "24px", fontFamily: "sans-serif" } },
      React.createElement("h2", { style: { marginBottom: "8px" } }, "Analysis Reports"),
      React.createElement(
        "p",
        { style: { color: "#666", marginBottom: "20px" } },
        "Reports are generated during the SonarQube analysis and reflect the latest run."
      ),
      content
    );
  }

  // Fetch available report types then render
  fetch(base + "/api/pdfreport/info?project=" + encodeURIComponent(projectKey))
    .then(function (r) { return r.json(); })
    .then(function (info) {
      ReactDOM.render(render(info), containerEl);
    })
    .catch(function () {
      ReactDOM.render(render({ pdf: false, html: false }), containerEl);
    });

  return containerEl;
});
