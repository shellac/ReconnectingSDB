/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.rootdev.fusekisdbconnect;

import org.apache.jena.assembler.Assembler;
import org.apache.jena.assembler.Mode;
import org.apache.jena.assembler.assemblers.AssemblerBase;
import org.apache.jena.assembler.assemblers.AssemblerGroup;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.sdb.SDB;
import org.apache.jena.sdb.StoreDesc;
import org.apache.jena.sdb.assembler.AssemblerVocab;
import org.apache.jena.sdb.assembler.StoreDescAssembler;
import org.apache.jena.sparql.core.DatasetImpl;
import org.apache.jena.sparql.core.assembler.AssemblerUtils;
import org.apache.jena.sparql.engine.QueryEngineRegistry;
import org.apache.jena.sparql.modify.UpdateEngineRegistry;
import org.apache.jena.sparql.util.graph.GraphUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.jena.sdb.util.Vocab;
import org.apache.jena.sdb.engine.QueryEngineSDB;
import org.apache.jena.sdb.modify.UpdateEngineSDB;
import org.apache.jena.assembler.JA;
/**
 *
 * @author Damian Steer <d.steer@bris.ac.uk>
 */
public class SDBConnect extends AssemblerBase {
	
    private static final String NS = "http://rootdev.net/vocab/jumble#" ;
	
    protected static boolean initialised = false;
    final static Logger log = LoggerFactory.getLogger(SDBConnect.class);
    
    
    
    final static Resource TYPE = Vocab.type(NS, "SDBConnect") ;
    final static Property UNION = Vocab.property(NS, "defaultUnionGraph");
    final static Property ReconnectQuery = Vocab.property(NS, "reconnectQuery");
    
    final static StoreDescAssembler sdAss = new StoreDescAssembler();
    
    static {
        init();
    }
    
    public static void init() {
    	
    	if(initialised) return;
    	
    	SDB.init();
    	    	
    	Assembler.general.implementWith(TYPE, new SDBConnect());
    	
    	log.warn("Hooking in to update and query engines");
        UpdateAndQueryEngine uqe = new UpdateAndQueryEngine();
        
        QueryEngineRegistry.addFactory(uqe);
        UpdateEngineRegistry.addFactory(uqe);
        
        initialised = true;
    }

    @Override
    public Object open(Assembler asmblr, Resource root, Mode mode) {
        
        if (root.hasProperty(UNION, "true")) {
            SDB.getContext().set(SDB.unionDefaultGraph, true);
        }
        
        String reconnectQuery = GraphUtils.getStringValue(root, ReconnectQuery);
        
        StoreDesc sd = sdAss.open(asmblr, root, mode);
        Dataset ds = DatasetImpl.wrap(new ReconnectingDatasetGraph(sd, reconnectQuery));
        
        AssemblerUtils.setContext(root, ds.getContext());
        
        return ds;
        //return new DatasetImpl(new ReconnectingDatasetGraph(sd));
    }
}
