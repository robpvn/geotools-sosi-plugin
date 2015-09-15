package org.geotools.data.sosi;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.FeatureReader;
import org.geotools.data.Query;
import org.geotools.data.Transaction;
import org.junit.Test;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

/**
 * Unit test for the SOSI Reader. All of the tests are lifted from JSOSI and
 * adapted to use the GeoTools facade.
 */
public class SOSITest {

	private List<SimpleFeature> getFeatures(String filename) throws URISyntaxException, IOException {
		FeatureReader<SimpleFeatureType, SimpleFeature> reader = getReader(filename);

		List<SimpleFeature> features = new ArrayList<SimpleFeature>();
		while (reader.hasNext()) {
			features.add(reader.next());
		}
		return features;
	}

	private FeatureReader<SimpleFeatureType, SimpleFeature> getReader(String filename)
			throws URISyntaxException, IOException {
		URL url = SOSITest.class.getResource(filename + ".SOS");
		File file = new File(url.toURI());
		assertTrue(file.canRead());

		// Define the datastore
		Map<String, Serializable> params = new HashMap<String, Serializable>();
		params.put("file", file);
		DataStore datastore = DataStoreFinder.getDataStore(params);

		assertNotNull("Couldn't get the right datastore", datastore);

		// Make a query for the typename (we only have the one)
		Query query = new Query(filename);

		// Start reading data
		FeatureReader<SimpleFeatureType, SimpleFeature> reader = datastore.getFeatureReader(query,
				Transaction.AUTO_COMMIT);
		return reader;
	}

	@Test
	public void testAddress() throws Exception {
		List<SimpleFeature> features = getFeatures("0219Adresser");

		assertEquals(33545, features.size());

		SimpleFeature f = features.get(0);
		assertEquals("Hans Hanssens vei !nocomment", f.getAttribute("GATENAVN"));
		assertEquals("SNARØYA", f.getAttribute("POSTNAVN"));
		assertEquals("0219", f.getAttribute("KOMM"));
		assertEquals("4", f.getAttribute("HUSNR"));
		assertNull(f.getAttribute("NØ"));

		Geometry geometry = (Geometry) f.getDefaultGeometry();
		assertNotNull(geometry);
		assertTrue(geometry instanceof Point);
		assertEquals(1, geometry.getCoordinates().length);
		assertEquals(253673.99, geometry.getCoordinates()[0].x, 0.01);
		assertEquals(6645919.76, geometry.getCoordinates()[0].y, 0.01);
	}

	@Test
	public void testVbase() throws IOException, URISyntaxException {
		FeatureReader<SimpleFeatureType, SimpleFeature> reader = getReader("Vbase_02");

		assertEquals("EPSG:ETRS89 / UTM zone 33N",
				reader.getFeatureType().getCoordinateReferenceSystem().getName().toString());

		SimpleFeature f1 = reader.next();
		assertEquals("P V 99834", f1.getAttribute("VNR"));
		assertNull(f1.getAttribute("GATENAVN"));
		Geometry geometry1 = (Geometry) f1.getDefaultGeometry();
		assertEquals(12, geometry1.getCoordinates().length);
		assertTrue(geometry1 instanceof LineString);

		SimpleFeature f2 = reader.next();
		assertEquals("Åsveien", f2.getAttribute("GATENAVN"));
		Geometry geometry2 = (Geometry) f2.getDefaultGeometry();
		assertEquals(15, geometry2.getCoordinates().length);
		assertTrue(geometry2 instanceof LineString);

		int count = 1;
		SimpleFeature f = null;
		while (reader.hasNext()) {
			f = reader.next();
			count++;
			assertNotNull(f.getDefaultGeometry());
		}

		assertTrue(count > 10000);

	}

	@Test
	public void testArealdekke() throws IOException, URISyntaxException {
		FeatureReader<SimpleFeatureType, SimpleFeature> reader = getReader("1421_Arealdekke");

		assertEquals("EPSG:ETRS89 / UTM zone 32N",
				reader.getFeatureType().getCoordinateReferenceSystem().getName().toString());

		SimpleFeature f1 = reader.next();
		assertEquals("10000101", f1.getAttribute("OPPDATERINGSDATO"));
		assertEquals("ÅpentOmråde", f1.getAttribute("OBJTYPE"));
		assertNull(f1.getAttribute("GATENAVN"));
		assertTrue(f1.getDefaultGeometry() instanceof Polygon);
		assertTrue(((Geometry) f1.getDefaultGeometry()).isValid());

		int count = 0;
		Set<String> objtypes = new HashSet<String>();

		SimpleFeature f = null;
		//SimpleFeature f5763 = null;
		while (reader.hasNext()) {
			f = reader.next();
			String objtype = (String) f.getAttribute("OBJTYPE");
			assertNotNull(objtype);

			count++;
			objtypes.add(objtype);

			// TODO: Lots of SOSI-specific stuff there

			/*
			 * if (f.getID().intValue() == 5763) { f5763 = f; } if
			 * (f.getGeometryType() != GeometryType.KURVE) { continue; }
			 * 
			 */
		}
		/*
		 * assertNotNull(f5763); assertEquals(5763, f5763.getID().intValue());
		 * assertEquals("Innsjø", f5763.get("OBJTYPE"));
		 * assertTrue(f5763.getGeometry() instanceof Polygon);
		 * assertTrue(f5763.getGeometry().isValid());
		 */
		assertEquals(21313, count);
		assertEquals(27, objtypes.size());

		reader.close();
	}

	@Test
	public void testNavnISO() throws IOException, URISyntaxException {
		FeatureReader<SimpleFeatureType, SimpleFeature> reader = getReader("1421_Navn_iso");

		assertEquals("EPSG:ETRS89 / UTM zone 32N",
				reader.getFeatureType().getCoordinateReferenceSystem().getName().toString());

		SimpleFeature fi = null;
		while (reader.hasNext()) {
			fi = reader.next();
			assertEquals("Skrivemåte", fi.getAttribute("OBJTYPE"));
			assertNotNull(fi.getDefaultGeometry());
			assertTrue(((Geometry) fi.getDefaultGeometry()).isValid());
		}
	}

	@Test
	public void testMissingGeometry() throws IOException, URISyntaxException {
		FeatureReader<SimpleFeatureType, SimpleFeature> reader = getReader("0540_Navn_utf8");

		assertEquals("EPSG:ETRS89 / UTM zone 33N",
				reader.getFeatureType().getCoordinateReferenceSystem().getName().toString());

		SimpleFeature fi = null;
		int count = 0;
		while (reader.hasNext()) {
			fi = reader.next();
			assertNotNull(fi.getDefaultGeometry());
			if ("Fønhuskoia".equals(fi.getAttribute("STRENG"))) {
				assertTrue(((Geometry) fi.getDefaultGeometry()).isEmpty());
			} else {
				assertFalse(((Geometry) fi.getDefaultGeometry()).isEmpty());
			}
			count++;
		}
		assertEquals(2304, count);
	}

	@Test
	public void testEnheterGrunnkrets() throws Exception {
		FeatureReader<SimpleFeatureType, SimpleFeature> reader = getReader("STAT_enheter_grunnkretser");

		assertEquals("EPSG:ETRS89 / UTM zone 33N",
				reader.getFeatureType().getCoordinateReferenceSystem().getName().toString());

		SimpleFeature fi = null;
		int count = 0;
		Set<String> objtypes = new HashSet<String>();
		while (reader.hasNext()) {
			fi = reader.next();
			assertNotNull(fi);
			assertNotNull(fi.getDefaultGeometry());
			count++;
			objtypes.add(fi.getAttribute("OBJTYPE").toString());
		}
		assertEquals(8, objtypes.size());
		assertEquals(79724, count);
	}

}
