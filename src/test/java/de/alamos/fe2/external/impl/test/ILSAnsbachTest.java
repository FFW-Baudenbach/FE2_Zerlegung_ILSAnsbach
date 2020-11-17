package de.alamos.fe2.external.impl.test;

import de.alamos.fe2.external.impl.ILSAnsbach;
import de.alamos.fe2.external.impl.Parameter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;

public class ILSAnsbachTest {

	@Test
	public void extract_emptyString() {
		ILSAnsbach impl = new ILSAnsbach();
		Map<String, String> map = impl.extract("");
		Assertions.assertNotNull(map);
		Assertions.assertEquals(8, map.size());
	}

	@Test
	public void extract_nullString() {
		ILSAnsbach impl = new ILSAnsbach();
		Map<String, String> map = impl.extract(null);
		Assertions.assertNotNull(map);
		Assertions.assertEquals(1, map.size());
		Assertions.assertTrue(map.containsKey(Parameter.ZERLEGUNG_LOG.getKey()));
	}

}
