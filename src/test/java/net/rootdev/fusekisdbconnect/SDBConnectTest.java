/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.rootdev.fusekisdbconnect;

import org.apache.jena.assembler.AssemblerHelp;
import org.apache.jena.assembler.assemblers.AssemblerGroup;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.sparql.core.DatasetImpl;
import org.apache.jena.sparql.core.assembler.AssemblerUtils;
import org.apache.jena.update.UpdateExecutionFactory;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateProcessor;
import org.apache.jena.update.UpdateRequest;
import org.apache.jena.util.FileManager;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author Damian Steer <d.steer@bris.ac.uk>
 */
public class SDBConnectTest {
    
    public SDBConnectTest() {
    }

    /**
     * Test of whenRequiredByAssembler method, of class SDBConnect.
     * What a terrible test. statics are bad!
     */
    @Test
    public void testWhenRequiredByAssembler() {
        Model config = FileManager.get().loadModel("basic.ttl");
        AssemblerHelp.loadArbitraryClasses(new AssemblerGroup.ExpandingAssemblerGroup(), config);
        assertTrue("Assembler loadClass initialised connector", SDBConnect.initialised);
    }
    
    @Test
    public void testAssemblerWorks() {
        Object result = AssemblerUtils.build("basic.ttl", SDBConnect.TYPE);
        assertNotNull("We have created an object", result);
        assertEquals("Made object superficially of right type", DatasetImpl.class, result.getClass());
        assertEquals("Made object of right type", ReconnectingDatasetGraph.class, ((Dataset) result).asDatasetGraph().getClass());
    }
    
    @Test
    public void testQueryWorks() {
        ReconnectingDatasetGraph toQuery = (ReconnectingDatasetGraph) ((Dataset) AssemblerUtils.build("basic.ttl", SDBConnect.TYPE)).asDatasetGraph();
        toQuery.getDatasetGraph().getStore().getTableFormatter().format();
        Dataset ds = DatasetImpl.wrap(toQuery);
        //Dataset ds = new DatasetImpl(toQuery);
        QueryExecution qe = QueryExecutionFactory.create("ASK { ?s ?p ?o }", ds);
        assertTrue("Querying works", !qe.execAsk());
    }
    
    @Test
    public void testFullQueryWorks() {
        ReconnectingDatasetGraph toQuery = (ReconnectingDatasetGraph) ((Dataset) AssemblerUtils.build("basic.ttl", SDBConnect.TYPE)).asDatasetGraph();
        toQuery.getDatasetGraph().getStore().getTableFormatter().format();
        Dataset ds = DatasetImpl.wrap(toQuery);
        //Dataset ds = new DatasetImpl(toQuery);
        QueryExecution qe = QueryExecutionFactory.create("SELECT * { ?s ?p ?o }", ds);
        ResultSet r = qe.execSelect();
        assertTrue("Querying works", !r.hasNext());
        qe.close();
        
        qe = QueryExecutionFactory.create("SELECT * { graph ?g { ?s ?p ?o } }", ds);
        r = qe.execSelect();
        assertTrue("Querying with named graphs works", !r.hasNext());
        qe.close();
    }
    
    @Test
    public void testUpdateWorks() {
        ReconnectingDatasetGraph toQuery = (ReconnectingDatasetGraph) ((Dataset) AssemblerUtils.build("basic.ttl", SDBConnect.TYPE)).asDatasetGraph();
        toQuery.getDatasetGraph().getStore().getTableFormatter().format();
        UpdateRequest ur = UpdateFactory.create("insert data { <http://example.com/> <http://example.com/prop> 1 }");
        UpdateProcessor u = UpdateExecutionFactory.create(ur, toQuery);
        u.execute();
        
        assertTrue("Update works", !toQuery.getDatasetGraph().isEmpty());
    }
    
    @Test
    public void testReconnectWorks() {
        ReconnectingDatasetGraph toQuery = (ReconnectingDatasetGraph) ((Dataset) AssemblerUtils.build("basic.ttl", SDBConnect.TYPE)).asDatasetGraph();
        toQuery.getDatasetGraph().getStore().getTableFormatter().format();
        Dataset ds = DatasetImpl.wrap(toQuery);
        //Dataset ds = new DatasetImpl(toQuery);
        QueryExecution qe = QueryExecutionFactory.create("ASK { ?s ?p ?o }", ds);
        assertTrue("Querying still works", !qe.execAsk());
        
        // Haha! Say goodbye!
        toQuery.getDatasetGraph().getStore().getConnection().close();
        
        qe = QueryExecutionFactory.create("ASK { ?s ?p ?o }", ds);
        assertTrue("Reconnected query still works", !qe.execAsk());
        
        qe = QueryExecutionFactory.create("ASK { ?s ?p ?o }", ds);
        assertTrue("Reconnected query works with test", !qe.execAsk());
    }
    
    @Test
    public void testUnionWorks() {
        Dataset ds = (Dataset) AssemblerUtils.build("union.ttl", SDBConnect.TYPE);
        ReconnectingDatasetGraph toQuery = (ReconnectingDatasetGraph) ds.asDatasetGraph();
        toQuery.getDatasetGraph().getStore().getTableFormatter().format();
        UpdateRequest ur = UpdateFactory.create(
                "insert data {"
                + "graph <http://example.com/a> { <http://example.com/1> <http://example.com/prop> 1 }"
                + "graph <http://example.com/b> { <http://example.com/2> <http://example.com/prop> 2 }"
                + "}");
        UpdateProcessor u = UpdateExecutionFactory.create(ur, toQuery);
        u.execute();
        
        QueryExecution qe = QueryExecutionFactory.create("SELECT * { ?s ?p ?o }", ds);
        ResultSetRewindable r = ResultSetFactory.makeRewindable(qe.execSelect());
        
        assertEquals("We have a union!", 2, r.size());
    }
}
