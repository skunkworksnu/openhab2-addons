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
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.PointType;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.netatmo.config.NetatmoChildConfiguration;
import org.openhab.binding.netatmo.config.NetatmoParentConfiguration;
import org.openhab.binding.netatmo.internal.ChannelTypeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.swagger.client.model.NAPlace;
import io.swagger.client.model.NAUserAdministrative;

/**
 * {@link NetatmoDeviceHandler} is the handler for a given
 * device accessed through the Netatmo Bridge
 *
 * @author Gaël L'hopital - Initial contribution OH2 version
 * @author Ing. Peter Weiss
 *
 */
public abstract class NetatmoDeviceHandler<X extends NetatmoParentConfiguration, Y extends Object>
        extends AbstractNetatmoThingHandler<X, Y> {

    protected Y device;
    protected NAUserAdministrative userAdministrative;
    protected Map<String, Object> childs = new HashMap<>();
    private Logger logger = LoggerFactory.getLogger(NetatmoDeviceHandler.class);
    private ScheduledFuture<?> refreshJob;

    public NetatmoDeviceHandler(Thing thing, Class<X> configurationClass) {
        super(thing, configurationClass);
    }

    @Override
    public void initialize() {
        super.initialize();

        if (getBridge() != null) {
            logger.debug("Initializing Netatmo Device with id '{}'", this.getClass().toString(), configuration.getId());
            if (getBridge().getStatus() == ThingStatus.ONLINE) {
                logger.debug("setting device '{}' online", configuration.getId());
                updateStatus(ThingStatus.ONLINE);
                logger.debug("scheduling update channel thread to run every {} ms", configuration.refreshInterval);
                refreshJob = scheduler.scheduleWithFixedDelay(new Runnable() {
                    @Override
                    public void run() {
                        updateChannels(configuration.getId());
                    }
                }, 1, configuration.refreshInterval.longValue(), TimeUnit.MILLISECONDS);
            } else {
                logger.debug("setting device '{}' offline (bridge or thing offline)", configuration.getId());
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.BRIDGE_OFFLINE);
            }
        } else {
            logger.debug("setting device '{}' offline (bridge == null)", configuration.getId());
            updateStatus(ThingStatus.OFFLINE);
        }
    }

    @Override
    public void dispose() {
        logger.debug("Running dispose()");
        if (refreshJob != null && !refreshJob.isCancelled()) {
            refreshJob.cancel(true);
            refreshJob = null;
        }
    }

    protected abstract Y updateReadings(String equipmentId);

    @Override
    protected void updateChannels(String equipmentId) {
        logger.debug("Trying to update channels on device {}", equipmentId);
        childs.clear();
        try {
            Y tmpDevice = updateReadings(equipmentId);
            if (tmpDevice != null) {
                logger.debug("Successfully updated device readings! Now updating channels");
                this.device = tmpDevice;
                super.updateChannels(equipmentId);
                updateChildModules(equipmentId);
            }
        } catch (Exception e) {
            logger.error("Exception when trying to update channels: {} on equipment {}", e.getMessage(), equipmentId);
        }
    }

    @Override
    protected State getNAThingProperty(String channelId) {
        try {
            switch (channelId) {
                case CHANNEL_LAST_STATUS_STORE:
                    Method getLastStatusStore = device.getClass().getMethod("getLastStatusStore");
                    Integer lastStatusStore = (Integer) getLastStatusStore.invoke(device);
                    return ChannelTypeUtils.toDateTimeType(lastStatusStore);
                case CHANNEL_LOCATION:
                    Method getPlace = device.getClass().getMethod("getPlace");
                    NAPlace place = (NAPlace) getPlace.invoke(device);
                    PointType point = new PointType(new DecimalType(place.getLocation().get(1)),
                            new DecimalType(place.getLocation().get(0)));
                    if (place.getAltitude() != null) {
                        point.setAltitude(new DecimalType(place.getAltitude()));
                    }
                    return point;
                case CHANNEL_WIFI_STATUS:
                    Method getWifiStatus = device.getClass().getMethod("getWifiStatus");
                    Integer wifiStatus = (Integer) getWifiStatus.invoke(device);
                    return new DecimalType(getSignalStrength(wifiStatus));
                case CHANNEL_UNIT:
                    return new DecimalType(userAdministrative.getUnit());
                default:
                    return super.getNAThingProperty(channelId);
            }
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException e) {
            logger.error("The device has no method to access {} property ", channelId.toString());
            return UnDefType.NULL;
        }

    }

    private void updateChildModules(String equipmentId) {
        logger.debug("Updating child modules of {}", equipmentId);
        for (Thing handler : getBridge().getThings()) {
            ThingHandler thingHandler = handler.getHandler();
            if (thingHandler instanceof NetatmoModuleHandler) {
                @SuppressWarnings("unchecked")
                NetatmoModuleHandler<NetatmoChildConfiguration, ?> moduleHandler = (NetatmoModuleHandler<NetatmoChildConfiguration, ?>) thingHandler;
                NetatmoChildConfiguration moduleConfiguration = moduleHandler.configuration;
                String parentId = moduleConfiguration.getParentId();
                if (equipmentId.equalsIgnoreCase(parentId)) {
                    String childId = moduleHandler.configuration.getId();
                    Object childValue = childs.get(childId);
                    if (childValue != null) {
                        logger.debug("Updating child module {}", childId);
                        moduleHandler.updateChannels(childValue);
                    } else {
                        logger.error("Child was not found in Netatmo answer for parent {} child {}", parentId, childId);
                    }
                }
            }
        }
    }

}
