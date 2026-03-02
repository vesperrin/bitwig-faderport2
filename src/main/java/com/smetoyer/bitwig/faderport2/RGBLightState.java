package com.smetoyer.bitwig.faderport2;

import com.bitwig.extension.api.Color;
import com.bitwig.extension.controller.api.HardwareLightVisualState;
import com.bitwig.extension.controller.api.InternalHardwareLightState;

import java.util.Objects;

/**
 * Encapsulates the 4-channel LED protocol for RGB buttons on the FaderPort v2.
 * The FP2 uses four MIDI channels for each RGB-capable button:
 *   Channel 0 (0x90): On/Off (127 = on, 0 = off)
 *   Channel 1 (0x91): Red   (0-127)
 *   Channel 2 (0x92): Green (0-127)
 *   Channel 3 (0x93): Blue  (0-127)
 */
public class RGBLightState extends InternalHardwareLightState
{
   public static final RGBLightState OFF       = new RGBLightState(0, 0, 0);
   public static final RGBLightState RED       = new RGBLightState(127, 0, 0);
   public static final RGBLightState GREEN     = new RGBLightState(0, 127, 0);
   public static final RGBLightState BLUE      = new RGBLightState(0, 0, 127);
   public static final RGBLightState YELLOW    = new RGBLightState(127, 127, 0);
   public static final RGBLightState CYAN      = new RGBLightState(0, 127, 127);
   public static final RGBLightState MAGENTA   = new RGBLightState(127, 0, 127);
   public static final RGBLightState WHITE     = new RGBLightState(127, 127, 127);
   public static final RGBLightState DIM_WHITE = new RGBLightState(40, 40, 40);
   public static final RGBLightState ORANGE    = new RGBLightState(127, 64, 0);

   private final int mRed;
   private final int mGreen;
   private final int mBlue;

   public RGBLightState(final int red, final int green, final int blue)
   {
      mRed = Math.max(0, Math.min(red, 127));
      mGreen = Math.max(0, Math.min(green, 127));
      mBlue = Math.max(0, Math.min(blue, 127));
   }

   public static RGBLightState fromBitwigColor(final Color color)
   {
      return new RGBLightState(
         clamp7bit(color.getRed()),
         clamp7bit(color.getGreen()),
         clamp7bit(color.getBlue()));
   }

   /** Returns the MIDI value to send on the given channel index (0-3). */
   public int getChannelValue(final int channel)
   {
      switch (channel)
      {
         case 0: return isOn() ? 127 : 0;
         case 1: return mRed;
         case 2: return mGreen;
         case 3: return mBlue;
         default: return 0;
      }
   }

   public boolean isOn()
   {
      return mRed > 0 || mGreen > 0 || mBlue > 0;
   }

   public int getRed()   { return mRed; }
   public int getGreen() { return mGreen; }
   public int getBlue()  { return mBlue; }

   @Override
   public HardwareLightVisualState getVisualState()
   {
      if (!isOn())
         return HardwareLightVisualState.createForColor(Color.blackColor());

      return HardwareLightVisualState.createForColor(
         Color.fromRGB(mRed / 127.0, mGreen / 127.0, mBlue / 127.0));
   }

   @Override
   public boolean equals(final Object o)
   {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      final RGBLightState that = (RGBLightState) o;
      return mRed == that.mRed && mGreen == that.mGreen && mBlue == that.mBlue;
   }

   @Override
   public int hashCode()
   {
      return Objects.hash(mRed, mGreen, mBlue);
   }

   private static int clamp7bit(final double value)
   {
      return Math.max(0, Math.min((int) (127.0 * value), 127));
   }
}
