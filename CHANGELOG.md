# graphANNIS Java API changelog

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Changed

- Allow to specify the segmentation when getting the subgraph with a context
- Updated to graphANNIS 0.24.0

## [0.23.0] - 2019-08-16


### Changed

- From this version on, graphANNIS-java follows its own versioning scheme independent of the graphANNIS core library.
- **Use "graphannis" as artifact ID instead "graphannis-api"**
- API now only accepts a single corpus name in `find`, `count`, `countExtra` and `graph` instead of a list of corpus names
- Removed some redundant API calls and re-prdered some arguments to better match the original Rust-API
- Automatically download released graphANNIS core binaries in build process instead of reyling on a script
- Update to JNA library 5.3.1

### Fixed

- Native library was not found on Mac OS X
- Create and deploy a P2 repository to GitHub pages to make using graphANNIS from Eclipse RCP projects like Hexatomic easier
- Update to bugfix version 2.9.9.1 from jackson-databind (CVE-2019-14379 and CVE-2019-14439)

