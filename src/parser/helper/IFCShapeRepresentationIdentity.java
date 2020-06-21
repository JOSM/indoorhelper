package parser.helper;

import parser.helper.IFCShapeRepresentationCatalog.RepresentationIdentifier;
import parser.helper.IFCShapeRepresentationCatalog.RepresentationType;

/**
 * Class representing identifier and type on an IFCSHAPEREPRESENTATION
 * @author rebsc
 *
 */
public class IFCShapeRepresentationIdentity {
	private RepresentationIdentifier identifier;
	private RepresentationType type;

	public IFCShapeRepresentationIdentity() {
		this.identifier = null;
		this.type = null;
	}

	public IFCShapeRepresentationIdentity(RepresentationIdentifier identifier, RepresentationType type) {
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

}