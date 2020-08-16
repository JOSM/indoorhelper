// License: GPL. For details, see LICENSE file.
package io.controller;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.FileHandler;
import java.util.logging.SimpleFormatter;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;

import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.MainMenu;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.gui.widgets.JMultilineLabel;
import org.openstreetmap.josm.gui.widgets.UrlLabel;
import org.openstreetmap.josm.spi.preferences.Config;
import org.openstreetmap.josm.tools.GBC;
import org.openstreetmap.josm.tools.ImageProvider;
import org.openstreetmap.josm.tools.Logging;

import io.model.ImportDataModel;
import io.parser.BIMtoOSMParser;
import io.renderer.ImportDataRenderer;
import io.views.ImportBIMDataAction;

/**
 * Import data controller class which provides the communication between the
 * ImportDataModel and the import views.
 *
 * @author rebsc
 */
public class ImportDataController implements ImportEventListener {

	private final ImportDataModel model;
	private JosmAction importBIMAction;
	private BIMtoOSMParser parser;

	private JProgressBar progressBar;
	private JFrame progressFrame;

	private String importedFilepath;

	public ImportDataController() {
		model = new ImportDataModel();
		parser = new BIMtoOSMParser(this);
		importBIMAction = new ImportBIMDataAction(this);
		MainMenu.add(MainApplication.getMenu().fileMenu, importBIMAction, false, 21);
		initProgressProcess();

		// add log file handler
		try {
			FileHandler fh = new FileHandler("C:/tmp/.josm/logfile_indoorhelper.log");
	        fh.setFormatter(new SimpleFormatter());
			Logging.getLogger().addHandler(fh);
		} catch (SecurityException | IOException e) {
			Logging.info(e.getMessage());
		}
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
		addInfoLabel();
		importedFilepath = filepath;
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
	public void onDataParsed(ArrayList<Way> ways, ArrayList<Node> nodes) {
		model.setImportData(ways, nodes);

		String layerName = String.format("BIMObject%2d", MainApplication.getLayerManager().getLayers().size());
		if(importedFilepath != null) {
			String[] parts = importedFilepath.split(File.separator.equals ("\\")? "\\\\": "/");
			layerName = parts[parts.length-1];
		}
		ImportDataRenderer.renderDataOnNewLayer(ways, nodes, layerName);
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
		progressBar.setStringPainted(true);
	    progressFrame.setLocationRelativeTo(MainApplication.getMainFrame());
	    progressFrame.pack();
	}

	/**
	 * Shows info panel at top
	 */
	private void addInfoLabel() {
		JPanel infoPanel = new JPanel();
		Font font = infoPanel.getFont().deriveFont(Font.PLAIN, 14.0f);
        JMultilineLabel iLabel = new JMultilineLabel(
                tr("BIM importer is in beta version! \n You can help to improve the BIM import by reporting bugs or other issues. " +
                		"For more details see the logfile: <i>C:/tmp/.josm/logfile_indoorhelper.log</i>"));
        UrlLabel issueURL = new UrlLabel(Config.getUrls().getJOSMWebsite() + "/newticket", tr("Report issue..."));
        issueURL.setFont(font);
        iLabel.setFont(font);
        iLabel.setForeground(Color.BLACK);
        infoPanel.setLayout(new GridBagLayout());
        infoPanel.add(iLabel, GBC.std(1, 1).fill());
        infoPanel.setBorder(new CompoundBorder(new EtchedBorder(EtchedBorder.LOWERED), new EmptyBorder(12, 12, 12, 12)));
        infoPanel.setBackground(new Color(224, 236, 249));
        infoPanel.add(issueURL, GBC.std(2, 1).fill());

        JButton closeButton = new JButton(ImageProvider.get("misc", "black_x"));
        closeButton.setContentAreaFilled(false);
        closeButton.setRolloverEnabled(true);
        closeButton.setBorderPainted(false);
        closeButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        closeButton.setToolTipText(tr("Hide this message"));
        closeButton.addActionListener(e -> {
            if (MainApplication.isDisplayingMapView()) {
            	infoPanel.setVisible(false);
            }
        });
        infoPanel.add(closeButton, GBC.std(3, 1).span(1, 2).anchor(GBC.EAST));
        MapFrame map = MainApplication.getMap();
        if(map != null)	map.addTopPanel(infoPanel);
	}

}
