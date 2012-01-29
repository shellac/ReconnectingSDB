/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.rootdev.fusekisdbconnect;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.sdb.engine.QueryEngineSDB;
import com.hp.hpl.jena.sdb.modify.UpdateEngineSDB;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.core.DatasetGraph;
import com.hp.hpl.jena.sparql.engine.Plan;
import com.hp.hpl.jena.sparql.engine.QueryEngineFactory;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.modify.UpdateEngine;
import com.hp.hpl.jena.sparql.modify.UpdateEngineFactory;
import com.hp.hpl.jena.sparql.util.Context;
import com.hp.hpl.jena.update.GraphStore;
import com.hp.hpl.jena.update.UpdateRequest;

/**
 *
 * @author Damian Steer <d.steer@bris.ac.uk>
 */
public class UpdateAndQueryEngine implements
        UpdateEngineFactory, QueryEngineFactory {
    
    /** Update methods **/
    
    @Override
    public boolean accept(UpdateRequest ur, GraphStore gs, Context cntxt) {
        return gs instanceof ReconnectingDatasetGraph;
    }

    @Override
    public UpdateEngine create(UpdateRequest ur, GraphStore gs, Binding bndng, Context cntxt) {
        ReconnectingDatasetGraph rdg = (ReconnectingDatasetGraph) gs;
        return new UpdateEngineSDB(rdg.getDatasetGraph(), ur, bndng, cntxt);
    }
    
    /** Query methods **/
    
    @Override
    public boolean accept(Query query, DatasetGraph dg, Context cntxt) {
        return dg instanceof ReconnectingDatasetGraph;
    }

    @Override
    public Plan create(Query query, DatasetGraph dg, Binding bndng, Context cntxt) {
        ReconnectingDatasetGraph rdg = (ReconnectingDatasetGraph) dg;
        QueryEngineSDB qe = new QueryEngineSDB(rdg.getDatasetGraph() , query, bndng, cntxt) ;
        return qe.getPlan() ;
    }

    @Override
    public boolean accept(Op op, DatasetGraph dg, Context cntxt) {
        return dg instanceof ReconnectingDatasetGraph;
    }

    @Override
    public Plan create(Op op, DatasetGraph dg, Binding bndng, Context cntxt) {
        ReconnectingDatasetGraph rdg = (ReconnectingDatasetGraph) dg;
        QueryEngineSDB qe = new QueryEngineSDB(rdg.getDatasetGraph(), op, bndng, cntxt) ;
        return qe.getPlan() ;
    }
    
}
