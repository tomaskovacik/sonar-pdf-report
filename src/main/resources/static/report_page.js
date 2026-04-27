window.registerExtension("sonarpdfreport/report_page", function (options) {
  var projectKey = options.component.key;
  var base = window.location.origin;

  var containerEl = document.createElement("div");
  containerEl.style.padding = "24px";
  containerEl.style.fontFamily = "sans-serif";

  var linkStyle = [
    "display:inline-block",
    "padding:8px 20px",
    "background:#236a97",
    "color:#fff",
    "border-radius:4px",
    "text-decoration:none",
    "margin-right:12px",
    "font-size:14px",
  ].join(";");

  function render(info) {
    containerEl.innerHTML = "";

    var h2 = document.createElement("h2");
    h2.style.marginBottom = "8px";
    h2.textContent = "Analysis Reports";
    containerEl.appendChild(h2);

    var desc = document.createElement("p");
    desc.style.color = "#666";
    desc.style.marginBottom = "20px";
    desc.textContent = "Reports are generated during the SonarQube analysis and reflect the latest run.";
    containerEl.appendChild(desc);

    if (!info.pdf && !info.html) {
      var msg = document.createElement("p");
      msg.style.color = "#666";
      msg.textContent = "No reports available yet. Run an analysis to generate a report.";
      containerEl.appendChild(msg);
    } else {
      var row = document.createElement("div");
      if (info.pdf) {
        var pdfLink = document.createElement("a");
        pdfLink.href = base + "/api/pdfreport/get?project=" + encodeURIComponent(projectKey) + "&content_type=pdf";
        pdfLink.target = "_blank";
        pdfLink.rel = "noopener noreferrer";
        pdfLink.style.cssText = linkStyle;
        pdfLink.textContent = "Download PDF Report";
        row.appendChild(pdfLink);
      }
      if (info.html) {
        var htmlLink = document.createElement("a");
        htmlLink.href = base + "/api/pdfreport/get?project=" + encodeURIComponent(projectKey) + "&content_type=html";
        htmlLink.target = "_blank";
        htmlLink.rel = "noopener noreferrer";
        htmlLink.style.cssText = linkStyle;
        htmlLink.textContent = "Download HTML Report";
        row.appendChild(htmlLink);
      }
      containerEl.appendChild(row);
    }
  }

  fetch(base + "/api/pdfreport/info?project=" + encodeURIComponent(projectKey))
    .then(function (r) { return r.json(); })
    .then(function (info) { render(info); })
    .catch(function () { render({ pdf: false, html: false }); });

  return containerEl;
});
