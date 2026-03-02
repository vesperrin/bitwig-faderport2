package com.smetoyer.bitwig.faderport2;

import com.bitwig.extension.api.PlatformType;
import com.bitwig.extension.controller.AutoDetectionMidiPortNamesList;
import com.bitwig.extension.controller.ControllerExtensionDefinition;
import com.bitwig.extension.controller.api.ControllerHost;

import java.util.UUID;

public class FaderPort2Definition extends ControllerExtensionDefinition
{
   private static final UUID DRIVER_ID =
      UUID.fromString("d3f7a1b2-c4e5-4f6a-8b9c-0d1e2f3a4b5c");

   @Override
   public String getName()
   {
      return "FaderPort 2";
   }

   @Override
   public String getAuthor()
   {
      return "Scott Metoyer";
   }

   @Override
   public String getVersion()
   {
      return "1.0.0";
   }

   @Override
   public UUID getId()
   {
      return DRIVER_ID;
   }

   @Override
   public String getHardwareVendor()
   {
      return "PreSonus";
   }

   @Override
   public String getHardwareModel()
   {
      return "FaderPort v2";
   }

   @Override
   public int getRequiredAPIVersion()
   {
      return 18;
   }

   @Override
   public int getNumMidiInPorts()
   {
      return 1;
   }

   @Override
   public int getNumMidiOutPorts()
   {
      return 1;
   }

   @Override
   public void listAutoDetectionMidiPortNames(
      final AutoDetectionMidiPortNamesList list,
      final PlatformType platformType)
   {
      switch (platformType)
      {
         case WINDOWS:
            list.add(new String[]{"PreSonus FP2"}, new String[]{"PreSonus FP2"});
            break;
         case MAC:
            list.add(new String[]{"PreSonus FP2"}, new String[]{"PreSonus FP2"});
            break;
         case LINUX:
            list.add(new String[]{"PreSonus FP2 MIDI 1"}, new String[]{"PreSonus FP2 MIDI 1"});
            break;
      }
   }

   @Override
   public FaderPort2Extension createInstance(final ControllerHost host)
   {
      return new FaderPort2Extension(this, host);
   }
}
