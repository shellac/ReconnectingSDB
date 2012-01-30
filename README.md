= Introduction to ReconnectingSDB =

A common issue with SDB is that the connection has died:
database connections are finite. We recommend that SDB is used
with connection pooling which avoids this issue, however that isn't
possible with fuseki.

This module doesn't pool connections! However it will check the
connection is working and attempt to reconnect if possible.

== Getting it ==

    $ git clone git@github.com:shellac/ReconnectingSDB.git
    $ cd ReconnectingSDB
    $ mvn install

The jar will be in `target/ReconnectingSDB-0.1-SNAPSHOT.jar`.

== Using it ==

Put the above jar on your classpath (fiddly with fuseki), and
use something like the following dataset description:

    @prefix rdfs:     <http://www.w3.org/2000/01/rdf-schema#> .
    @prefix rdf:      <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .
    @prefix ja:       <http://jena.hpl.hp.com/2005/11/Assembler#> .
    @prefix sdb:      <http://jena.hpl.hp.com/2007/sdb#> .
    @prefix jumble:   <http://rootdev.net/vocab/jumble#> .
    
    # For use with Joseki.
    [] ja:loadClass "net.rootdev.fusekisdbconnect.SDBConnect" .
    
    jumble:SDBConnect  rdfs:subClassOf  ja:RDFDataset .
    
    <#dataset> rdf:type jumble:SDBConnect ;
        sdb:layout "layout2/index" ;
        sdb:connection [
            rdf:type sdb:SDBConnection ;
            sdb:sdbType "h2:file" ;
            sdb:sdbName "DB/H2" ;
        ]
    .
