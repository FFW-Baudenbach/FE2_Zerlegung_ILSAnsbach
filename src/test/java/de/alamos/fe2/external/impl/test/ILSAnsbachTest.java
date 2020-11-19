package de.alamos.fe2.external.impl.test;

import de.alamos.fe2.external.impl.ILSAnsbach;
import de.alamos.fe2.external.impl.Parameter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Map;

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
	public void test_example1() throws IOException {
		String example1 = new String(getClass().getClassLoader().getResourceAsStream("example01.txt").readAllBytes());
		ILSAnsbach impl = new ILSAnsbach();
		Map<String, String> map = impl.extract(example1);
		Assertions.assertNotNull(map);
		Assertions.assertEquals(9, map.size());

		// Einsatzort
		Assertions.assertEquals("Teststraße", map.get(Parameter.STREET.getKey()));
		Assertions.assertEquals("42", map.get(Parameter.HOUSE.getKey()));
		Assertions.assertEquals("91460", map.get(Parameter.POSTALCODE.getKey()));
		Assertions.assertEquals("Baudenbach", map.get(Parameter.CITY.getKey()));
		Assertions.assertTrue(map.get(Parameter.EINSATZORT.getKey()).contains("X: 0123456 Y: 9876543"));

		// Einsatzgrund
		Assertions.assertTrue(map.get(Parameter.EINSATZGRUNG.getKey()).contains("B1012"));

		// Einsatzmittel
		Assertions.assertTrue(map.get(Parameter.EINSATZMITTEL.getKey()).contains("FL BAUD 11/1"));
		Assertions.assertTrue(map.get(Parameter.EINSATZMITTEL.getKey()).contains("FL BAUD 42/1"));
		Assertions.assertEquals("FL BAUD 11/1\nFL BAUD 42/1", map.get(Parameter.VEHICLES.getKey()));

		// Bemerkung
		Assertions.assertEquals("Beispieltext", map.get(Parameter.BEMERKUNG.getKey()));
	}
}
