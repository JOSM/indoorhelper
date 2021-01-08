// License: AGPL. For details, see LICENSE file.
package io.parser.utils.optimizer;

import io.parser.utils.ParserUtility;
import org.openstreetmap.gui.jmapviewer.OsmMercator;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.tools.Logging;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Class optimizing OSM data to avoid unnecessary nodes/ways in data set
 *
 * @author rebsc
 */
public class OutputOptimizer {

    /**
     * Method optimizes the osm data following the set configurations
     *
     * @param config describes the optimization
     * @param ds     to optimize
     */
    public static void optimize(Configuration config, DataSet ds) {
        // if merge close nodes enabled
        if (config.MERGE_CLOSE_NODES) {
            // for each node find merch candidates
            ArrayList<Merge> merges = findMerges(ds, config.MERGE_DISTANCE);
            Collections.reverse(merges);
            int preCount = ds.getNodes().size() + ds.getWays().size();

            // merge candidates to target
            ArrayList<Integer> levels = ParserUtility.getLevelList(ds);
            levels.forEach(level -> mergeData(merges, ds, level));

            Logging.info(String.format("%s-OutputOptimizerReport: OSM primitives reduced by factor %.2f",
                    OutputOptimizer.class.getName(),
                    1.0-((double)(ds.getNodes().size() + ds.getWays().size()) / preCount)));
        }
    }

    /**
     * Method merges nodes in data set following the mergeLayout.
     * This method only merges data on defined level.
     *
     * @param mergeLayout holding information about merge targets and candidates
     * @param ds          data set to merge data in
     * @param level       only consider data with this level tag
     */
    private static void mergeData(ArrayList<Merge> mergeLayout, DataSet ds, int level) {
        mergeLayout.forEach(target -> {
            Node dsTarget = (Node) ds.getPrimitiveById(target.target.getPrimitiveId());
            if (ParserUtility.getLevelTag(dsTarget) != level) return;

            target.mergeToTarget.forEach(candidate -> {
                Node dsCandidate = (Node) ds.getPrimitiveById(candidate.getPrimitiveId());
                if (ParserUtility.getLevelTag(dsCandidate) != level) return;
                // find way containing dsCandidate and replace node
                dsCandidate.getParentWays().forEach(way -> {
                    while (way.containsNode(dsCandidate)) {
                        way.addNode(way.getNodes().indexOf(dsCandidate), dsTarget);
                        way.removeNode(dsCandidate);
                    }
                });
                ds.removePrimitive(dsCandidate);
            });

        });
    }

    /**
     * Creates for each node a {@link Merge} object holding the node as root and
     * other nodes in data which can be merged to root in mergeToRoot. If a node in data can
     * be merged to root node will be decided by the distance between root and node which needs to be
     * smaller than the param mergeDistance.
     *
     * @param ds            data set to find merges in
     * @param mergeDistance max. distance between nodes so that the nodes can be merged
     * @return Set of merges
     */
    private static ArrayList<Merge> findMerges(DataSet ds, double mergeDistance) {
        ArrayList<Merge> merges = new ArrayList<>();
        OsmMercator mercator = new OsmMercator();
        ArrayList<Node> nodes = new ArrayList<>(ds.getNodes());

        for (int i = 0; i < nodes.size(); ++i) {
            Node targetNode = nodes.get(i);
            Merge merge = new Merge(targetNode);

            for (int j = i + 1; j < nodes.size(); ++j) {
                Node mergeCandidate = nodes.get(j);
                if (mercator.getDistance(targetNode.lat(), targetNode.lon(), mergeCandidate.lat(), mergeCandidate.lon()) < mergeDistance) {
                    merge.mergeToTarget.add(mergeCandidate);
                }
            }

            if (!merge.mergeToTarget.isEmpty()) {
                merges.add(merge);
            }
        }
        return merges;
    }

    private static class Merge {
        public final Node target;
        public final List<Node> mergeToTarget;

        public Merge(Node root) {
            target = root;
            mergeToTarget = new ArrayList<>();
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
