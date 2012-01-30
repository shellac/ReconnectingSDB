/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.rootdev.fusekisdbconnect;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.sdb.SDBFactory;
import com.hp.hpl.jena.sdb.StoreDesc;
import com.hp.hpl.jena.sdb.sql.SDBConnectionFactory;
import com.hp.hpl.jena.sdb.store.DatasetStoreGraph;
import com.hp.hpl.jena.sparql.core.DatasetGraphBase;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.update.GraphStore;
import java.util.Iterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Damian Steer <d.steer@bris.ac.uk>
 */
public class ReconnectingDatasetGraph extends DatasetGraphBase
    implements GraphStore {
    
    final static Logger log = LoggerFactory.getLogger(ReconnectingDatasetGraph.class);
    
    private final StoreDesc storeDesc;
    private DatasetStoreGraph store;
        
    public ReconnectingDatasetGraph(StoreDesc storeDesc) {
        this.storeDesc = storeDesc;
    }
    
    public DatasetStoreGraph getDatasetGraph() {
        return getConnectedStore();
    }
    
    private DatasetStoreGraph getConnectedStore() {
        if (store == null) {
            store = (DatasetStoreGraph) SDBFactory.connectGraphStore(
                    SDBConnectionFactory.create(storeDesc.connDesc),
                    storeDesc);
        }
        else if (dead(store)) {
            close(store);
            store = (DatasetStoreGraph) SDBFactory.connectGraphStore(
                    SDBConnectionFactory.create(storeDesc.connDesc),
                    storeDesc);
        }
        
        return store;
    }
    
    private static boolean dead(DatasetStoreGraph store) {
        try {
            store.getStore().getConnection().execQuery("SELECT 1;");
            return false;
        } catch (Exception e) {
            log.warn("Connection test failed. Assuming dead. [{}]", e.getMessage());
            return true;
        }
    }
    
    private static void close(DatasetStoreGraph store) {
        try {
            store.getStore().close();
        } catch (Exception e) {
            log.warn("Exception closing store", e);
        }
    }
    
    // Ignore
    
    @Override
    public Graph getDefaultGraph() {
        return getConnectedStore().getDefaultGraph();
    }

    @Override
    public Graph getGraph(Node node) {
        return getConnectedStore().getGraph(node);
    }

    @Override
    public Iterator<Node> listGraphNodes() {
        return getConnectedStore().listGraphNodes();
    }

    @Override
    public Iterator<Quad> find(Node node, Node node1, Node node2, Node node3) {
        return getConnectedStore().find(node, node1, node2, node3);
    }

    @Override
    public Iterator<Quad> findNG(Node node, Node node1, Node node2, Node node3) {
        return getConnectedStore().findNG(node, node1, node2, node3);
    }

    @Override
    public Dataset toDataset() {
        return getConnectedStore().toDataset();
    }

    @Override
    public void startRequest() {
        getConnectedStore().startRequest();
    }

    @Override
    public void finishRequest() {
        getConnectedStore().finishRequest();
    }
    
}
