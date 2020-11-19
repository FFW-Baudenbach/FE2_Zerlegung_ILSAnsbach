package de.alamos.fe2.external.impl;

import de.alamos.fe2.external.interfaces.IAlarmExtractor;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	public Map<String, String> extract(final String input) {
		Map<String, String> result = new HashMap<>();
		try {
			if (input == null)
				throw new IllegalArgumentException("Input is null");

			if (!input.contains("ILS Ansbach")) {
				throw new IllegalStateException("Seems not to be an alarm fax");
			}

			// First make some general cleanup
			var cleanedInput = applyGlobalReplacements(input);

			// Split by keyword
			var divided = divideByKeywords(cleanedInput);
			result.putAll(divided);

			// Extract address parameters (street, house, postalCode, city)
			var address = extractAddress(result.get(Parameter.EINSATZORT.getKey()));
			result.putAll(address);

			// Extract vehicles
			var vehicles = extractVehicles(result.get(Parameter.EINSATZMITTEL.getKey()));
			result.put(Parameter.VEHICLES.getKey(), vehicles);
		}
		catch (RuntimeException e) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);

			result.put(Parameter.ZERLEGUNG_LOG.getKey(), sw.toString());
		}
		return result;
	}

	private String applyGlobalReplacements(final String input) {

		String result = input;

		// Fix common ocr mistakes
		result = result.replaceAll("-+", "-");
		result = result.replaceAll("\\s+", " ");
		result = result.replaceAll("=", ":");
		result = result.replaceAll("#8", "#B"); // Happens on
		result = result.replaceAll("(?i)StraBe", "Straße");
		result = result.replaceAll("(?i)0rt", "Ort");
		result = result.replaceAll("(?i)0bjekt", "Objekt");

		// Special handling address: Many whitespaces, harmonize to be able to extract later on better
		result = result.replaceAll("Straße\\s*:\\s*", "Straße:");
		result = result.replaceAll("Haus-Nr\\.\\s*:\\s*", "Haus-Nr.:");
		result = result.replaceAll("Ort\\s*:\\s*", "Ort:");
		result = result.replaceAll("Objekt\\s*:\\s*", "Objekt:");

		return result;
	}

	private Map<String, String> divideByKeywords(final String input) {

		int idxEinsatzOrt = input.indexOf("EINSATZORT");
		int idxZielOrt = input.indexOf("ZIELORT");
		int idxEinsatzGrund = input.indexOf("EINSATZGRUND");
		int idxEinsatzMittel = input.indexOf("EINSATZMITTEL");
		int idxBemerkung = input.indexOf("BEMERKUNG");
		int idxEndeFax = input.indexOf("ENDE FAX");

		// Sometimes 'Zielort' not part of Fax, take 'Einsatzgrund' then
		if (idxZielOrt == -1) {
			idxZielOrt = idxEinsatzGrund;
		}

		String einsatzort = input.substring(idxEinsatzOrt, idxZielOrt);
		String einsatzgrund = input.substring(idxEinsatzGrund, idxEinsatzMittel);
		String einsatzmittel = input.substring(idxEinsatzMittel, idxBemerkung);
		String bemerkung = input.substring(idxBemerkung, idxEndeFax);

		// Clean the field form any surroundings
		bemerkung = bemerkung.substring(9).replaceAll("(-)+", " ").trim();

		return Map.of(
				Parameter.EINSATZORT.getKey(), einsatzort,
				Parameter.EINSATZGRUNG.getKey(), einsatzgrund,
				Parameter.EINSATZMITTEL.getKey(), einsatzmittel,
				Parameter.BEMERKUNG.getKey(), bemerkung);
	}

	private Map<String, String> extractAddress(String input) {

		// Was cleaned previously
		int idxStrasse = input.indexOf("Straße:");
		int idxHausNr = input.indexOf("Haus-Nr.:");
		int idxOrt = input.indexOf("Ort:");
		int idxObjekt = input.indexOf("Objekt:");

		String street = input.substring(idxStrasse + 7, idxHausNr);
		String house = input.substring(idxHausNr + 9, idxOrt);
		String city = input.substring(idxOrt + 4, idxObjekt);

		// Avoid double mentioning of city
		if (city.contains(" - ")) {
			city = city.substring(0, city.indexOf(" - "));
		}

		// Extract postal and if there remove from city parameter
		String postal = city.replaceAll("\\D+","");
		city = city.replaceAll(postal, "");

		return Map.of(
				Parameter.STREET.getKey(), street.trim(),
				Parameter.HOUSE.getKey(), house.trim(),
				Parameter.POSTALCODE.getKey(), postal.trim(),
				Parameter.CITY.getKey(), city.trim());
	}

	private String extractVehicles(final String input) {

		List<String> vehicles = new ArrayList<>();

		// Simplest approach: Just parse for our vehicles
		if (input.contains("FL BAUD 11/1"))
			vehicles.add("FL BAUD 11/1");
		if (input.contains("FL BAUD 42/1"))
			vehicles.add("FL BAUD 42/1");
		if (input.contains("FL BAUD 49/1"))
			vehicles.add("FL BAUD 49/1");

		return String.join(System.lineSeparator(), vehicles);
	}

}
