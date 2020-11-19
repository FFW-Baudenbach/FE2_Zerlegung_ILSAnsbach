package de.alamos.fe2.external.impl;

import de.alamos.fe2.external.enums.EAlarmDataEntries;

public enum Parameter {
    //First the official ones copied from class
    TEXT(EAlarmDataEntries.TEXT.getKey()),
    STREET(EAlarmDataEntries.STREET.getKey()),
    HOUSE(EAlarmDataEntries.HOUSE.getKey()),
    CITY(EAlarmDataEntries.CITY.getKey()),
    POSTALCODE(EAlarmDataEntries.POSTALCODE.getKey()),
    BUILDING_NAME(EAlarmDataEntries.BUILDING_NAME.getKey()),
    LOCATION_ADDITIOnAL(EAlarmDataEntries.LOCATION_ADDITIOnAL.getKey()),
    KEYWORD(EAlarmDataEntries.KEYWORD.getKey()),
    KEYWORD_DESCRIPTION(EAlarmDataEntries.KEYWORD_DESCRIPTION.getKey()),
    KEYWORD_IDENTIFICATION(EAlarmDataEntries.KEYWORD_IDENTIFICATION.getKey()),
    KEYWORD_ADDITIONAL(EAlarmDataEntries.KEYWORD_ADDITIONAL.getKey()),
    KEYWORD_CATEGORY(EAlarmDataEntries.KEYWORD_CATEGORY.getKey()),
    CALLER(EAlarmDataEntries.CALLER.getKey()),
    CALLER_CONTACT(EAlarmDataEntries.CALLER_CONTACT.getKey()),
    LAT(EAlarmDataEntries.LAT.getKey()),
    LNG(EAlarmDataEntries.LNG.getKey()),
    DESTINATION(EAlarmDataEntries.DESTINATION.getKey()),
    VEHICLES("vehicles"), // should be part of enum

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
