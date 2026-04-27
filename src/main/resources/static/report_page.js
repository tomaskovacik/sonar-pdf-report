window.registerExtension("sonarpdfreport/report_page", function (options) {
  var projectKey = options.component.key;
  var base = window.location.origin;

  var containerEl = document.createElement("div");
  containerEl.style.padding = "24px";
  containerEl.style.fontFamily = "sans-serif";

  var btnStyle = [
    "display:inline-block",
    "padding:8px 20px",
    "background:#236a97",
    "color:#fff",
    "border-radius:4px",
    "border:none",
    "margin-right:12px",
    "font-size:14px",
    "cursor:pointer",
  ].join(";");

  function downloadUrl(type) {
    return base + "/api/pdfreport/get?project=" + encodeURIComponent(projectKey) + "&content_type=" + type;
  }

  function triggerDownload(url) {
    var a = document.createElement("a");
    a.href = url;
    a.download = "";
    document.body.appendChild(a);
    a.click();
    setTimeout(function () { document.body.removeChild(a); }, 100);
  }

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
      var btnRow = document.createElement("div");
      if (info.pdf) {
        var pdfBtn = document.createElement("button");
        pdfBtn.style.cssText = btnStyle;
        pdfBtn.textContent = "Download PDF Report";
        pdfBtn.addEventListener("click", function () { triggerDownload(downloadUrl("pdf")); });
        btnRow.appendChild(pdfBtn);
      }
      if (info.html) {
        var htmlBtn = document.createElement("button");
        htmlBtn.style.cssText = btnStyle;
        htmlBtn.textContent = "Download HTML Report";
        htmlBtn.addEventListener("click", function () { triggerDownload(downloadUrl("html")); });
        btnRow.appendChild(htmlBtn);
      }
      containerEl.appendChild(btnRow);
    }
  }

  fetch(base + "/api/pdfreport/info?project=" + encodeURIComponent(projectKey))
    .then(function (r) { return r.json(); })
    .then(function (info) { render(info); })
    .catch(function () { render({ pdf: false, html: false }); });

  return containerEl;
});
