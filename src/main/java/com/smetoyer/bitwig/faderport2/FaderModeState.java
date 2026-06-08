package com.smetoyer.bitwig.faderport2;

/**
 * Small value object to contain fader/mode-related mutable state.
 */
public class FaderModeState {
    private boolean shiftHeld = false;
    private boolean masterModeActive = false;
    private boolean deviceModeActive = false;
    private boolean flipModeActive = false;

    public boolean isShiftHeld() { return shiftHeld; }
    public void setShiftHeld(boolean v) { shiftHeld = v; }

    public boolean isMasterModeActive() { return masterModeActive; }
    public void setMasterModeActive(boolean v) { masterModeActive = v; }

    public boolean isDeviceModeActive() { return deviceModeActive; }
    public void setDeviceModeActive(boolean v) { deviceModeActive = v; }

    public boolean isFlipModeActive() { return flipModeActive; }
    public void setFlipModeActive(boolean v) { flipModeActive = v; }
}
