
graphANNIS Java Bindings
========================

GraphANNIS is a library for corpus linguistic queries.
This are the Java bindings to its API and some additional utility tools to e.g. convert AQL to its JSON representation.

How to compile
---------------

First, you need to build and install the graphANNIS library with cargo for your system.
Copy the resulting libgraphannis\_capi.so/.dll/.dylib file to `java-api/src/main/resources/<platform>/` where the platform is one of the following:

| Operating system       | `<platform>`  |
|------------------------|---------------|
| Linux (64 bit)         | linux-x86-64  |
| MacOS X (64 bit)       | darwin-x86-64 |
| Windows (64 bit)       | win32-x86-64  |

This project uses Maven as build system. In order to build the packages, type

```
mvn install
```

after you copied the shared library.

3rd party dependencies
----------------------

This software depends on several 3rd party libraries. These are documented in the THIRD-PARTY.txt file in this folder.

Author(s)
---------

* Thomas Krause (thomaskrause@posteo.de)
