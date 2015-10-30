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
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.feature.FeatureIterator;
import org.junit.Test;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

/**
 * Unit test for the SOSI Reader. All of the tests are lifted from JSOSI and
 * adapted to use the GeoTools facade. Probably more integration test than unit test.
 */
public class SOSITest {

	private SimpleFeatureCollection getFeatureCollection(String filename, String extension) throws URISyntaxException, IOException {
		URL url = SOSITest.class.getResource(String.format("%s.%s", filename, extension));
		File file = new File(url.toURI());
		assertTrue(file.canRead());

		// Define the datastore
		Map<String, Serializable> params = new HashMap<String, Serializable>();
		params.put("file", file);
		DataStore datastore = DataStoreFinder.getDataStore(params);

		assertNotNull("Couldn't get the right datastore", datastore);
		
		SimpleFeatureSource source = datastore.getFeatureSource(filename);
		SimpleFeatureCollection featureColl = source.getFeatures();
		return featureColl;
	}

	@Test
	public void testAddress() throws Exception {
		SimpleFeatureCollection featureColl = getFeatureCollection("0219Adresser","SOS");
		
		FeatureIterator<SimpleFeature> iterator = featureColl.features();

		assertEquals(33545, featureColl.size());

		SimpleFeature f = iterator.next();
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
		SimpleFeatureCollection featureColl = getFeatureCollection("Vbase_02","SOS");
		FeatureIterator<SimpleFeature> iterator = featureColl.features();

		assertEquals("EPSG:ETRS89 / UTM zone 33N",
				featureColl.getSchema().getCoordinateReferenceSystem().getName().toString());

		SimpleFeature f1 = iterator.next();
		assertEquals("P V 99834", f1.getAttribute("VNR"));
		assertNull(f1.getAttribute("GATENAVN"));
		Geometry geometry1 = (Geometry) f1.getDefaultGeometry();
		assertEquals(12, geometry1.getCoordinates().length);
		assertTrue(geometry1 instanceof LineString);

		SimpleFeature f2 = iterator.next();
		assertEquals("Åsveien", f2.getAttribute("GATENAVN"));
		Geometry geometry2 = (Geometry) f2.getDefaultGeometry();
		assertEquals(15, geometry2.getCoordinates().length);
		assertTrue(geometry2 instanceof LineString);

		int count = 1;
		SimpleFeature f = null;
		while (iterator.hasNext()) {
			f = iterator.next();
			count++;
			assertNotNull(f.getDefaultGeometry());
		}

		assertTrue(count > 10000);

	}

	@Test
	public void testArealdekke() throws IOException, URISyntaxException {
		SimpleFeatureCollection featureColl = getFeatureCollection("1421_Arealdekke","sos");
		FeatureIterator<SimpleFeature> iterator = featureColl.features();

		assertEquals("EPSG:ETRS89 / UTM zone 32N",
				featureColl.getSchema().getCoordinateReferenceSystem().getName().toString());

		SimpleFeature f1 = iterator.next();
		assertEquals("10000101", f1.getAttribute("OPPDATERINGSDATO"));
		assertEquals("ÅpentOmråde", f1.getAttribute("OBJTYPE"));
		assertNull(f1.getAttribute("GATENAVN"));
		assertTrue(f1.getDefaultGeometry() instanceof Polygon);
		assertTrue(((Geometry) f1.getDefaultGeometry()).isValid());

		int count = 0;
		Set<String> objtypes = new HashSet<String>();

		SimpleFeature f = null;
		//SimpleFeature f5763 = null;
		while (iterator.hasNext()) {
			f = iterator.next();
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

	}

	@Test
	public void testNavnISO() throws IOException, URISyntaxException {
		SimpleFeatureCollection featureColl = getFeatureCollection("1421_Navn_iso","sos");
		FeatureIterator<SimpleFeature> iterator = featureColl.features();

		assertEquals("EPSG:ETRS89 / UTM zone 32N",
				featureColl.getSchema().getCoordinateReferenceSystem().getName().toString());

		SimpleFeature fi = null;
		while (iterator.hasNext()) {
			fi = iterator.next();
			assertEquals("Skrivemåte", fi.getAttribute("OBJTYPE"));
			assertNotNull(fi.getDefaultGeometry());
			assertTrue(((Geometry) fi.getDefaultGeometry()).isValid());
		}
	}

	@Test
	public void testMissingGeometry() throws IOException, URISyntaxException {
		SimpleFeatureCollection featureColl = getFeatureCollection("0540_Navn_utf8","sos");
		FeatureIterator<SimpleFeature> iterator = featureColl.features();

		assertEquals("EPSG:ETRS89 / UTM zone 33N",
				featureColl.getSchema().getCoordinateReferenceSystem().getName().toString());

		SimpleFeature fi = null;
		int count = 0;
		while (iterator.hasNext()) {
			fi = iterator.next();
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
		SimpleFeatureCollection featureColl = getFeatureCollection("STAT_enheter_grunnkretser","sos");
		FeatureIterator<SimpleFeature> iterator = featureColl.features();

		assertEquals("EPSG:ETRS89 / UTM zone 33N",
				featureColl.getSchema().getCoordinateReferenceSystem().getName().toString());

		SimpleFeature fi = null;
		int count = 0;
		Set<String> objtypes = new HashSet<String>();
		while (iterator.hasNext()) {
			fi = iterator.next();
			assertNotNull(fi);
			assertNotNull(fi.getDefaultGeometry());
			count++;
			objtypes.add(fi.getAttribute("OBJTYPE").toString());
		}
		assertEquals(8, objtypes.size());
		assertEquals(79724, count);
	}

}
