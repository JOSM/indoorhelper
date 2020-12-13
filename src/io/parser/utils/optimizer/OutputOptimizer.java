// License: AGPL. For details, see LICENSE file.
package io.parser.utils.optimizer;

import org.openstreetmap.gui.jmapviewer.OsmMercator;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.tools.Pair;

import java.util.ArrayList;
import java.util.List;

/**
 * Class optimizing OSM data to avoid unnecessary nodes/ways in data set
 *
 * @author rebsc
 */
public class OutputOptimizer {

    public static Pair<ArrayList<Node>, ArrayList<Way>> optimize(Configuration config, Pair<ArrayList<Node>, ArrayList<Way>> data) {
        ArrayList<Merge> merges = new ArrayList<>();

        // for each node find nodes that can be merged/replaced with/by it.
        OsmMercator mercator = new OsmMercator();
        data.a.forEach(rootNode -> {
            Merge merge = new Merge(rootNode);
            data.a.forEach(mergeCandidate -> {
                if (rootNode.getId() != mergeCandidate.getId()
                        && mercator.getDistance(rootNode.lat(), rootNode.lon(), mergeCandidate.lat(), mergeCandidate.lon()) < config.MERGE_DISTANCE) {
                    merge.mergeToRoot.add(mergeCandidate);
                }
            });
            if (!merge.mergeToRoot.isEmpty()) {
                merges.add(merge);
            }
        });

        // TODO implement


        return data;
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
