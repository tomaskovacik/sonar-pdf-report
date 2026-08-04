window.registerExtension("sonarpdfreport/report_page", function (options) {
  const el = options.el;
  const projectKey = options.component.key;
  const base = window.location.origin;

  el.style.padding = "24px";
  el.style.fontFamily = "sans-serif";

  const btnStyle = [
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

  function triggerDownload(url, filename) {
    fetch(url)
      .then(function (r) { return r.blob(); })
      .then(function (blob) {
        const blobUrl = URL.createObjectURL(blob);
        const a = document.createElement("a");
        a.href = blobUrl;
        a.download = filename;
        document.body.appendChild(a);
        a.click();
        a.remove();
        URL.revokeObjectURL(blobUrl);
      });
  }

  function render(info) {
    el.innerHTML = "";

    const h2 = document.createElement("h2");
    h2.style.marginBottom = "8px";
    h2.textContent = "Analysis Reports";
    el.appendChild(h2);

    const desc = document.createElement("p");
    desc.style.color = "#666";
    desc.style.marginBottom = "20px";
    desc.textContent = "Reports are generated during the SonarQube analysis and reflect the latest run.";
    el.appendChild(desc);

    if (!info.pdf && !info.html) {
      const msg = document.createElement("p");
      msg.style.color = "#666";
      msg.textContent = "No reports available yet. Run an analysis to generate a report.";
      el.appendChild(msg);
    } else {
      const row = document.createElement("div");
      if (info.pdf) {
        const pdfBtn = document.createElement("button");
        pdfBtn.style.cssText = btnStyle;
        pdfBtn.textContent = "Download PDF Report";
        pdfBtn.onclick = function () {
          triggerDownload(
            base + "/api/pdfreport/get?project=" + encodeURIComponent(projectKey) + "&content_type=pdf",
            projectKey + ".pdf"
          );
        };
        row.appendChild(pdfBtn);
      }
      if (info.html) {
        const htmlBtn = document.createElement("button");
        htmlBtn.style.cssText = btnStyle;
        htmlBtn.textContent = "Download HTML Report";
        htmlBtn.onclick = function () {
          triggerDownload(
            base + "/api/pdfreport/get?project=" + encodeURIComponent(projectKey) + "&content_type=html",
            projectKey + ".html"
          );
        };
        row.appendChild(htmlBtn);
      }
      el.appendChild(row);
    }
  }

  fetch(base + "/api/pdfreport/info?project=" + encodeURIComponent(projectKey))
    .then(function (r) { return r.json(); })
    .then(function (info) { render(info); })
    .catch(function () { render({ pdf: false, html: false }); });

  return function () { el.innerHTML = ""; };
});
