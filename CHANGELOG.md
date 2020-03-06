# graphANNIS Java API changelog

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [0.27.0] - 2020-03-06

### Changed

- Update graphANNIS to 0.26.0

## [0.26.0] - 2019-11-25

### Changed

- Support multiple corpora in find, count, count_extra and frequency


## [0.25.0] - 2019-11-15

### Changed

- Updated to graphANNIS 0.24.0

### Fixed

- Bump jackson-databind from 2.9.10 to 2.9.10.1 for multiple CVEs

## [0.24.0] - 2019-10-15

### Changed

- Allow to specify the segmentation when getting the subgraph with a context
- Using released core version 0.23.1

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

[Unreleased]: https://github.com/korpling/graphANNIS/compare/v0.27.0...HEAD
[0.27.0]: https://github.com/korpling/graphANNIS/compare/v0.26.0...v0.27.0
[0.26.0]: https://github.com/korpling/graphANNIS/compare/v0.25.0...v0.26.0
[0.25.0]: https://github.com/korpling/graphANNIS/compare/v0.24.0...v0.25.0
[0.24.0]: https://github.com/korpling/graphANNIS/compare/v0.23.0...v0.24.0
