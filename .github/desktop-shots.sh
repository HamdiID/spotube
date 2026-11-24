#!/bin/bash
# Runs the desktop build in a phone-sized window under Xvfb and screenshots a few demo modes.
mkdir -p shots
export SPOTUBE_WINDOW_SIZE=402x874
export SKIKO_RENDER_API=SOFTWARE
export GDK_SCALE=2
Xvfb :99 -screen 0 1100x2000x24 >/dev/null 2>&1 &
export DISPLAY=:99
sleep 2

run() {
  local mode="$1" wait="$2" name="$3"
  SPOTUBE_DEMO="$mode" ./gradlew :composeApp:run --console=plain > "shots/run_$name.log" 2>&1 &
  local pid=$!
  local wid=""
  for i in $(seq 1 240); do
    wid=$(xdotool search --onlyvisible --name "Spotube" 2>/dev/null | head -1)
    [ -n "$wid" ] && break
    kill -0 $pid 2>/dev/null || break
    sleep 2
  done
  if [ -n "$wid" ]; then
    sleep "$wait"
    import -window "$wid" "shots/$name.png" 2>>shots/import.log || import -window root "shots/$name.png"
    [ -n "$4" ] && { sleep "$4"; import -window "$wid" "shots/${name}_late.png" 2>/dev/null; }
  else
    echo "no window for $name" >> shots/errors.txt
    import -window root "shots/${name}_root.png" 2>/dev/null
  fi
  pkill -f "org.spotube.spotube.MainKt" 2>/dev/null
  kill $pid 2>/dev/null
  wait $pid 2>/dev/null
  sleep 3
}

run home 20 home 15
run search 10 search
run library 10 library
run settings 10 settings
run track 20 mini
run "track player" 15 player 15
