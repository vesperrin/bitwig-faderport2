# PreSonus FaderPort v2 - Bitwig Extension Test Checklist

**Extension Version**: 1.0
**Bitwig Version**: ___________
**Date**: ___________
**Tester**: ___________

## Prerequisites

- [ ] FaderPort v2 is in Studio One native mode (hold Next on power-up, press Solo)
- [ ] Extension builds successfully (`./gradlew build`)
- [ ] `PreSonusFaderPort2.bwextension` is in `~/Documents/Bitwig Studio/Extensions/`
- [ ] Bitwig discovers "PreSonus FaderPort v2" in Settings > Controllers
- [ ] MIDI In and Out are assigned to "PreSonus FP2"
- [ ] Extension activates without errors in Bitwig log

---

## 1. Transport Controls

| # | Test | Expected | Pass | Notes |
|---|------|----------|------|-------|
| 1.1 | Press **Play** | Playback starts; Play LED lights up | [ ] | |
| 1.2 | Press **Stop** | Playback stops; Stop LED lights up | [ ] | |
| 1.3 | Press **Stop** while stopped | Transport returns to position 0 | [ ] | |
| 1.4 | Press **Record** | Recording toggles; Record LED reflects state | [ ] | |
| 1.5 | Press **Loop** | Loop mode toggles; Loop LED reflects state | [ ] | |
| 1.6 | Press **Click** | Metronome toggles; Click LED reflects state | [ ] | |
| 1.7 | Hold **Rewind** | Transport rewinds; Rewind LED on while held | [ ] | |
| 1.8 | Release **Rewind** | Rewinding stops; Rewind LED turns off | [ ] | |
| 1.9 | Hold **FFwd** | Transport fast-forwards; FFwd LED on while held | [ ] | |
| 1.10 | Release **FFwd** | Fast-forwarding stops; FFwd LED turns off | [ ] | |

---

## 2. Fader - Default Mode (Volume)

| # | Test | Expected | Pass | Notes |
|---|------|----------|------|-------|
| 2.1 | Move fader | Selected track volume changes in Bitwig | [ ] | |
| 2.2 | Change volume in Bitwig | Motor fader moves to match | [ ] | |
| 2.3 | Select a different track | Motor fader jumps to new track's volume | [ ] | |
| 2.4 | Touch fader while motor is moving | Motor stops fighting your touch | [ ] | |
| 2.5 | Move fader to 0 (bottom) | Volume is at minimum | [ ] | |
| 2.6 | Move fader to max (top) | Volume is at maximum | [ ] | |

---

## 3. Fader - Master Mode

| # | Test | Expected | Pass | Notes |
|---|------|----------|------|-------|
| 3.1 | Press **Master** | Master mode activates; Master LED lights up | [ ] | |
| 3.2 | Move fader in Master mode | Master track volume changes | [ ] | |
| 3.3 | Change master volume in Bitwig | Motor fader follows master volume | [ ] | |
| 3.4 | Press **Master** again | Master mode deactivates; fader returns to selected track volume | [ ] | |

---

## 4. Fader - Device Mode

| # | Test | Expected | Pass | Notes |
|---|------|----------|------|-------|
| 4.1 | Select a track with a device | Prerequisite setup | [ ] | |
| 4.2 | Press **Section** | Device mode activates; Section LED lights up | [ ] | |
| 4.3 | Move fader in Device mode | First remote control parameter changes | [ ] | |
| 4.4 | Turn encoder in Device mode | Scrolls through device parameters | [ ] | |
| 4.5 | Press encoder in Device mode | Resets current parameter to default | [ ] | |
| 4.6 | Press **Section** again | Device mode deactivates; fader returns to volume | [ ] | |
| 4.7 | Activate Device mode with no device | Fader does nothing (no crash) | [ ] | |

---

## 5. Fader - Flip Mode

| # | Test | Expected | Pass | Notes |
|---|------|----------|------|-------|
| 5.1 | Press **Shift + Pan** | Flip mode activates | [ ] | |
| 5.2 | Move fader in Flip mode | Selected track pan changes | [ ] | |
| 5.3 | Turn encoder in Flip mode | Selected track volume changes (swapped) | [ ] | |
| 5.4 | Press **Shift + Pan** again | Flip mode deactivates; fader returns to volume | [ ] | |

---

## 6. Encoder - Pan Mode (Default)

| # | Test | Expected | Pass | Notes |
|---|------|----------|------|-------|
| 6.1 | On startup, **Pan** LED | Pan LED is green (default mode) | [ ] | |
| 6.2 | Turn encoder clockwise | Pan moves right | [ ] | |
| 6.3 | Turn encoder counter-clockwise | Pan moves left | [ ] | |
| 6.4 | Press encoder | Pan resets to center | [ ] | |
| 6.5 | Select different track | Encoder reflects new track's pan | [ ] | |

---

## 7. Encoder - Channel Mode

| # | Test | Expected | Pass | Notes |
|---|------|----------|------|-------|
| 7.1 | Press **Channel** | Channel mode activates; Channel LED is cyan | [ ] | |
| 7.2 | Pan LED | Pan LED turns off | [ ] | |
| 7.3 | Turn encoder clockwise | Selects next track | [ ] | |
| 7.4 | Turn encoder counter-clockwise | Selects previous track | [ ] | |
| 7.5 | Press **Channel** again | Returns to Pan mode; Pan LED green, Channel LED off | [ ] | |

---

## 8. Encoder - Scroll Mode

| # | Test | Expected | Pass | Notes |
|---|------|----------|------|-------|
| 8.1 | Press **Scroll** | Scroll mode activates; Scroll LED is yellow | [ ] | |
| 8.2 | Turn encoder clockwise | Timeline scrolls forward | [ ] | |
| 8.3 | Turn encoder counter-clockwise | Timeline scrolls backward | [ ] | |
| 8.4 | Press encoder | Starts playback from current position | [ ] | |
| 8.5 | Press **Scroll** again | Returns to Pan mode | [ ] | |

---

## 9. Encoder - Marker Mode

| # | Test | Expected | Pass | Notes |
|---|------|----------|------|-------|
| 9.1 | Press **Marker** | Marker mode activates; Marker LED is magenta | [ ] | |
| 9.2 | Turn encoder clockwise | Jumps to next marker/cue | [ ] | |
| 9.3 | Turn encoder counter-clockwise | Jumps to previous marker/cue | [ ] | |
| 9.4 | Press **Marker** again | Returns to Pan mode | [ ] | |

---

## 10. Channel Strip

| # | Test | Expected | Pass | Notes |
|---|------|----------|------|-------|
| 10.1 | Press **Solo** | Selected track solo toggles; Solo LED reflects state | [ ] | |
| 10.2 | Press **Mute** | Selected track mute toggles; Mute LED reflects state | [ ] | |
| 10.3 | Press **Arm** | Selected track record arm toggles; Arm LED reflects state | [ ] | |
| 10.4 | Press **Bypass** (device enabled) | Device bypassed; Bypass LED turns red | [ ] | |
| 10.5 | Press **Bypass** (device bypassed) | Device enabled; Bypass LED turns green | [ ] | |
| 10.6 | Press **Link** | Cursor device pins/unpins; Link LED blue when pinned | [ ] | |
| 10.7 | Press **Prev** | Previous track selected | [ ] | |
| 10.8 | Press **Next** | Next track selected | [ ] | |
| 10.9 | Press **Prev** on first track | No crash; stays on first track or wraps | [ ] | |
| 10.10 | Press **Next** on last track | No crash; stays on last track or wraps | [ ] | |

---

## 11. Automation

| # | Test | Expected | Pass | Notes |
|---|------|----------|------|-------|
| 11.1 | Press **Read** | Automation write enabled; Read LED green | [ ] | |
| 11.2 | Press **Read** again | Automation write disabled; Read LED off | [ ] | |
| 11.3 | Press **Write** | Write automation mode; Write LED red | [ ] | |
| 11.4 | Press **Touch** | Touch automation mode; Touch LED orange | [ ] | |
| 11.5 | Press **Shift + Read** | Automation off | [ ] | |
| 11.6 | Press **Shift + Write** | Trim mode | [ ] | |
| 11.7 | Press **Shift + Touch** | Latch mode | [ ] | |

---

## 12. Shift Combos

| # | Test | Expected | Pass | Notes |
|---|------|----------|------|-------|
| 12.1 | Press **Shift + Prev** | Undo last action | [ ] | |
| 12.2 | Press **Shift + Next** | Redo last action | [ ] | |
| 12.3 | Press **Shift + Solo** | Clear all solos | [ ] | |
| 12.4 | Press **Shift + Mute** | Clear all mutes | [ ] | |
| 12.5 | Press **Shift + Pan** | Toggle fader flip mode | [ ] | |
| 12.6 | Press **Shift + Master** | Toggle detail/note editor panel | [ ] | |
| 12.7 | Press **Shift + Click** | Toggle mixer panel | [ ] | |
| 12.8 | Press **Shift + Section** | Toggle device panel | [ ] | |
| 12.9 | Press **Shift + Marker** | Toggle browser | [ ] | |
| 12.10 | Hold **Shift** | Shift LED lights up while held | [ ] | |
| 12.11 | Release **Shift** | Shift LED turns off | [ ] | |

---

## 13. LED Feedback

| # | Test | Expected | Pass | Notes |
|---|------|----------|------|-------|
| 13.1 | Solo track in Bitwig (mouse) | Solo LED lights up on FP2 | [ ] | |
| 13.2 | Mute track in Bitwig (mouse) | Mute LED lights up on FP2 | [ ] | |
| 13.3 | Arm track in Bitwig (mouse) | Arm LED lights up on FP2 | [ ] | |
| 13.4 | Enable loop in Bitwig (mouse) | Loop LED lights up on FP2 | [ ] | |
| 13.5 | Enable metronome in Bitwig (mouse) | Click LED lights up on FP2 | [ ] | |
| 13.6 | Start playback in Bitwig (mouse) | Play LED lights up on FP2 | [ ] | |
| 13.7 | Start recording in Bitwig (mouse) | Record LED lights up on FP2 | [ ] | |
| 13.8 | Encoder mode LEDs are mutually exclusive | Only one encoder mode LED is lit at a time | [ ] | |
| 13.9 | RGB LEDs show correct colors | Bypass=green/red, Link=blue, Pan=green, Channel=cyan, Scroll=yellow, Marker=magenta | [ ] | |

---

## 14. Edge Cases

| # | Test | Expected | Pass | Notes |
|---|------|----------|------|-------|
| 14.1 | No tracks in project | No crash; controls are inert | [ ] | |
| 14.2 | No devices on selected track | Bypass/Link/Device mode do nothing; no crash | [ ] | |
| 14.3 | Switch tracks rapidly | Fader and LEDs update correctly | [ ] | |
| 14.4 | Disconnect FP2 USB | Bitwig handles gracefully; no crash | [ ] | |
| 14.5 | Reconnect FP2 USB | Extension reconnects (may need re-enable) | [ ] | |
| 14.6 | Load a new project | Extension re-initializes; fader/LEDs update | [ ] | |
| 14.7 | Multiple encoder mode switches | Modes switch cleanly; no stuck LEDs | [ ] | |
| 14.8 | Activate Master + Device modes | Last activated wins for fader; no crash | [ ] | |
| 14.9 | Shift held during mode switches | No unexpected behavior | [ ] | |
| 14.10 | Keep-alive | FP2 stays responsive after 5+ minutes idle | [ ] | |

---

## Summary

| Section | Total | Passed | Failed | Skipped |
|---------|-------|--------|--------|---------|
| 1. Transport | 10 | | | |
| 2. Fader - Default | 6 | | | |
| 3. Fader - Master | 4 | | | |
| 4. Fader - Device | 7 | | | |
| 5. Fader - Flip | 4 | | | |
| 6. Encoder - Pan | 5 | | | |
| 7. Encoder - Channel | 5 | | | |
| 8. Encoder - Scroll | 5 | | | |
| 9. Encoder - Marker | 4 | | | |
| 10. Channel Strip | 10 | | | |
| 11. Automation | 7 | | | |
| 12. Shift Combos | 11 | | | |
| 13. LED Feedback | 9 | | | |
| 14. Edge Cases | 10 | | | |
| **TOTAL** | **97** | | | |

---

*Generated for PreSonus FaderPort v2 Bitwig Extension*
