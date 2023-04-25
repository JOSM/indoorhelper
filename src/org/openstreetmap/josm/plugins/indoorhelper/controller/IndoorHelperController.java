// License: AGPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.indoorhelper.controller;

import org.openstreetmap.josm.plugins.indoorhelper.model.IndoorHelperModel;
import org.openstreetmap.josm.plugins.indoorhelper.model.LevelRangeVerifier;
import org.openstreetmap.josm.plugins.indoorhelper.model.TagCatalog.IndoorObject;
import org.openstreetmap.josm.plugins.indoorhelper.views.LevelSelectorView;
import org.openstreetmap.josm.plugins.indoorhelper.views.ToolBoxView;
import org.openstreetmap.josm.actions.ValidateAction;
import org.openstreetmap.josm.data.Preferences;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.OsmDataManager;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Tag;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.help.HelpBrowser;
import org.openstreetmap.josm.gui.mappaint.MapPaintStyles;
import org.openstreetmap.josm.spi.preferences.Config;
import org.openstreetmap.josm.spi.preferences.MapListSetting;
import org.openstreetmap.josm.spi.preferences.Setting;
import org.openstreetmap.josm.tools.Shortcut;

import javax.swing.*;
import java.awt.event.*;
import java.util.*;

import static org.openstreetmap.josm.tools.I18n.tr;

/**
 * IndoorHelper controller class which provides the communication between the
 * IndoorHelperModel and the different views.
 *
 * @author egru
 */
public class IndoorHelperController {

    private final IndoorHelperModel model = new IndoorHelperModel();
    private ToolBoxView toolboxView;
    private String workingLevel, levelNum;
    private final SpaceAction spaceAction = new SpaceAction();
    private Shortcut spaceShortcut;
    private final EnterAction enterAction = new EnterAction();
    private Shortcut enterShortcut;
    private boolean outerHelp, innerHelp, levelHelp;
    private Collection<OsmPrimitive> innerRelation;
    private LevelSelectorView selectorView;

    /**
     * The listener which provides the handling of the applyButton.
     * Gets the texts which were written by the user and writes them to the OSM-data.
     * After that it checks the tagged data.
     *
     * @author egru
     */
    private final ActionListener toolApplyButtonListener = e -> {
        IndoorObject indoorObject = toolboxView.getSelectedObject();

        // collecting all tags
        List<Tag> tags = new ArrayList<>();
        if (!toolboxView.getLevelCheckBoxStatus() && !workingLevel.equals("")) {
            tags.add(new Tag("level", workingLevel));
        }
        if (!toolboxView.getLevelNameText().isEmpty() && !toolboxView.getLevelCheckBoxStatus()) {
            tags.add(new Tag("level_name", toolboxView.getLevelNameText()));
        }
        if (!toolboxView.getNameText().isEmpty()) {
            tags.add(new Tag("name", toolboxView.getNameText()));
        }
        if (!toolboxView.getRefText().isEmpty()) {
            tags.add(new Tag("ref", toolboxView.getRefText()));
        }
        if (!toolboxView.getRepeatOnText().isEmpty()) {
            tags.add(new Tag("repeat_on", toolboxView.getRepeatOnText()));
        }
        if (!toolboxView.getLevelNameText().isEmpty() && !toolboxView.getLevelCheckBoxStatus()) {
            tags.add(new Tag("level_name", toolboxView.getLevelNameText()));
        }

        // Tagging to OSM Data
        model.addTagsToOSM(indoorObject, tags);

        // Reset UI elements
        toolboxView.resetUiElements();

        // Do the validation process
        new ValidateAction().doValidate(true);

        refreshPresets();
    };

    /**
     * The listener which is called when a new item in the object list is selected.
     */
    private final ItemListener toolObjectItemListener = e -> {
        if (toolboxView.getSelectedObject().equals(IndoorObject.ROOM)) {
            toolboxView.setNRUiElementsEnabled(true);
            toolboxView.setROUiElementsEnabled(false);
        } else if (toolboxView.getSelectedObject().equals(IndoorObject.STEPS)
                || toolboxView.getSelectedObject().equals(IndoorObject.ELEVATOR)) {
            toolboxView.setROUiElementsEnabled(true);
            toolboxView.setNRUiElementsEnabled(true);
        } else {
            toolboxView.setROUiElementsEnabled(false);
        }
    };

    /**
     * The listener which is called when the LevelCheckBox is selected.
     */
    private final ItemListener toolLevelCheckBoxListener = e -> toolboxView
            .setLVLUiElementsEnabled(e.getStateChange() != ItemEvent.SELECTED);

    /**
     * The listener which is called when the helpButton got pushed.
     */
    private final ActionListener toolHelpButtonListener = e -> HelpBrowser.setUrlForHelpTopic("Plugin/IndoorHelper");

    /**
     * The listener which is called when the addLevelButton got pushed.
     */
    private final ActionListener toolAddLevelButtonListener = e -> {
        if (selectorView == null) {
            selectorView = new LevelSelectorView();
            addLevelSelectorListeners();

            // Show LevelSelectorView
            selectorView.setVisible(true);
        } else {
            // Put focus back on LevelSelectorView
            selectorView.toFront();
        }
    };

    /**
     * The listener which is called when the MultiCheckBox is selected.
     */
    private final ItemListener toolMultiCheckBoxListener = e -> toolboxView
            .setMultiUiElementsEnabled(e.getStateChange() != ItemEvent.SELECTED);

    /**
     * The listener which is called when the OUTER Button got pushed.
     */
    private final ActionListener toolOuterButtonListener = e -> {
        // Select drawing action
        MainApplication.getMap().selectDrawTool(false);

        // For space shortcut to add the relation after spacebar got pushed {@link SpaceAction}
        outerHelp = true;
        innerHelp = false;
    };

    /**
     * The listener which is called when the INNER Button got pushed.
     */
    private final ActionListener toolInnerButtonListener = e -> {
        // Select drawing action
        MainApplication.getMap().selectDrawTool(false);

        // For space shortcut to edit the relation after enter got pushed {@link SpaceAction}{@link EnterAction}
        innerHelp = true;
        outerHelp = false;
    };

    /**
     * Listener for preset button 1.
     */
    private final ActionListener preset1Listener = e -> {
        model.addTagsToOSM(toolboxView.getPreset1());
        refreshPresets();
    };

    /**
     * Listener for preset button 2.
     */
    private final ActionListener preset2Listener = e -> {
        model.addTagsToOSM(toolboxView.getPreset2());
        refreshPresets();
    };

    /**
     * Listener for preset button 3.
     */
    private final ActionListener preset3Listener = e -> {
        model.addTagsToOSM(toolboxView.getPreset3());
        refreshPresets();
    };

    /**
     * Listener for preset button 4.
     */
    private final ActionListener preset4Listener = e -> {
        model.addTagsToOSM(toolboxView.getPreset4());
        refreshPresets();
    };

    /**
     * Updates the preset button from the current ranking.
     */
    private void refreshPresets() {
        toolboxView.setPresetButtons(model.getPresetRanking());
    }

    /**
     * Specific listener for the applyButton
     */
    private final ActionListener toolLevelOkButtonListener = e -> {
        levelHelp = true;

        // Get insert level number out of SelectorView
        if (!selectorView.getLevelNumber().equals("")) {
            levelNum = selectorView.getLevelNumber();

            // Unset visibility
            selectorView.dispose();
            // Select draw-action
            MainApplication.getMap().selectDrawTool(false);

        } else {
            JOptionPane.showMessageDialog(null, tr("Please insert a value."), tr("Error"), JOptionPane.ERROR_MESSAGE);
        }

        selectorView = null;
    };

    /**
     * Specific listener for the cancelButton
     */
    private final ActionListener toolLevelCancelButtonListener = e -> {
        selectorView.dispose();
        selectorView = null;
    };

    /**
     * General listener for LevelSelectorView window
     */
    class ToolSelectorWindowListener extends WindowAdapter {
        @Override
        public void windowClosed(WindowEvent e) {
            selectorView = null;
        }

        @Override
        public void windowClosing(WindowEvent e) {
            selectorView = null;
        }
    }

    /**
     * Constructor for the {@link IndoorHelperController} which initiates model and views.
     */
    public IndoorHelperController() {
        toolboxView = new ToolBoxView();

        // set preference if no value ist set already
        setPluginPreferences(true);

        // Ui elements
        toolboxView.setAllUiElementsEnabled(true);
        toolboxView.setROUiElementsEnabled(false);

        addToolboxListeners();
        MainApplication.getMap().addToggleDialog(toolboxView);

        // Shortcuts
        spaceShortcut = Shortcut.registerShortcut("mapmode:space", "ConfirmObjectDrawing", KeyEvent.VK_SPACE, Shortcut.DIRECT);
        MainApplication.registerActionShortcut(spaceAction, spaceShortcut);

        enterShortcut = Shortcut.registerShortcut("mapmode:enter", "ConfirmMultipolygonSelection", KeyEvent.VK_ENTER, Shortcut.DIRECT);
        MainApplication.registerActionShortcut(enterAction, enterShortcut);

        // Helper
        outerHelp = false;
        innerHelp = false;
        levelHelp = false;
        innerRelation = null;
        workingLevel = "";
        levelNum = "";
    }

    /**
     * Adds the button- and box-listeners to the {@link ToolBoxView}.
     */
    private void addToolboxListeners() {
        if (toolboxView != null) {
            toolboxView.setApplyButtonListener(toolApplyButtonListener);
            toolboxView.setLevelCheckBoxListener(toolLevelCheckBoxListener);
            toolboxView.setHelpButtonListener(toolHelpButtonListener);
            toolboxView.setAddLevelButtonListener(toolAddLevelButtonListener);
            toolboxView.setObjectItemListener(toolObjectItemListener);
            toolboxView.setOuterButtonListener(toolOuterButtonListener);
            toolboxView.setInnerButtonListener(toolInnerButtonListener);
            toolboxView.setMultiCheckBoxListener(toolMultiCheckBoxListener);
            toolboxView.setPreset1Listener(preset1Listener);
            toolboxView.setPreset2Listener(preset2Listener);
            toolboxView.setPreset3Listener(preset3Listener);
            toolboxView.setPreset4Listener(preset4Listener);
        }
    }

    /**
     * Adds the button-listeners to the {@link LevelSelectorView}.
     */
    private void addLevelSelectorListeners() {
        if (selectorView != null) {
            selectorView.setOkButtonListener(toolLevelOkButtonListener);
            selectorView.setCancelButtonListener(toolLevelCancelButtonListener);
            selectorView.setSelectorWindowListener(new ToolSelectorWindowListener());
        }
    }

    /**
     * Shortcut for spacebar
     */
    private class SpaceAction extends AbstractAction {
        private static final long serialVersionUID = 1L;

        @Override
        public void actionPerformed(ActionEvent e) {
            if (outerHelp) {
                // Create new relation and add the currently drawn object to it
                model.addRelation("outer");
                MainApplication.getMap().selectSelectTool(false);
                outerHelp = false;

                // Clear currently selection
                MainApplication.getLayerManager().getEditDataSet().clearSelection();
            } else if (innerHelp) {
                // Save new drawn relation for adding
                innerRelation = MainApplication.getLayerManager().getEditDataSet().getAllSelected();
                MainApplication.getMap().selectSelectTool(false);

                // Clear currently selection
                MainApplication.getLayerManager().getEditDataSet().clearSelection();
            } else if (levelHelp) {
                List<Tag> tags = new ArrayList<>();
                tags.add(new Tag("level", levelNum));

                // Add level tag
                model.addTagsToOSM(tags);

                // Change action
                MainApplication.getMap().selectSelectTool(false);
                levelHelp = false;
            }
        }
    }

    /**
     * Shortcut for enter
     */
    private class EnterAction extends AbstractAction {
        private static final long serialVersionUID = 1L;

        @Override
        public void actionPerformed(ActionEvent e) {
            if (innerHelp && !outerHelp) {
                // Edit the new drawn relation member to selected relation
                model.editRelation("inner", innerRelation);
                innerHelp = false;
            } else if ((innerHelp && outerHelp) || (outerHelp && !innerHelp)) {
                JOptionPane.showMessageDialog(null,
                        tr("Please press spacebar first to add \"outer\" object to relation."), tr("Relation-Error"),
                        JOptionPane.ERROR_MESSAGE);
                innerHelp = false;
                outerHelp = false;
            }
        }
    }

    /**
     * Updates visibility of objects tagged by repeat_on key using the active working level.
     */
    public void updateRepeatOnKeyFilter() {
        String key = "repeat_on";
        try{
            Integer.parseInt(workingLevel);
        }catch(Exception e){
            System.out.println("No support of -unsetSpecificKeyFilter- for float values right now.");
            return;
        }

        DataSet editDataSet = OsmDataManager.getInstance().getEditDataSet();
        if (editDataSet != null) {
            ArrayList<OsmPrimitive> primitiveCollection = new ArrayList<>(editDataSet.allPrimitives());
            ArrayList<OsmPrimitive> primitivesToDisable = new ArrayList<>();
            int level = Integer.parseInt(workingLevel);
            for (OsmPrimitive primitive : primitiveCollection) {
                if ((primitive.isDisabledAndHidden() || primitive.isDisabled()) && primitive.hasKey(key)) {
                    primitivesToDisable.add(primitive);
                }
            }

            // TODO check if we really need to perform this on a new thread
            new Thread(() -> primitivesToDisable.forEach(primitive -> {
                if (LevelRangeVerifier.isPartOfWorkingLevel(primitive.get(key), level)) {
                    primitive.unsetDisabledState();
                }
            })).start();
        }
    }

    /**
     * Function sets the current working level and updates the toolbox level label
     *
     * @param indoorLevel current working level as string
     */
    public void setWorkingLevel(String indoorLevel) {
        workingLevel = indoorLevel;
        toolboxView.setLevelLabel(workingLevel);
    }

    /**
     * Forces JOSM to load the mappaint settings.
     */
    private static void updateSettings() {
        Preferences.main().init(false);
        MapPaintStyles.readFromPreferences();
    }

    /**
     * Enables or disables the preferences for the mapcss-style if no preference is set already.
     * Else uses set preference.
     *
     * @param enabled Activates or disables the settings (if no preference set).
     */
    private static void setPluginPreferences(boolean enabled) {
        Map<String, Setting<?>> settings = Preferences.main().getAllSettings();
        String sep = System.getProperty("file.separator");

        MapListSetting styleMapListSetting = (MapListSetting) settings.get("mappaint.style.entries");
        List<Map<String, String>> styleMaps = new ArrayList<>();
        if (styleMapListSetting != null) {
            styleMaps = styleMapListSetting.getValue();
        }

        List<Map<String, String>> styleMapsNew = new ArrayList<>();

        if (!styleMaps.isEmpty()) {
            styleMapsNew.addAll(styleMaps);
        }

        for (Map<String, String> map : styleMapsNew) {
            if (map.containsValue(tr("Indoor"))) {
                // find saved preference value
                enabled = map.containsValue(tr("true"));
                styleMapsNew.remove(map);
                break;
            }
        }

        Map<String, String> indoorMapPaint = new HashMap<>();
        indoorMapPaint.put("title", tr("Indoor"));
        indoorMapPaint.put("active", Boolean.toString(enabled));
        indoorMapPaint.put("url", Preferences.main().getPluginsDirectory().toString() + sep
                + "indoorhelper" + sep + "resources" + sep + "sit.mapcss");
        styleMapsNew.add(indoorMapPaint);
        Config.getPref().putListOfMaps("mappaint.style.entries", styleMapsNew);
        updateSettings();
    }
}
