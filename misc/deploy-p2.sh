#!/bin/bash

# Used by CI to deploy the existing target/repository/<version> directory to Github Pages

if [ -n "$GITHUB_API_KEY" ]; then
    cd "$GITHUB_WORKSPACE"

    echo "cloning gh-pages from ${GITHUB_REPOSITORY}"
    git clone -q  -b gh-pages https://$GITHUB_API_KEY@github.com/${GITHUB_REPOSITORY} gh-pages &>/dev/null
    cd gh-pages
    mkdir -p p2/${SHORT_VERSION}
    cd p2/${SHORT_VERSION}
    # remove all old file
    rm -Rf *
    # copy the P2 repository content from the maven build directory
    cp -R ${GITHUB_WORKSPACE}/target/repository/* .
    git add .
    git -c user.name='gh-actions' -c user.email='gh-actions' commit -m "add p2 repository for version ${SHORT_VERSION}"
    echo "pushing to gh-pages to ${GITHUB_REPOSITORY}"
    git push -q https://$GITHUB_API_KEY@github.com/${GITHUB_REPOSITORY} gh-pages &>/dev/null
    cd "$GITHUB_WORKSPACE"
else
	>&2 echo "Cannot deploy P2 repository because GITHUB_API_KEY environment variable is not set"
	exit 1
fi