#!/bin/bash
# Relaunches the app in a few demo modes (SPOTUBE_DEMO, simulator only) and saves screenshots in shots/.
DEV="$1"
BID="$2"
shot() { xcrun simctl io "$DEV" screenshot "shots/$1.png" >/dev/null 2>&1; }
run() {
  xcrun simctl terminate "$DEV" "$BID" >/dev/null 2>&1 || true
  sleep 1
  SIMCTL_CHILD_SPOTUBE_DEMO="$1" xcrun simctl launch "$DEV" "$BID" >/dev/null 2>&1
  sleep "$2"
  shot "$3"
}

run home 25 home
sleep 10; shot home_late
run search 15 search
run library 15 library
run settings 12 settings
run track 25 mini
run "track player" 20 player
sleep 15; shot player_late
