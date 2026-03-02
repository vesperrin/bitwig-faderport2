package com.smetoyer.bitwig.faderport2.framework;

import java.util.ArrayList;
import java.util.List;

/**
 * A named group of bindings that can be activated and deactivated together.
 * When active, all contained bindings are applied to the hardware surface.
 * When deactivated, all bindings are removed.
 */
public class Layer
{
   private final Layers mLayers;
   private final String mName;
   private final List<Binding> mBindings = new ArrayList<>();
   private boolean mIsActive = false;

   public Layer(final Layers layers, final String name)
   {
      mLayers = layers;
      mName = name;
      layers.addLayer(this);
   }

   public String getName()
   {
      return mName;
   }

   public void addBinding(final Binding binding)
   {
      mBindings.add(binding);
      if (mIsActive)
      {
         binding.activate();
      }
   }

   public void activate()
   {
      if (!mIsActive)
      {
         mIsActive = true;
         for (final Binding binding : mBindings)
         {
            binding.activate();
         }
      }
   }

   public void deactivate()
   {
      if (mIsActive)
      {
         mIsActive = false;
         for (final Binding binding : mBindings)
         {
            binding.deactivate();
         }
      }
   }

   public boolean isActive()
   {
      return mIsActive;
   }

   public void toggleIsActive()
   {
      if (mIsActive)
         deactivate();
      else
         activate();
   }

   public void setIsActive(final boolean active)
   {
      if (active)
         activate();
      else
         deactivate();
   }

   public Layers getLayers()
   {
      return mLayers;
   }
}
