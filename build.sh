#!/bin/bash
set -e

VERSION="$1"
source ./build.properties
PROJECT="$NDLAOrganization/$NDLAComponentName"

if [ -z $VERSION ]
then
    VERSION="SNAPSHOT"
fi

docker build -t $PROJECT:$VERSION . -f ./docker/application/Dockerfile
echo "BUILT $PROJECT:$VERSION"
