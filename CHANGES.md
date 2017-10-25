# Changelog

## 0.2.1
- Fix issue with missing sbt 1.0.x compatible `marathon-templating` artifact ([#7](https://github.com/Tapad/sbt-marathon/issues/7))
- Templating uses latest available version of Twirl (1.3.12)

## 0.2.0
- Support sbt 1.0.x in addition to sbt 0.13.x

## 0.1.3
- Fix issue with `marathonServiceRestart` task ([#4](https://github.com/Tapad/sbt-marathon/issues/4))

## 0.1.2
- Support HTTP URLs
- Support URLs that contain basic access authentication credentials

## 0.1.1
- Add `marathonSetServiceUrl` command
  - Allows users to change the `marathonServiceUrl` value during an sbt session
  - Useful for targeting multiple Mesos clusters / Marathon instances during application deployment

## 0.1.0
- First release
