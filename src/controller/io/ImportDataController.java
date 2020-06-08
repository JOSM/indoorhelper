// License: GPL. For details, see LICENSE file.
package controller.io;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JProgressBar;

import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.data.osm.DataSet;
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
	BIMtoOSMParser parser;

	private JProgressBar progressBar;
	private JFrame progressFrame;

	public ImportDataController() {
		model = new ImportDataModel();
		parser = new BIMtoOSMParser(this);
		importBIMAction = new ImportBIMDataAction(this);
		MainMenu.add(MainApplication.getMenu().fileMenu, importBIMAction);
		initProgressProcess();
	}

	@Override
	public void onBIMImport(String filepath) {
		importBIMData(filepath);
	}

	/**
	 * Method handles parsing of import data.
	 * @param filepath Full path of import file
	 */
	private void importBIMData(String filepath) {
		progressFrame.setVisible(true);
		// parse data, parse on extra thread to show progress bar while parsing
		new Thread(new Runnable() {
			@Override
	        public void run() {
				parser.parse(filepath);
			 	progressFrame.setVisible(false);
			}
		}).start();
	}

	@Override
	public void onDataParsed(DataSet osmData) {
		model.setImportData(osmData);
		// TODO render data
	}

	/**
	 * Initializes progress frame and progress bar used while loading file
	 */
	private void initProgressProcess() {
    	progressBar = new JProgressBar( 0, 100 );
		progressBar.setIndeterminate(true);
		progressBar.setStringPainted(true);
		progressBar.setString("loading file");
		progressFrame = new JFrame();
		progressFrame.add(progressBar, BorderLayout.PAGE_START);
		progressFrame.setUndecorated(true);
		progressBar.setStringPainted( true );
	    progressFrame.setAlwaysOnTop(true);
	    progressFrame.setLocationRelativeTo(progressFrame.getOwner());
	    progressFrame.pack();
	}

}
