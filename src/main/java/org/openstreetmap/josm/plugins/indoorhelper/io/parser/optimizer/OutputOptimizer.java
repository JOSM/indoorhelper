// License: AGPL. For details, see LICENSE file.
package io.parser.optimizer;

import io.parser.math.ParserGeoMath;
import io.parser.utils.ParserUtility;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.tools.Logging;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Class providing methods to optimize OSM data files to avoid unnecessary nodes/ways in data set
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
        if (config.MERGE_CLOSE_NODES) {
            int preCount = ds.getNodes().size() + ds.getWays().size();

            // for each level merge possible nodes
            ArrayList<Integer> levels = ParserUtility.getLevelList(ds);
            for (Integer level : levels) {
                ArrayList<Merge> merges = findMerges(ds, config.MERGE_DISTANCE, level);
                mergeData(merges, ds);
            }

            Logging.info(String.format("%s-OutputOptimizerReport: OSM primitives reduced by factor %.2f",
                    OutputOptimizer.class.getName(),
                    1.0 - ((double) (ds.getNodes().size() + ds.getWays().size()) / preCount)));
        }
    }

    /**
     * Method merges nodes in data set following the mergeLayout.
     *
     * @param mergeLayout holding information about merge targets and candidates
     * @param ds          data set to merge data in
     */
    private static void mergeData(ArrayList<Merge> mergeLayout, DataSet ds) {
        Collections.reverse(mergeLayout);

        for (Merge target : mergeLayout) {
            Node dsTarget = (Node) ds.getPrimitiveById(target.target.getPrimitiveId());

            for (Node candidate : target.mergeCandidates) {
                Node dsCandidate = (Node) ds.getPrimitiveById(candidate.getPrimitiveId());
                if (dsCandidate == null) {
                    //TODO Improve the merge to avoid this case!
                    Logging.info(String.format("%s-OutputOptimizerReport: Merge candidate is NULL, this should not happen!",
                            OutputOptimizer.class.getName()));
                    continue;
                }
                // replace nodes in parent ways
                dsCandidate.getParentWays().forEach(way -> {
                    while (way.containsNode(dsCandidate)) {
                        way.addNode(way.getNodes().indexOf(dsCandidate), dsTarget);
                        way.removeNode(dsCandidate);
                    }
                });
                // remove primitive
                ds.removePrimitive(dsCandidate);
            }
        }

        Collections.reverse(mergeLayout);
    }

    /**
     * This method creates a {@link Merge} object for each node in dataset on defined level.
     * The {@link Merge} object holds the node as target and a list of other nodes (tagged with the same level tag)
     * as merge candidates. Whether a node is a merge candidate or not will be determined by the distance between
     * target and node which needs to be smaller than the mergeDistance.
     * If a target has no merge candidates it will not be included in the returned list.
     *
     * @param ds            data set to find merges in
     * @param mergeDistance distance between nodes so that the nodes can be merged
     * @param level         only object with this level tag will be considered
     * @return Set of merges
     */
    private static ArrayList<Merge> findMerges(DataSet ds, double mergeDistance, int level) {
        //TODO find merges by using clustering algorithm

        ArrayList<Merge> merges = new ArrayList<>();
        ArrayList<Node> nodes = new ArrayList<>(ds.getNodes());

        for (int i = 0; i < nodes.size(); ++i) {
            Node targetNode = nodes.get(i);
            // skip if node is part of another or none level
            Number targetLevel = ParserUtility.getLevelTag(targetNode);
            if (targetLevel == null || (int) targetLevel != level) continue;
            Merge merge = new Merge(targetNode);

            for (int j = i + 1; j < nodes.size(); ++j) {
                Node mergeCandidate = nodes.get(j);
                // skip if node is part of another or none level
                Number candidateLevel = ParserUtility.getLevelTag(mergeCandidate);
                if (candidateLevel == null || (int) candidateLevel != level) continue;
                // skip (for now) if node is part of the same way
                if (ParserUtility.nodesPartOfSameWay(targetNode, mergeCandidate)) continue;

                // check distance between target and merge candidate
                double distance = ParserGeoMath.getDistance(
                        targetNode.lat(), targetNode.lon(), mergeCandidate.lat(), mergeCandidate.lon());
                if (distance < mergeDistance) {
                    merge.mergeCandidates.add(mergeCandidate);
                }
            }

            if (!merge.mergeCandidates.isEmpty()) {
                merges.add(merge);
            }
        }

        return merges;
    }

    private static class Merge {
        public final Node target;
        public final List<Node> mergeCandidates; // merge to target

        public Merge(Node root) {
            target = root;
            mergeCandidates = new ArrayList<>();
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
