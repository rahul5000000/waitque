#!/usr/bin/env bash
# wait-for-it.sh — Wait for a host:port to become available

set -e

HOSTPORT=$1
TIMEOUT=${2:-30}
HOST=$(echo $HOSTPORT | cut -d: -f1)
PORT=$(echo $HOSTPORT | cut -d: -f2)

echo "Waiting for $HOST:$PORT (timeout: ${TIMEOUT}s)..."

for i in $(seq $TIMEOUT); do
  nc -z $HOST $PORT && echo "Connected to $HOST:$PORT!" && exit 0
  sleep 1
done

echo "Timeout after ${TIMEOUT}s waiting for $HOST:$PORT"
exit 1
