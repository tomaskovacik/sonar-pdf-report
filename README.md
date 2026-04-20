Sonar PDF Report Plugin
=========================


This is a fork from a fork of the Opensource version.

For the sake of traceability, I kept the package renaming of the previous fork from [(https://github.com/somasuraj3/test-p-6.7)](https://github.com/somasuraj3/test-p-6.7).

I kept tthe license and the code.

The plugin has been tested on SonarQube 8.0, on Linux 64 bits.

At that time, the plugin worked but I cannot say I have done a proper exhaustive testing. If you find a bug, creates an issue. However since I am very busy, unless **you are a company and with the possibility to donate**, the fixes will be treated with a low priority.



## Description / Features

Generate a project quality report in PDF format with the most relevant information from SonarQube web interface. The report aims to be a deliverable as part of project documentation.

The report contains:

* Dashboard
* Violations by categories
* Hotspots:
  * Most violated rules
  * Most violated files
  * Most complex classes
  * Most duplicated files
* Dashboard, violations and hotspots for all child modules (if they exists)

## Installation

1. Install the plugin through the [Update Center](http://docs.sonarqube.org/display/SONAR/Update+Center) or download it into the SONARQUBE_HOME/extensions/plugins directory
1. Restart SonarQube

## Usage

SonarQube PDF works as a post-job task. In this way, a PDF report is generated after each analysis in SonarQube.

### Authentication

The plugin uses the SonarQube Web API to collect project data. These API endpoints require a **User Token** — Analysis Tokens (both global and project-scoped) are **not** supported and will result in authentication errors.

Set your User Token via the `SONAR_USER_TOKEN` environment variable before running the analysis:

```
export SONAR_USER_TOKEN=<your_sonarqube_user_token>
```

Alternatively, you can pass the token through the `sonar.token` scanner property (e.g. `-Dsonar.token=<token>`). When both are present, `SONAR_USER_TOKEN` takes precedence.

### Configuration

You can skip report generation or select report type (executive or workbook) globally or at the project level.

In the previous version, you  Sonar Scanner configuration should contains the following property :

```
sonar.leak.period=NUMBER_OF_DAYS
```  



### Download the report

PDF report can be downloaded from the SonarQube GUI or from the SONAR output folder ( example target/sonar with a Maven project).


Issue tracking:
https://jira.codehaus.org/browse/SONARPLUGINS/component/14372

CI builds:
https://sonarplugins.ci.cloudbees.com/job/report-pdf
