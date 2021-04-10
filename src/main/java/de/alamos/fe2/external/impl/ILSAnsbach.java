package de.alamos.fe2.external.impl;

import de.alamos.fe2.external.interfaces.IAlarmExtractor;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.text.StringEscapeUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementierung für Stichworterkennung und Füllung weiterer Parameter für die
 * Zerlegungslogik von Alamos FE2.
 * Spezialisiert auf Fax der ILS Ansbach, sowie einige Spezialitäten von der FFW Baudenbach.
 */
public class ILSAnsbach implements IAlarmExtractor {

	private final String[] _knownVehicles = {"FL BAUD 11/1", "FL BAUD 42/1", "FL BAUD 49/1"};

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
			if (StringUtils.isBlank(input))
				throw new IllegalArgumentException("Not valid input passed");

			if (!StringUtils.contains(input, "ILS Ansbach"))
				throw new IllegalStateException("Term 'ILS Ansbach' cannot be found");

			// Equivalent to "Globale Ersetzung"
			var cleanedInput = applyGlobalReplacements(input);

			// Equivalent to "Stichwortzerlegung"
			var keywords = divideByKeywords(cleanedInput);
			result.putAll(keywords);

			// "Adresserkennung" (no equivalent in FE2, coordinates are still handled by FE2)
			var address = extractAddress(result.get(Parameter.EINSATZORT.getKey()));
			result.putAll(address);

			// "Fahrzeugerkennung" (no equivalent in FE2)
			var vehicles = extractVehicles(result.get(Parameter.EINSATZMITTEL.getKey()));
			result.putAll(vehicles);

			// Extract 'Einsatznummer'
			result.put(Parameter.EINSATZNUMMER.getKey(), extractEinsatznummer(cleanedInput));
		}
		catch (RuntimeException e) {
			// In case of an unhandled Exception, write it to parameter for traceability
			result.put(Parameter.ZERLEGUNG_LOG.getKey(), ExceptionUtils.getStackTrace(e));
		}
		return result;
	}

	/**
	 * Diese Methode ist angelehnt an die Globale Ersetzungslogik von FE2.
	 * Sinn und Zweck ist es, typische OCR Fehler zu beheben und generell bspw. Leerzeichen zu reduzieren
	 * um im späteren Verlauf auf einem definierten Text arbeiten zu können.
	 *
	 * @param input der Eingabetext
	 * @return bearbeiteter Text
	 */
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
		result = result.replaceAll("(?i)ENDE [EF]AX", "ENDE FAX");

		// Special handling address: Many whitespaces, harmonize to be able to extract later on easier
		result = result.replaceAll("Straße\\s*:\\s*", "Straße:");
		result = result.replaceAll("Haus-Nr\\.\\s*:\\s*", "Haus-Nr.:");
		result = result.replaceAll("Ort\\s*:\\s*", "Ort:");
		result = result.replaceAll("Objekt\\s*:\\s*", "Objekt:");
		result = result.replaceAll("Plannummer\\s*:\\s*", "Plannummer:");

		return result;
	}

	/**
	 * Diese Methode ist angelehnt an die Stichwortzerlegung in FE2.
	 * Ziel ist es das Alarmfax in einzelne Abschnitte zu unterteilen.
	 * Die einzelnen Teile werden direkt mit dem FE2 Parameter verknüpft
	 *
	 * @param input der Eingabetext
	 * @return eine Map von Parametern mit dem entsprechenden Teil des Eingabetextes
	 */
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
				Parameter.EINSATZGRUND.getKey(), einsatzgrund,
				Parameter.EINSATZMITTEL.getKey(), einsatzmittel,
				Parameter.BEMERKUNG.getKey(), bemerkung);
	}

	/**
	 * Diese Methode behandelt den Einsatzort-Block des Alarmtextes.
	 * Es wird versucht, so viele Informationen wie möglich aus dem Fax zu extrahieren, bspw. Ort, PLZ, ...
	 *
	 * @param input der Eingabetext
	 * @return eine Map mit allen gefundenen Parametern mit den entsprechenden Werten
	 */
	private Map<String, String> extractAddress(String input) {

		// Was cleaned previously
		int idxStrasse = input.indexOf("Straße:");
		int idxHausNr = input.indexOf("Haus-Nr.:");
		int idxOrt = input.indexOf("Ort:");
		int idxObjekt = input.indexOf("Objekt:");
		int idxPlannummer = input.indexOf("Plannummer:");

		String street = input.substring(idxStrasse + 7, idxHausNr);
		String house = input.substring(idxHausNr + 9, idxOrt);
		String city = input.substring(idxOrt + 4, idxObjekt);
		String objekt = input.substring(idxObjekt + 7, idxPlannummer);

		// Sometimes street contains '> Musterhausen'
		street = street.replaceAll("^\\s*>\\s*", "Richtung ");

		// Avoid double mentioning of city
		if (city.contains(" - ")) {
			city = city.substring(0, city.indexOf(" - "));
		}

		// Extract postal and if there remove from city parameter
		String postal = city.replaceAll("\\D+","");
		city = city.replaceAll(postal, "");

		// Generate formatted output
		String formatted = street.trim() + " " + house.trim() + System.lineSeparator();
		formatted += StringUtils.isBlank(objekt) ? "" : (objekt.trim() + System.lineSeparator());
		formatted += postal.trim() + " " + city.trim();
		String htmlFormatted = formatted.replaceAll(System.lineSeparator(), "<br/>");

		return Map.of(
				Parameter.EINSATZORT_FORMATIERT.getKey(), formatted.trim(),
				Parameter.EINSATZORT_FORMATIERT_HTML.getKey(), htmlFormatted.trim(),
				Parameter.STREET.getKey(), street.trim(),
				Parameter.HOUSE.getKey(), house.trim(),
				Parameter.POSTCODE.getKey(), postal.trim(),
				Parameter.CITY.getKey(), city.trim(),
				Parameter.OBJEKT.getKey(), objekt.trim());
	}

	/**
	 * Diese Methode behandelt den Einsatzmittel-Block des Alarmfaxes.
	 * Es werden verschiedene Parameter gesetzt, die dann später im Alarmablauf verwendet werden können.
	 *
	 * @param input der Eingabetext
	 * @return eine Map mit allen gefundenen Parametern mit den entsprechenden Werten
	 */
	private Map<String, String> extractVehicles(final String input) {

		var resultMap = new HashMap<String, String>(2);

		// The vehicles parameter needs to pass the vehicle names
		// in order to be recognized by AM4 and aMobile Pro.
		// Look in text for any occurrence of the vehicle key.
		List<String> vehicles = Arrays.stream(_knownVehicles).filter(input::contains).collect(Collectors.toList());
		resultMap.put(Parameter.VEHICLES.getKey(), String.join(System.lineSeparator(), vehicles));

		//-----------------------------------------------------------------------------
		// In addition, parse each and every Einsatzmittel for alarmtext plugin

		// Clean beginning and end first, then split
		String cleanedInput = input.replaceAll("^EINSATZMITTEL\\s*-*\\s*", "");
		cleanedInput = cleanedInput.replaceAll("\\s*-*\\s*$", "");
		String[] resources = cleanedInput.split("Einsatzmittel\\s*:\\s*", 0);

		List<String> allResources = new ArrayList<>();
		// 5.1.3 NEA FF Baudenbach Alarmiert : 07.11.2020 15:11:54 Geforderte Ausstattung :
		for (String resource : resources) {
			// Ignore empty elements
			if (StringUtils.isBlank(resource)) {
				continue;
			}

			// Replace dotted prefix
			resource = resource.replaceAll("^[\\d\\.\\s]*", "");

			// Extract unit (Einheit) and facilities (Ausstattungen)
			String unit = resource.substring(0, resource.indexOf("Alarmiert")).trim();
			String facilities = resource.substring(resource.lastIndexOf(":") + 1).trim();

			// We are not interested in Infoalarm, nor in ourselves as this is clear
			if (unit.contains("Infoalarm") || unit.equals("NEA FF Baudenbach")) {
				continue;
			}

			// Also some KBx Alarms not relevant
			if (unit.contains("NEA-L") && unit.contains("Abschnitt")) {
				continue;
			}

			// Too much prosa
			if (facilities.matches("Sonderausrüstung KB\\w")) {
				facilities = "";
			}

			// Ignore too short things
			if (facilities.length() > 3) {
				unit += " (" + facilities + ")";
			}

			allResources.add(unit);
		}

		resultMap.put(Parameter.EINSATZMITTEL_LISTE.getKey(), String.join(System.lineSeparator(), allResources));
		resultMap.put(Parameter.EINSATZMITTEL_HTML.getKey(), generateResourcesAsHtml(allResources));

		return resultMap;
	}

	/**
	 * Diese Hilfsmethode baut aus der Liste von bereits aufbereiteten Einsatzmitteln
	 * einen HTML-Formatierten Text, der bspw. für den Mail-Versand später benutzt werden kann.
	 * Es wird als Stichpunktliste formatiert. Eigene Einheiten werden hervorgehoben.
	 *
	 * @param resources Liste von Einsatzmitteln
	 * @return Einsatzmittel mit
	 */
	private String generateResourcesAsHtml(List<String> resources)
	{
		StringBuilder builder = new StringBuilder();
		builder.append("<ul>");
		for (String resource : resources) {
			builder.append("<li>");
			if (Arrays.stream(_knownVehicles).anyMatch(resource::contains)) {
				builder.append("<span style=\"color: #5700a3;\"><strong>");
				builder.append(StringEscapeUtils.escapeHtml4(resource));
				builder.append("</strong></span>");
			}
			else {
				builder.append(StringEscapeUtils.escapeHtml4(resource));
			}
			builder.append("</li>");
		}
		builder.append("</ul>");

		return builder.toString();
	}

	/**
	 * Extract Einsatznummer from input
	 * @param input
	 * @return
	 */
	private String extractEinsatznummer(String input) {
		int idxEinsatznummer = input.indexOf("Einsatznummer:");
		int idxEnd = input.indexOf(" - ", idxEinsatznummer);
		String einsatznummer = input.substring(idxEinsatznummer + 14, idxEnd);
		return einsatznummer.trim();
	}

}
