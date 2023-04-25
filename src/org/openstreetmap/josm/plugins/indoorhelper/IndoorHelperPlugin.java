// License: AGPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.indoorhelper;

import org.openstreetmap.josm.data.Preferences;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.gui.autofilter.AutoFilter;
import org.openstreetmap.josm.gui.autofilter.AutoFilterManager;
import org.openstreetmap.josm.gui.layer.MainLayerManager.ActiveLayerChangeEvent;
import org.openstreetmap.josm.gui.layer.MainLayerManager.ActiveLayerChangeListener;
import org.openstreetmap.josm.gui.layer.MapViewPaintable.PaintableInvalidationEvent;
import org.openstreetmap.josm.gui.layer.MapViewPaintable.PaintableInvalidationListener;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;
import org.openstreetmap.josm.plugins.indoorhelper.controller.IndoorHelperController;
import org.openstreetmap.josm.plugins.indoorhelper.io.controller.ImportDataController;
import org.openstreetmap.josm.tools.Logging;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

/**
 * This is the main class for the indoorhelper plug-in.
 *
 * @author egru
 */
public class IndoorHelperPlugin extends Plugin implements PaintableInvalidationListener, ActiveLayerChangeListener {

    private IndoorHelperController indoorController;    // controller for indoor helper panel
    private ImportDataController importController = null;        // controller for import function

    private final String pluginDir = Preferences.main().getPluginsDirectory().toString();
    private final String[] styleFiles = {"sit.mapcss", "entrance_door_icon.png", "entrance_icon.png", "elevator_icon.png"};

    /**
     * Constructor for the plug-in.
     * <p>
     * Exports the needed files and adds them to the settings.
     *
     * @param info general information about the plug-in
     * @throws IOException if any I/O error occurs
     */
    public IndoorHelperPlugin(PluginInformation info) throws IOException {
        super(info);
        try {
            exportStyleFiles();
        } catch (Exception e) {
            Logging.info(e.getMessage());
        }
        MainApplication.getLayerManager().addAndFireActiveLayerChangeListener(this);
    }

    @Override
    public void mapFrameInitialized(MapFrame oldFrame, MapFrame newFrame) {
        super.mapFrameInitialized(oldFrame, newFrame);

        if (oldFrame == null && newFrame != null) {
            // Secures that the plug-in is only loaded, if a new MapFrame is created.
            indoorController = new IndoorHelperController();
            if (importController == null) {
                importController = new ImportDataController();
            }
        }
    }

    /**
     * Exports the mapCSS files to the resources directory.
     *
     * @throws IOException if any I/O error occurs
     */
    private void exportStyleFiles() throws IOException {
        File jarFile = new File(Preferences.main().getPluginsDirectory().toURI().getPath() + "/indoorhelper.jar");
        if (jarFile.isFile()) {
            Logging.info("Copying style resource files from jar to file system");
            try (JarFile jar = new JarFile(jarFile)) {
                for (String filename : styleFiles) {
                    ZipEntry zip = jar.getEntry("data/" + filename);
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

    @Override
    public void activeOrEditLayerChanged(ActiveLayerChangeEvent e) {
        OsmDataLayer editLayer = MainApplication.getLayerManager().getEditLayer();
        if (editLayer != null) {
            editLayer.addInvalidationListener(this);
        }
    }

    @Override
    public void paintableInvalidated(PaintableInvalidationEvent event) {
        AutoFilter currentAutoFilter = AutoFilterManager.getInstance().getCurrentAutoFilter();
        if (currentAutoFilter != null) {
            if (indoorController != null) {
                indoorController.setWorkingLevel(currentAutoFilter.getLabel());
                indoorController.updateRepeatOnKeyFilter();
            }
        } else if (indoorController != null) {
            indoorController.setWorkingLevel("");
        }
    }

}
