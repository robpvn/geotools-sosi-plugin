package org.geotools.data.sosi;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.geotools.data.Query;
import org.geotools.data.store.ContentDataStore;
import org.geotools.data.store.ContentEntry;
import org.geotools.data.store.ContentFeatureSource;
import org.geotools.feature.NameImpl;
import org.opengis.feature.type.Name;

import no.jsosi.SosiReader;

/**
 * Reads Norwegian SOSI geodata files.
 * 
 * @author robnor
 *
 */
public class SOSIDataStore extends ContentDataStore {

	File file;

	public SOSIDataStore(File file) {
		this.file = file;
	}

	@Override
	protected ContentFeatureSource createFeatureSource(ContentEntry entry) throws IOException {
		return new SOSIFeatureSource(entry, Query.ALL);
	}

	@Override
	protected List<Name> createTypeNames() throws IOException {
		String name = file.getName();
		name = name.substring(0, name.lastIndexOf('.'));

		Name typeName = new NameImpl(name);
		return Collections.singletonList(typeName);
	}

	/**
	 * Allow read access to file; for our package visible "friends". Please
	 * close the reader when done.
	 * 
	 * @return CsvReader for file
	 */
	SosiReader read() throws IOException {
		SosiReader reader = new SosiReader(file);
		return reader;
	}

}
