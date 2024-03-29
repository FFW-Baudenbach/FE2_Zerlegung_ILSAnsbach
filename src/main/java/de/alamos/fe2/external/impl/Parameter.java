package de.alamos.fe2.external.impl;

import de.alamos.fe2.external.enums.EAlarmDataEntries;

/**
 * Definition von benutzten Parametern für diese Zerlegung.
 * Handelt es sich um einen 'offiziellen' Parameter, so wird auch der Key aus der FE2-Library verwendet.
 * Mit dieser Zwischendefinition können FE2 Parameter sowie eigene auf die selbe Art verwendet werden.
 */
public enum Parameter {
    // First the official ones copied from class
    STREET(EAlarmDataEntries.STREET.getKey()),
    HOUSE(EAlarmDataEntries.HOUSE.getKey()),
    CITY(EAlarmDataEntries.CITY.getKey()),
    POSTCODE(EAlarmDataEntries.POSTALCODE.getKey()),
    VEHICLES("vehicles"), // should be part of official enum

    // Now custom ones. Prefixed with "custom_" to avoid any side effect with FE2-Parameters.
    BEMERKUNG("custom_bemerkung"),
    EINSATZGRUND("custom_einsatzgrund"),
    EINSATZMITTEL("custom_einsatzmittel"),
    EINSATZNUMMER("custom_einsatznummer"),
    EINSATZORT("custom_einsatzort"),
    EINSATZORT_FORMATIERT("custom_einsatzort_formatiert"),
    EINSATZORT_FORMATIERT_HTML("custom_einsatzort_formatiert_html"),
    EINSATZMITTEL_VEHICLES("custom_vehicles"),
    EINSATZMITTEL_LISTE("custom_einsatzmittel_liste"),
    EINSATZMITTEL_HTML("custom_einsatzmittel_html"),
    OBJEKT("custom_objekt"),
    ZERLEGUNG_LOG("custom_zerlegung_log");

    private final String key;

    Parameter(String key) {
        this.key = key;
    }

    public String getKey() {
        return this.key;
    }
}
