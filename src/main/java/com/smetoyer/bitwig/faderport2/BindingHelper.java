package com.smetoyer.bitwig.faderport2;

import com.bitwig.extension.controller.api.*;
import com.bitwig.extension.controller.api.ControllerHost;
import com.smetoyer.bitwig.faderport2.framework.Layer;

/**
 * Small helper to centralize binding creation so `FaderPort2Extension` stays smaller.
 */
public class BindingHelper {
    private final ControllerHost host;
    private final RelativeHardwareKnob encoder;

    public BindingHelper(final ControllerHost host, final RelativeHardwareKnob encoder) {
        this.host = host;
        this.encoder = encoder;
    }

    public void bindPressed(final Layer layer, final HardwareButton button, final Runnable action) {
        final HardwareActionBindable actionBindable = host.createAction(action, () -> "");
        layer.addBinding(new com.smetoyer.bitwig.faderport2.framework.Binding() {
            private HardwareActionBinding mBinding;

            @Override
            protected void onActivate() {
                mBinding = button.pressedAction().addBinding(actionBindable);
            }

            @Override
            protected void onDeactivate() {
                if (mBinding != null) {
                    mBinding.removeBinding();
                    mBinding = null;
                }
            }
        });
    }

    public void bindReleased(final Layer layer, final HardwareButton button, final Runnable action) {
        final HardwareActionBindable actionBindable = host.createAction(action, () -> "");
        layer.addBinding(new com.smetoyer.bitwig.faderport2.framework.Binding() {
            private HardwareActionBinding mBinding;

            @Override
            protected void onActivate() {
                mBinding = button.releasedAction().addBinding(actionBindable);
            }

            @Override
            protected void onDeactivate() {
                if (mBinding != null) {
                    mBinding.removeBinding();
                    mBinding = null;
                }
            }
        });
    }

    public void bindEncoder(final Layer layer, final SettableRangedValue target) {
        layer.addBinding(new com.smetoyer.bitwig.faderport2.framework.Binding() {
            private RelativeHardwareControlBinding mBinding;

            @Override
            protected void onActivate() {
                mBinding = encoder.addBindingWithRange(target, 0, 1);
            }

            @Override
            protected void onDeactivate() {
                if (mBinding != null) {
                    mBinding.removeBinding();
                    mBinding = null;
                }
            }
        });
    }

    public void bindEncoderStepping(final Layer layer, final Runnable decrementAction, final Runnable incrementAction) {
        // API's first parameter is positive/right movement -> increment first
        layer.addBinding(new com.smetoyer.bitwig.faderport2.framework.Binding() {
            private RelativeHardwareControlBinding mBinding;

            @Override
            protected void onActivate() {
                mBinding = host.createRelativeHardwareControlStepTarget(
                    host.createAction(incrementAction, () -> ""),
                    host.createAction(decrementAction, () -> "")
                ).addBindingWithSensitivity(encoder, 1.0);
            }

            @Override
            protected void onDeactivate() {
                if (mBinding != null) {
                    mBinding.removeBinding();
                    mBinding = null;
                }
            }
        });
    }
}
