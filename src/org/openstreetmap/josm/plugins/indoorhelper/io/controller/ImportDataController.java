// License: AGPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.indoorhelper.io.controller;

import org.openstreetmap.josm.plugins.indoorhelper.io.actions.ImportBIMDataAction;
import org.openstreetmap.josm.plugins.indoorhelper.io.model.ImportDataModel;
import org.openstreetmap.josm.plugins.indoorhelper.io.parser.BIMtoOSMParser;
import org.openstreetmap.josm.plugins.indoorhelper.io.renderer.ImportDataRenderer;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.data.Preferences;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.MainMenu;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.gui.widgets.JMultilineLabel;
import org.openstreetmap.josm.gui.widgets.UrlLabel;
import org.openstreetmap.josm.spi.preferences.Config;
import org.openstreetmap.josm.tools.GBC;
import org.openstreetmap.josm.tools.ImageProvider;
import org.openstreetmap.josm.tools.Logging;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.jar.JarFile;
import java.util.logging.FileHandler;
import java.util.logging.SimpleFormatter;
import java.util.zip.ZipEntry;

import static java.awt.GridBagConstraints.EAST;
import static org.openstreetmap.josm.tools.I18n.tr;

/**
 * Import data controller class handles communication between {@link ImportDataModel} and import views.
 */
public class ImportDataController implements ImportEventListener {

    private final ImportDataModel model;
    private String importedFilepath;
    private final String pluginDir = Preferences.main().getPluginsDirectory().toString();

    private JFrame progressFrame;
    private JProgressBar progressBar;
    private JPanel infoPanel;

    private final String[] BIMResources = {"IFC2X3_TC1.exp", "IFC4.exp"};

    public ImportDataController() {
        model = new ImportDataModel();
        JosmAction importBIMAction = new ImportBIMDataAction(this);
        MainMenu.add(MainApplication.getMenu().fileMenu, importBIMAction, false, 21);

        // add log file handler
        try {
            FileHandler fh = new FileHandler(pluginDir + "/logfile_indoorhelper.log");
            fh.setFormatter(new SimpleFormatter());
            Logging.getLogger().addHandler(fh);
        } catch (SecurityException | IOException e) {
            Logging.info(e.getMessage());
        }

        // export resource files used by BuildingSMARTLibrary from jar to file system
        try {
            exportBIMResource();
        } catch (Exception e) {
            Logging.info(e.getMessage());
        }
    }

    @Override
    public void onBIMImport(String filepath) {
        importBIMData(filepath);
    }

    /**
     * Method handles parsing of import data
     *
     * @param filepath Full path of import file
     */
    private void importBIMData(String filepath) {
        addInfoLabel();
        importedFilepath = filepath;
        initProgressProcess();
        progressFrame.setVisible(true);
        new Thread(() -> {
            new BIMtoOSMParser(this, pluginDir + "/indoorhelper/").parse(importedFilepath);
            progressFrame.setVisible(false);
        }).start();
    }

    @Override
    public void onDataParsed(DataSet ds) {
        model.setImportData(ds);
        String layerName = String.format("BIMObject%2d", MainApplication.getLayerManager().getLayers().size());
        if (importedFilepath != null) {
            String[] parts = importedFilepath.split(File.separator.equals("\\") ? "\\\\" : "/");
            layerName = parts[parts.length - 1];
        }
        ImportDataRenderer.renderDataOnNewLayer(ds, layerName);
    }

    @Override
    public void onProcessStatusChanged(String statusMsg) {
        progressBar.setString(statusMsg);
    }

    /**
     * Initializes progress frame and progress bar used while loading file
     */
    private void initProgressProcess() {
        progressBar = new JProgressBar(0, 100);
        progressBar.setName("progressBar");
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
        MapFrame map = MainApplication.getMap();
        if (map == null) return;
        if (infoPanel != null) return;

        infoPanel = new JPanel();
        Font font = infoPanel.getFont().deriveFont(Font.PLAIN, 14.0f);
        JMultilineLabel iLabel = new JMultilineLabel(
                tr("BIM importer is in beta version! \n You can help to improve the BIM import by reporting bugs or other issues. " +
                        "For more details see logfile: <i>" + pluginDir + File.separator + "logfile_indoorhelper.log</i>"));
        UrlLabel issueURL = new UrlLabel(Config.getUrls().getJOSMWebsite() + "/newticket", tr("Report bug"));
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
        infoPanel.add(closeButton, GBC.std(3, 1).span(1, 2).anchor(EAST));
        map.addTopPanel(infoPanel);
    }

    /**
     * Export BIM resources embedded in jar into file system
     *
     * @throws Exception IOException
     */
    private void exportBIMResource() throws Exception {
        File jarFile = new File(Preferences.main().getPluginsDirectory().toURI().getPath() + "/indoorhelper.jar");
        if (jarFile.isFile()) {
            Logging.info("Copying BIM resource files from jar to file system");
            try (JarFile jar = new JarFile(jarFile)) {
                for (String filename : BIMResources) {
                    ZipEntry zip = jar.getEntry("resources/" + filename);
                    InputStream is = jar.getInputStream(zip);
                    if (Files.notExists(Paths.get(pluginDir + "/indoorhelper/resources/"))) {
                        if (!new File(pluginDir + "/indoorhelper/resources").mkdirs()) {
                            return;
                        }
                    }
                    Files.copy(is, Paths.get(pluginDir + "/indoorhelper/resources/" + filename));
                    is.close();
                }
            }
        }
    }
}
