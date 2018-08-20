[![Build Status](https://travis-ci.org/corpus-tools/graphANNIS-java.svg?branch=develop)](https://travis-ci.org/corpus-tools/graphANNIS-java)

graphANNIS Java Bindings
========================

GraphANNIS is a library for corpus linguistic queries.
This are the Java bindings to its API and some additional utility tools to e.g. convert AQL to its JSON representation.

How to compile
---------------

You will need to build and install the graphANNIS library (Rust version) with cargo for your system before you can build the Java project with Maven.

- Install the latest version (at least 1.28.0) of Rust:
- Clone the graphANNIS library  from https://github.com/corpus-tools/graphANNIS/
- Execute `cargo build --release --features "c-api"` in the cloned repository
- Change to a clone of this graphANNIS Java bindings repository
- Copy the resulting  shared library file `<graphANNIS-repo>/target/release/libgraphannis.so` (`libgraphannis.dylib` under MacOS X and `graphannis.dll` under Windows) to `src/main/resources/<platform>/` where the platform is one of the following:

| Operating system       | `<platform>`  |
|------------------------|---------------|
| Linux (64 bit)         | linux-x86-64  |
| MacOS X (64 bit)       | darwin-x86-64 |
| Windows (64 bit)       | win32-x86-64  |

- Now compile the Java project with Maven (http://maven.apache.org/)
```
mvn install
```


3rd party dependencies
----------------------

This software depends on several 3rd party libraries. These are documented in the THIRD-PARTY.txt file in this folder.

Author(s)
---------

* Thomas Krause (thomaskrause@posteo.de)
