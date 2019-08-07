#!/bin/bash

# Used by CI to deploy the existing target/repository/<version> directory to Github Pages

if [ -n "$GITHUB_API_KEY" ]; then
    cd "$TRAVIS_BUILD_DIR"

    echo "cloning gh-pages"
    git clone -q  -b gh-pages https://$GITHUB_API_KEY@github.com/korpling/graphANNIS-java gh-pages &>/dev/null
    cd gh-pages
    mkdir -p p2/${SHORT_VERSION}
    cd p2/${SHORT_VERSION}
    cp -R ${TRAVIS_BUILD_DIR}/target/repository/* .
    git add .
    git -c user.name='travis' -c user.email='travis' commit -m "add p2 repository for version ${SHORT_VERSION}"
    echo "pushing to gh-pages"
    git push -q https://$GITHUB_API_KEY@github.com/korpling/graphANNIS gh-pages &>/dev/null
    cd "$TRAVIS_BUILD_DIR"
fi