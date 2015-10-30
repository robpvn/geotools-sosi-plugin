#SOSI reader plugin for GeoTools

This plugin allows geoTools to read features from SOSI standard .sos files. 
(SOSI is a Norwegian geodata standard.) It makes use of the [JSOSI](https://github.com/halset/jsosi) 
library to parse the files, and is really just a wrapper around it that complies 
with the GeoTools API and plugin discovery system.

##Disclaimer
This was hacked together quickly and dirtily at FOSS4G 2015 after attending the GeoTools
DataStore Workshop. Under no circumstances should you use it anywhere near production code!
For serious work with SOSI and GeoTools, use FME to transform it into something edible.

It needs a fair bit more work before I'd consider submitting it to GeoTools Unsupported Modules.

##Problems
 - It can't do getCountInternal because SOSI doesn't have any info like that in the head
 (You would have to iterate through the entire file to count it)
  - Since we might have to do that anyway for the next point, we could cache that data as well
 - The way it does BuildFeatureType is hacky, horrible and wasteful. Since SOSI files
 don't come with any kind of schema in the file (you can use external object catalogues
 for that, but you're not required), the only way I've found to get the feature types
 is to scan the first 60 000 objects and check all of their properties, then use that
 to define the featuretype. (In one of the files I tested 60K was needed to sample 
 enough of the objects to get all the possible variations...) Needless to say, this means
 setting up the FeatureSource takes as long as running through all the data for most files.
  - We might want to make the number of iterations a user supplied parameter.
  - We can implement some kind of cache of discovered schema for future reuse.
  - SOSI files kan refer to an object catalogue in the header, so if we have those we can supply
  them, either in some kind of repository (may be license issues there) or as a user parameter.
 - Oh yeah, everything is read as a String.
 - I don't actually know the SOSI standard and have based this on what I see in JSOSI and
 asking people who know SOSI basic questions.
 
 ##License
 Like JSOSI, this is under the [Apache License, version 2.0](http://www.apache.org/licenses/LICENSE-2.0)
