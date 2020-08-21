// License: AGPL. For details, see LICENSE file.
package io.parser.data.ifc;

import io.parser.data.ifc.IFCShapeRepresentationCatalog.RepresentationIdentifier;
import io.parser.data.ifc.IFCShapeRepresentationCatalog.RepresentationType;
import nl.tue.buildingsmart.express.population.EntityInstance;

/**
 * Class representing identifier and type on an IFCSHAPEREPRESENTATION
 * @author rebsc
 *
 */
public class IFCShapeRepresentationIdentity {
	private EntityInstance rootObjectEntity;
	private EntityInstance representationObjectEntity;
	private RepresentationIdentifier identifier;
	private RepresentationType type;

	public IFCShapeRepresentationIdentity() {
		this.rootObjectEntity = null;
		this.representationObjectEntity = null;
		this.identifier = null;
		this.type = null;
	}

	public IFCShapeRepresentationIdentity(EntityInstance representationObject, EntityInstance rootEntity, RepresentationIdentifier identifier, RepresentationType type) {
		this.setRootObjectEntity(rootEntity);
		this.representationObjectEntity = representationObject;
		this.identifier = identifier;
		this.type = type;
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
		if(this.identifier != null && this.type != null) return true;
		return false;
	}

	public EntityInstance getRepresentationObjectEntity() {
		return representationObjectEntity;
	}

	public void setRepresentationObjectEntity(EntityInstance representationObjectEntity) {
		this.representationObjectEntity = representationObjectEntity;
	}

	public EntityInstance getRootObjectEntity() {
		return rootObjectEntity;
	}

	public void setRootObjectEntity(EntityInstance rootObjectEntity) {
		this.rootObjectEntity = rootObjectEntity;
	}


}