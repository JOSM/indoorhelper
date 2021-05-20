// License: AGPL. For details, see LICENSE file.
package model;

import org.openstreetmap.josm.plugins.indoorhelper.model.IndoorLevel;
import org.junit.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests of {@link IndoorLevel} class.
 */
public class IndoorLevelTest {

    /**
     * Test case for {@link IndoorLevel#isPartOfWorkingLevel} method.
     */
    @Test
    public void testIsPartOfWorkingLevel() {
        assertTrue(IndoorLevel.isPartOfWorkingLevel("-3--1", -3));
        assertTrue(IndoorLevel.isPartOfWorkingLevel("-3--1", -2));
        assertTrue(IndoorLevel.isPartOfWorkingLevel("-3--1", -1));
        assertFalse(IndoorLevel.isPartOfWorkingLevel("-3--1", 0));
        assertFalse(IndoorLevel.isPartOfWorkingLevel("-3--1", -4));

        assertTrue(IndoorLevel.isPartOfWorkingLevel("-1;0;1", -1));
        assertTrue(IndoorLevel.isPartOfWorkingLevel("-1;0;1", 0));
        assertTrue(IndoorLevel.isPartOfWorkingLevel("-1;0;1", 1));

        assertFalse(IndoorLevel.isPartOfWorkingLevel("1;2", 0));
        assertTrue(IndoorLevel.isPartOfWorkingLevel("1;2", 1));
        assertTrue(IndoorLevel.isPartOfWorkingLevel("1;2", 2));

        assertFalse(IndoorLevel.isPartOfWorkingLevel("1", 0));
        assertTrue(IndoorLevel.isPartOfWorkingLevel("1", 1));

        assertFalse(IndoorLevel.isPartOfWorkingLevel("0-3", -1));
        assertTrue(IndoorLevel.isPartOfWorkingLevel("0-3", 0));
        assertTrue(IndoorLevel.isPartOfWorkingLevel("0-3", 1));
        assertTrue(IndoorLevel.isPartOfWorkingLevel("0-3", 3));
        assertFalse(IndoorLevel.isPartOfWorkingLevel("0-3", 4));

        assertFalse(IndoorLevel.isPartOfWorkingLevel("2;3;4", 1));
        assertTrue(IndoorLevel.isPartOfWorkingLevel("2;3;4", 2));
        assertTrue(IndoorLevel.isPartOfWorkingLevel("2;3;4", 3));
        assertTrue(IndoorLevel.isPartOfWorkingLevel("2;3;4", 4));

        assertFalse(IndoorLevel.isPartOfWorkingLevel(".", 4));
        assertFalse(IndoorLevel.isPartOfWorkingLevel(";t-3", 4));
    }
}
