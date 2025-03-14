// License: AGPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.indoorhelper.views;

import org.openstreetmap.josm.plugins.indoorhelper.model.TagCatalog.IndoorObject;

import javax.swing.*;

/**
 * Button with a specific IndoorObject attached to it.
 *
 * @author egru
 */
class PresetButton extends JButton {

    private IndoorObject indoorObject;

    PresetButton(IndoorObject object) {
        this.setIndoorObject(object);
    }

    public IndoorObject getIndoorObject() {
        return this.indoorObject;
    }

    public void setIndoorObject(IndoorObject object) {
        this.indoorObject = object;
        this.setText(indoorObject.toString());
        this.setToolTipText("Fast Tag: " + indoorObject.toString());
    }
}
