#!/bin/bash

if [ -z "$JAVA_HOME" ]
then
    echo "There is no JAVA_HOME"
    exit 1
fi

if [ -z "$CATALINA_HOME" ]
then
    echo "There is no CATALINA_HOME"
    exit 1
fi

if [ -z "$CATALINA_BASE" ]
then
    echo "There is no CATALINA_BASE"
    exit 1
fi

LOG_DIR=$CATALINA_BASE/logs
if [ ! -d "$LOG_DIR" ]; then
  mkdir $LOG_DIR
fi

$CATALINA_HOME/bin/catalina.sh start


