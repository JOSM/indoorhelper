// License: AGPL. For details, see LICENSE file.
package io.parser.utils.optimizer;

import io.parser.utils.ParserUtility;
import org.openstreetmap.gui.jmapviewer.OsmMercator;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.tools.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Class optimizing OSM data to avoid unnecessary nodes/ways in data set
 *
 * @author rebsc
 */
public class OutputOptimizer {

    /**
     * Method optimizes the osm data following the set configurations
     * @param config describes the optimization
     * @param data to optimize
     * @return optimized data
     */
    public static Pair<ArrayList<Node>, ArrayList<Way>> optimize(Configuration config, Pair<ArrayList<Node>, ArrayList<Way>> data) {
        ArrayList<Node> nodes = data.a;
        ArrayList<Way> ways = data.b;

        Pair<ArrayList<Node>, ArrayList<Way>> optimizedData = data;

        if(config.MERGE_CLOSE_NODES){
            // for each node find nodes that can be merged with it
            ArrayList<Merge> merges = findMerges(data, config.MERGE_DISTANCE);

            // create deep copy of data
            ArrayList<Node> nodesCopy = (ArrayList<Node>) ParserUtility.deepCopyNodeList(nodes);
            ArrayList<Way> waysCopy = (ArrayList<Way>) ParserUtility.deepCopyWayList(ways);

            // merge data
            merges.forEach(merge -> {
                nodes.forEach(node -> {
                    if (merge.mergeToRoot.contains(node)) {
                        AtomicInteger i = new AtomicInteger();
                        ways.forEach(way -> {
                            if (way.containsNode(node)) {
                                // replace all merged nodes in way by merge.root
                                mergeNodesInWay(waysCopy.get(i.get()), node, merge.root);
                            }
                            i.getAndIncrement();
                        });
                        // merge nodes, delete node from nodes list
                        nodesCopy.remove(node);
                    }
                });
            });

            optimizedData = new Pair<>(nodesCopy, waysCopy);
        }

        return optimizedData;
    }

    /**
     * Creates for each node a {@link Merge} object holding the node as root and
     * other nodes in data which can be merged to root in mergeToRoot. If a node in data can
     * be merged to root node will be decided by the distance between root and node which needs to be
     * smaller than the param mergeDistance.
     *
     * @param data          to find merges in
     * @param mergeDistance max. distance between nodes so that the nodes can be merged
     * @return Set of merges
     */
    private static ArrayList<Merge> findMerges(Pair<ArrayList<Node>, ArrayList<Way>> data, double mergeDistance) {
        ArrayList<Merge> merges = new ArrayList<>();
        OsmMercator mercator = new OsmMercator();

        for (int i = 0; i < data.a.size(); ++i) {
            Node rootNode = data.a.get(i);
            Merge merge = new Merge(rootNode);

            for (int j = i + 1; j < data.a.size(); ++j) {
                Node mergeCandidate = data.a.get(j);
                if (mercator.getDistance(rootNode.lat(), rootNode.lon(), mergeCandidate.lat(), mergeCandidate.lon()) < mergeDistance) {
                    merge.mergeToRoot.add(mergeCandidate);
                }
            }

            if (!merge.mergeToRoot.isEmpty()) {
                merges.add(merge);
            }
        }
        return merges;
    }

    /**
     * Method replaces all nodes with coordinates equal to nodeToMerge by mergeRoot
     *
     * @param way         to merge nodes in
     * @param nodeToMerge coordinates equals nodes to merge
     * @param mergeRoot   node to use as merge root
     */
    private static void mergeNodesInWay(Way way, Node nodeToMerge, Node mergeRoot) {
        way.getNodes().forEach(n -> {
            if (n.getCoor().equalsEpsilon(nodeToMerge.getCoor())) {
                n.cloneFrom(mergeRoot);
            }
        });
    }

    private static class Merge {
        public final Node root;
        public final List<Node> mergeToRoot;

        public Merge(Node root) {
            this.root = root;
            mergeToRoot = new ArrayList<>();
        }
    }

    /**
     * Configuration class for output optimization tasks
     */
    public static class Configuration {
        public final boolean MERGE_CLOSE_NODES;
        public final double MERGE_DISTANCE;

        public Configuration(boolean mergeCloseNodes, double mergeDistance) {
            MERGE_CLOSE_NODES = mergeCloseNodes;
            MERGE_DISTANCE = mergeDistance;
        }
    }
}
