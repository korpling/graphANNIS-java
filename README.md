[![Javadocs](http://www.javadoc.io/badge/org.corpus-tools/graphannis-api.svg)](http://www.javadoc.io/doc/org.corpus-tools/graphannis-api)

# graphANNIS Java Bindings

GraphANNIS is a library for corpus linguistic queries and these are its Java bindings to **graphANNIS version 0.22.0**.

## How to compile

You can compile the Java project with Maven (http://maven.apache.org/)
```
mvn install
```

This will automatically download the the graphANNIS binaries to the `target/native/<platform>` folder, where platform is one of the following:

| Operating system       | `<platform>`  |
|------------------------|---------------|
| Linux (64 bit)         | linux-x86-64  |
| MacOS X (64 bit)       | darwin        |
| Windows (64 bit)       | win32-x86-64  |

You can change the `core.version` property in the `pom.xml` to use a different released version of graphANNIS.

To compile graphANNIS on your own (e.g. for using a non-released version)

- Clone the graphANNIS library  from https://github.com/korpling/graphANNIS/
- Follow the [graphANNIS compile instructions](https://github.com/korpling/graphANNIS#how-to-compile)
- Copy the resulting shared library file `<graphANNIS-repo>/target/release/libgraphannis.so` (`libgraphannis.dylib` under MacOS X and `graphannis.dll` under Windows) into the `target/native/<platform>` folder.

### Creating a P2 repository

For easier integration of graphANNIS into Eclipse RCP projects like Hexatomic, it is possible to create a P2 repository with the graphANNIS bundles (and its dependencies).
Execute

```bash
mvn p2:site
```

to create a local P2 repository in `target/repository`. You can serve this locally with Jetty using

```bash
mvn jetty:run -Djetty.http.port=9999
```

and access it at http://localhost:9999/site/. You can choose the port freely, e.g. to avoid conflicts with other local P2 repositories.

## 3rd party dependencies

This software depends on several 3rd party libraries. These are documented in the THIRD-PARTY.txt file in this folder.

## Author(s)

* Thomas Krause (thomaskrause@posteo.de)
