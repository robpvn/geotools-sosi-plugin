package org.geotools.data.sosi;

import java.io.IOException;
import java.util.Map;
import java.util.NoSuchElementException;

import org.geotools.data.FeatureReader;
import org.geotools.data.Query;
import org.geotools.data.store.ContentState;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import no.jsosi.Feature;
import no.jsosi.SosiReader;

public class SOSIFeatureReader implements FeatureReader<SimpleFeatureType, SimpleFeature> {

	/** State used when reading file */
    protected ContentState state;
    
    /**
     * Current row number - used in the generation of FeatureId.
     * TODO: Subclass ContentState to track row
     */
    private int row;

    protected SosiReader reader;
    
    /** Utility class used to build features */
    protected SimpleFeatureBuilder builder;
    
    private Feature next;
	
	public SOSIFeatureReader(ContentState state, Query query) throws IOException {
		this.state = state;
		SOSIDataStore store = (SOSIDataStore) state.getEntry().getDataStore();
		reader = store.read();
		builder = new SimpleFeatureBuilder( state.getFeatureType() );
        row = 0;
		//TODO: We don't have any sanity checks on the file
	}

	public void close() throws IOException {
		if( reader != null ){
            reader.close();
            reader = null;
        }
        builder = null;

	}

	public SimpleFeatureType getFeatureType() {
        return (SimpleFeatureType) state.getFeatureType();
	}

	public boolean hasNext() throws IOException {
		if( next != null ){
            return true;
        }
        else {
            next = readFeature(); // read next feature so we can check
            return next != null;
        }
	}

	public SimpleFeature next() throws IOException, IllegalArgumentException, NoSuchElementException {
		Feature feature;
        if( next != null ){
            feature = next;
            next = null;
        }
        else {
            feature = readFeature();
        }
        
        //From JSOSI to SimpleFeature
        
        feature.getGeometry();
        Map<String, Object> attributes = feature.getAttributeMap();

        
        for (String key : attributes.keySet()) {
        	//TODO: Not sure what to do about missing attrs!
        	if (attributes.containsKey(key)) {
        		builder.set(key	, attributes.get(key));
        	}
		}
        
        builder.set("Geometry", feature.getGeometry());
        
        return this.buildFeature();
	}
	
	/** Build feature using the current row number to generate FeatureId */
    protected SimpleFeature buildFeature() {
        row += 1;
        return builder.buildFeature( state.getEntry().getTypeName()+"."+row );
    }
	
	private Feature readFeature() throws IOException {
		return reader.nextFeature();
	}

}
