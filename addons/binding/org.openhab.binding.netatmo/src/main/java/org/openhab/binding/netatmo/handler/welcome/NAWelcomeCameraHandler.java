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

import java.io.IOException;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.netatmo.config.NetatmoChildConfiguration;
import org.openhab.binding.netatmo.handler.NetatmoModuleHandler;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import io.swagger.client.model.NAWelcomeCameras;

/**
 * {@link NAWelcomeCameraHandler} is the class used to handle the Welcome Camera Data
 *
 * @author Ing. Peter Weiss - Welcome camera implementation
 *
 */
public class NAWelcomeCameraHandler extends NetatmoModuleHandler<NetatmoChildConfiguration, NAWelcomeCameras> {
    private final static String WELCOME_VOD_VIDEO_POOR = "/vod/%s/files/poor/index.m3u8";
    private final static String WELCOME_VOD_VIDEO_LOW = "/vod/%s/files/low/index.m3u8";
    private final static String WELCOME_VOD_VIDEO_MEDIUM = "/vod/%s/files/medium/index.m3u8";
    private final static String WELCOME_VOD_VIDEO_HIGH = "/vod/%s/files/high/index.m3u8";

    public NAWelcomeCameraHandler(Thing thing) {
        super(thing, NetatmoChildConfiguration.class);
    }

    /*
     * @Override
     * protected void updateChannels(Object module) {
     * try {
     * for (Thing thing : getBridgeHandler().getThing().getThings()) {
     * ThingHandler thingHandler = thing.getHandler();
     * if (thingHandler instanceof NAWelcomeHomeHandler) {
     * NAWelcomeHomeHandler welcomeHomeHandler = (NAWelcomeHomeHandler) thingHandler;
     * String parentId = welcomeHomeHandler.getId();
     * if (parentId != null && parentId.equals(getParentId())) {
     *
     * for (NAWelcomeCameras myCamera : getWelcomeHomes(getParentId()).getCameras()) {
     * if (myCamera.getId().equalsIgnoreCase(getId())) {
     * this.camera = myCamera;
     * super.updateChannels();
     * break;
     * }
     * }
     *
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
                case CHANNEL_WELCOME_CAMERA_STATUS:
                    return module.getStatus() != null
                            ? ("on".equalsIgnoreCase(module.getStatus()) ? OnOffType.ON : OnOffType.OFF)
                            : UnDefType.UNDEF;
                case CHANNEL_WELCOME_CAMERA_SDSTATUS:
                    return module.getSdStatus() != null
                            ? ("on".equalsIgnoreCase(module.getSdStatus()) ? OnOffType.ON : OnOffType.OFF)
                            : UnDefType.UNDEF;
                case CHANNEL_WELCOME_CAMERA_ALIMSTATUS:
                    return module.getAlimStatus() != null
                            ? ("on".equalsIgnoreCase(module.getAlimStatus()) ? OnOffType.ON : OnOffType.OFF)
                            : UnDefType.UNDEF;
                case CHANNEL_WELCOME_CAMERA_VPNURL:
                    return module.getVpnUrl() != null ? new StringType(module.getVpnUrl()) : UnDefType.UNDEF;
                case CHANNEL_WELCOME_CAMERA_ISLOCAL:
                    return module.getIsLocal() != null ? (module.getIsLocal() ? OnOffType.ON : OnOffType.OFF)
                            : UnDefType.UNDEF;

                case CHANNEL_WELCOME_CAMERA_LIVEPICTURE_URL:
                    return getLivePictureUrl();
                case CHANNEL_WELCOME_CAMERA_LIVEVIDEOPOOR_URL:
                    return getLiveVideoUrl(POOR);
                case CHANNEL_WELCOME_CAMERA_LIVEVIDEOLOW_URL:
                    return getLiveVideoUrl(LOW);
                case CHANNEL_WELCOME_CAMERA_LIVEVIDEOMEDIUM_URL:
                    return getLiveVideoUrl(MEDIUM);
                case CHANNEL_WELCOME_CAMERA_LIVEVIDEOHIGH_URL:
                    return getLiveVideoUrl(HIGH);

                default:
                    return super.getNAThingProperty(chanelId);
            }
        } catch (Exception e) {
            return UnDefType.UNDEF;
        }
    }

    /**
     * Get the url for the live picture
     *
     * @return Url of the live Picture or UnDefType.UNDEF
     */
    private State getLivePictureUrl() {
        State ret = UnDefType.UNDEF;
        if (module != null) {
            String sUrl = getCameraUrl(); // getVideoUrl(camera.getId());
            if (sUrl != null) {
                ret = new StringType(sUrl + WELCOME_LIVE_PICTURE);
            }
        }

        return ret;
    }

    public String getVideoUrl(int iQuality, String sVideoID) {
        String sUrl = getCameraUrl();
        if (sUrl != null && sVideoID != null) {
            switch (iQuality) {
                case POOR:
                    sUrl += String.format(WELCOME_VOD_VIDEO_POOR, sVideoID);
                    break;
                case LOW:
                    sUrl += String.format(WELCOME_VOD_VIDEO_LOW, sVideoID);
                    break;
                case MEDIUM:
                    sUrl += String.format(WELCOME_VOD_VIDEO_MEDIUM, sVideoID);
                    break;
                case HIGH:
                    sUrl += String.format(WELCOME_VOD_VIDEO_HIGH, sVideoID);
                    break;
                default:
                    sUrl = null;
                    break;
            }

            if (sUrl != null) {
                return sUrl;
            }
        }
        return null;
    }

    public String getCameraUrl(/* String vpnurl */) {
        // Ceci n'est pas sensé changer à chaque fois, il faudrait donc le peupler à la première
        String vpnUrl = module.getVpnUrl();
        String ret = vpnUrl;

        try {
            // Read the local Url
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder().url(vpnUrl + WELCOME_PING).build();
            Response response = client.newCall(request).execute();

            if (response.isSuccessful()) {
                String json = response.body().string();
                JsonElement resp = new JsonParser().parse(json);
                String localUrl = resp.getAsJsonObject().get("local_url").getAsString();

                // Validate the local Url
                request = new Request.Builder().url(localUrl + WELCOME_PING).build();
                response = client.newCall(request).execute();

                if (response.isSuccessful()) {
                    String json2 = response.body().string();
                    JsonElement resp2 = new JsonParser().parse(json2);
                    String localUrl2 = resp2.getAsJsonObject().get("local_url").getAsString();

                    if (localUrl.equals(localUrl2)) {
                        ret = localUrl;
                    }
                }
            }

        } catch (IOException e) {
        }
        return ret;
    }

    /**
     * Get the url of the live video strem
     *
     * @param i
     *
     * @return Url of the video stream or UnDefType.UNDEF
     */
    private State getLiveVideoUrl(int iQuality) {
        State ret = UnDefType.UNDEF;

        if (module != null) {
            String sUrl = getCameraUrl(); // getVideoUrl(camera.getId());
            if (sUrl != null) {
                switch (iQuality) {
                    case POOR:
                        sUrl += WELCOME_LIVE_VIDEO_POOR;
                        break;
                    case LOW:
                        sUrl += WELCOME_LIVE_VIDEO_LOW;
                        break;
                    case MEDIUM:
                        sUrl += WELCOME_LIVE_VIDEO_MEDIUM;
                        break;
                    case HIGH:
                        sUrl += WELCOME_LIVE_VIDEO_HIGH;
                        break;
                    default:
                        sUrl = null;
                        break;
                }

                if (sUrl != null) {
                    ret = new StringType(sUrl);
                }
            }
        }

        return ret;
    }

}
