/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.rootdev.fusekisdbconnect;

import com.hp.hpl.jena.assembler.Assembler;
import com.hp.hpl.jena.assembler.Mode;
import com.hp.hpl.jena.assembler.assemblers.AssemblerBase;
import com.hp.hpl.jena.assembler.assemblers.AssemblerGroup;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.sdb.SDB;
import com.hp.hpl.jena.sdb.StoreDesc;
import com.hp.hpl.jena.sdb.assembler.StoreDescAssembler;
import com.hp.hpl.jena.sparql.core.DatasetImpl;
import com.hp.hpl.jena.sparql.engine.QueryEngineRegistry;
import com.hp.hpl.jena.sparql.modify.UpdateEngineRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Damian Steer <d.steer@bris.ac.uk>
 */
public class SDBConnect extends AssemblerBase {
    protected static boolean initialised = false;
    
    final static Logger log = LoggerFactory.getLogger(SDBConnect.class);
    
    final static Resource TYPE = ResourceFactory.createResource("http://rootdev.net/vocab/jumble#SDBConnect");
    final static Property UNION = ResourceFactory.createProperty("http://rootdev.net/vocab/jumble#defaultUnionGraph");
    
    final static StoreDescAssembler sdAss = new StoreDescAssembler();
    
    static {
        log.warn("Hooking in to update and query engines");
        UpdateAndQueryEngine uqe = new UpdateAndQueryEngine();
        QueryEngineRegistry.addFactory(uqe);
        UpdateEngineRegistry.addFactory(uqe);
    }
    
    // Hook into assembler
    public static void whenRequiredByAssembler( AssemblerGroup g )
    {
        log.warn("Hooking in to assemblers");
        initialised = true;
        if (g == null) g = Assembler.general;
        g.implementWith(TYPE, new SDBConnect());
    }

    @Override
    public Object open(Assembler asmblr, Resource rsrc, Mode mode) {
        
        if (rsrc.hasProperty(UNION, "true")) {
            SDB.getContext().set(SDB.unionDefaultGraph, true);
        }
        
        StoreDesc sd = sdAss.open(asmblr, rsrc, mode);
        return DatasetImpl.wrap(new ReconnectingDatasetGraph(sd));
        //return new DatasetImpl(new ReconnectingDatasetGraph(sd));
    }
}
