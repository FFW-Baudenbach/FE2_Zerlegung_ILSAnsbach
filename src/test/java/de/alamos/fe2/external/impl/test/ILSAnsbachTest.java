package de.alamos.fe2.external.impl.test;

import de.alamos.fe2.external.impl.ILSAnsbach;
import de.alamos.fe2.external.impl.Parameter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

public class ILSAnsbachTest {

	@Test
	public void extract_emptyString() {
		ILSAnsbach impl = new ILSAnsbach();
		Map<String, String> map = impl.extract("");
		Assertions.assertNotNull(map);
		Assertions.assertEquals(1, map.size());
	}

	@Test
	public void extract_nullString() {
		ILSAnsbach impl = new ILSAnsbach();
		Map<String, String> map = impl.extract(null);
		Assertions.assertNotNull(map);
		Assertions.assertEquals(1, map.size());
	}

	@Test
	public void extract_nonsense() {
		ILSAnsbach impl = new ILSAnsbach();
		Map<String, String> map = impl.extract("Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.");
		Assertions.assertNotNull(map);
		Assertions.assertEquals(1, map.size());
	}

	@Test
	public void test_example01() throws IOException {
		String example1 = new String(Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("example01.txt")).readAllBytes());
		ILSAnsbach impl = new ILSAnsbach();
		Map<String, String> map = impl.extract(example1);
		Assertions.assertNotNull(map);
		Assertions.assertEquals(16, map.size());

		Assertions.assertEquals("B 5.1 201203 4711", map.get(Parameter.EINSATZNUMMER.getKey()));

		// Einsatzort
		String expFormattedEinsatzort = """
				Teststraße 42
				Testabschnitt
				Testobjekt
				91460 Baudenbach
				""".trim();
		Assertions.assertEquals(expFormattedEinsatzort, map.get(Parameter.EINSATZORT_FORMATIERT.getKey()));
		Assertions.assertEquals("Teststraße 42<br/>Testabschnitt<br/>Testobjekt<br/>91460 Baudenbach", map.get(Parameter.EINSATZORT_FORMATIERT_HTML.getKey()));
		Assertions.assertEquals("Teststraße", map.get(Parameter.STREET.getKey()));
		Assertions.assertEquals("42", map.get(Parameter.HOUSE.getKey()));
		Assertions.assertEquals("91460", map.get(Parameter.POSTCODE.getKey()));
		Assertions.assertEquals("Baudenbach", map.get(Parameter.CITY.getKey()));
		Assertions.assertEquals("Testobjekt", map.get(Parameter.OBJEKT.getKey()));
		Assertions.assertTrue(map.get(Parameter.EINSATZORT.getKey()).contains("X: 0123456 Y: 9876543"));

		// Einsatzgrund
		Assertions.assertTrue(map.get(Parameter.EINSATZGRUND.getKey()).contains("B1012"));

		// Einsatzmittel
		Assertions.assertTrue(map.get(Parameter.EINSATZMITTEL.getKey()).contains("FL BAUD 11/1"));
		Assertions.assertTrue(map.get(Parameter.EINSATZMITTEL.getKey()).contains("FL BAUD 42/1"));
		Assertions.assertEquals("FL BAUD 11/1" + System.lineSeparator() + "FL BAUD 42/1", map.get(Parameter.VEHICLES.getKey()));

		Assertions.assertEquals(map.get(Parameter.EINSATZMITTEL_VEHICLES.getKey()), map.get(Parameter.VEHICLES.getKey()));

		String expVehAlTxt = """
    			FL BAUD 11/1 (Ex-Warngerät)
				FL BAUD 42/1
				FL NEA-L 100/99 (KBM Mustermann)
				FL STG 48/1 (Pressluftatmer [Gerät + Maske])
				""".trim();
		Assertions.assertEquals(expVehAlTxt, map.get(Parameter.EINSATZMITTEL_LISTE.getKey()));

		String expVehAlTxtHtml = "<ul><li><span style=\"color: #5700a3;\"><strong>FL BAUD 11/1 (Ex-Warnger&auml;t)</strong></span></li><li><span style=\"color: #5700a3;\"><strong>FL BAUD 42/1</strong></span></li><li>FL NEA-L 100/99 (KBM Mustermann)</li><li>FL STG 48/1 (Pressluftatmer [Ger&auml;t + Maske])</li></ul>";
		Assertions.assertEquals(expVehAlTxtHtml, map.get(Parameter.EINSATZMITTEL_HTML.getKey()));

		// Bemerkung
		Assertions.assertEquals("Beispieltext", map.get(Parameter.BEMERKUNG.getKey()));
	}
}
