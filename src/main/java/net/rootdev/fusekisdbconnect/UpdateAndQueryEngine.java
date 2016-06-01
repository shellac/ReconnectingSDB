/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.rootdev.fusekisdbconnect;

import org.apache.jena.query.Query;
import org.apache.jena.sdb.engine.QueryEngineSDB;
import org.apache.jena.sdb.modify.UpdateEngineSDB;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.engine.Plan;
import org.apache.jena.sparql.engine.QueryEngineFactory;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.modify.UpdateEngine;
import org.apache.jena.sparql.modify.UpdateEngineFactory;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.update.GraphStore;
import org.apache.jena.update.UpdateRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Damian Steer <d.steer@bris.ac.uk>
 */
public class UpdateAndQueryEngine implements
        UpdateEngineFactory, QueryEngineFactory {
    
    final static Logger log = LoggerFactory.getLogger(UpdateAndQueryEngine.class);
    
    /** Update methods **/

	@Override
	public boolean accept(DatasetGraph dg, Context cntxt) {
		return dg instanceof ReconnectingDatasetGraph;
	}

	@Override
	public UpdateEngine create(DatasetGraph dg, Binding bndng, Context cntxt) {
		ReconnectingDatasetGraph rdg = (ReconnectingDatasetGraph) dg;
        return new UpdateEngineSDB(rdg.getDatasetGraph(), bndng, cntxt);
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
