/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.rootdev.fusekisdbconnect;

import com.hp.hpl.jena.assembler.AssemblerHelp;
import com.hp.hpl.jena.assembler.assemblers.AssemblerGroup;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.sparql.core.DatasetImpl;
import com.hp.hpl.jena.sparql.core.assembler.AssemblerUtils;
import com.hp.hpl.jena.update.UpdateExecutionFactory;
import com.hp.hpl.jena.update.UpdateFactory;
import com.hp.hpl.jena.update.UpdateProcessor;
import com.hp.hpl.jena.update.UpdateRequest;
import com.hp.hpl.jena.util.FileManager;
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
        assertEquals("Made object of right type", ReconnectingDatasetGraph.class, result.getClass());
    }
    
    @Test
    public void testQueryWorks() {
        ReconnectingDatasetGraph toQuery = (ReconnectingDatasetGraph) AssemblerUtils.build("basic.ttl", SDBConnect.TYPE);
        toQuery.getDatasetGraph().getStore().getTableFormatter().format();
        Dataset ds = DatasetImpl.wrap(toQuery);
        QueryExecution qe = QueryExecutionFactory.create("ASK { ?s ?p ?o }", ds);
        assertTrue("Querying works", !qe.execAsk());
    }
    
    @Test
    public void testUpdateWorks() {
        ReconnectingDatasetGraph toQuery = (ReconnectingDatasetGraph) AssemblerUtils.build("basic.ttl", SDBConnect.TYPE);
        toQuery.getDatasetGraph().getStore().getTableFormatter().format();
        UpdateRequest ur = UpdateFactory.create("insert data { <http://example.com/> <http://example.com/prop> 1 }");
        UpdateProcessor u = UpdateExecutionFactory.create(ur, toQuery);
        u.execute();
        
        assertTrue("Update works", !toQuery.getDatasetGraph().isEmpty());
    }
    
    @Test
    public void testReconnectWorks() {
        ReconnectingDatasetGraph toQuery = (ReconnectingDatasetGraph) AssemblerUtils.build("basic.ttl", SDBConnect.TYPE);
        toQuery.getDatasetGraph().getStore().getTableFormatter().format();
        Dataset ds = DatasetImpl.wrap(toQuery);
        QueryExecution qe = QueryExecutionFactory.create("ASK { ?s ?p ?o }", ds);
        assertTrue("Querying still works", !qe.execAsk());
        
        // Haha! Say goodbye!
        toQuery.getDatasetGraph().getStore().getConnection().close();
        
        qe = QueryExecutionFactory.create("ASK { ?s ?p ?o }", ds);
        assertTrue("Reconnected query still works", !qe.execAsk());
        
        qe = QueryExecutionFactory.create("ASK { ?s ?p ?o }", ds);
        assertTrue("Reconnected query works with test", !qe.execAsk());
    }
}
