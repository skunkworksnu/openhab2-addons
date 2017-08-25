/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.netatmo.handler;

import static org.openhab.binding.netatmo.NetatmoBindingConstants.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.netatmo.config.NetatmoChildConfiguration;
import org.openhab.binding.netatmo.config.NetatmoParentConfiguration;
import org.openhab.binding.netatmo.internal.ChannelTypeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link NetatmoModuleHandler} is the handler for a given
 * module device accessed through the Netatmo Device
 *
 * @author Gaël L'hopital - Initial contribution OH2 version
 *
 */
public abstract class NetatmoModuleHandler<X extends NetatmoChildConfiguration, Y>
        extends AbstractNetatmoThingHandler<X, Y> {
    private Logger logger = LoggerFactory.getLogger(NetatmoModuleHandler.class);
    private int batteryMin = 0;
    private int batteryLow = 0;
    private int batteryMax = 1;
    protected Y module;

    protected NetatmoModuleHandler(Thing thing, Class<X> configurationClass) {
        super(thing, configurationClass);
    }

    @Override
    public void initialize() {
        super.initialize();
    }

    private void initializeBatteryLevels() {
        this.batteryMax = getIntegerProperty(PROPERTY_BATTERY_MAX);
        this.batteryMin = getIntegerProperty(PROPERTY_BATTERY_MIN);
        this.batteryLow = getIntegerProperty(PROPERTY_BATTERY_LOW);
    }

    // when batteries are freshly changed, API may return a value superior to batteryMax !
    private int getBatteryPercent() throws NoSuchMethodException, SecurityException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException {
        if (batteryMax == 1) {
            initializeBatteryLevels();
        }

        Method getBatteryVp = module.getClass().getMethod("getBatteryVp");
        Integer value = (Integer) getBatteryVp.invoke(module);

        int correctedVp = Math.min(value.intValue(), batteryMax);
        return (100 * (correctedVp - batteryMin) / (batteryMax - batteryMin));
    }

    @Override
    protected State getNAThingProperty(String channelId) {
        if (module != null) {
            try {

                switch (channelId) {
                    case CHANNEL_BATTERY_LEVEL:
                        return ChannelTypeUtils.toDecimalType(getBatteryPercent());
                    case CHANNEL_LOW_BATTERY:
                        Method getBatteryVp;
                        getBatteryVp = module.getClass().getMethod("getBatteryVp");

                        Integer value = (Integer) getBatteryVp.invoke(module);
                        return value.intValue() < batteryLow ? OnOffType.ON : OnOffType.OFF;
                    case CHANNEL_LAST_MESSAGE:
                        Method getLastMessage = module.getClass().getMethod("getLastMessage");
                        Integer lastMessage = (Integer) getLastMessage.invoke(module);
                        return ChannelTypeUtils.toDateTimeType(lastMessage);
                    case CHANNEL_RF_STATUS:
                        Method getRfStatus = module.getClass().getMethod("getRfStatus");
                        Integer rfStatus = (Integer) getRfStatus.invoke(module);
                        return ChannelTypeUtils.toDecimalType(getSignalStrength(rfStatus));

                }
            } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
                    | InvocationTargetException e) {
                logger.error("The module has no method to access {} property ", channelId.toString());
                return UnDefType.NULL;
            }

        }
        return super.getNAThingProperty(channelId);
    }

    @SuppressWarnings("unchecked")
    protected void updateChannels(Object module) {
        this.module = (Y) module;
        super.updateChannels(configuration.getParentId());
    }

    protected void requestParentRefresh() {
        logger.debug("Updating parent modules of {}", configuration.getId());
        for (Thing thing : getBridge().getThings()) {
            ThingHandler thingHandler = thing.getHandler();
            if (thingHandler instanceof NetatmoDeviceHandler) {
                @SuppressWarnings("unchecked")
                NetatmoDeviceHandler<NetatmoParentConfiguration, ?> deviceHandler = (NetatmoDeviceHandler<NetatmoParentConfiguration, ?>) thingHandler;
                NetatmoParentConfiguration deviceConfiguration = deviceHandler.configuration;
                if (deviceConfiguration.getId().equalsIgnoreCase(configuration.getParentId())) {
                    // I'm your father Luke
                    thingHandler.handleCommand(null, RefreshType.REFRESH);
                }
            }
        }
    }

}
