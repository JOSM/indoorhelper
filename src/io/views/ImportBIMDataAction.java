// License: AGPL. For details, see LICENSE file.
package io.views;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.gui.MainApplication;

import io.controller.ImportEventListener;

/**
 * Menu entry action for BIM import function.
 *
 * @author rebsc
 */
@SuppressWarnings("serial")
public class ImportBIMDataAction extends JosmAction {

	ImportEventListener importListener;

	public ImportBIMDataAction(ImportEventListener listener){
		super(tr("Import BIM File"), "dialogs/bim_small", null, null, false);
		importListener = listener;
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
	    JFileChooser fc = new JFileChooser();
	    FileNameExtensionFilter filter = new FileNameExtensionFilter("IFC", "ifc");
	    fc.setFileFilter(filter);
	    int returnVal = fc.showOpenDialog(MainApplication.getMainFrame());
	    if(returnVal == JFileChooser.APPROVE_OPTION) {
	    	importListener.onBIMImport(fc.getSelectedFile().getPath());
	    }
	}
}
