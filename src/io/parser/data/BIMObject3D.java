// License: AGPL. For details, see LICENSE file.
package io.parser.data;

import io.model.BIMtoOSMCatalog;
import nl.tue.buildingsmart.express.population.EntityInstance;
import org.openstreetmap.josm.data.coor.LatLon;

import java.util.List;

/**
 * Class holding OSM relevant data of 3D BIM object
 *
 * @author rebsc
 */
public class BIMObject3D {

    // object identity
    private int objectId;
    private BIMtoOSMCatalog.BIMObject type;
    private EntityInstance BIMFileRootEntity;
    private EntityInstance representationEntity;

    // object representation data
    private Point3D cartesianPlacement;
    private LatLon geodeticPlacement;
    private List<Point3D> cartesianShapeCoordinates;
    private List<LatLon> geodeticShapeCoordinates;

    public BIMObject3D(int id, EntityInstance representationEntity, BIMtoOSMCatalog.BIMObject type) {
        this.objectId = id;
        this.representationEntity = representationEntity;
        this.type = type;
    }

    public BIMObject3D(
            int id,
            BIMtoOSMCatalog.BIMObject type,
            Point3D cartesianCorner,
            List<Point3D> shapeCoordinates) {
        this.objectId = id;
        this.type = type;
        this.cartesianPlacement = cartesianCorner;
        this.geodeticPlacement = null;
        this.cartesianShapeCoordinates = shapeCoordinates;
        this.geodeticShapeCoordinates = null;
    }

    public BIMtoOSMCatalog.BIMObject getType() {
        return type;
    }

    public void setType(BIMtoOSMCatalog.BIMObject type) {
        this.type = type;
    }

    public Point3D getCartesianPlacement() {
        return cartesianPlacement;
    }

    public void setCartesianPlacement(Point3D cartesianCorner) {
        this.cartesianPlacement = cartesianCorner;
    }

    public List<Point3D> getCartesianShapeCoordinates() {
        return cartesianShapeCoordinates;
    }

    public void setCartesianShapeCoordinates(List<Point3D> cartesianShapeCoordinates) {
        this.cartesianShapeCoordinates = cartesianShapeCoordinates;
    }

    public int getObjectId() {
        return objectId;
    }

    public void setObjectId(int objectId) {
        this.objectId = objectId;
    }

    public LatLon getGeodeticPlacement() {
        return geodeticPlacement;
    }

    public void setGeodeticPlacement(LatLon geodeticPlacement) {
        this.geodeticPlacement = geodeticPlacement;
    }

    public List<LatLon> getGeodeticShapeCoordinates() {
        return geodeticShapeCoordinates;
    }

    public void setGeodeticShapeCoordinates(List<LatLon> geodeticShapeCoordinates) {
        this.geodeticShapeCoordinates = geodeticShapeCoordinates;
    }

    public EntityInstance getRepresentationEntity() {
        return representationEntity;
    }

    public void setRepresentationEntity(EntityInstance representationEntity) {
        this.representationEntity = representationEntity;
    }

    public EntityInstance getBIMFileRootEntity() {
        return BIMFileRootEntity;
    }

    public void setBIMFileRootEntity(EntityInstance rootEntity) {
        this.BIMFileRootEntity = rootEntity;
    }

}
