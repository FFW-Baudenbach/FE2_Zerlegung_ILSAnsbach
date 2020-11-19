package de.alamos.fe2.external.impl;

import de.alamos.fe2.external.enums.EAlarmDataEntries;

/**
 * This is a subset of the official Alamos Enums.
 * Intention is that we don't need everything and that we want to 
 * extend by custom parameters.
 */
public enum Parameter {
    //First the official ones copied from class
    STREET(EAlarmDataEntries.STREET.getKey()),
    HOUSE(EAlarmDataEntries.HOUSE.getKey()),
    CITY(EAlarmDataEntries.CITY.getKey()),
    POSTALCODE(EAlarmDataEntries.POSTALCODE.getKey()),
    VEHICLES("vehicles"), // should be part of official enum

    //Now custom ones
    BEMERKUNG("custom_bemerkung"),
    EINSATZGRUNG("custom_einsatzgrund"),
    EINSATZMITTEL("custom_einsatzmittel"),
    EINSATZORT("custom_einsatzort"),
    ZERLEGUNG_LOG("custom_zerlegung_log");

    private final String key;

    Parameter(String key) {
        this.key = key;
    }

    public String getKey() {
        return this.key;
    }
}
