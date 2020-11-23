// License: AGPL. For details, see LICENSE file.
package io.parser.data;

import io.model.BIMtoOSMCatalog;
import io.parser.math.Matrix3D;
import io.parser.math.Vector3D;
import nl.tue.buildingsmart.express.population.EntityInstance;
import org.openstreetmap.josm.data.coor.LatLon;

import java.util.ArrayList;
import java.util.List;

/**
 * Class holding OSM relevant data of 3D BIM object
 *
 * @author rebsc
 */
public class BIMObject3D {

    // object identity
    private int id;
    private BIMtoOSMCatalog.BIMObject type;
    private EntityInstance rootEntity;
    private EntityInstance placementEntity;
    private EntityInstance representationEntity;

    // object representation data
    private List<Vector3D> cartesianShapeCoordinates;
    private List<LatLon> geodeticShapeCoordinates;

    // transformation matrices
    private Vector3D translation;
    private Matrix3D rotation;

    public BIMObject3D(int id) {
        this.id = id;
        cartesianShapeCoordinates = new ArrayList<>();
        geodeticShapeCoordinates = new ArrayList<>();
        translation = new Vector3D();
        rotation = new Matrix3D();
        rotation.setIdentity();
    }

    public BIMObject3D(
            int id,
            BIMtoOSMCatalog.BIMObject type,
            Vector3D cartesianOrigin,
            List<Vector3D> shapeCoordinates) {
        this.id = id;
        this.type = type;
        cartesianShapeCoordinates = shapeCoordinates;
        geodeticShapeCoordinates = new ArrayList<>();
        translation = cartesianOrigin;
        rotation = new Matrix3D();
        rotation.setIdentity();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public BIMtoOSMCatalog.BIMObject getType() {
        return type;
    }

    public void setType(BIMtoOSMCatalog.BIMObject type) {
        this.type = type;
    }

    public EntityInstance getRootEntity() {
        return rootEntity;
    }

    public void setRootEntity(EntityInstance rootEntity) {
        this.rootEntity = rootEntity;
    }

    public EntityInstance getPlacementEntity() {
        return placementEntity;
    }

    public void setPlacementEntity(EntityInstance placementEntity) {
        this.placementEntity = placementEntity;
    }

    public EntityInstance getRepresentationEntity() {
        return representationEntity;
    }

    public void setRepresentationEntity(EntityInstance representationEntity) {
        this.representationEntity = representationEntity;
    }

    public Vector3D getCartesianPlacement() {
        // TODO implement
        throw new UnsupportedOperationException();
    }

    public List<Vector3D> getCartesianShapeCoordinates() {
        return cartesianShapeCoordinates;
    }

    public void setCartesianShapeCoordinates(List<Vector3D> cartesianShapeCoordinates) {
        this.cartesianShapeCoordinates = cartesianShapeCoordinates;
    }

    public LatLon getGeodeticPlacement() {
        // TODO implement
        throw new UnsupportedOperationException();
    }

    public List<LatLon> getGeodeticShapeCoordinates() {
        return geodeticShapeCoordinates;
    }

    public void setGeodeticShapeCoordinates(List<LatLon> geodeticShapeCoordinates) {
        this.geodeticShapeCoordinates = geodeticShapeCoordinates;
    }

    public Vector3D getTranslation() {
        return translation;
    }

    public void setTranslation(Vector3D translation) {
        this.translation = translation;
    }

    public Matrix3D getRotation() {
        return rotation;
    }

    public void setRotation(Matrix3D rotation) {
        this.rotation = rotation;
    }
}
