// License: AGPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.indoorhelper.io.parser.data.ifc;

import org.openstreetmap.josm.plugins.indoorhelper.io.parser.data.ifc.IfcRepresentationCatalog.*;
import nl.tue.buildingsmart.express.population.EntityInstance;

/**
 * Class holding identifier and types of IfcRepresentation
 */
public class IfcRepresentation {
    // IFC attributes
    private RepresentationIdentifier identifier;
    private RepresentationType type;

    // local attributes
    private EntityInstance entity;
    private EntityInstance rootEntity;

    public IfcRepresentation() {
        // default
    }

    public IfcRepresentation(RepresentationIdentifier identifier, RepresentationType type, EntityInstance representationObject, EntityInstance rootEntity) {
        this.identifier = identifier;
        this.type = type;
        this.entity = representationObject;
        this.rootEntity = rootEntity;
    }

    public RepresentationIdentifier getIdentifier() {
        return identifier;
    }

    public String getIdentifierString() {
        return identifier.name();
    }

    public RepresentationType getType() {
        return type;
    }

    public String getTypeString() {
        return type.name();
    }

    public void setIdentifier(RepresentationIdentifier identifier) {
        this.identifier = identifier;
    }

    public void setType(RepresentationType type) {
        this.type = type;
    }

    public boolean isFilled() {
        return this.identifier != null && this.type != null;
    }

    public EntityInstance getEntity() {
        return entity;
    }

    public void setEntity(EntityInstance entity) {
        this.entity = entity;
    }

    public EntityInstance getRootEntity() {
        return rootEntity;
    }

    public void setRootEntity(EntityInstance rootEntity) {
        this.rootEntity = rootEntity;
    }

}