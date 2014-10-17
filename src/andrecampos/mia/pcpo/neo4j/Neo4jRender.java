package andrecampos.mia.pcpo.neo4j;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.kernel.GraphDatabaseAPI;
import org.neo4j.server.WrappingNeoServerBootstrapper;

/**
 * Created by andrecampos on 10/10/14.
 */
public class Neo4jRender {

    static GraphDatabaseService graphDb;
    /**
     *
     * @param args
     */
    public static void main(String args[]) throws InterruptedException {

        new Thread(new Runnable() {
            @Override
            public void run() {
                graphDb = new GraphDatabaseFactory().newEmbeddedDatabase(
                        "/Applications/neo4j-community-2.0.1/data/graph.db"
                );

                WrappingNeoServerBootstrapper srv = new WrappingNeoServerBootstrapper((GraphDatabaseAPI) graphDb);
                srv.start();
            }
        }).start();


        Thread.sleep(10000);

        try ( Transaction tx = graphDb.beginTx() )
        {
            Node firstNode = graphDb.createNode();
            firstNode.setProperty( "J", "J, " );
            Node secondNode = graphDb.createNode();
            secondNode.setProperty( "J", "andre friends" );

            Relationship relationship = firstNode.createRelationshipTo(secondNode, ArcRelationShip.LINK);
            relationship.setProperty( "J", "brave J" );
            tx.success();
        }

//        graphDb.shutdown();

    }

}
