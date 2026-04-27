/* global React */
window.registerExtension("sonar-pdf-report/report_page", function (options) {
  var projectKey = options.component.key;
  var base = window.location.origin;

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

  return React.createElement(
    "div",
    { style: { padding: "24px", fontFamily: "sans-serif" } },
    React.createElement(
      "h2",
      { style: { marginBottom: "8px" } },
      "Analysis Reports"
    ),
    React.createElement(
      "p",
      { style: { color: "#666", marginBottom: "20px" } },
      "Reports are generated during the SonarQube analysis and reflect the latest run."
    ),
    React.createElement(
      "div",
      null,
      React.createElement(
        "a",
        { href: downloadUrl("pdf"), target: "_blank", style: btnStyle },
        "Download PDF Report"
      ),
      React.createElement(
        "a",
        { href: downloadUrl("html"), target: "_blank", style: btnStyle },
        "Download HTML Report"
      )
    )
  );
});
