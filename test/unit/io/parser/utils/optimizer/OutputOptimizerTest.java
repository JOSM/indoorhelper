package io.parser.utils.optimizer;

import org.junit.Test;
import org.junit.jupiter.api.Disabled;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.testutils.DatasetFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Disabled("Causes java.lang.SecurityException: class \"org.openstreetmap.josm.tools.MemoryManagerTest\"'s " +
        "signer information does not match signer information of other classes in the same package\n")
public class OutputOptimizerTest {

    /**
     * Setup test.
     */
//    @Rule
//    @SuppressFBWarnings(value = "URF_UNREAD_PUBLIC_OR_PROTECTED_FIELD")
//    public JOSMTestRules test = new JOSMTestRules().preferences();

    public ArrayList<LatLon> testNodes1 = new ArrayList<LatLon>() {
        {
            add(new LatLon(50.81400880917, 12.9240191164));
            add(new LatLon(50.81410474868, 12.92479892858));
            add(new LatLon(50.81385983524, 12.92487440414));
            add(new LatLon(50.81376389523, 2.92409459195));
            add(new LatLon(50.81400148316, 12.92402966682));
            add(new LatLon(50.81377465301, 12.92411024043));
            add(new LatLon(50.81378210558, 12.92416279334));
            add(new LatLon(50.81400893569, 12.92408221974));
            add(new LatLon(50.81400798293, 12.92402096965));
            add(new LatLon(50.81400418702, 12.92402214016));
            add(new LatLon(50.8140999079, 12.92479970393));
            add(new LatLon(50.81410370381, 12.92479853342));
        }
    };

    /**
     * Test case for {@link OutputOptimizer#optimize} method.
     */
    @Test
    public void testOptimize() {
//        ArrayList<Node> nodes = llsToNodes(testNodes1);
//        ArrayList<Way> ways = new ArrayList<>();
//        Pair<ArrayList<Node>, ArrayList<Way>> testData = new Pair<>(nodes, ways);
//        OutputOptimizer.optimize(testData);
        // TODO implement after fixing java.lang.SecurityException
    }

    private ArrayList<Node> llsToNodes(List<LatLon> lls) {
        ArrayList<Node> nodes = new ArrayList<>();
        AtomicInteger id = new AtomicInteger();
        lls.forEach(ll -> nodes.add(llToNode(ll, id.incrementAndGet())));
        return nodes;
    }

    private Node llToNode(LatLon ll, int id) {
        DatasetFactory dsFactory = new DatasetFactory();
        dsFactory.addNode(id);
        Node n = dsFactory.getNode(id);
        n.setCoor(ll);
        return n;
    }
}