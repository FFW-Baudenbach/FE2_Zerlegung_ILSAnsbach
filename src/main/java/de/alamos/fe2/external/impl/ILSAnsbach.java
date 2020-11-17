package de.alamos.fe2.external.impl;

import java.util.*;

import de.alamos.fe2.external.interfaces.IAlarmExtractor;

/**
 * Implementierung für Stichworterkennung und Füllung weiterer Parameter für die Zerlegungslogik von Alamos FE2.
 */
public class ILSAnsbach implements IAlarmExtractor {

	/**
	 * Zerlegt den Textinput von der Texterkennung und extrahiert
	 * Werte in Key/Value Paare für die Weiterverarbeitung mit FE2.
	 * 
	 * @param input Input String von Texterkennung.
	 * @return Map von Parametern mit Werten.
	 */
	@Override
	public Map<String, String> extract(String input) {
		input = Objects.requireNonNullElse (input, "");
		Map<String, String> result = new HashMap<>();

		// First make some general cleanup
		var cleanedInput = cleanInput(input);

		// Split by keyword
		var divided = divideByKeywords(cleanedInput);
		result.putAll(divided);

		// Extract address parameters (street, house, city)
		var address = extractAddress(result.get(Parameter.EINSATZORT.getKey()));
		result.putAll(address);

		// Extract vehicles
		var vehicles = extractVehicles(result.get(Parameter.EINSATZMITTEL.getKey()));
		result.put(Parameter.VEHICLES.getKey(), vehicles);

		return result;
	}

	private String cleanInput(final String input) {

		//TODO: General regex

		return input;
	}

	private Map<String, String> divideByKeywords(final String input) {

		//TODO: Stichwortzerlegung
		/*
		EINSATZORT;ZIELORT;einsatzort
		EINSATZGRUND;EINSATZMITTEL;einsatzgrund
		EINSATZMITTEL;BEMERKUNG;einsatzmittel
		BEMERKUNG;ENDE FAX;bemerkung
		 */

		String einsatzort = "";
		String einsatzgrund = "";
		String einsatzmittel = "";
		String bemerkung = "";

		return Map.of(
				Parameter.EINSATZORT.getKey(), einsatzort,
				Parameter.EINSATZGRUNG.getKey(), einsatzgrund,
				Parameter.EINSATZMITTEL.getKey(), einsatzmittel,
				Parameter.BEMERKUNG.getKey(), bemerkung);
	}

	private Map<String, String> extractAddress(String input) {

		//TODO: Address

		String street = "";
		String house = "";
		String city = "";

		return Map.of(
				Parameter.STREET.getKey(), street,
				Parameter.HOUSE.getKey(), house,
				Parameter.CITY.getKey(), city);
	}

	private String extractVehicles(final String input) {

		List<String> vehicles = new ArrayList<>();

		// TODO Extract vehicles

		return String.join(System.lineSeparator(), vehicles);
	}

}
