#!/bin/bash
# Drives the simulator through a few screens and saves screenshots in shots/.
DEV="$1"
BID="$2"
shot() { xcrun simctl io "$DEV" screenshot "shots/$1.png" >/dev/null 2>&1; }
LOG=shots/idb.log
tap() { idb ui tap --udid "$DEV" "$1" "$2" >>$LOG 2>&1 || echo "tap fail $1 $2" >>$LOG; sleep "${3:-2}"; }
swipe() { idb ui swipe --udid "$DEV" "$1" "$2" "$3" "$4" --duration 0.25 >>$LOG 2>&1 || true; sleep "${5:-2}"; }

{ brew tap facebook/fb && brew install idb-companion; } >>$LOG 2>&1
python3 -m venv /tmp/idbenv >>$LOG 2>&1 && /tmp/idbenv/bin/pip install fb-idb >>$LOG 2>&1
export PATH="/tmp/idbenv/bin:$PATH"
which idb idb_companion >>$LOG 2>&1
idb_companion --udid "$DEV" > shots/companion.log 2>&1 &
sleep 5
idb connect localhost 10882 >>$LOG 2>&1 || idb connect "$DEV" >>$LOG 2>&1 || true
idb list-targets >>$LOG 2>&1
idb ui describe-all --udid "$DEV" > shots/ax_welcome.json 2>>$LOG || true

# Onboarding: primary button bottom-right, three pages.
tap 330 805 3; shot welcome_2
tap 330 805 3; shot welcome_3
tap 330 805 6; shot home_1
sleep 15; shot home_2
idb ui describe-all --udid "$DEV" > shots/ax_home.json 2>/dev/null || true
# Bottom bar tabs (Home, Search, Library, Settings)
tap 150 830 4; shot search
tap 250 830 4; shot library
tap 350 830 4; shot settings
tap 50 830 4; shot home_3
