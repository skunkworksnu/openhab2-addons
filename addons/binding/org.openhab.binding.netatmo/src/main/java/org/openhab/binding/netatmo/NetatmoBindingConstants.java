/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.netatmo;

import java.util.Set;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

import com.google.common.collect.ImmutableSet;

/**
 * The {@link NetatmoBinding} class defines common constants, which are used
 * across the whole binding.
 *
 * @author Gaël L'hopital - Initial contribution
 * @author Ing. Peter Weiss - Welcome camera implementation
 */
public class NetatmoBindingConstants {

    private static final String BINDING_ID = "netatmo";

    // Configuration keys
    public static final String EQUIPMENT_ID = "id";
    public static final String PARENT_ID = "parentId";

    // List of all Bridge Type UIDs
    public static final ThingTypeUID APIBRIDGE_THING_TYPE = new ThingTypeUID(BINDING_ID, "netatmoapi");

    // List of all Weather Station Type UIDs
    public static final ThingTypeUID MAIN_THING_TYPE = new ThingTypeUID(BINDING_ID, "NAMain");
    public static final ThingTypeUID MODULE1_THING_TYPE = new ThingTypeUID(BINDING_ID, "NAModule1");
    public static final ThingTypeUID MODULE2_THING_TYPE = new ThingTypeUID(BINDING_ID, "NAModule2");
    public static final ThingTypeUID MODULE3_THING_TYPE = new ThingTypeUID(BINDING_ID, "NAModule3");
    public static final ThingTypeUID MODULE4_THING_TYPE = new ThingTypeUID(BINDING_ID, "NAModule4");

    // List of all Thermostat Type UIDs
    public static final ThingTypeUID PLUG_THING_TYPE = new ThingTypeUID(BINDING_ID, "NAPlug");
    public static final ThingTypeUID THERM1_THING_TYPE = new ThingTypeUID(BINDING_ID, "NATherm1");

    // List of all Welcome Home Thing Type UIDs
    public final static ThingTypeUID WELCOME_HOME_THING_TYPE = new ThingTypeUID(BINDING_ID, "NAWelcomeHome");
    public final static ThingTypeUID WELCOME_CAMERA_THING_TYPE = new ThingTypeUID(BINDING_ID, "NACamera");
    public final static ThingTypeUID WELCOME_PERSON_THING_TYPE = new ThingTypeUID(BINDING_ID, "NAWelcomePerson");
    public final static ThingTypeUID WELCOME_EVENT_THING_TYPE = new ThingTypeUID(BINDING_ID, "NAWelcomeEvent");

    // List of all Wheater Station Channel ids
    public static final String CHANNEL_TEMPERATURE = "Temperature";
    public static final String CHANNEL_TEMP_TREND = "TempTrend";
    public static final String CHANNEL_HUMIDITY = "Humidity";
    public static final String CHANNEL_HUMIDEX = "Humidex";
    public static final String CHANNEL_TIMEUTC = "TimeStamp";
    public static final String CHANNEL_DEWPOINT = "Dewpoint";
    public static final String CHANNEL_DEWPOINTDEP = "DewpointDepression";
    public static final String CHANNEL_HEATINDEX = "HeatIndex";
    public static final String CHANNEL_LAST_STATUS_STORE = "LastStatusStore";
    public static final String CHANNEL_LAST_MESSAGE = "LastMessage";
    public static final String CHANNEL_LOCATION = "Location";
    public static final String CHANNEL_BOILER_ON = "BoilerOn";
    public static final String CHANNEL_BOILER_OFF = "BoilerOff";
    public static final String CHANNEL_DATE_MAX_TEMP = "date_max_temp";
    public static final String CHANNEL_DATE_MIN_TEMP = "date_min_temp";
    public static final String CHANNEL_MAX_TEMP = "min_temp";
    public static final String CHANNEL_MIN_TEMP = "max_temp";
    public static final String CHANNEL_ABSOLUTE_PRESSURE = "AbsolutePressure";
    public static final String CHANNEL_CO2 = "Co2";
    public static final String CHANNEL_NOISE = "Noise";
    public static final String CHANNEL_PRESSURE = "Pressure";
    public static final String CHANNEL_PRESS_TREND = "PressTrend";
    public static final String CHANNEL_RAIN = "Rain";
    public static final String CHANNEL_SUM_RAIN1 = "SumRain1";
    public static final String CHANNEL_SUM_RAIN24 = "SumRain24";
    public static final String CHANNEL_WIND_ANGLE = "WindAngle";
    public static final String CHANNEL_WIND_STRENGTH = "WindStrength";
    public static final String CHANNEL_GUST_ANGLE = "GustAngle";
    public static final String CHANNEL_GUST_STRENGTH = "GustStrength";
    public static final String CHANNEL_LOW_BATTERY = "LowBattery";
    public static final String CHANNEL_BATTERY_LEVEL = "BatteryVP";
    public static final String CHANNEL_WIFI_STATUS = "WifiStatus";
    public static final String CHANNEL_RF_STATUS = "RfStatus";
    public static final String CHANNEL_UNIT = "Unit";
    public static final String CHANNEL_WIND_UNIT = "WindUnit";
    public static final String CHANNEL_PRESSURE_UNIT = "PressureUnit";

    // Thermostat specific channels
    public static final String CHANNEL_SETPOINT_MODE = "SetpointMode";
    public static final String CHANNEL_SETPOINT_END_TIME = "SetpointEndTime";
    public static final String CHANNEL_SETPOINT_TEMP = "Sp_Temperature";
    public static final String CHANNEL_THERM_RELAY = "ThermRelayCmd";
    public static final String CHANNEL_THERM_ORIENTATION = "ThermOrientation";

    // Module Properties
    public static final String PROPERTY_BATTERY_MIN = "batteryMin";
    public static final String PROPERTY_BATTERY_MAX = "batteryMax";
    public static final String PROPERTY_BATTERY_LOW = "batteryLow";
    public static final String PROPERTY_SIGNAL_LEVELS = "signalLevels";

    // Welcome Home specific channels
    public final static String CHANNEL_WELCOME_HOME_CITY = "welcomeHomeCity";
    public final static String CHANNEL_WELCOME_HOME_COUNTRY = "welcomeHomeCountry";
    public final static String CHANNEL_WELCOME_HOME_TIMEZONE = "welcomeHomeTimezone";
    public final static String CHANNEL_WELCOME_HOME_SOMEBODYATHOME = "welcomeHomeSomebodyAtHome";
    public final static String CHANNEL_WELCOME_HOME_PERSONCOUNT = "welcomeHomePersonCount";
    public final static String CHANNEL_WELCOME_HOME_UNKNOWNCOUNT = "welcomeHomeUnknownCount";

    public final static String CHANNEL_WELCOME_PERSON_ID = "welcomePersonId";
    public final static String CHANNEL_WELCOME_PERSON_LASTSEEN = "welcomePersonLastSeen";
    public final static String CHANNEL_WELCOME_PERSON_OUTOFSIGHT = "welcomePersonOutOfSight";
    public final static String CHANNEL_WELCOME_PERSON_FACEID = "welcomePersonFaceID";
    public final static String CHANNEL_WELCOME_PERSON_FACEVERSION = "welcomePersonFaceVersion";
    public final static String CHANNEL_WELCOME_PERSON_FACEKEY = "welcomePersonFaceKey";
    public final static String CHANNEL_WELCOME_PERSON_PSEUDO = "welcomePersonPseudo";
    public final static String CHANNEL_WELCOME_PERSON_ATHOME = "welcomePersonAtHome";
    public final static String CHANNEL_WELCOME_PERSON_LASTEVENTID = "welcomePersonLastEventID";
    public final static String CHANNEL_WELCOME_PERSON_LASTMESSAGE = "welcomePersonLastEventMessage";
    public final static String CHANNEL_WELCOME_PERSON_LASTTIME = "welcomePersonLastEventTime";
    public final static String CHANNEL_WELCOME_PERSON_AVATARPICTURE_URL = "welcomePersonAvatarPictureUrl";
    public final static String CHANNEL_WELCOME_PERSON_LASTEVENTPICTURE_URL = "welcomePersonLastEventPictureUrl";

    public final static String CHANNEL_WELCOME_CAMERA_STATUS = "welcomeCameraStatus";
    public final static String CHANNEL_WELCOME_CAMERA_SDSTATUS = "welcomeCameraSdStatus";
    public final static String CHANNEL_WELCOME_CAMERA_ALIMSTATUS = "welcomeCameraAlimStatus";
    public final static String CHANNEL_WELCOME_CAMERA_VPNURL = "welcomeCameraVpnUrl";
    public final static String CHANNEL_WELCOME_CAMERA_ISLOCAL = "welcomeCameraIsLocal";
    public final static String CHANNEL_WELCOME_CAMERA_LIVEPICTURE_URL = "welcomeCameraLivePictureUrl";
    public final static String CHANNEL_WELCOME_CAMERA_LIVEVIDEOPOOR_URL = "welcomeCameraLiveVideoPoorUrl";
    public final static String CHANNEL_WELCOME_CAMERA_LIVEVIDEOLOW_URL = "welcomeCameraLiveVideoLowUrl";
    public final static String CHANNEL_WELCOME_CAMERA_LIVEVIDEOMEDIUM_URL = "welcomeCameraLiveVideoMediumUrl";
    public final static String CHANNEL_WELCOME_CAMERA_LIVEVIDEOHIGH_URL = "welcomeCameraLiveVideoHighUrl";

    public final static String CHANNEL_WELCOME_EVENT_ID = "welcomeEventId";
    public final static String CHANNEL_WELCOME_EVENT_TYPE = "welcomeEventType";
    public final static String CHANNEL_WELCOME_EVENT_TIME = "welcomeEventTime";
    public final static String CHANNEL_WELCOME_EVENT_CAMERAID = "welcomeEventCameraId";
    public final static String CHANNEL_WELCOME_EVENT_PERSONID = "welcomeEventPersonId";
    public final static String CHANNEL_WELCOME_EVENT_SNAPSHOTID = "welcomeEventSnapshotId";
    public final static String CHANNEL_WELCOME_EVENT_SNAPSHOTVERSION = "welcomeEventSnapshotVersion";
    public final static String CHANNEL_WELCOME_EVENT_SNAPSHOTKEY = "welcomeEventSnapshotKey";
    public final static String CHANNEL_WELCOME_EVENT_VIDEOID = "welcomeEventVideoID";
    public final static String CHANNEL_WELCOME_EVENT_VIDEOSTATUS = "welcomeEventVideoStatus";
    public final static String CHANNEL_WELCOME_EVENT_ISARRIVAL = "welcomeEventIsArrival";
    public final static String CHANNEL_WELCOME_EVENT_MESSAGE = "welcomeEventMessage";
    public final static String CHANNEL_WELCOME_EVENT_SUBTYPE = "welcomeEventSubType";
    public final static String CHANNEL_WELCOME_EVENT_PICTURE_URL = "welcomeEventPictureUrl";
    public final static String CHANNEL_WELCOME_EVENT_VIDEOPOOR_URL = "welcomeEventVideoPoorUrl";
    public final static String CHANNEL_WELCOME_EVENT_VIDEOLOW_URL = "welcomeEventVideoLowUrl";
    public final static String CHANNEL_WELCOME_EVENT_VIDEOMEDIUM_URL = "welcomeEventVideoMediumUrl";
    public final static String CHANNEL_WELCOME_EVENT_VIDEOHIGH_URL = "welcomeEventVideoHighUrl";

    public final static String WELCOME_LIVE_PICTURE = "/live/snapshot_720.jpg";
    public final static String WELCOME_LIVE_VIDEO_POOR = "/live/files/poor/index.m3u8";
    public final static String WELCOME_LIVE_VIDEO_LOW = "/live/files/low/index.m3u8";
    public final static String WELCOME_LIVE_VIDEO_MEDIUM = "/live/files/medium/index.m3u8";
    public final static String WELCOME_LIVE_VIDEO_HIGH = "/live/files/high/index.m3u8";

    public final static String WELCOME_PICTURE_URL = "https://api.netatmo.com/api/getcamerapicture";
    public final static String WELCOME_PICTURE_IMAGEID = "image_id";
    public final static String WELCOME_PICTURE_KEY = "key";
    public final static String WELCOME_PING = "/command/ping";

    // Camera feeds Resolution Levels
    public final static int POOR = 1;
    public final static int LOW = 2;
    public final static int MEDIUM = 3;
    public final static int HIGH = 4;

    // List of all supported physical devices and modules
    public static final Set<ThingTypeUID> SUPPORTED_DEVICE_THING_TYPES_UIDS = ImmutableSet.of(MAIN_THING_TYPE,
            MODULE1_THING_TYPE, MODULE2_THING_TYPE, MODULE3_THING_TYPE, MODULE4_THING_TYPE, PLUG_THING_TYPE,
            THERM1_THING_TYPE, WELCOME_HOME_THING_TYPE, WELCOME_CAMERA_THING_TYPE, WELCOME_PERSON_THING_TYPE,
            WELCOME_EVENT_THING_TYPE);

    public static final Set<String> MEASURABLE_CHANNELS = ImmutableSet.of(CHANNEL_BOILER_ON, CHANNEL_BOILER_OFF);

}
