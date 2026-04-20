Sonar PDF Report Plugin
=========================

This is a fork of the open-source SonarQube PDF Report plugin, maintained for compatibility with modern SonarQube releases.

The plugin has been tested on SonarQube 26.4 Community Edition on Linux 64-bit.

If you find a bug, please open an issue on [GitHub](https://github.com/tomaskovacik/sonar-pdf-report/issues).

## Description / Features

Generate a project quality report in **PDF** or **HTML** format with the most relevant information from the SonarQube web interface. The report is intended to be a deliverable as part of project documentation.

The report contains:

* Dashboard
* Violations by categories
* Hotspots:
  * Most violated rules
  * Most violated files
  * Most complex classes
  * Most duplicated files
* Dashboard, violations and hotspots for all child modules (if they exist)

## Compatibility

| Plugin version | SonarQube version |
|---|---|
| 1.7.1 | 26.4 (Community Edition) |

## Installation

1. Download the plugin JAR from the [Releases](https://github.com/tomaskovacik/sonar-pdf-report/releases) page.
2. Copy it into the `SONARQUBE_HOME/extensions/plugins` directory.
3. Restart SonarQube.

## Usage

SonarQube PDF Report works as a post-job task. A report is generated automatically after each analysis.

### Authentication

The plugin uses the SonarQube Web API to collect project data. These API endpoints require a **User Token**.

> ⚠️ **Analysis Tokens** (both global and project-scoped) are **not** supported and will cause authentication errors. You must use a **User Token**.

Set your User Token via the `SONAR_USER_TOKEN` environment variable **before** running the analysis:

```bash
export SONAR_USER_TOKEN=<your_sonarqube_user_token>
sonar-scanner -Dsonar.projectKey=my-project ...
```

If `SONAR_USER_TOKEN` is not set, report generation is skipped and a warning is logged.

### Report format

By default the plugin produces a **PDF** report. To generate an **HTML** report instead, pass the `report.type` property to the scanner:

```bash
# PDF (default)
sonar-scanner -Dreport.type=pdf ...

# HTML
sonar-scanner -Dreport.type=html ...
```

The report file is written to the scanner work directory (e.g. `target/sonar/` for Maven projects).

### Configuration reference

All properties can be set globally in the SonarQube Administration UI or per-project, or passed on the command line with `-D`.

| Property | Default | Description |
|---|---|---|
| `sonar.pdf.skip` | `false` | Set to `true` to skip report generation entirely. |
| `report.type` | `pdf` | Output format. Allowed values: `pdf`, `html`. |
| `report.logo` | _(none)_ | URL of a logo image to display on the report front page. |
| `sonar.pdf.other.metrics` | _(none)_ | Comma-separated list of additional metric keys to include in the report. |
| `sonar.pdf.issue.details` | `NONE` | Type(s) of issues to include. Allowed values: `BUG`, `CODE_SMELL`, `VULNERABILITY` (comma-separated). |
| `sonar.leak.period` | _(auto)_ | Number of days for the leak period. When omitted the plugin tries to detect the default. |

### Example: Maven project

```bash
export SONAR_USER_TOKEN=squ_xxxxxxxxxxxxxxxx

mvn sonar:sonar \
  -Dsonar.host.url=http://localhost:9000 \
  -Dsonar.projectKey=my-project \
  -Dreport.type=pdf \
  -Dsonar.pdf.issue.details=BUG,VULNERABILITY
```

## Issue tracking

https://github.com/tomaskovacik/sonar-pdf-report/issues

## License

This project is licensed under the [GNU Lesser General Public License v3.0](LICENSE.md).
