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

    public static Pair<ArrayList<Node>, ArrayList<Way>> optimize(Pair<ArrayList<Node>, ArrayList<Way>> data) {
        ArrayList<Merge> merges = new ArrayList<>();

        // for each node find nodes that can be merged/replaced with/by it.
        OsmMercator mercator = new OsmMercator();
        data.a.forEach(rootNode -> data.a.forEach(mergeCandidate -> {
            Merge merge = new Merge(rootNode);
            if (mercator.getDistance(rootNode.lat(), rootNode.lon(), mergeCandidate.lat(), mergeCandidate.lon()) < 0.1) {
                merge.mergeToRoot.add(mergeCandidate);
            }
        }));

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
}
