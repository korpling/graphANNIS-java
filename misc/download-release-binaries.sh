#!/bin/bash

if [ -z "$1" ]; then
        echo "Missing argument \"version\""
        exit 1
fi

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null && pwd )"

LINUX_FILE="libgraphannis_capi.so"
echo "Downloading $LINUX_FILE"
curl -o "$DIR/../java-api/src/main/resources/linux-x86-64/$LINUX_FILE" -L "https://github.com/corpus-tools/graphANNIS/releases/download/$1/$LINUX_FILE"

MAC_FILE="libgraphannis_capi.dylib"
echo "Downloading $MAC_FILE"
curl -o "$DIR/../java-api/src/main/resources/darwin-x86-64/$MAC_FILE" -L "https://github.com/corpus-tools/graphANNIS/releases/download/$1/$MAC_FILE"

WIN_FILE="graphannis_capi.dll"
echo "Downloading $WIN_FILE"
curl -o "$DIR/../java-api/src/main/resources/win32-x86-64/$WIN_FILE" -L "https://github.com/corpus-tools/graphANNIS/releases/download/$1/$WIN_FILE"
