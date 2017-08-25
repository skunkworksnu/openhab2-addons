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

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.netatmo.config.NetatmoChildConfiguration;
import org.openhab.binding.netatmo.handler.NetatmoModuleHandler;
import org.openhab.binding.netatmo.internal.ChannelTypeUtils;

import io.swagger.client.model.NAWelcomeEvents;

/**
 * {@link NAWelcomeEventHandler} is the class used to handle the Welcome Event Data
 *
 * @author Ing. Peter Weiss - Welcome camera implementation
 *
 */
public class NAWelcomeEventHandler extends NetatmoModuleHandler<NetatmoChildConfiguration, NAWelcomeEvents> {

    public NAWelcomeEventHandler(Thing thing) {
        super(thing, NetatmoChildConfiguration.class);
    }

    @Override
    public void updateChannels(Object module) {
        String person = this.module.getPersonId();
        if (person != null) {
            NAWelcomePersonHandler personHandler = (NAWelcomePersonHandler) getBridgeHandler().findModule(person);
            personHandler.setEvent(this.module);
        }
        super.updateChannels(module);
    }

    /*
     * @Override
     * protected void updateChannels() {
     * try {
     * for (Thing thing : getBridgeHandler().getThing().getThings()) {
     * ThingHandler thingHandler = thing.getHandler();
     * if (thingHandler instanceof NAWelcomeHomeHandler) {
     * NAWelcomeHomeHandler welcomeHomeHandler = (NAWelcomeHomeHandler) thingHandler;
     * String parentId = welcomeHomeHandler.getId();
     * if (parentId != null && parentId.equals(getConfiguration().getParentId())) {
     * int index = Integer.parseInt((getId().substring(getId().indexOf('#') + 1))) - 1;
     * this.event = getWelcomeHomes(getConfiguration().getParentId()).getEvents().get(index);
     * super.updateChannels();
     * break;
     * }
     * }
     * }
     *
     * } catch (Exception e) {
     * updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, e.getMessage());
     * }
     * }
     */

    @Override
    protected State getNAThingProperty(String chanelId) {
        try {
            switch (chanelId) {
                case CHANNEL_WELCOME_EVENT_ID:
                    return module.getId() != null ? new StringType(module.getId()) : UnDefType.UNDEF;
                case CHANNEL_WELCOME_EVENT_TYPE:
                    return module.getType() != null ? new StringType(module.getType()) : UnDefType.UNDEF;
                case CHANNEL_WELCOME_EVENT_TIME:
                    return module.getTime() != null ? ChannelTypeUtils.toDateTimeType(module.getTime())
                            : UnDefType.UNDEF;
                case CHANNEL_WELCOME_EVENT_CAMERAID:
                    return module.getCameraId() != null ? new StringType(module.getCameraId()) : UnDefType.UNDEF;
                case CHANNEL_WELCOME_EVENT_PERSONID:
                    return module.getPersonId() != null ? new StringType(module.getPersonId()) : UnDefType.UNDEF;
                case CHANNEL_WELCOME_EVENT_SNAPSHOTID:
                    return module.getSnapshot().getId() != null ? new StringType(module.getSnapshot().getId())
                            : UnDefType.UNDEF;
                case CHANNEL_WELCOME_EVENT_SNAPSHOTVERSION:
                    return module.getSnapshot().getVersion() != null
                            ? new DecimalType(module.getSnapshot().getVersion()) : UnDefType.UNDEF;
                case CHANNEL_WELCOME_EVENT_SNAPSHOTKEY:
                    return module.getSnapshot().getKey() != null ? new StringType(module.getSnapshot().getKey())
                            : UnDefType.UNDEF;
                case CHANNEL_WELCOME_EVENT_VIDEOID:
                    return module.getVideoId() != null ? new StringType(module.getVideoId()) : UnDefType.UNDEF;
                case CHANNEL_WELCOME_EVENT_VIDEOSTATUS:
                    return module.getVideoStatus() != null ? new StringType(module.getVideoStatus()) : UnDefType.UNDEF;
                case CHANNEL_WELCOME_EVENT_ISARRIVAL:
                    return module.getIsArrival() != null ? (module.getIsArrival() ? OnOffType.ON : OnOffType.OFF)
                            : UnDefType.UNDEF;
                case CHANNEL_WELCOME_EVENT_MESSAGE:
                    return module.getMessage() != null ? new StringType(module.getMessage()) : UnDefType.UNDEF;
                case CHANNEL_WELCOME_EVENT_SUBTYPE:
                    return module.getSubType() != null ? new DecimalType(module.getSubType()) : UnDefType.UNDEF;

                case CHANNEL_WELCOME_EVENT_PICTURE_URL:
                    return module.getSnapshot() != null ? getBridgeHandler().getPictureUrl(module.getSnapshot().getId(),
                            module.getSnapshot().getKey()) : UnDefType.UNDEF;
                case CHANNEL_WELCOME_EVENT_VIDEOPOOR_URL:
                    return getVideoUrl(POOR, module.getVideoId());
                case CHANNEL_WELCOME_EVENT_VIDEOLOW_URL:
                    return getVideoUrl(LOW, module.getVideoId());
                case CHANNEL_WELCOME_EVENT_VIDEOMEDIUM_URL:
                    return getVideoUrl(MEDIUM, module.getVideoId());
                case CHANNEL_WELCOME_EVENT_VIDEOHIGH_URL:
                    return getVideoUrl(HIGH, module.getVideoId());

                default:
                    return super.getNAThingProperty(chanelId);
            }
        } catch (Exception e) {
            return UnDefType.UNDEF;
        }
    }

    /**
     * Get the url of the live video strem
     *
     * @param i
     *
     * @return Url of the video stream or UnDefType.UNDEF
     */
    private State getVideoUrl(int iQuality, String sVideoID) {
        State ret = UnDefType.UNDEF;
        NAWelcomeCameraHandler camera = (NAWelcomeCameraHandler) getBridgeHandler().findModule(module.getCameraId());

        if (module != null && camera != null) {
            String sUrl = camera.getVideoUrl(iQuality, sVideoID);
            if (sUrl != null) {
                ret = new StringType(sUrl);
            }
        }

        return ret;
    }

}
