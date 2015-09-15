package org.geotools.data.sosi;

import java.awt.RenderingHints.Key;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFactorySpi;
import org.geotools.util.KVP;

public class SOSIDataStoreFactory implements DataStoreFactorySpi {

	/**
     * Public "no argument" constructor called by Factory Service Provider (SPI) entry listed in
     * META-INF/services/org.geotools.data.DataStoreFactorySPI
     */
    public SOSIDataStoreFactory() {
    }
	
	@Override
	public boolean canProcess(Map<String, Serializable> params) {
		try {
            File file = (File) FILE_PARAM.lookUp(params);
            if (file != null) {
                return file.getPath().toLowerCase().endsWith(".sos");
            }
        } catch (IOException e) {
            // ignore as we are expected to return true or false
        }
        return false;
	}

	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return "Norwegian national standard geodata format";
	}

	@Override
	public String getDisplayName() {
		// TODO Auto-generated method stub
		return "SOSI";
	}

	//Usually define the important hints as static consts
	/** Parameter description of information required to connect */
    public static final Param FILE_PARAM = new Param("file", File.class, "SOSI file", true,
            null, new KVP(Param.EXT, "sos")); //KVP is a helper for defining common params

    //An open ended map of all sorts of parameters for using the data store. Can be just about anything!
    //Usually used to generate UIs for adding needed config
    @Override
    public Param[] getParametersInfo() {
        return new Param[] { FILE_PARAM }; //Can have many types of params
    }

	/** Confirm DataStore availability, null if unknown */
    Boolean isAvailable = null;

    /**
     * Test to see if this DataStore is available, for example if it has all the appropriate libraries to construct an instance.
     * 
     * This method is used for interactive applications, so as to not advertise support for formats that will not function.
     * 
     * @return <tt>true</tt> if and only if this factory is available to create DataStores.
     */
    @Override
    public synchronized boolean isAvailable() {
        if (isAvailable == null) {
            try {
            	//We depend on this lib, so it has to be in the path. (Normally we would add it to deps or bundle it)
                Class sosReaderType = Class.forName("no.jsosi.SosiReader"); 
                isAvailable = true;
            } catch (ClassNotFoundException e) {
                isAvailable = false;
            }
        }
        return isAvailable;
    }

    /** No implementation hints required at this time */
    @Override
    public Map<Key, ?> getImplementationHints() {
        return Collections.emptyMap();
    }

	@Override
	public DataStore createDataStore(Map<String, Serializable> params) throws IOException {
		File file = (File) FILE_PARAM.lookUp(params);
        return new SOSIDataStore(file);
	}

	@Override
	public DataStore createNewDataStore(Map<String, Serializable> params) throws IOException {
		throw new UnsupportedOperationException("SOSI Datastore is read only");
	}

}
