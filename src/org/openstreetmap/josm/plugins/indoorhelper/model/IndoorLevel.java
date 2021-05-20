// License: AGPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.indoorhelper.model;

import org.openstreetmap.josm.data.osm.Tag;

/**
 * The class to save a level of the building.
 *
 * @author egru
 */

public class IndoorLevel {

    private Tag levelNumberTag;
    private Tag nameTag;

    /**
     * Constructor which adds the level number.
     *
     * @param levelNumber number of the level
     */
    public IndoorLevel(int levelNumber) {
        this.setLevelNumber(levelNumber);
    }

    /**
     * Constructor which adds level number and name tag.
     *
     * @param levelNumber number of the level
     * @param nameTag     optional name tag for the level
     */
    public IndoorLevel(int levelNumber, String nameTag) {
        this.setLevelNumber(levelNumber);
        this.setNameTag(nameTag);
    }

    /**
     * Getter for the level tag
     *
     * @return the complete level number tag
     */
    public Tag getLevelNumberTag() {
        return this.levelNumberTag;
    }

    /**
     * Function to get the level number
     *
     * @return level number as an Integer
     */
    public int getLevelNumber() {
        return Integer.parseInt(this.levelNumberTag.getValue());
    }

    /**
     * Setter for the level number
     *
     * @param levelNumber number of the level
     */
    public void setLevelNumber(int levelNumber) {
        this.levelNumberTag = new Tag("indoor:level", Integer.toString(levelNumber));
    }

    /**
     * Getter for the name tag
     *
     * @return the complete name tag
     */
    public Tag getNameTag() {
        return this.nameTag;
    }

    /**
     * Function to get the optional name of the level.
     *
     * @return String with the optional name.
     */
    public String getName() {
        return this.nameTag.getValue();
    }

    /**
     * Setter for the name tag
     *
     * @param nameTag String which optionally describes the level
     */
    public void setNameTag(String nameTag) {
        this.nameTag = new Tag("indoor:level:name", nameTag);
    }

    public boolean hasEmptyName() {
        return this.nameTag == null;
    }

    /**
     * Checks if repeat_on tag value includes current working level value
     *
     * @param repeatOnValue tag as string
     * @param workingLevel  current working level as value
     * @return true if repeat_on tag includes current working level, else false
     */
    public static boolean isPartOfWorkingLevel(String repeatOnValue, int workingLevel) {
        // check if repeat_on value includes amount of levels
        if (repeatOnValue.contains(";")) {
            try {
                String[] levels = repeatOnValue.split(";");
                for (String levelIndex : levels) {
                    if (workingLevel == Integer.parseInt(levelIndex)) return true;
                }
            } catch (Exception ignored) {
                return false;
            }
        }
        // check if repeat_on value includes a range
        else if (repeatOnValue.contains("-")) {
            try {
                char[] elements = repeatOnValue.toCharArray();
                int minLimit = 0;
                int maxLimit = 0;

                if (repeatOnValue.lastIndexOf("-") == 1) {    // case 2-1
                    minLimit = Integer.parseInt(Character.toString(elements[0]));
                    maxLimit = Integer.parseInt(Character.toString(elements[2]));
                } else if (repeatOnValue.lastIndexOf("-") == 2) {   // case -2-1
                    minLimit = Integer.parseInt(Character.toString(elements[1])) * -1;
                    maxLimit = Integer.parseInt(Character.toString(elements[3]));
                } else if (repeatOnValue.lastIndexOf("-") == 3) {   // case -2--1
                    minLimit = Integer.parseInt(Character.toString(elements[1])) * -1;
                    maxLimit = Integer.parseInt(Character.toString(elements[4])) * -1;
                }

                if (minLimit == maxLimit) {
                    return false;
                }

                if (minLimit <= workingLevel && maxLimit >= workingLevel) {
                    return true;
                }
            } catch (Exception ignored) {
                return false;
            }
        }
        // repeat_on value is single value
        else {
            try {
                return workingLevel == Integer.parseInt(repeatOnValue);
            } catch (Exception ignored) {
                return false;
            }
        }
        return false;
    }
}
