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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.netatmo.config.NetatmoParentConfiguration;
import org.openhab.binding.netatmo.internal.ChannelTypeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.swagger.client.CollectionFormats.CSVParams;
import io.swagger.client.model.NAMeasureResponse;

/**
 * {@link AbstractNetatmoThingHandler} is the abstract class that handles
 * common behaviors of all netatmo things
 *
 * @author Gaël L'hopital - Initial contribution
 * @author Ing. Peter Weiss - Welcome camera implementation
 *
 */
abstract class AbstractNetatmoThingHandler<X extends NetatmoParentConfiguration, Y> extends BaseThingHandler {
    protected static Logger logger = LoggerFactory.getLogger(AbstractNetatmoThingHandler.class);

    final Class<X> configurationClass;
    public X configuration = null;

    private List<Integer> signalThresholds = null;
    private List<String> measuredChannels = new ArrayList<>();
    protected NAMeasureResponse measures = null;

    AbstractNetatmoThingHandler(Thing thing, Class<X> configurationClass) {
        super(thing);
        this.configurationClass = configurationClass;
    }

    @Override
    public void initialize() {
        configuration = this.getConfigAs(configurationClass);
        // getThing().getChannels().forEach(channel -> addChannelToMeasures(channel.getUID()));
        super.initialize();
    }

    /*
     * If this channel value is provided as a measure, then add it
     * in the getMeasure parameter list
     */
    protected void addChannelToMeasures(ChannelUID channelUID) {
        if (MEASURABLE_CHANNELS.contains(channelUID.getId())) {
            measuredChannels.add(channelUID.getId());
        }
    }

    /*
     * If this channel value is provided as a measure, then delete
     * it in the getMeasure parameter list
     */
    protected void removeChannelFromMeasures(ChannelUID channelUID) {
        String channel = channelUID.getId();
        measuredChannels.remove(channel);
    }

    protected CSVParams getMeasuresAsCsv() {
        if (measuredChannels.size() > 0) {
            return new CSVParams(measuredChannels);
        } else {
            return null;
        }
    }

    protected Float getMeasureValue(String channelId) {
        int index = measuredChannels.indexOf(channelId);
        return (index != -1) ? measures.getBody().get(0).getValue().get(0).get(index) : null;
    }

    @Override
    public void channelUnlinked(ChannelUID channelUID) {
        super.channelUnlinked(channelUID);
        removeChannelFromMeasures(channelUID);
    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        super.channelLinked(channelUID);
        addChannelToMeasures(channelUID);
    }

    // Protects property loading from missing entries Issue 1137
    protected String getProperty(String propertyName) {
        final Map<String, String> properties = thing.getProperties();
        String value = properties.get(propertyName);
        if (value == null) {
            logger.warn("Property named {} was not found", propertyName);
        }
        return value;
    }

    protected Integer getIntegerProperty(String propertyName) {
        String value = getProperty(propertyName);
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            logger.warn("Invalid property {} value {}", propertyName, value);
            return null;
        }
    }

    protected void updateChannels(String equipmentId) {
        logger.debug("Updating channels");

        for (Channel channel : getThing().getChannels()) {

            String channelId = channel.getUID().getId();
            logger.debug("Updating channel {}", channelId);
            State state = getNAThingProperty(channelId);
            if (state != null) {
                updateState(channel.getUID(), state);
            }
        }

        updateStatus(ThingStatus.ONLINE);
    }

    public NetatmoBridgeHandler getBridgeHandler() {
        return (NetatmoBridgeHandler) this.getBridge().getHandler();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command == RefreshType.REFRESH) {
            logger.debug("Refreshing {}", channelUID);
            updateChannels(configuration.getId());
        }
    }

    protected State getNAThingProperty(String channelId) {
        return (measures != null) ? ChannelTypeUtils.toDecimalType(getMeasureValue(channelId)) : UnDefType.NULL;

    }

    private void initializeThresholds() {
        signalThresholds = new ArrayList<Integer>();
        String signalLevels = getProperty(PROPERTY_SIGNAL_LEVELS);
        if (signalLevels != null) {
            List<String> thresholds = Arrays.asList(signalLevels.split(","));
            for (String threshold : thresholds) {
                signalThresholds.add(Integer.parseInt(threshold));
            }
        }

    }

    int getSignalStrength(int signalLevel) {
        if (signalThresholds == null) {
            initializeThresholds();
        }

        int level;
        for (level = 0; level < signalThresholds.size(); level++) {
            if (signalLevel > signalThresholds.get(level)) {
                break;
            }
        }
        return level;
    }

    // protected String getVideoUrl_ee(String cameraID) {
    // return videoUrl.get(cameraID);
    // }

}
