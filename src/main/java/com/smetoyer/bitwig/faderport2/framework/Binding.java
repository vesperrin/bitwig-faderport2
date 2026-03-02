package com.smetoyer.bitwig.faderport2.framework;

/**
 * Abstract base for a hardware-to-parameter binding that can be activated and deactivated.
 * Subclasses implement the actual binding/unbinding logic using the Bitwig API.
 */
public abstract class Binding
{
   private boolean mIsActive = false;

   /** Called when the owning layer is activated. Creates the actual hardware binding. */
   protected abstract void onActivate();

   /** Called when the owning layer is deactivated. Removes the hardware binding. */
   protected abstract void onDeactivate();

   public final void activate()
   {
      if (!mIsActive)
      {
         mIsActive = true;
         onActivate();
      }
   }

   public final void deactivate()
   {
      if (mIsActive)
      {
         mIsActive = false;
         onDeactivate();
      }
   }

   public boolean isActive()
   {
      return mIsActive;
   }
}
