package de.alamos.fe2.external.impl;

import de.alamos.fe2.external.interfaces.IAlarmExtractor;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementierung für Stichworterkennung und Füllung weiterer Parameter für die
 * Zerlegungslogik von Alamos FE2.
 * Spezialisiert auf Fax der ILS Ansbach, sowie einige Spezialitäten von der FFW Baudenbach.
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

			if (!input.contains("ILS Ansbach"))
				throw new IllegalStateException("Seems not to be an alarm fax");

			// "Globale Ersetzung"
			var cleanedInput = applyGlobalReplacements(input);

			// "Textzerlegung"
			var divided = divideByKeywords(cleanedInput);
			result.putAll(divided);

			// "Adresserkennung" (street, house, postalCode, city)
			var address = extractAddress(result.get(Parameter.EINSATZORT.getKey()));
			result.putAll(address);

			// "Fahrzeugerkennung" (sadly no automatic feature of FE2, so do it in here)
			var vehicles = extractVehicles(result.get(Parameter.EINSATZMITTEL.getKey()));
			result.putAll(vehicles);
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
		result = result.replaceAll("#8", "#B"); // Happens on keyword
		result = result.replaceAll("(?i)StraBe", "Straße");
		result = result.replaceAll("(?i)0rt", "Ort");
		result = result.replaceAll("(?i)0bjekt", "Objekt");
		result = result.replaceAll("lnfo", "Info");

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

	private Map<String, String> extractVehicles(final String input) {

		var resultMap = new HashMap<String, String>(2);

		// The vehicles parameter needs to pass the vehicle names in order to be recognized by AM4 and aMobile Pro
		List<String> vehicles = new ArrayList<>();
		if (input.contains("FL BAUD 11/1"))
			vehicles.add("FL BAUD 11/1");
		if (input.contains("FL BAUD 42/1"))
			vehicles.add("FL BAUD 42/1");
		if (input.contains("FL BAUD 49/1"))
			vehicles.add("FL BAUD 49/1");
		resultMap.put(Parameter.VEHICLES.getKey(), String.join(System.lineSeparator(), vehicles));

		// In addition, parse each and every Einsatzmittel for alarmtext

		// Clean beginning and end first, then split
		String cleanedInput = input.replaceAll("^EINSATZMITTEL\\s*-*\\s*", "");
		cleanedInput = cleanedInput.replaceAll("\\s*-*\\s*$", "");
		String[] einsatzmittel = cleanedInput.split("Einsatzmittel\\s*:\\s*", 0);

		String vehiclesAlarmtext = "";
		// 5.1.3 NEA FF Baudenbach Alarmiert : 07.11.2020 15:11:54 Geforderte Ausstattung :
		for (String e : einsatzmittel) {
			// Ignore empty elements
			if (e == null || e.length() == 0) {
				continue;
			}

			// Replace dotted prefix
			e = e.replaceAll("^[\\d\\.\\s]*", "");

			// Extract Einheit and Ausstattung
			String einheit = e.substring(0, e.indexOf("Alarmiert")).trim();
			String ausstattung = e.substring(e.lastIndexOf(":") + 1).trim();

			// We are not interested in Infoalarm, nor in ourselves as this is clear
			if (einheit.contains("Infoalarm") || einheit.equals("NEA FF Baudenbach")) {
				continue;
			}

			// Too much prosa
			if (ausstattung.matches("Sonderausrüstung KB\\w")) {
				ausstattung = "";
			}

			// Ignore too short things
			if (ausstattung.length() > 3) {
				einheit += " (" + ausstattung + ")";
			}

			vehiclesAlarmtext += einheit + System.lineSeparator();
		}

		resultMap.put(Parameter.VEHICLES_ALARMTEXT.getKey(), vehiclesAlarmtext.trim());

		return resultMap;
	}

}
