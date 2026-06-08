# PreSonus FaderPort v2 — Bitwig Studio Extension

A Bitwig Studio controller extension for the PreSonus FaderPort v2 (single-channel, one-fader model). Written in Java against the Bitwig Extension API v18.

---

## Requirements

- Bitwig Studio (tested on 5.x)
- PreSonus FaderPort v2 in **Studio One native mode**
- Java 25 (for building from source only)

### Setting Studio One Native Mode on the FaderPort v2

1. Hold the **Next** button while powering on the unit
2. Press the **Solo** button
3. The unit is now in Studio One native mode (required for this extension)

---

## Installation

### Pre-built (recommended)

1. Download `PreSonusFaderPort2.bwextension` from the [`dist/`](dist/) folder
2. Copy it to your Bitwig extensions directory:
   - **macOS**: `~/Documents/Bitwig Studio/Extensions/`
   - **Windows**: `%USERPROFILE%\Documents\Bitwig Studio\Extensions\`
   - **Linux**: `~/Bitwig Studio/Extensions/`
3. Open Bitwig Studio → **Settings → Controllers → Add controller manually**
4. Select **PreSonus → FaderPort v2**
5. Set MIDI In and Out to **PreSonus FP2**

### Build from Source

```bash
JAVA_HOME=/path/to/jdk5 ./gradlew build
```

The compiled extension is automatically copied to your Bitwig Extensions directory on a successful build.

---

## Controls

### Transport

| Button | Action |
|--------|--------|
| Play | Toggle playback |
| Stop | Stop playback |
| Record | Toggle arranger record |
| Loop | Toggle loop |
| Click | Toggle metronome |
| Rewind | Rewind |
| FFwd | Fast forward |

### Channel Strip

| Button | Action |
|--------|--------|
| Arm | Toggle record arm on selected track |
| Solo | Toggle solo on selected track |
| Mute | Toggle mute on selected track |
| Bypass | Toggle bypass on selected device (green = enabled, red = bypassed) |
| Link | Pin/unpin cursor device (blue = pinned) |
| Prev | Select previous track (or device in Device mode) |
| Next | Select next track (or device in Device mode) |

### Fader Modes

| Button | Mode | Fader Controls |
|--------|------|---------------|
| *(default)* | Volume | Selected track volume |
| Master | Master | Master track volume |
| Section | Device | First remote control parameter of selected device |
| Shift + Pan | Flip | Selected track pan (encoder controls volume) |

### Encoder Modes

| Button | Mode | Encoder Controls |
|--------|------|-----------------|
| Pan *(default)* | Pan | Selected track pan — push to reset to center |
| Channel | Channel Nav | Navigate tracks (CW = next, CCW = previous) |
| Scroll | Scroll | Arranger timeline position — push to play |
| Marker | Marker | Jump between cue markers |

In **Device mode**, the encoder scrolls through remote control pages regardless of the active encoder mode.

### Automation

| Button | Action |
|--------|--------|
| Read | Toggle automation write enabled |
| Write | Set automation mode: Write |
| Touch | Set automation mode: Touch |
| Shift + Read | Set automation mode: Latch |
| Shift + Touch | Set automation mode: Latch |

### Shift Combinations

| Combo | Action |
|-------|--------|
| Shift + Prev | Undo |
| Shift + Next | Redo |
| Shift + Solo | Clear all solos |
| Shift + Mute | Clear all mutes |
| Shift + Pan | Toggle fader flip mode |
| Shift + Master | Toggle note/detail editor |
| Shift + Click | Toggle mixer |
| Shift + Section | Toggle device panel |
| Shift + Marker | Toggle browser |

---

## LED Colors

| Light | Color | Meaning |
|-------|-------|---------|
| Bypass | Green | Device enabled |
| Bypass | Red | Device bypassed |
| Link | Blue | Device pinned |
| Pan | Green | Pan encoder mode active |
| Channel | Cyan | Channel Nav encoder mode active |
| Scroll | Yellow | Scroll encoder mode active |
| Marker | Magenta | Marker encoder mode active |
| Read | Green | Latch automation mode |
| Write | Red | Write automation mode |
| Touch | Orange | Touch automation mode |
| Touch | Yellow | Latch automation mode (via Touch LED) |

---

## Developer: Code → Actions mapping

This section maps important helper methods in the code to the user-facing actions described above. It can be useful when linking docs to the implementation in `src/main/java`.

- **Playback:** `togglePlayback()` — Play button (toggle play/stop)
- **Stop:** `stopPlayback()` — Stop button
- **Record:** `toggleRecord()` — Record button (toggle arranger record)
- **Loop:** `toggleLoop()` — Loop button
- **Metronome:** `toggleMetronome()` — Click button (toggle metronome)
- **Rewind / FFwd:** `rewindTransport()`, `fastForwardTransport()` — Rewind / FFwd buttons
- **Pan / Encoder reset:** `resetPan()` — Pan encoder push
- **Channel navigation:** `selectPreviousTrack()`, `selectNextTrack()` — Channel encoder mode
- **Scroll:** `scrollLeft()`, `scrollRight()` — Scroll encoder mode
- **Markers:** `jumpToPreviousMarker()`, `jumpToNextMarker()` — Marker encoder mode
- **Device parameter:** `resetDeviceParam()` — Encoder push in Device mode
- **Device mode toggle:** `toggleDeviceMode()` — Section (Device) button
- **Flip mode toggle:** `toggleFlipMode()` — Shift + Pan
- **Shift overlays:** `undoAction()`, `redoAction()`, `clearAllSolos()`, `clearAllMutes()` — Shift combos

Implementation: `src/main/java/com/smetoyer/bitwig/faderport2/FaderPort2Extension.java`

MIT License — see [LICENSE](LICENSE)
