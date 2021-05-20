// License: AGPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.indoorhelper.io.actions;

import org.openstreetmap.josm.plugins.indoorhelper.io.controller.ImportEventListener;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.gui.MainApplication;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.ActionEvent;

import static org.openstreetmap.josm.tools.I18n.tr;

/**
 * Menu entry action for BIM import function.
 */
public class ImportBIMDataAction extends JosmAction {

    private ImportEventListener importListener;

    public ImportBIMDataAction(ImportEventListener listener) {
        super(tr("Import BIM File"), "dialogs/bim_small", null, null, false);
        importListener = listener;
    }

    @Override
    public void actionPerformed(ActionEvent arg0) {
        JFileChooser fc = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("IFC", "ifc");
        fc.setFileFilter(filter);
        int returnVal = fc.showOpenDialog(MainApplication.getMainFrame());
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            importListener.onBIMImport(fc.getSelectedFile().getPath());
        }
    }
}
