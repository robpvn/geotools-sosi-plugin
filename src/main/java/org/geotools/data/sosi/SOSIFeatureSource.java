package org.geotools.data.sosi;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.geotools.data.FeatureReader;
import org.geotools.data.Query;
import org.geotools.data.store.ContentEntry;
import org.geotools.data.store.ContentFeatureSource;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;

import com.vividsolutions.jts.geom.Geometry;

import no.jsosi.Feature;
import no.jsosi.SosiReader;

public class SOSIFeatureSource extends ContentFeatureSource {

	public SOSIFeatureSource(ContentEntry entry, Query query) {
		super(entry, query);
	}
	
	@Override
	public SOSIDataStore getDataStore() {
        return (SOSIDataStore) super.getDataStore();
    }

	@Override
	protected SimpleFeatureType buildFeatureType() throws IOException {
		SosiReader reader = getDataStore().read();
		
		try {
			SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
	        builder.setName(entry.getName());
	        
	        //TODO: Needs improvement! I don't know how we get this kind of info 
	        // It isn't in the JSOSI API, and I don't know enough about SOSI to know if it's possible
	        // We rather hackily spin it off the first 60 000 items(empirically tested!!!) and hope the rest are similar..
	        // (Which makes the setup waaaaay too slow) (It could be a param, but that would be rather user hostile)
	        // Sadly, this won't always work because some files can have variable amounts of attributes!
	        SOSIDataStore store = (SOSIDataStore) getState().getEntry().getDataStore();
			reader = store.read();
			
			Feature firstFeature = reader.nextFeature();
			int i = 0;
			Set<String> attributeKeys = new HashSet<String>(firstFeature.getAttributeMap().keySet());
			
			Feature feature;
			while ((feature = reader.nextFeature()) != null && i < 600000) {	
				attributeKeys.addAll(feature.getAttributeMap().keySet());
				i++;
			}
			
			for (String key : attributeKeys) {
	        	builder.add(key, String.class);
			}	

	        //Seems to work even if we don't set the CRS TODO: investigate if that is the case!
	        try {
				builder.setCRS(CRS.decode(reader.getCrs()));
			} catch (NoSuchAuthorityCodeException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (FactoryException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        builder.add("Geometry", Geometry.class);
			
	        
	        // build the type (it is immutable and cannot be modified)
            final SimpleFeatureType SCHEMA = builder.buildFeatureType();
            return SCHEMA;
		} finally {
			reader.close();
		}
	}

	@Override
	protected ReferencedEnvelope getBoundsInternal(Query arg0) throws IOException {
		// TODO Auto-generated method stub
		//SOSI files often have this, but it's not supported in JSOSI. Would need to implement there!
		// (Or just copy all that code in here and make any other changes we need/Port this to JSOSI?)
		return null;
	}

	@Override
	protected int getCountInternal(Query arg0) throws IOException {
		// TODO Auto-generated method stub
		return -1;
	}

	@Override
	protected FeatureReader<SimpleFeatureType, SimpleFeature> getReaderInternal(Query query) throws IOException {
		// TODO Auto-generated method stub
		//Either return new SOSIFeatureReader(getState(), query); or just use jsosi directly? 
		return new SOSIFeatureReader(getState(), query);
		
		
	}

}
