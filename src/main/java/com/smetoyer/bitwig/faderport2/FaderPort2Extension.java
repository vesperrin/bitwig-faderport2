package com.smetoyer.bitwig.faderport2;

import com.bitwig.extension.controller.ControllerExtension;
import com.bitwig.extension.controller.api.*;
import com.smetoyer.bitwig.faderport2.framework.Layer;
import com.smetoyer.bitwig.faderport2.framework.LayerGroup;
import com.smetoyer.bitwig.faderport2.framework.Layers;


/**
 * PreSonus FaderPort v2 controller extension for Bitwig Studio.
 * Requires the FP2 to be set to Studio One native mode
 * (hold Next on power-up, then press Solo).
 *
 * Single main class following the FP8/16 extension pattern with layer methods.
 */
public class FaderPort2Extension extends ControllerExtension
{
   // ---- MIDI Protocol Constants ----

   // Button note numbers
   private static final int NOTE_ARM          = 0x00;
   private static final int NOTE_BYPASS       = 0x03;
   private static final int NOTE_LINK         = 0x05;
   private static final int NOTE_SOLO         = 0x08;
   private static final int NOTE_MUTE         = 0x10;
   private static final int NOTE_ENCODER_PUSH = 0x20;
   private static final int NOTE_PAN          = 0x2A;
   private static final int NOTE_PREV         = 0x2E;
   private static final int NOTE_NEXT         = 0x2F;
   private static final int NOTE_CHANNEL      = 0x36;
   private static final int NOTE_SCROLL       = 0x38;
   private static final int NOTE_MASTER       = 0x3A;
   private static final int NOTE_CLICK        = 0x3B;
   private static final int NOTE_SECTION      = 0x3C;
   private static final int NOTE_MARKER       = 0x3D;
   private static final int NOTE_SHIFT        = 0x46;
   private static final int NOTE_AUTO_READ    = 0x4A;
   private static final int NOTE_AUTO_WRITE   = 0x4B;
   private static final int NOTE_AUTO_TOUCH   = 0x4D;
   private static final int NOTE_LOOP         = 0x56;
   private static final int NOTE_REWIND       = 0x5B;
   private static final int NOTE_FFWD         = 0x5C;
   private static final int NOTE_STOP         = 0x5D;
   private static final int NOTE_PLAY         = 0x5E;
   private static final int NOTE_RECORD       = 0x5F;
   private static final int NOTE_FADER_TOUCH  = 0x68;

   private static final int CC_ENCODER = 0x10;

   // RGB-capable button notes (support 4-channel protocol)
   private static final int[] RGB_BUTTONS = {
      NOTE_BYPASS, NOTE_LINK, NOTE_PAN, NOTE_CHANNEL, NOTE_SCROLL,
      NOTE_MARKER, NOTE_AUTO_READ, NOTE_AUTO_WRITE, NOTE_AUTO_TOUCH
   };

   // ---- Hardware Surface ----
   private HardwareSurface mHardwareSurface;
   private HardwareSlider mFader;
   private HardwareButton mFaderTouch;
   private AbsoluteHardwareControlBinding mFaderBinding; // Current active fader binding
   private RelativeHardwareKnob mEncoder;
   private MidiOut mMidiOut;

   // Buttons
   private HardwareButton mArmButton;
   private HardwareButton mSoloButton;
   private HardwareButton mMuteButton;
   private HardwareButton mBypassButton;
   private HardwareButton mLinkButton;
   private HardwareButton mPanButton;
   private HardwareButton mPrevButton;
   private HardwareButton mNextButton;
   private HardwareButton mChannelButton;
   private HardwareButton mScrollButton;
   private HardwareButton mMasterButton;
   private HardwareButton mClickButton;
   private HardwareButton mSectionButton;
   private HardwareButton mMarkerButton;
   private HardwareButton mShiftButton;
   private HardwareButton mAutoReadButton;
   private HardwareButton mAutoWriteButton;
   private HardwareButton mAutoTouchButton;
   private HardwareButton mLoopButton;
   private HardwareButton mRewindButton;
   private HardwareButton mFfwdButton;
   private HardwareButton mStopButton;
   private HardwareButton mPlayButton;
   private HardwareButton mRecordButton;
   private HardwareButton mEncoderPush;

   // RGB lights
   private MultiStateHardwareLight mBypassLight;
   private MultiStateHardwareLight mLinkLight;
   private MultiStateHardwareLight mPanLight;
   private MultiStateHardwareLight mChannelLight;
   private MultiStateHardwareLight mScrollLight;
   private MultiStateHardwareLight mMarkerLight;
   private MultiStateHardwareLight mAutoReadLight;
   private MultiStateHardwareLight mAutoWriteLight;
   private MultiStateHardwareLight mAutoTouchLight;

   // Standard (on/off) lights
   private OnOffHardwareLight mArmLight;
   private OnOffHardwareLight mSoloLight;
   private OnOffHardwareLight mMuteLight;
   private OnOffHardwareLight mMasterLight;
   private OnOffHardwareLight mClickLight;
   private OnOffHardwareLight mSectionLight;
   private OnOffHardwareLight mLoopLight;
   private OnOffHardwareLight mStopLight;
   private OnOffHardwareLight mPlayLight;
   private OnOffHardwareLight mRecordLight;
   private OnOffHardwareLight mShiftLight;
   private OnOffHardwareLight mRewindLight;
   private OnOffHardwareLight mFfwdLight;

   // ---- Bitwig API Objects ----
   private Transport mTransport;
   private CursorTrack mCursorTrack;
   private PinnableCursorDevice mCursorDevice;
   private CursorRemoteControlsPage mRemoteControls;
   private MasterTrack mMasterTrack;
   private Application mApplication;

   // ---- Layer Framework ----
   private Layers mLayers;
   private Layer mDefaultLayer;
   private Layer mChannelStripLayer;
   private Layer mAutoLayer;
   private Layer mShiftLayer;

   // Encoder mode layers (mutually exclusive)
   private Layer mPanLayer;
   private Layer mChannelNavLayer;
   private Layer mScrollLayer;
   private Layer mMarkerLayer;
   private LayerGroup mEncoderModeGroup;

   // Fader override layers
   private Layer mMasterLayer;
   private Layer mDeviceLayer;
   private Layer mFlipLayer;

   // ---- State ----
   private boolean mIsShiftHeld = false;
   private boolean mMasterModeActive = false;
   private boolean mDeviceModeActive = false;
   private boolean mFlipModeActive = false;
   private Layer mSavedEncoderLayer = null; // saved while device mode suspends encoder group

   protected FaderPort2Extension(
      final FaderPort2Definition definition,
      final ControllerHost host)
   {
      super(definition, host);
   }

   @Override
   public void init()
   {
      final ControllerHost host = getHost();

      // 1. MIDI I/O
      final MidiIn midiIn = host.getMidiInPort(0);
      mMidiOut = host.getMidiOutPort(0);

      // 2. Bitwig API objects
      mTransport = host.createTransport();
      mCursorTrack = host.createCursorTrack("fp2_cursor", "FP2 Cursor", 0, 0, true);
      mCursorDevice = mCursorTrack.createCursorDevice("fp2_device", "FP2 Device", 0,
         CursorDeviceFollowMode.FOLLOW_SELECTION);
      mRemoteControls = mCursorDevice.createCursorRemoteControlsPage(1);
      mMasterTrack = host.createMasterTrack(0);
      mApplication = host.createApplication();

      // Subscribe to all values read during flush/updateHardware
      mTransport.isPlaying().addValueObserver(v -> {});
      mTransport.isArrangerRecordEnabled().addValueObserver(v -> {});
      mTransport.isArrangerLoopEnabled().addValueObserver(v -> {});
      mTransport.isMetronomeEnabled().addValueObserver(v -> {});
      mTransport.automationWriteMode().addValueObserver(v -> {});
      mTransport.isArrangerAutomationWriteEnabled().addValueObserver(v -> {});
      mCursorTrack.solo().addValueObserver(v -> {});
      mCursorTrack.mute().addValueObserver(v -> {});
      mCursorTrack.arm().addValueObserver(v -> {});
      mCursorTrack.name().addValueObserver(v -> {});
      mCursorDevice.isEnabled().addValueObserver(v -> {});
      mCursorDevice.isPinned().addValueObserver(v -> {});
      mRemoteControls.getParameter(0).name().addValueObserver(v -> {});

      // 3. Hardware surface
      mHardwareSurface = host.createHardwareSurface();
      createHardwareControls(midiIn);

      // Subscribe hardware button isPressed() values used in LED suppliers
      mRewindButton.isPressed().addValueObserver(v -> {});
      mFfwdButton.isPressed().addValueObserver(v -> {});
      mShiftButton.isPressed().addValueObserver(v -> {});
      mFaderTouch.isPressed().addValueObserver(v -> {});

      // 4. Create layers
      mLayers = new Layers(this);
      createLayers();

      // 5. Keep-alive timer
      startKeepAlive();

      host.println("FaderPort v2 initialized.");
   }

   // ===========================================================================
   // Hardware Surface Creation
   // ===========================================================================

   private void createHardwareControls(final MidiIn midiIn)
   {
      createMotorFader(midiIn);
      createClickEncoder(midiIn);
      createTransportButtons(midiIn);
      createChannelStripButtons(midiIn);
      createModeButtons(midiIn);
      createAutoButtons(midiIn);
      createShiftButton(midiIn);
   }

   private void createMotorFader(final MidiIn midiIn)
   {
      mFader = mHardwareSurface.createHardwareSlider("fader");
      mFader.setAdjustValueMatcher(midiIn.createAbsolutePitchBendValueMatcher(0));

      mFaderTouch = mHardwareSurface.createHardwareButton("fader_touch");
      mFaderTouch.pressedAction().setActionMatcher(
         midiIn.createNoteOnActionMatcher(0, NOTE_FADER_TOUCH));
      mFaderTouch.releasedAction().setActionMatcher(
         midiIn.createNoteOffActionMatcher(0, NOTE_FADER_TOUCH));
      mFader.setHardwareButton(mFaderTouch);

      // The hardware binding created in updateFaderHardwareBinding() will handle
      // sending fader values to the DAW. The motor fader position is updated by
      // the value observers for each fader target.
   }

   private void createClickEncoder(final MidiIn midiIn)
   {
      mEncoder = mHardwareSurface.createRelativeHardwareKnob("encoder");
      mEncoder.setAdjustValueMatcher(
         midiIn.createRelativeSignedBitCCValueMatcher(0, CC_ENCODER, 128));
      mEncoder.setSensitivity(1.0);
      mEncoder.setStepSize(1.0 / 128.0);

      mEncoderPush = createToggleButton(midiIn, "encoder_push", NOTE_ENCODER_PUSH);
   }

   private void createTransportButtons(final MidiIn midiIn)
   {
      mPlayButton = createToggleButton(midiIn, "play", NOTE_PLAY);
      mPlayLight = createOnOffLight("play_light", NOTE_PLAY);
      mPlayButton.setBackgroundLight(mPlayLight);

      mStopButton = createToggleButton(midiIn, "stop", NOTE_STOP);
      mStopLight = createOnOffLight("stop_light", NOTE_STOP);
      mStopButton.setBackgroundLight(mStopLight);

      mRecordButton = createToggleButton(midiIn, "record", NOTE_RECORD);
      mRecordLight = createOnOffLight("record_light", NOTE_RECORD);
      mRecordButton.setBackgroundLight(mRecordLight);

      mLoopButton = createToggleButton(midiIn, "loop", NOTE_LOOP);
      mLoopLight = createOnOffLight("loop_light", NOTE_LOOP);
      mLoopButton.setBackgroundLight(mLoopLight);

      mRewindButton = createToggleButton(midiIn, "rewind", NOTE_REWIND);
      mRewindLight = createOnOffLight("rewind_light", NOTE_REWIND);
      mRewindButton.setBackgroundLight(mRewindLight);

      mFfwdButton = createToggleButton(midiIn, "ffwd", NOTE_FFWD);
      mFfwdLight = createOnOffLight("ffwd_light", NOTE_FFWD);
      mFfwdButton.setBackgroundLight(mFfwdLight);

      mClickButton = createToggleButton(midiIn, "click", NOTE_CLICK);
      mClickLight = createOnOffLight("click_light", NOTE_CLICK);
      mClickButton.setBackgroundLight(mClickLight);
   }

   private void createChannelStripButtons(final MidiIn midiIn)
   {
      mArmButton = createToggleButton(midiIn, "arm", NOTE_ARM);
      mArmLight = createOnOffLight("arm_light", NOTE_ARM);
      mArmButton.setBackgroundLight(mArmLight);

      mSoloButton = createToggleButton(midiIn, "solo", NOTE_SOLO);
      mSoloLight = createOnOffLight("solo_light", NOTE_SOLO);
      mSoloButton.setBackgroundLight(mSoloLight);

      mMuteButton = createToggleButton(midiIn, "mute", NOTE_MUTE);
      mMuteLight = createOnOffLight("mute_light", NOTE_MUTE);
      mMuteButton.setBackgroundLight(mMuteLight);

      mBypassButton = createToggleButton(midiIn, "bypass", NOTE_BYPASS);
      mBypassLight = createRGBLight("bypass_light", NOTE_BYPASS);
      mBypassButton.setBackgroundLight(mBypassLight);

      mLinkButton = createToggleButton(midiIn, "link", NOTE_LINK);
      mLinkLight = createRGBLight("link_light", NOTE_LINK);
      mLinkButton.setBackgroundLight(mLinkLight);

      mPrevButton = createToggleButton(midiIn, "prev", NOTE_PREV);
      mNextButton = createToggleButton(midiIn, "next", NOTE_NEXT);
   }

   private void createModeButtons(final MidiIn midiIn)
   {
      mPanButton = createToggleButton(midiIn, "pan", NOTE_PAN);
      mPanLight = createRGBLight("pan_light", NOTE_PAN);
      mPanButton.setBackgroundLight(mPanLight);

      mChannelButton = createToggleButton(midiIn, "channel", NOTE_CHANNEL);
      mChannelLight = createRGBLight("channel_light", NOTE_CHANNEL);
      mChannelButton.setBackgroundLight(mChannelLight);

      mScrollButton = createToggleButton(midiIn, "scroll", NOTE_SCROLL);
      mScrollLight = createRGBLight("scroll_light", NOTE_SCROLL);
      mScrollButton.setBackgroundLight(mScrollLight);

      mMasterButton = createToggleButton(midiIn, "master", NOTE_MASTER);
      mMasterLight = createOnOffLight("master_light", NOTE_MASTER);
      mMasterButton.setBackgroundLight(mMasterLight);

      mSectionButton = createToggleButton(midiIn, "section", NOTE_SECTION);
      mSectionLight = createOnOffLight("section_light", NOTE_SECTION);
      mSectionButton.setBackgroundLight(mSectionLight);

      mMarkerButton = createToggleButton(midiIn, "marker", NOTE_MARKER);
      mMarkerLight = createRGBLight("marker_light", NOTE_MARKER);
      mMarkerButton.setBackgroundLight(mMarkerLight);
   }

   private void createAutoButtons(final MidiIn midiIn)
   {
      mAutoReadButton = createToggleButton(midiIn, "auto_read", NOTE_AUTO_READ);
      mAutoReadLight = createRGBLight("auto_read_light", NOTE_AUTO_READ);
      mAutoReadButton.setBackgroundLight(mAutoReadLight);

      mAutoWriteButton = createToggleButton(midiIn, "auto_write", NOTE_AUTO_WRITE);
      mAutoWriteLight = createRGBLight("auto_write_light", NOTE_AUTO_WRITE);
      mAutoWriteButton.setBackgroundLight(mAutoWriteLight);

      mAutoTouchButton = createToggleButton(midiIn, "auto_touch", NOTE_AUTO_TOUCH);
      mAutoTouchLight = createRGBLight("auto_touch_light", NOTE_AUTO_TOUCH);
      mAutoTouchButton.setBackgroundLight(mAutoTouchLight);
   }

   private void createShiftButton(final MidiIn midiIn)
   {
      mShiftButton = createToggleButton(midiIn, "shift", NOTE_SHIFT);
      mShiftLight = createOnOffLight("shift_light", NOTE_SHIFT);
      mShiftButton.setBackgroundLight(mShiftLight);
   }

   /** Create a standard toggle button with note on/off matchers. */
   private HardwareButton createToggleButton(
      final MidiIn midiIn, final String id, final int note)
   {
      final HardwareButton button = mHardwareSurface.createHardwareButton(id);
      button.pressedAction().setActionMatcher(
         midiIn.createNoteOnActionMatcher(0, note));
      button.releasedAction().setActionMatcher(
         midiIn.createNoteOffActionMatcher(0, note));
      return button;
   }

   /** Create a standard on/off LED light. */
   private OnOffHardwareLight createOnOffLight(final String id, final int note)
   {
      final OnOffHardwareLight light = mHardwareSurface.createOnOffHardwareLight(id);
      light.onUpdateHardware(() ->
         mMidiOut.sendMidi(0x90, note, light.isOn().currentValue() ? 127 : 0));
      return light;
   }

   /** Create an RGB LED light using the 4-channel protocol. */
   private MultiStateHardwareLight createRGBLight(final String id, final int note)
   {
      final MultiStateHardwareLight light =
         mHardwareSurface.createMultiStateHardwareLight(id);

      light.state().onUpdateHardware(state -> {
         RGBLightState rgb = RGBLightState.OFF;
         if (state instanceof RGBLightState)
            rgb = (RGBLightState) state;

         for (int ch = 0; ch < 4; ch++)
         {
            mMidiOut.sendMidi(0x90 + ch, note, rgb.getChannelValue(ch));
         }
      });

      return light;
   }

   // ===========================================================================
   // Layer Creation & Binding
   // ===========================================================================

   private void createLayers()
   {
      // Always-active layers
      mDefaultLayer = new Layer(mLayers, "Default");
      mChannelStripLayer = new Layer(mLayers, "ChannelStrip");
      mAutoLayer = new Layer(mLayers, "Automation");
      mShiftLayer = new Layer(mLayers, "Shift");

      // Encoder mode layers (mutually exclusive)
      mPanLayer = new Layer(mLayers, "Pan");
      mChannelNavLayer = new Layer(mLayers, "ChannelNav");
      mScrollLayer = new Layer(mLayers, "Scroll");
      mMarkerLayer = new Layer(mLayers, "Marker");
      mEncoderModeGroup = new LayerGroup(mPanLayer, mChannelNavLayer, mScrollLayer, mMarkerLayer);

      // Fader override layers
      mMasterLayer = new Layer(mLayers, "Master");
      mDeviceLayer = new Layer(mLayers, "Device");
      mFlipLayer = new Layer(mLayers, "Flip");

      // Initialize bindings
      initDefaultLayer();
      initChannelStripLayer();
      initAutomationLayer();
      initShiftLayer();
      initPanLayer();
      initChannelNavLayer();
      initScrollLayer();
      initMarkerLayer();
      initMasterLayer();
      initDeviceLayer();
      initFlipLayer();

      // Activate defaults
      mDefaultLayer.activate();
      mChannelStripLayer.activate();
      mAutoLayer.activate();
      mEncoderModeGroup.setActiveLayer(mPanLayer);
   }

   // ---------------------------------------------------------------------------
   // Default Layer: Transport, default fader (volume), shift tracking
   // ---------------------------------------------------------------------------

   private void initDefaultLayer()
   {
      // Transport: Play
      bindPressed(mDefaultLayer, mPlayButton, () -> mTransport.play());
      mPlayLight.isOn().setValueSupplier(mTransport.isPlaying());

      // Transport: Stop
      bindPressed(mDefaultLayer, mStopButton, () -> mTransport.stop());
      mStopLight.isOn().setValueSupplier(() -> !mTransport.isPlaying().get());

      // Transport: Record
      bindPressed(mDefaultLayer, mRecordButton,
         () -> mTransport.isArrangerRecordEnabled().toggle());
      mRecordLight.isOn().setValueSupplier(mTransport.isArrangerRecordEnabled());

      // Transport: Loop
      bindPressed(mDefaultLayer, mLoopButton,
         () -> mTransport.isArrangerLoopEnabled().toggle());
      mLoopLight.isOn().setValueSupplier(mTransport.isArrangerLoopEnabled());

      // Transport: Click (metronome)
      bindPressed(mDefaultLayer, mClickButton,
         () -> mTransport.isMetronomeEnabled().toggle());
      mClickLight.isOn().setValueSupplier(mTransport.isMetronomeEnabled());

      // Transport: Rewind / FFwd
      bindPressed(mDefaultLayer, mRewindButton, () -> mTransport.rewind());
      bindPressed(mDefaultLayer, mFfwdButton, () -> mTransport.fastForward());
      mRewindLight.isOn().setValueSupplier(mRewindButton.isPressed());
      mFfwdLight.isOn().setValueSupplier(mFfwdButton.isPressed());

      // Shift tracking
      bindPressed(mDefaultLayer, mShiftButton, () -> {
         mIsShiftHeld = true;
         mShiftLayer.activate();
      });
      bindReleased(mDefaultLayer, mShiftButton, () -> {
         mIsShiftHeld = false;
         mShiftLayer.deactivate();
      });
      mShiftLight.isOn().setValueSupplier(mShiftButton.isPressed());

      // Fader is managed outside the layer system - see updateFaderHardwareBinding()
      // Motor fader feedback: observers for all potential fader targets (normalized 0.0-1.0)
      mCursorTrack.volume().value().addValueObserver(v -> { if (!mMasterModeActive && !mDeviceModeActive && !mFlipModeActive) onFaderTargetChanged(v); });
      mMasterTrack.volume().value().addValueObserver(v -> { if (mMasterModeActive) onFaderTargetChanged(v); });
      mRemoteControls.getParameter(0).value().addValueObserver(v -> { if (mDeviceModeActive) onFaderTargetChanged(v); });
      mCursorTrack.pan().value().addValueObserver(v -> { if (mFlipModeActive) onFaderTargetChanged(v); });
      // Bind fader to default target
      updateFaderHardwareBinding();

      // Encoder mode buttons
      bindPressed(mDefaultLayer, mPanButton, () -> {
         if (!mIsShiftHeld) mEncoderModeGroup.setActiveLayer(mPanLayer);
      });
      bindPressed(mDefaultLayer, mChannelButton,
         () -> mEncoderModeGroup.setActiveLayer(mChannelNavLayer));
      bindPressed(mDefaultLayer, mScrollButton,
         () -> mEncoderModeGroup.setActiveLayer(mScrollLayer));
      bindPressed(mDefaultLayer, mMarkerButton,
         () -> mEncoderModeGroup.setActiveLayer(mMarkerLayer));

      // Fader mode buttons
      bindPressed(mDefaultLayer, mMasterButton, () -> {
         if (!mIsShiftHeld)
         {
            mMasterModeActive = !mMasterModeActive;
            mMasterLayer.setIsActive(mMasterModeActive);
            if (mMasterModeActive)
            {
               mDeviceModeActive = false;
               mDeviceLayer.setIsActive(false);
            }
            updateFaderBinding();
         }
      });
      mMasterLight.isOn().setValueSupplier(() -> mMasterModeActive);

      bindPressed(mDefaultLayer, mSectionButton, () -> {
         if (!mIsShiftHeld)
         {
            mDeviceModeActive = !mDeviceModeActive;
            mDeviceLayer.setIsActive(mDeviceModeActive);
            if (mDeviceModeActive)
            {
               mMasterModeActive = false;
               mMasterLayer.setIsActive(false);
               // Suspend the encoder mode group so the device layer's
               // encoder binding is the only one active
               mSavedEncoderLayer = mEncoderModeGroup.getActiveLayer();
               mEncoderModeGroup.setActiveLayer(null);
            }
            else
            {
               // Restore the previous encoder mode
               mEncoderModeGroup.setActiveLayer(
                  mSavedEncoderLayer != null ? mSavedEncoderLayer : mPanLayer);
               mSavedEncoderLayer = null;
            }
            updateFaderBinding();
         }
      });
      mSectionLight.isOn().setValueSupplier(() -> mDeviceModeActive);
   }

   // ---------------------------------------------------------------------------
   // Channel Strip Layer: Solo, Mute, Arm, Bypass, Link, Prev/Next
   // ---------------------------------------------------------------------------

   private void initChannelStripLayer()
   {
      // Solo
      bindPressed(mChannelStripLayer, mSoloButton, () -> {
         if (!mIsShiftHeld) mCursorTrack.solo().toggle();
      });
      mSoloLight.isOn().setValueSupplier(mCursorTrack.solo());

      // Mute
      bindPressed(mChannelStripLayer, mMuteButton, () -> {
         if (!mIsShiftHeld) mCursorTrack.mute().toggle();
      });
      mMuteLight.isOn().setValueSupplier(mCursorTrack.mute());

      // Arm
      bindPressed(mChannelStripLayer, mArmButton, () -> mCursorTrack.arm().toggle());
      mArmLight.isOn().setValueSupplier(mCursorTrack.arm());

      // Bypass (device enable) - RGB: green = enabled, red = bypassed
      bindPressed(mChannelStripLayer, mBypassButton,
         () -> mCursorDevice.isEnabled().toggle());
      mBypassLight.state().setValueSupplier(() ->
         mCursorDevice.isEnabled().get() ? RGBLightState.GREEN : RGBLightState.RED);

      // Link (pin/unpin cursor device) - RGB: blue = pinned
      bindPressed(mChannelStripLayer, mLinkButton,
         () -> mCursorDevice.isPinned().toggle());
      mLinkLight.state().setValueSupplier(() ->
         mCursorDevice.isPinned().get() ? RGBLightState.BLUE : RGBLightState.OFF);

      // Prev / Next: navigate tracks (overridden by shift layer and device layer)
      bindPressed(mChannelStripLayer, mPrevButton, () -> {
         if (!mIsShiftHeld)
         {
            if (mDeviceModeActive)
               mCursorDevice.selectPrevious();
            else
               mCursorTrack.selectPrevious();
         }
      });
      bindPressed(mChannelStripLayer, mNextButton, () -> {
         if (!mIsShiftHeld)
         {
            if (mDeviceModeActive)
               mCursorDevice.selectNext();
            else
               mCursorTrack.selectNext();
         }
      });
   }

   // ---------------------------------------------------------------------------
   // Automation Layer: Read, Write, Touch + shift variants
   // ---------------------------------------------------------------------------

   private void initAutomationLayer()
   {
      // Read: toggle automation write enable (Shift: automation off)
      bindPressed(mAutoLayer, mAutoReadButton, () -> {
         if (mIsShiftHeld)
            mTransport.automationWriteMode().set("latch");
         else
            mTransport.toggleWriteArrangerAutomation();
      });
      mAutoReadLight.state().setValueSupplier(() -> {
         final String mode = mTransport.automationWriteMode().get();
         return "latch".equals(mode) ? RGBLightState.GREEN
            : RGBLightState.OFF;
      });

      // Write: write mode (Shift: trim)
      bindPressed(mAutoLayer, mAutoWriteButton, () -> {
         mTransport.automationWriteMode().set("write");
      });
      mAutoWriteLight.state().setValueSupplier(() -> {
         final String mode = mTransport.automationWriteMode().get();
         return "write".equals(mode) ? RGBLightState.RED : RGBLightState.OFF;
      });

      // Touch: touch mode (Shift: latch)
      bindPressed(mAutoLayer, mAutoTouchButton, () -> {
         if (mIsShiftHeld)
            mTransport.automationWriteMode().set("latch");
         else
            mTransport.automationWriteMode().set("touch");
      });
      mAutoTouchLight.state().setValueSupplier(() -> {
         final String mode = mTransport.automationWriteMode().get();
         if ("touch".equals(mode)) return RGBLightState.ORANGE;
         if ("latch".equals(mode)) return RGBLightState.YELLOW;
         return RGBLightState.OFF;
      });
   }

   // ---------------------------------------------------------------------------
   // Shift Layer: overlay actions while Shift is held
   // ---------------------------------------------------------------------------

   private void initShiftLayer()
   {
      // Shift+Prev = Undo
      bindPressed(mShiftLayer, mPrevButton, () -> mApplication.undo());

      // Shift+Next = Redo
      bindPressed(mShiftLayer, mNextButton, () -> mApplication.redo());

      // Shift+Solo = Clear all solos
      bindPressed(mShiftLayer, mSoloButton, () -> mCursorTrack.solo().set(false));

      // Shift+Mute = Clear all mutes
      bindPressed(mShiftLayer, mMuteButton, () -> mCursorTrack.mute().set(false));

      // Shift+Pan = Toggle fader flip mode
      bindPressed(mShiftLayer, mPanButton, () -> {
         mFlipModeActive = !mFlipModeActive;
         mFlipLayer.setIsActive(mFlipModeActive);
         updateFaderBinding();
      });

      // Shift+Master = Toggle detail editor panel
      bindPressed(mShiftLayer, mMasterButton,
         () -> mApplication.toggleNoteEditor());

      // Shift+Click = Toggle mixer panel
      bindPressed(mShiftLayer, mClickButton,
         () -> mApplication.toggleMixer());

      // Shift+Section = Toggle device panel
      bindPressed(mShiftLayer, mSectionButton,
         () -> mApplication.toggleDevices());

      // Shift+Marker = Toggle browser
      bindPressed(mShiftLayer, mMarkerButton,
         () -> mApplication.toggleBrowserVisibility());
   }

   // ---------------------------------------------------------------------------
   // Encoder Mode: Pan (default) - encoder controls selected track pan
   // ---------------------------------------------------------------------------

   private void initPanLayer()
   {
      bindEncoder(mPanLayer, mCursorTrack.pan().value());
      bindPressed(mPanLayer, mEncoderPush, () -> mCursorTrack.pan().reset());
      mPanLight.state().setValueSupplier(() ->
         mPanLayer.isActive() ? RGBLightState.GREEN : RGBLightState.OFF);
   }

   // ---------------------------------------------------------------------------
   // Encoder Mode: Channel - encoder navigates tracks
   // ---------------------------------------------------------------------------

   private void initChannelNavLayer()
   {
      bindEncoderStepping(mChannelNavLayer,
         () -> mCursorTrack.selectPrevious(),
         () -> mCursorTrack.selectNext());
      mChannelLight.state().setValueSupplier(() ->
         mChannelNavLayer.isActive() ? RGBLightState.CYAN : RGBLightState.OFF);
   }

   // ---------------------------------------------------------------------------
   // Encoder Mode: Scroll - encoder scrolls arranger timeline
   // ---------------------------------------------------------------------------

   private void initScrollLayer()
   {
      bindEncoderStepping(mScrollLayer,
         () -> mTransport.incPosition(-1, true),
         () -> mTransport.incPosition(1, true));
      bindPressed(mScrollLayer, mEncoderPush,
         () -> mTransport.play());
      mScrollLight.state().setValueSupplier(() ->
         mScrollLayer.isActive() ? RGBLightState.YELLOW : RGBLightState.OFF);
   }

   // ---------------------------------------------------------------------------
   // Encoder Mode: Marker - encoder jumps between cue markers
   // ---------------------------------------------------------------------------

   private void initMarkerLayer()
   {
      bindEncoderStepping(mMarkerLayer,
         () -> mTransport.jumpToPreviousCueMarker(),
         () -> mTransport.jumpToNextCueMarker());

      mMarkerLight.state().setValueSupplier(() ->
         mMarkerLayer.isActive() ? RGBLightState.MAGENTA : RGBLightState.OFF);
   }

   // ---------------------------------------------------------------------------
   // Fader Mode: Master - fader controls master volume
   // ---------------------------------------------------------------------------

   private void initMasterLayer()
   {
      // Motor feedback handled by central observer in initDefaultLayer
   }

   // ---------------------------------------------------------------------------
   // Fader Mode: Device - fader controls device parameter, encoder scrolls params
   // ---------------------------------------------------------------------------

   private void initDeviceLayer()
   {
      final RemoteControl param = mRemoteControls.getParameter(0);
      // Motor feedback handled by central observer in initDefaultLayer

      bindEncoderStepping(mDeviceLayer,
         () -> mRemoteControls.selectPrevious(),
         () -> mRemoteControls.selectNext());
      bindPressed(mDeviceLayer, mEncoderPush, () -> param.reset());
   }

   // ---------------------------------------------------------------------------
   // Fader Mode: Flip - fader controls pan, encoder controls volume
   // ---------------------------------------------------------------------------

   private void initFlipLayer()
   {
      // Motor feedback handled by central observer in initDefaultLayer
      bindEncoder(mFlipLayer, mCursorTrack.volume().value());
   }

   // ===========================================================================
   // Fader Binding Management
   // ===========================================================================

   /**
    * Returns the currently active fader target based on mode state.
    */
   private SettableRangedValue getActiveFaderTarget()
   {
      if (mDeviceModeActive) return mRemoteControls.getParameter(0).value();
      if (mMasterModeActive) return mMasterTrack.volume().value();
      if (mFlipModeActive) return mCursorTrack.pan().value();
      return mCursorTrack.volume().value();
   }

   /**
    * Rebind the fader hardware to the currently active target.
    * Called whenever the fader mode changes.
    */
   private void updateFaderBinding()
   {
      updateFaderHardwareBinding();
   }

   /**
    * Creates and manages the hardware binding for the fader to send values to the DAW.
    * Removes any existing binding and creates a new one for the currently active target.
    * Motor fader feedback is handled separately by the value observers.
    */
   private void updateFaderHardwareBinding()
   {
      final SettableRangedValue target = getActiveFaderTarget();
      
      // Remove old binding if it exists
      if (mFaderBinding != null)
      {
         mFaderBinding.removeBinding();
         mFaderBinding = null;
      }
      
      // Create new binding for the active target
      // This allows the fader to send values to the DAW when moved
      if (target != null)
      {
         mFaderBinding = mFader.addBindingWithRange(target, 0, 1);
      }
      
      // Also update motor fader position to show current target value
      final int value14bit = (int) (target.get() * 16383);
      sendFaderPosition(value14bit);
   }

   /** Called when the active fader target value changes externally. */
   private void onFaderTargetChanged(final double normalizedValue)
   {
      // Only update motor if fader is not being touched
      if (!mFaderTouch.isPressed().get())
      {
         final int value14bit = (int) (normalizedValue * 16383);
         sendFaderPosition(value14bit);
      }
   }

   /** Send pitch bend to move the motor fader. */
   private void sendFaderPosition(final int value14bit)
   {
      final int lsb = value14bit & 0x7F;
      final int msb = (value14bit >> 7) & 0x7F;
      mMidiOut.sendMidi(0xE0, lsb, msb);
   }

   // ===========================================================================
   // Binding Helpers
   // ===========================================================================

   /**
    * Bind a button press to a Runnable action within a layer.
    * The HardwareActionBindable is created immediately (during init),
    * and only the binding is added/removed at runtime.
    */
   private void bindPressed(final Layer layer, final HardwareButton button,
      final Runnable action)
   {
      final HardwareActionBindable actionBindable =
         getHost().createAction(action, () -> "");
      layer.addBinding(new com.smetoyer.bitwig.faderport2.framework.Binding()
      {
         private HardwareActionBinding mBinding;

         @Override
         protected void onActivate()
         {
            mBinding = button.pressedAction().addBinding(actionBindable);
         }

         @Override
         protected void onDeactivate()
         {
            if (mBinding != null)
            {
               mBinding.removeBinding();
               mBinding = null;
            }
         }
      });
   }

   /**
    * Bind a button release to a Runnable action within a layer.
    */
   private void bindReleased(final Layer layer, final HardwareButton button,
      final Runnable action)
   {
      final HardwareActionBindable actionBindable =
         getHost().createAction(action, () -> "");
      layer.addBinding(new com.smetoyer.bitwig.faderport2.framework.Binding()
      {
         private HardwareActionBinding mBinding;

         @Override
         protected void onActivate()
         {
            mBinding = button.releasedAction().addBinding(actionBindable);
         }

         @Override
         protected void onDeactivate()
         {
            if (mBinding != null)
            {
               mBinding.removeBinding();
               mBinding = null;
            }
         }
      });
   }

   /**
    * Bind the encoder (relative control) to a ranged value within a layer.
    */
   private void bindEncoder(final Layer layer, final SettableRangedValue target)
   {
      layer.addBinding(new com.smetoyer.bitwig.faderport2.framework.Binding()
      {
         private RelativeHardwareControlBinding mBinding;

         @Override
         protected void onActivate()
         {
            mBinding = mEncoder.addBindingWithRange(target, 0, 1);
         }

         @Override
         protected void onDeactivate()
         {
            if (mBinding != null)
            {
               mBinding.removeBinding();
               mBinding = null;
            }
         }
      });
   }

   /**
    * Bind the encoder for stepping through discrete items
    * (prev/next track, prev/next parameter, etc.).
    * All Bitwig objects are created during init; only bindings change at runtime.
    */
   private void bindEncoderStepping(final Layer layer,
      final Runnable decrementAction, final Runnable incrementAction)
   {
      final ControllerHost host = getHost();
      // The API's first parameter fires on positive (rightward) encoder movement,
      // so increment goes first to make right-turn = forward/next/down.
      final RelativeHardwarControlBindable stepTarget =
         host.createRelativeHardwareControlStepTarget(
            host.createAction(incrementAction, () -> ""),
            host.createAction(decrementAction, () -> ""));

      layer.addBinding(new com.smetoyer.bitwig.faderport2.framework.Binding()
      {
         private RelativeHardwareControlBinding mBinding;

         @Override
         protected void onActivate()
         {
            mBinding = stepTarget.addBindingWithSensitivity(mEncoder, 1.0);
         }

         @Override
         protected void onDeactivate()
         {
            if (mBinding != null)
            {
               mBinding.removeBinding();
               mBinding = null;
            }
         }
      });
   }

   // ===========================================================================
   // Keep-Alive Timer
   // ===========================================================================

   private void startKeepAlive()
   {
      getHost().scheduleTask(this::sendKeepAlive, 1000);
   }

   private void sendKeepAlive()
   {
      mMidiOut.sendMidi(0xA0, 0x00, 0x00);
      getHost().scheduleTask(this::sendKeepAlive, 1000);
   }

   // ===========================================================================
   // Extension Lifecycle
   // ===========================================================================

   @Override
   public void exit()
   {
      // Turn off all standard LEDs
      final int[] stdNotes = {
         NOTE_ARM, NOTE_SOLO, NOTE_MUTE, NOTE_MASTER, NOTE_CLICK,
         NOTE_SECTION, NOTE_LOOP, NOTE_REWIND, NOTE_FFWD,
         NOTE_STOP, NOTE_PLAY, NOTE_RECORD, NOTE_SHIFT
      };
      for (final int note : stdNotes)
      {
         mMidiOut.sendMidi(0x90, note, 0);
      }

      // Turn off all RGB LEDs
      for (final int note : RGB_BUTTONS)
      {
         for (int ch = 0; ch < 4; ch++)
         {
            mMidiOut.sendMidi(0x90 + ch, note, 0);
         }
      }

      // Move fader to bottom
      sendFaderPosition(0);

      getHost().println("FaderPort v2 exited.");
   }

   @Override
   public void flush()
   {
      mHardwareSurface.updateHardware();
   }
}
