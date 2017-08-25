/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.netatmo.handler.welcome;

import static org.openhab.binding.netatmo.NetatmoBindingConstants.*;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.netatmo.config.NetatmoParentConfiguration;
import org.openhab.binding.netatmo.handler.NetatmoDeviceHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.swagger.client.model.NAWelcomeCameras;
import io.swagger.client.model.NAWelcomeEvents;
import io.swagger.client.model.NAWelcomeHomeData;
import io.swagger.client.model.NAWelcomeHomes;
import io.swagger.client.model.NAWelcomePersons;

/**
 * {@link NAWelcomeHomeHandler} is the class used to handle the Welcome Home Data
 *
 * @author Gaël L'hopital - Initial contribution
 * @author Ing. Peter Weiss - Welcome camera implementation
 *
 */

public class NAWelcomeHomeHandler extends NetatmoDeviceHandler<NetatmoParentConfiguration, NAWelcomeHomes> {
    private static Logger logger = LoggerFactory.getLogger(NAWelcomeHomeHandler.class);

    // private ScheduledFuture<?> bridgeHandlerJob;
    // private ScheduledFuture<?> refreshJob;

    // private static HashMap<String, NAWelcomeHomes> homes = new HashMap<String, NAWelcomeHomes>();
    // private static HashMap<String, String> urls = new HashMap<String, String>();

    private int iPerson = -1;
    private int iUnknown = -1;
    private boolean bSomebodyAtHome = false;

    public NAWelcomeHomeHandler(Thing thing) {
        super(thing, NetatmoParentConfiguration.class);
    }

    /*
     * @Override
     * public void initialize() {
     * super.initialize();
     * // initBridgeScheduler();
     * // initWelcomeScheduler();
     * }
     */

    /*
     * Je ne comprends pas à quoi sert ceci
     * private void initBridgeScheduler() {
     * logger.debug("scheduling bridge thread to run every {} ms", 60000);
     * bridgeHandlerJob = scheduler.scheduleWithFixedDelay(new Runnable() {
     *
     * @Override
     * public void run() {
     * if (getBridge() != null) {
     * logger.debug("Initializing Netatmo Welcome Home");
     * if (getBridge().getStatus() == ThingStatus.ONLINE) {
     * logger.debug("setting Welcome Home online");
     * updateStatus(ThingStatus.ONLINE);
     *
     * bridgeHandlerJob.cancel(true);
     * bridgeHandlerJob = null;
     *
     * initWelcomeScheduler();
     *
     * } else {
     * logger.debug("setting Welcome Home '{}' offline (bridge or thing offline)", getId());
     * updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.BRIDGE_OFFLINE);
     * }
     * } else {
     * logger.debug("setting Welcome Home offline (bridge == null)");
     * updateStatus(ThingStatus.OFFLINE);
     * }
     * }
     * }, 1, 60000, TimeUnit.MILLISECONDS);
     * }
     */

    /*
     * private void initWelcomeScheduler() {
     * logger.debug("scheduling update channel thread to run every {} ms", configuration.refreshInterval);
     * refreshJob = scheduler.scheduleWithFixedDelay(new Runnable() {
     *
     * @Override
     * public void run() {
     * updateChannels();
     * }
     * }, 1, configuration.refreshInterval.longValue(), TimeUnit.MILLISECONDS);
     * }
     */

    /*
     * @Override
     * public void dispose() {
     * logger.debug("Running dispose()");
     * if (bridgeHandlerJob != null && !bridgeHandlerJob.isCancelled()) {
     * bridgeHandlerJob.cancel(true);
     * bridgeHandlerJob = null;
     * }
     * if (refreshJob != null && !refreshJob.isCancelled()) {
     * refreshJob.cancel(true);
     * refreshJob = null;
     * }
     * }
     */

    /*
     * protected void setWelcomeHomes(String homeID, NAWelcomeHomes welcomeHomes) {
     * homes.put(homeID, welcomeHomes);
     *
     * // Build the url for video streaming and the live camera picture
     * for (NAWelcomeCameras camera : welcomeHomes.getCameras()) {
     * String url = getNetatmoWelcomeUrl(camera.getVpnUrl());
     * setVideoUrl(camera.getId(), url);
     * }
     * }
     *
     * protected void setVideoUrl(String cameraID, String videoUrl) {
     * urls.put(cameraID, videoUrl);
     * }
     */

    @Override
    protected NAWelcomeHomes updateReadings(String equipmentId) {
        NAWelcomeHomeData myHomeData = getBridgeHandler().getWelcomeDataBody(equipmentId);
        if (myHomeData != null) {
            for (NAWelcomeHomes myHome : myHomeData.getHomes()) {
                if (myHome.getId().equalsIgnoreCase(equipmentId)) {
                    logger.debug("Successfully read data for welcome home '{}'", myHome.getId());

                    for (NAWelcomeCameras camera : myHome.getCameras()) {
                        childs.put(camera.getId(), camera);
                    }

                    logger.debug("welcome home '{}' sort events", myHome.getId());
                    Collections.sort(myHome.getEvents(), new Comparator<NAWelcomeEvents>() {
                        @Override
                        public int compare(NAWelcomeEvents s1, NAWelcomeEvents s2) {
                            return s2.getTime().compareTo(s1.getTime());
                        }
                    });

                    for (NAWelcomeEvents event : myHome.getEvents()) {
                        childs.put(event.getId(), event);
                    }

                    logger.debug("welcome home '{}' sort persons", myHome.getId());
                    Collections.sort(myHome.getPersons(), new Comparator<NAWelcomePersons>() {
                        @Override
                        public int compare(NAWelcomePersons s1, NAWelcomePersons s2) {
                            return s2.getLastSeen().compareTo(s1.getLastSeen());
                        }
                    });

                    for (NAWelcomePersons person : myHome.getPersons()) {
                        childs.put(person.getId(), person);
                    }

                    // setWelcomeHomes(myHome.getId(), myHome);

                    // Check if somebody is at home
                    iPerson = 0;
                    iUnknown = 0;
                    bSomebodyAtHome = false;
                    HashMap<String, NAWelcomePersons> foundPerson = new HashMap<String, NAWelcomePersons>();
                    myHome.getPersons();

                    logger.debug("welcome home '{}' calculate Persons at home count", myHome.getId());
                    for (NAWelcomePersons person : myHome.getPersons()) {
                        if (foundPerson.get(person.getId()) == null) {
                            foundPerson.put(person.getId(), person);
                            if (person.getPseudo() != null) {
                                if (!person.getOutOfSight()) {
                                    iPerson++;
                                }
                            } else {
                                if (!person.getOutOfSight()) {
                                    iUnknown++;
                                }
                            }
                        }
                    }
                    if (iPerson > 0 || iUnknown > 0) {
                        bSomebodyAtHome = true;
                    }

                    // super.updateChannels(equipmentId);

                    // logger.debug("welcome home '{}' update child things", myHome.getId());
                    // updateWelcomeThings();
                    // break;
                }
            }
            // currently this code makes the assumption that the user
            // only have one station
            return myHomeData.getHomes().get(0);
        }
        return null;
    }

    /**
     *
     * @param url The VPNUrl for which the local url should be found
     * @return The local Url or the vpn url if the request is not local
     */
    /*
     * private String getNetatmoWelcomeUrl(String vpnurl) {
     * String ret = vpnurl;
     *
     * try {
     * // Read the local Url
     * OkHttpClient client = new OkHttpClient();
     * Request request = new Request.Builder().url(vpnurl + WELCOME_PING).build();
     * Response response = client.newCall(request).execute();
     *
     * if (response.isSuccessful()) {
     * String json = response.body().string();
     * JsonElement resp = new JsonParser().parse(json);
     * String localUrl = resp.getAsJsonObject().get("local_url").getAsString();
     *
     * // Validate the local Url
     * request = new Request.Builder().url(localUrl + WELCOME_PING).build();
     * response = client.newCall(request).execute();
     *
     * if (response.isSuccessful()) {
     * String json2 = response.body().string();
     * JsonElement resp2 = new JsonParser().parse(json2);
     * String localUrl2 = resp2.getAsJsonObject().get("local_url").getAsString();
     *
     * if (localUrl.equals(localUrl2)) {
     * ret = localUrl;
     * }
     * }
     * }
     *
     * } catch (IOException e) {
     * }
     * return ret;
     * }
     */

    /*
     * protected NAWelcomeHomes getWelcomeHomes(String homeID) {
     * return homes.get(homeID);
     * }
     */

    @Override
    protected State getNAThingProperty(String chanelId) {
        try {
            switch (chanelId) {
                case CHANNEL_WELCOME_HOME_CITY:
                    return device.getPlace().getCity() != null ? new StringType(device.getPlace().getCity())
                            : UnDefType.UNDEF;
                case CHANNEL_WELCOME_HOME_COUNTRY:
                    return device.getPlace().getCountry() != null ? new StringType(device.getPlace().getCountry())
                            : UnDefType.UNDEF;
                case CHANNEL_WELCOME_HOME_TIMEZONE:
                    return device.getPlace().getTimezone() != null ? new StringType(device.getPlace().getTimezone())
                            : UnDefType.UNDEF;
                case CHANNEL_WELCOME_HOME_SOMEBODYATHOME:
                    return bSomebodyAtHome ? OnOffType.ON : OnOffType.OFF;
                case CHANNEL_WELCOME_HOME_PERSONCOUNT:
                    return iPerson != -1 ? new DecimalType(iPerson) : UnDefType.UNDEF;
                case CHANNEL_WELCOME_HOME_UNKNOWNCOUNT:
                    return iUnknown != -1 ? new DecimalType(iUnknown) : UnDefType.UNDEF;

                default:
                    return super.getNAThingProperty(chanelId);
            }
        } catch (Exception e) {
            return UnDefType.UNDEF;
        }
    }

    /*
     * private void updateWelcomeThings() {
     * for (Thing thing : getBridgeHandler().getThing().getThings()) {
     * ThingHandler thingHandler = thing.getHandler();
     * if (thingHandler instanceof NAWelcomeCameraHandler) {
     * NAWelcomeCameraHandler welcomeHandler = (NAWelcomeCameraHandler) thingHandler;
     * String parentId = welcomeHandler.getParentId();
     * if (parentId != null && parentId.equals(getId())) {
     * logger.debug("Updating welcome camera {}", welcomeHandler.getId());
     * welcomeHandler.updateChannels();
     * }
     * } else if (thingHandler instanceof NAWelcomePersonHandler) {
     * NAWelcomePersonHandler welcomeHandler = (NAWelcomePersonHandler) thingHandler;
     * String parentId = welcomeHandler.getConfiguration().getParentId();
     * if (parentId != null && parentId.equals(getId())) {
     * logger.debug("Updating welcome person {}", welcomeHandler.getId());
     * welcomeHandler.updateChannels();
     * }
     * }
     * if (thingHandler instanceof NAWelcomeEventHandler) {
     * NAWelcomeEventHandler welcomeHandler = (NAWelcomeEventHandler) thingHandler;
     * String parentId = welcomeHandler.getConfiguration().getParentId();
     * if (parentId != null && parentId.equals(getId())) {
     * logger.debug("Updating welcome event {}", welcomeHandler.getId());
     * welcomeHandler.updateChannels();
     * }
     * }
     * }
     * }
     */

    /*
     * protected void updateChannels() {
     * logger.debug("Updating device channels");
     *
     * for (Channel channel : getThing().getChannels()) {
     * String channelId = channel.getUID().getId();
     * State state = getNAThingProperty(channelId);
     * if (state != null) {
     * updateState(channel.getUID(), state);
     * }
     * }
     *
     * updateStatus(ThingStatus.ONLINE);
     * }
     */

}
