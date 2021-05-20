// License: AGPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.indoorhelper.io.parser.data;

import nl.tue.buildingsmart.express.population.EntityInstance;

import java.util.List;

/**
 * Data structure holding specific BIM data elements.
 *
 * @author rebsc
 */
public class BIMDataCollection {
    private EntityInstance ifcSite;    // root of data set
    private List<EntityInstance> areaObjects;
    private List<EntityInstance> wallObjects;
    private List<EntityInstance> columnObjects;
    private List<EntityInstance> doorObjects;
    private List<EntityInstance> stairObjects;
    private List<EntityInstance> windowObjects;
    private List<EntityInstance> relVoidsElements;    // IfcRelVoidsElement

    public EntityInstance getIfcSite() {
        return ifcSite;
    }

    public void setIfcSite(EntityInstance ifcSite) {
        this.ifcSite = ifcSite;
    }

    public List<EntityInstance> getAreaObjects() {
        return areaObjects;
    }

    public void setAreaObjects(List<EntityInstance> areaObjects) {
        this.areaObjects = areaObjects;
    }

    public List<EntityInstance> getWallObjects() {
        return wallObjects;
    }

    public void setWallObjects(List<EntityInstance> wallObjects) {
        this.wallObjects = wallObjects;
    }

    public List<EntityInstance> getColumnObjects() {
        return columnObjects;
    }

    public void setColumnObjects(List<EntityInstance> columnObjects) {
        this.columnObjects = columnObjects;
    }

    public List<EntityInstance> getDoorObjects() {
        return doorObjects;
    }

    public void setDoorObjects(List<EntityInstance> doorObjects) {
        this.doorObjects = doorObjects;
    }

    public List<EntityInstance> getStairObjects() {
        return stairObjects;
    }

    public void setStairObjects(List<EntityInstance> stairObjects) {
        this.stairObjects = stairObjects;
    }

    public List<EntityInstance> getWindowObjects() {
        return windowObjects;
    }

    public void setWindowObjects(List<EntityInstance> windowObjects) {
        this.windowObjects = windowObjects;
    }

    public List<EntityInstance> getRelVoidsElements() {
        return relVoidsElements;
    }

    public void setRelVoidsElements(List<EntityInstance> relVoidsElements) {
        this.relVoidsElements = relVoidsElements;
    }

    public int getSize() {
        return (areaObjects.size() + wallObjects.size() +
                columnObjects.size() + doorObjects.size() + stairObjects.size());
    }

}
