/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.netatmo.handler.station;

import static org.openhab.binding.netatmo.NetatmoBindingConstants.*;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.netatmo.config.NetatmoParentConfiguration;
import org.openhab.binding.netatmo.handler.NetatmoDeviceHandler;
import org.openhab.binding.netatmo.internal.ChannelTypeUtils;

import io.swagger.client.model.NADashboardData;
import io.swagger.client.model.NAMain;
import io.swagger.client.model.NAStationDataBody;
import io.swagger.client.model.NAStationModule;

/**
 * {@link NAMainHandler} is the base class for all current Netatmo
 * weather station equipments (both modules and devices)
 *
 * @author Gaël L'hopital - Initial contribution OH2 version
 *
 */
public class NAMainHandler extends NetatmoDeviceHandler<NetatmoParentConfiguration, NAMain> {

    public NAMainHandler(Thing thing) {
        super(thing, NetatmoParentConfiguration.class);
    }

    @Override
    protected NAMain updateReadings(String equipmentId) {
        NAStationDataBody stationDataBody = getBridgeHandler().getStationsDataBody(equipmentId);
        if (stationDataBody != null) {
            userAdministrative = stationDataBody.getUser().getAdministrative();
            for (NAStationModule module : stationDataBody.getDevices().get(0).getModules()) {
                childs.put(module.getId(), module);
            }
            // currently this code makes the assumption that the user
            // only have one station
            return stationDataBody.getDevices().get(0);
        }
        return null;
    }

    @Override
    protected State getNAThingProperty(String channelId) {
        NADashboardData dashboardData = device.getDashboardData();
        switch (channelId) {
            case CHANNEL_CO2:
                return ChannelTypeUtils.toDecimalType(dashboardData.getCO2());
            case CHANNEL_TEMPERATURE:
                return ChannelTypeUtils.toDecimalType(dashboardData.getTemperature());
            case CHANNEL_NOISE:
                return ChannelTypeUtils.toDecimalType(dashboardData.getNoise());
            case CHANNEL_PRESSURE:
                return ChannelTypeUtils.toDecimalType(dashboardData.getPressure());
            case CHANNEL_ABSOLUTE_PRESSURE:
                return ChannelTypeUtils.toDecimalType(dashboardData.getAbsolutePressure());
            case CHANNEL_TIMEUTC:
                return ChannelTypeUtils.toDateTimeType(dashboardData.getTimeUtc());
            case CHANNEL_HUMIDITY:
                return ChannelTypeUtils.toDecimalType(dashboardData.getHumidity());
            case CHANNEL_HUMIDEX:
                return ChannelTypeUtils.toDecimalType(
                        WeatherUtils.getHumidex(dashboardData.getTemperature(), dashboardData.getHumidity()));
            case CHANNEL_HEATINDEX:
                return ChannelTypeUtils.toDecimalType(
                        WeatherUtils.getHeatIndex(dashboardData.getTemperature(), dashboardData.getHumidity()));
            case CHANNEL_DEWPOINT:
                return ChannelTypeUtils.toDecimalType(
                        WeatherUtils.getDewPoint(dashboardData.getTemperature(), dashboardData.getHumidity()));
            case CHANNEL_DEWPOINTDEP:
                Double dewpoint = WeatherUtils.getDewPoint(dashboardData.getTemperature(), dashboardData.getHumidity());
                return ChannelTypeUtils
                        .toDecimalType(WeatherUtils.getDewPointDep(dashboardData.getTemperature(), dewpoint));
            case CHANNEL_WIND_UNIT:
                return new DecimalType(userAdministrative.getWindunit());
            case CHANNEL_PRESSURE_UNIT:
                return new DecimalType(userAdministrative.getPressureunit());

            default:
                return super.getNAThingProperty(channelId);
        }
    }

}
