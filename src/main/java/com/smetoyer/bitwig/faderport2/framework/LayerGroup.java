package com.smetoyer.bitwig.faderport2.framework;

/**
 * Ensures mutual exclusion among a set of layers. When one layer in the group
 * is activated, all others are deactivated. This is used for encoder modes
 * (Pan, Channel, Scroll, Marker) where only one can be active at a time.
 */
public class LayerGroup
{
   private final Layer[] mLayers;
   private Layer mActiveLayer;

   public LayerGroup(final Layer... layers)
   {
      mLayers = layers;
   }

   /**
    * Activates the given layer and deactivates all others in the group.
    * If the layer is already active, this is a no-op.
    */
   /**
    * Activates the given layer and deactivates all others in the group.
    * Pass null to deactivate all layers without activating any.
    * If the layer is already active, this is a no-op.
    */
   public void setActiveLayer(final Layer layer)
   {
      if (mActiveLayer == layer)
         return;

      for (final Layer l : mLayers)
      {
         if (l != layer)
            l.deactivate();
      }

      if (layer != null)
         layer.activate();

      mActiveLayer = layer;
   }

   public Layer getActiveLayer()
   {
      return mActiveLayer;
   }

   public Layer[] getLayers()
   {
      return mLayers;
   }
}
