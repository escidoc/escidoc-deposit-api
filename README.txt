= eSciDoc Ingest Client API =

The eSciDoc Ingest Client API supports ingesting resources into the eSciDoc 
Infrastructure from different sources. It extends the eSciDoc Java Client 
Library by adding a simplified approach for ingesting resources into eSciDoc.

The API comes with three concrete implementations of an Ingester:

* The DirectoryIngester for ingesting directories and files from a
  local filesystem as Containers and Items into the eSciDoc
  Infrastructure.

* The ByNameIngester which ingests resources of a given type from
  a given list of names. So, the resulting resources contain the
  minimum set of information but a name. 

* The DefaultIngester ingests a resource from a given eSciDoc XML
  representation. This implementation is included for demonstration
  purposes.

All three concrete implementations of an Ingester are direct subclasses of 
AbstractIngester, which provides a major part of the required functionality.

The API package includes an example properties file which should be renamed 
to "escidoc-ingest-client.properties" and be placed in the same directory an
Ingest Tool is started from. 

You will find examples for using the three provided concrete implementations 
classes and Javadoc documentation within the API package. A good starting 
point for using the API documentation might be the class DirectoryIngester 
or AbstractIngester.

Note:
If the DB-Cache is disabled in the configuration of your local eSciDoc 
Infrastructure, a recache and reindex will be necessary after ingest. 
The two relevant properties in the escidoc-core.properties file are:

* escidoc-core.notify.indexer.enabled
* de.escidoc.core.common.business.fedora.resources.DbResourceCache.enabled

The default value for both properties is "true". If they are set to "false", 
you may recache and reindex via the Administration Tool (accessible via the 
start page of your eSciDoc Infrastructure instance).
