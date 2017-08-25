/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.netatmo.discovery;

import static org.openhab.binding.netatmo.NetatmoBindingConstants.*;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.netatmo.config.NetatmoChildConfiguration;
import org.openhab.binding.netatmo.handler.NetatmoBridgeHandler;
import org.openhab.binding.netatmo.handler.NetatmoDeviceHandler;
import org.openhab.binding.netatmo.handler.NetatmoModuleHandler;

import io.swagger.client.model.NAMain;
import io.swagger.client.model.NAPlug;
import io.swagger.client.model.NAStationDataBody;
import io.swagger.client.model.NAStationModule;
import io.swagger.client.model.NAThermostat;
import io.swagger.client.model.NAThermostatDataBody;
import io.swagger.client.model.NAWelcomeCameras;
import io.swagger.client.model.NAWelcomeEvents;
import io.swagger.client.model.NAWelcomeHomeData;
import io.swagger.client.model.NAWelcomeHomes;
import io.swagger.client.model.NAWelcomePersons;

/**
 * The {@link NetatmoModuleDiscoveryService} searches for available Netatmo
 * devices and modules connected to the API console
 *
 * @author Gaël L'hopital - Initial contribution
 * @author Ing. Peter Weiss - Welcome camera implementation
 *
 */
public class NetatmoModuleDiscoveryService extends AbstractDiscoveryService {
    private static final int SEARCH_TIME = 2;

    private Integer eventCount = 0;
    private final NetatmoBridgeHandler netatmoBridgeHandler;

    public NetatmoModuleDiscoveryService(NetatmoBridgeHandler netatmoBridgeHandler) {
        super(SUPPORTED_DEVICE_THING_TYPES_UIDS, SEARCH_TIME);
        this.netatmoBridgeHandler = netatmoBridgeHandler;
    }

    @Override
    public void startScan() {
        // Discover station and modules
        NAStationDataBody stationsDataBody = netatmoBridgeHandler.getStationsDataBody(null);
        if (stationsDataBody != null) {
            List<NAMain> stations = stationsDataBody.getDevices();
            stations.forEach(station -> {
                onDeviceAddedInternal(station.getId(), station.getType(), station.getStationName());
                List<NAStationModule> modules = station.getModules();
                modules.forEach(module -> {
                    onModuleAddedInternal(station.getId(), module.getId(), module.getType(), module.getModuleName());
                });
            });
        }
        // Discover plug and thermostats
        NAThermostatDataBody thermostatsDataBody = netatmoBridgeHandler.getThermostatsDataBody(null);
        if (thermostatsDataBody != null) {
            List<NAPlug> plugs = thermostatsDataBody.getDevices();
            plugs.forEach(plug -> {
                onDeviceAddedInternal(plug.getId(), plug.getType(), plug.getStationName());
                List<NAThermostat> thermostats = plug.getModules();
                thermostats.forEach(thermostat -> {
                    onModuleAddedInternal(plug.getId(), thermostat.getId(), thermostat.getType(),
                            thermostat.getModuleName());
                });
            });
        }
        // Discover Homes
        NAWelcomeHomeData welcomeHomeData = netatmoBridgeHandler.getWelcomeDataBody(null);
        if (welcomeHomeData != null) {
            eventCount = netatmoBridgeHandler.getWelcomeEventThings();
            List<NAWelcomeHomes> homes = welcomeHomeData.getHomes();
            if (homes != null) {
                homes.forEach(home -> {
                    onDeviceAddedInternal(home.getId(), WELCOME_HOME_THING_TYPE.getId().toString(), home.getName());
                    // Discover Cameras
                    List<NAWelcomeCameras> cameras = home.getCameras();
                    cameras.forEach(camera -> {
                        onModuleAddedInternal(home.getId(), camera.getId(), camera.getType(), camera.getName());
                    });
                    // Discover persons
                    List<NAWelcomePersons> persons = home.getPersons();
                    Collections.sort(persons, new Comparator<NAWelcomePersons>() {
                        @Override
                        public int compare(NAWelcomePersons s1, NAWelcomePersons s2) {
                            return s2.getLastSeen().compareTo(s1.getLastSeen());
                        }
                    });

                    final AtomicInteger countPerson = new AtomicInteger();
                    persons.forEach(person -> {
                        String personName = person.getPseudo();
                        if (personName == null) {
                            personName = "Unknown Person " + countPerson.incrementAndGet();
                        }
                        onModuleAddedInternal(home.getId(), person.getId(),
                                WELCOME_PERSON_THING_TYPE.getId().toString(), personName);
                    });
                    // Discover Events
                    List<NAWelcomeEvents> events = home.getEvents();
                    Collections.sort(events, new Comparator<NAWelcomeEvents>() {
                        @Override
                        public int compare(NAWelcomeEvents s1, NAWelcomeEvents s2) {
                            return s2.getTime().compareTo(s1.getTime());
                        }
                    });

                    final AtomicInteger countEvent = new AtomicInteger();
                    events.forEach(event -> {
                        if (countEvent.get() <= eventCount) {
                            String eventName = "Event " + countEvent.incrementAndGet();
                            onModuleAddedInternal(home.getId(), event.getId(),
                                    WELCOME_EVENT_THING_TYPE.getId().toString(), eventName);
                        }
                    });
                });
            }

        }

        stopScan();
    }

    private void onDeviceAddedInternal(String id, String type, String name) {
        // Prevent from adding already known devices
        netatmoBridgeHandler.getThing().getThings().stream()
                .filter(thing -> thing.getHandler() instanceof NetatmoDeviceHandler).forEach(thing -> {
                    NetatmoDeviceHandler<?, ?> device = (NetatmoDeviceHandler<?, ?>) thing.getHandler();
                    // NetatmoParentConfiguration configuration = device.getConfiguration();
                    if (device.configuration.getId().equalsIgnoreCase(id)) {
                        return;
                    }
                });

        ThingUID thingUID = findThingUID(type, id);
        Map<String, Object> properties = new HashMap<>(1);

        properties.put(EQUIPMENT_ID, id);

        addDiscoveredThing(thingUID, properties, name);
    }

    private void onModuleAddedInternal(String deviceId, String moduleId, String moduleType, String moduleName) {
        // Prevent for adding already known modules
        for (Thing thing : netatmoBridgeHandler.getThing().getThings()) {
            if (thing.getHandler() instanceof NetatmoModuleHandler) {
                NetatmoModuleHandler<?, ?> module = (NetatmoModuleHandler<?, ?>) thing.getHandler();
                NetatmoChildConfiguration configuration = module.configuration;
                if (configuration.getParentId().equalsIgnoreCase(deviceId)
                        && module.configuration.getId().equalsIgnoreCase(moduleId)) {
                    return;
                }
            }
        }

        ThingUID thingUID = findThingUID(moduleType, moduleId);
        Map<String, Object> properties = new HashMap<>(2);

        properties.put(EQUIPMENT_ID, moduleId);
        properties.put(PARENT_ID, deviceId);

        addDiscoveredThing(thingUID, properties, moduleName);
    }

    private void addDiscoveredThing(ThingUID thingUID, Map<String, Object> properties, String displayLabel) {
        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withProperties(properties)
                .withBridge(netatmoBridgeHandler.getThing().getUID()).withLabel(displayLabel).build();

        thingDiscovered(discoveryResult);
    }

    private ThingUID findThingUID(String thingType, String thingId) throws IllegalArgumentException {
        for (ThingTypeUID supportedThingTypeUID : getSupportedThingTypes()) {
            String uid = supportedThingTypeUID.getId();

            if (uid.equalsIgnoreCase(thingType)) {

                return new ThingUID(supportedThingTypeUID, netatmoBridgeHandler.getThing().getUID(),
                        thingId.replaceAll("[^a-zA-Z0-9_]", ""));
            }
        }

        throw new IllegalArgumentException("Unsupported device type discovered : " + thingType);
    }

}
