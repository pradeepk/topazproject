#!/usr/bin/env bash
# $HeadURL::                                                                                      $
# $Id$
#
# Copyright (c) 2006 by Topaz, Inc.
# http://topazproject.org
#
# Licensed under the Educational Community License version 1.0
# http://opensource.org/licenses/ecl1.php

# WARNING:
#  ant must be in path and configured properly (ANT_HOME)
#  mvn must be in path and configured properly
#  JAVA_HOME must be set appropriately

# TODO: Subroutines to stop servers. Check to see if running. If so and fail to stop, show errors.
# TODO: If ecqs starts but fedora fails to start, shutdown ecqs
# TODO: Build site so it integrates with trac
# TODO: Prefix all logs w/something easy to search on
# TODO: Copy failed surefire reports to stdout/err
# TODO: Command line args to select what part of build to do? (for testing)

MVN=/home/tools/maven2/bin/mvn
RM=rm

TOPAZ_INSTALL_DIR=$HOME/topazproject-install
MVN_REPOSITORY=$HOME/.m2/repository
MVN_REPOSITORY_TOPAZ=${MVN_REPOSITORY}/org/topazproject
SITE_ABS_DIR=/home/mavensite/docssite

# Don't exit if we get a meaningless error
set +e

echo "Removing potentially stale directory: ${MVN_REPOSITORY_TOPAZ}"
rm -rf ${MVN_REPOSITORY_TOPAZ}

# Build our ant-tasks first
echo "Building ant-tasks-plugin first"
(cd build/ant-tasks-plugin; ${MVN} install)

echo "Making sure ecqs is stopped: mvn ant-tasks:ecqs-stop"
${MVN} ant-tasks:ecqs-stop   > /dev/null
echo "Making sure fedora is stopped: mvn ant-tasks:fedora-stop"
${MVN} ant-tasks:fedora-stop > /dev/null 2>&1

# Do a build, if it fails, just exit
echo "Running our build: mvn clean install --batch-mode"
set -e
${MVN} clean install --batch-mode
N=$?

echo "Removing potentially stale directory: ${TOPAZ_INSTALL_DIR}"
rm -rf ${TOPAZ_INSTALL_DIR}

# If any of these fail, we should just give up
set -e
echo "Installing ecqs: mvn ant-tasks:ecqs-install"
${MVN} ant-tasks:ecqs-install
echo "Installing fedora: mvn ant-tasks:fedora-install"
${MVN} ant-tasks:fedora-install
echo "Starting ecqs: mvn ant-tasks:ecqs-start"
${MVN} -DSPAWN=true ant-tasks:ecqs-start
echo "Starting fedora: mvn ant-tasks:fedora-start"
${MVN} -DSPAWN=true ant-tasks:fedora-start

# The rest of these things we do whether they succeed or not
set +e

# Make sure fedora has time to startup
echo "Sleeping a bit........(10 seconds)"
sleep 10

if [ ${N} -eq 0 ]; then
  echo "Running integration tests: mvn clean -Pit-startenv install --batch-mode"
  (cd topazproject/integrationtests; ${MVN} -Pit-startenv clean install --batch-mode)

  echo "Creating documentation: cd integrationtests; mvn site-deploy"
  rm -rf ${SITE_ABS_DIR}/*
  (cd topazproject/integrationtests; \
    ${MVN} -Dtopazproject.site.url=file://${SITE_ABS_DIR} site-deploy)
fi

echo "Stopping ecqs"
${MVN} ant-tasks:ecqs-stop   > /dev/null
echo "Stopping fedora"
${MVN} ant-tasks:fedora-stop > /dev/null 2>&1

# Return build result
exit ${N}