#SOSI reader plugin for GeoTools

This plugin allows geoTools to read features from SOSI standard .sos files. 
(SOSI is a Norwegian geodata standard.) It makes use of the [JSOSI](https://github.com/halset/jsosi) 
library to parse the files, and is really just a wrapper around it that complies 
with the GeoTools API and plugin discovery system.

##Problems
 - It can't do getBoundsInternal even though that exists in SOSI file headers.
 (It's not supported by JSOSI)
 - It can't do getCountInternal because SOSI doesn't have any info like that in the head
 (You would have to iterate through the entire file to count it)
 - The way it does BuildFeatureType is hacky, horrible and wasteful. Since SOSI files
 don't come with any kind of schema in the file (you can use external object catalogues
 for that, but you're not required), the only way I've found to get the feature types
 is to scan the first 60 000 objects and check all of their properties, then use that
 to define the featuretype. (In one of the files I tested 60K was needed to sample 
 enough of the objects to get all the possible variations...) Needless to say, this means
 setting up the FeatureSource takes as long as running through all the data for most files.
 (We might want to make the number of iterations a user supplied parameter.)
 - I don't actually know the SOSI standard and have based this on what I see in JSOSI and
 asking people who know SOSI basic questions.
 
 ##License
 Like JSOSI, this is under the [Apache License, version 2.0](http://www.apache.org/licenses/LICENSE-2.0)