/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.rootdev.fusekisdbconnect;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.sdb.Store;
import org.apache.jena.sdb.StoreDesc;
import org.apache.jena.sdb.store.DatasetStore;
import org.apache.jena.sdb.store.StoreFactory;
import org.apache.jena.shared.Lock;
import org.apache.jena.sparql.core.DatasetGraphBase;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.sdb.store.DatasetGraphSDB;
import java.util.Iterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Damian Steer <d.steer@bris.ac.uk>
 */
public class ReconnectingDatasetGraph extends DatasetGraphBase {
    
    final static Logger log = LoggerFactory.getLogger(ReconnectingDatasetGraph.class);
    
    private final StoreDesc storeDesc;
    private final String query;
    private DatasetGraphSDB datasetGraph;
        
    public ReconnectingDatasetGraph(StoreDesc storeDesc, String query) {
        this.storeDesc = storeDesc;
        this.query = query;
    }
    
    public DatasetGraphSDB getDatasetGraph() {
    	Store store = null;
    	if(datasetGraph == null || dead(store = datasetGraph.getStore()))
    	{
    		close(store);
    		store = StoreFactory.create(storeDesc);
    		datasetGraph = DatasetStore.createDatasetStoreGraph(store);
    	}
        
        return datasetGraph;
    }
    
    private boolean dead(Store store) {
        try {
            store.getConnection().execQuery(this.query);
            return false;
        } catch (Exception e) {
            log.warn("Connection test failed. Assuming dead. [{}]", e.getMessage());
            return true;
        }
    }
    
    private void close(Store store) {    	
        try {
        	if(store == null) return;
            store.close();
        } catch (Exception e) {
            log.warn("Exception closing store", e);
        }
    }
    
    // Ignore
    
    @Override
    public Graph getDefaultGraph() {
        return getDatasetGraph().getDefaultGraph();
    }

    @Override
    public Graph getGraph(Node node) {
        return getDatasetGraph().getGraph(node);
    }

    @Override
    public Iterator<Node> listGraphNodes() {
        return getDatasetGraph().listGraphNodes();
    }

    @Override
    public Iterator<Quad> find(Node node, Node node1, Node node2, Node node3) {
        return getDatasetGraph().find(node, node1, node2, node3);
    }

    @Override
    public Iterator<Quad> findNG(Node node, Node node1, Node node2, Node node3) {
        return getDatasetGraph().findNG(node, node1, node2, node3);
    }

	@Override
	public boolean supportsTransactions() {
		return getDatasetGraph().supportsTransactions();
	}

	@Override
	public void abort() {
		getDatasetGraph().abort();
		
	}

	@Override
	public void begin(ReadWrite mode) {
		getDatasetGraph().begin(mode);
		
	}

	@Override
	public void commit() {
		getDatasetGraph().commit();		
	}

	@Override
	public void end() {
		getDatasetGraph().end();
		
	}

	@Override
	public boolean isInTransaction() {
		return getDatasetGraph().isInTransaction();
	}

	@Override
	public void addGraph(Node node, Graph graph) {
		getDatasetGraph().addGraph(node, graph);
		
	}

	@Override
	public void removeGraph(Node node) {
		getDatasetGraph().removeGraph(node);
		
	}

	@Override
	public void add(Quad quad) {
		getDatasetGraph().add(quad);
		
	}

	@Override
	public void add(Node g, Node s, Node p, Node o) {
		getDatasetGraph().add(g, s, p, o);
		
	}

	@Override
	public void clear() {
		getDatasetGraph().clear();
		
	}

	@Override
	public void close() {
		getDatasetGraph().close();
		
	}

	@Override
	public boolean contains(Quad quad) {
		return getDatasetGraph().contains(quad);
	}

	@Override
	public boolean contains(Node g, Node s, Node p, Node o) {
		return getDatasetGraph().contains(g, s, p, o);
	}

	@Override
	public boolean containsGraph(Node graphNode) {
		return getDatasetGraph().containsGraph(graphNode);
	}

	@Override
	public void delete(Quad quad) {
		getDatasetGraph().delete(quad);
		
	}

	@Override
	public void delete(Node g, Node s, Node p, Node o) {
		getDatasetGraph().delete(g, s, p, o);
		
	}

	@Override
	public void deleteAny(Node g, Node s, Node p, Node o) {
		getDatasetGraph().deleteAny(g, s, p, o);
		
	}

	@Override
	public Iterator<Quad> find() {
		return getDatasetGraph().find();
	}

	@Override
	public Iterator<Quad> find(Quad quad) {
		return getDatasetGraph().find(quad);
	}

	@Override
	public Context getContext() {
		return getDatasetGraph().getContext();
	}

	@Override
	public Lock getLock() {
		return getDatasetGraph().getLock();
	}

	@Override
	public boolean isEmpty() {
		return getDatasetGraph().isEmpty();
	}

	@Override
	public void setDefaultGraph(Graph g) {
		getDatasetGraph().setDefaultGraph(g);
		
	}

	@Override
	public long size() {
		return getDatasetGraph().size();
	}    
}
