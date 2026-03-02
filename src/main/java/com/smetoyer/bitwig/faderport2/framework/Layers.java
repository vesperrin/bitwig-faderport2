package com.smetoyer.bitwig.faderport2.framework;

import com.bitwig.extension.controller.ControllerExtension;

import java.util.ArrayList;
import java.util.List;

/**
 * Registry of all layers in the extension. Provides a reference to the host
 * for creating actions and scheduling tasks.
 */
public class Layers
{
   private final ControllerExtension mExtension;
   private final List<Layer> mLayers = new ArrayList<>();

   public Layers(final ControllerExtension extension)
   {
      mExtension = extension;
   }

   public void addLayer(final Layer layer)
   {
      mLayers.add(layer);
   }

   public ControllerExtension getExtension()
   {
      return mExtension;
   }

   public List<Layer> getLayers()
   {
      return mLayers;
   }
}
