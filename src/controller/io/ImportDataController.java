// License: GPL. For details, see LICENSE file.
package controller.io;

import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.MainMenu;

import model.io.ImportDataModel;
import parser.BIMtoOSMParser;
import views.io.ImportBIMDataAction;

/**
 * Import data controller class which provides the communication between the
 * ImportDataModel and the import views.
 *
 * @author rebsc
 */
public class ImportDataController implements ImportEventListener{

	private final ImportDataModel model;
	private JosmAction importBIMAction;

	public ImportDataController() {
		model = new ImportDataModel();
		importBIMAction = new ImportBIMDataAction(this);
		MainMenu.add(MainApplication.getMenu().fileMenu, importBIMAction);
	}

	@Override
	public void onBIMImport(String filepath) {
		importBIMData(filepath);
	}

	/**
	 * Method handles parsing hand rendering of import data.
	 * @param filepath Full path of import file
	 */
	private void importBIMData(String filepath) {
		// parse data

		model.setImportData(new BIMtoOSMParser().parse(filepath));
		// TODO render data on new layer

	}


}
