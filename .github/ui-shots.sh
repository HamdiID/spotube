#!/bin/bash
# Drives the simulator through a few screens and saves screenshots in shots/.
DEV="$1"
BID="$2"
shot() { xcrun simctl io "$DEV" screenshot "shots/$1.png" >/dev/null 2>&1; }
tap() { idb ui tap --udid "$DEV" "$1" "$2" >/dev/null 2>&1 || true; sleep "${3:-2}"; }
swipe() { idb ui swipe --udid "$DEV" "$1" "$2" "$3" "$4" --duration 0.25 >/dev/null 2>&1 || true; sleep "${5:-2}"; }

brew tap facebook/fb >/dev/null 2>&1 && brew install idb-companion >/dev/null 2>&1
pip3 install --break-system-packages fb-idb >/dev/null 2>&1 || pip3 install fb-idb >/dev/null 2>&1
idb connect "$DEV" >/dev/null 2>&1 || true
idb ui describe-all --udid "$DEV" > shots/ax_welcome.json 2>/dev/null || true

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
