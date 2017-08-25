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
import io.swagger.client.model.NAWelcomePersons;

/**
 * {@link NAWelcomePersonHandler} is the class used to handle the Welcome Home Data
 *
 * @author Ing. Peter Weiss - Welcome camera implementation
 *
 */
public class NAWelcomePersonHandler extends NetatmoModuleHandler<NetatmoChildConfiguration, NAWelcomePersons> {

    // protected NAWelcomePersons person;
    private NAWelcomeEvents event;

    public NAWelcomePersonHandler(Thing thing) {
        super(thing, NetatmoChildConfiguration.class);
    }

    /*
     * @Override
     * protected void updateChannels() {
     * try {
     * for (Thing thing : getBridgeHandler().getThing().getThings()) {
     * ThingHandler thingHandler = thing.getHandler();
     * if (thingHandler instanceof NAWelcomeHomeHandler) {
     * NAWelcomeHomeHandler welcomeHomeHandler = (NAWelcomeHomeHandler) thingHandler;
     * String parentId = welcomeHomeHandler.configuration.getId();
     * if (parentId != null && parentId.equals(configuration.getParentId())) {
     * 
     * if (configuration.getId() != null && configuration.getId().startsWith("unknownperson")) {
     * int index = Integer.parseInt(
     * (configuration.getId().substring(configuration.getId().indexOf('#') + 1)));
     * 
     * int i = 0;
     * for (NAWelcomePersons myPerson : getWelcomeHomes(getConfiguration().getParentId())
     * .getPersons()) {
     * if (myPerson.getPseudo() == null && index == ++i) {
     * this.module = myPerson;
     * super.updateChannels();
     * 
     * try {
     * List<NAWelcomeEvents> myEvents = getWelcomeHomes(
     * configuration.getParentId()).getEvents();
     * for (NAWelcomeEvents myEvent : myEvents) {
     * if (myEvent.getPersonId().equals(getId())) {
     * this.event = myEvent;
     * break;
     * }
     * }
     * } catch (Exception e) {
     * this.event = null;
     * }
     * 
     * break;
     * }
     * }
     * } else {
     * for (NAWelcomePersons myPerson : getWelcomeHomes(configuration.getParentId())
     * .getPersons()) {
     * if (myPerson.getId().equalsIgnoreCase(configuration.getId())) {
     * this.module = myPerson;
     * super.updateChannels();
     * 
     * try {
     * List<NAWelcomeEvents> myEvents = getWelcomeHomes(configuration.getParentId())
     * .getEvents();
     * for (NAWelcomeEvents myEvent : myEvents) {
     * if (myEvent.getPersonId().equals(configuration.getId())) {
     * this.event = myEvent;
     * break;
     * }
     * }
     * 
     * } catch (Exception e) {
     * this.event = null;
     * }
     * 
     * break;
     * }
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
                case CHANNEL_WELCOME_PERSON_ID:
                    return module.getId() != null ? new StringType(module.getId()) : UnDefType.UNDEF;
                case CHANNEL_WELCOME_PERSON_LASTSEEN:
                    return module.getId() != null ? ChannelTypeUtils.toDateTimeType(module.getLastSeen())
                            : UnDefType.UNDEF;
                case CHANNEL_WELCOME_PERSON_OUTOFSIGHT:
                    return module.getOutOfSight() != null ? (module.getOutOfSight() ? OnOffType.ON : OnOffType.OFF)
                            : UnDefType.UNDEF;
                case CHANNEL_WELCOME_PERSON_FACEID:
                    return module.getFace().getId() != null ? new StringType(module.getFace().getId())
                            : UnDefType.UNDEF;
                case CHANNEL_WELCOME_PERSON_FACEVERSION:
                    return module.getFace().getVersion() != null ? new DecimalType(module.getFace().getVersion())
                            : UnDefType.UNDEF;
                case CHANNEL_WELCOME_PERSON_FACEKEY:
                    return module.getFace().getKey() != null ? new StringType(module.getFace().getKey())
                            : UnDefType.UNDEF;
                case CHANNEL_WELCOME_PERSON_PSEUDO:
                    return module.getPseudo() != null ? new StringType(module.getPseudo()) : UnDefType.UNDEF;
                case CHANNEL_WELCOME_PERSON_ATHOME:
                    return module.getOutOfSight() != null ? (module.getOutOfSight() ? OnOffType.OFF : OnOffType.ON)
                            : UnDefType.UNDEF;
                case CHANNEL_WELCOME_PERSON_LASTEVENTID:
                    return event.getId() != null ? new StringType(event.getId()) : UnDefType.UNDEF;
                case CHANNEL_WELCOME_PERSON_LASTMESSAGE:
                    return event.getMessage() != null ? new StringType(event.getMessage()) : UnDefType.UNDEF;
                case CHANNEL_WELCOME_PERSON_LASTTIME:
                    return event.getTime() != null ? ChannelTypeUtils.toDateTimeType(event.getTime()) : UnDefType.UNDEF;
                case CHANNEL_WELCOME_PERSON_AVATARPICTURE_URL:
                    return module.getFace() != null
                            ? getBridgeHandler().getPictureUrl(module.getFace().getId(), module.getFace().getKey())
                            : UnDefType.UNDEF;
                case CHANNEL_WELCOME_PERSON_LASTEVENTPICTURE_URL:
                    return event.getSnapshot() != null ? getBridgeHandler().getPictureUrl(event.getSnapshot().getId(),
                            event.getSnapshot().getKey()) : UnDefType.UNDEF;

                default:
                    return super.getNAThingProperty(chanelId);
            }
        } catch (Exception e) {
            return UnDefType.UNDEF;
        }
    }

    public void setEvent(NAWelcomeEvents module) {
        event = module;

    }

}
