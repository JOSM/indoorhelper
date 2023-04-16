// License: AGPL. For details, see LICENSE file.
package model;

import org.openstreetmap.josm.plugins.indoorhelper.model.LevelRangeVerifier;
import org.junit.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests of {@link LevelRangeVerifier} class.
 */
public class LevelRangeVerifierTest {

    /**
     * Test case for {@link LevelRangeVerifier#isPartOfWorkingLevel(String, int)} method.
     */
    @Test
    public void testIsPartOfWorkingLevel() {
        assertTrue(LevelRangeVerifier.isPartOfWorkingLevel("-3--1", -3));
        assertTrue(LevelRangeVerifier.isPartOfWorkingLevel("-3--1", -2));
        assertTrue(LevelRangeVerifier.isPartOfWorkingLevel("-3--1", -1));
        assertFalse(LevelRangeVerifier.isPartOfWorkingLevel("-3--1", 0));
        assertFalse(LevelRangeVerifier.isPartOfWorkingLevel("-3--1", -4));

        assertTrue(LevelRangeVerifier.isPartOfWorkingLevel("-2-14", -2));
        assertTrue(LevelRangeVerifier.isPartOfWorkingLevel("-2-14", 9));
        assertTrue(LevelRangeVerifier.isPartOfWorkingLevel("-2-14", 14));
        assertFalse(LevelRangeVerifier.isPartOfWorkingLevel("-2-14", -3));
        assertFalse(LevelRangeVerifier.isPartOfWorkingLevel("-2-14", 15));

        assertTrue(LevelRangeVerifier.isPartOfWorkingLevel("-1;0;1", -1));
        assertTrue(LevelRangeVerifier.isPartOfWorkingLevel("-1;0;1", 0));
        assertTrue(LevelRangeVerifier.isPartOfWorkingLevel("-1;0;1", 1));

        assertFalse(LevelRangeVerifier.isPartOfWorkingLevel("1;2", 0));
        assertTrue(LevelRangeVerifier.isPartOfWorkingLevel("1;2", 1));
        assertTrue(LevelRangeVerifier.isPartOfWorkingLevel("1;2", 2));

        assertFalse(LevelRangeVerifier.isPartOfWorkingLevel("1", 0));
        assertTrue(LevelRangeVerifier.isPartOfWorkingLevel("1", 1));

        assertFalse(LevelRangeVerifier.isPartOfWorkingLevel("0-3", -1));
        assertTrue(LevelRangeVerifier.isPartOfWorkingLevel("0-3", 0));
        assertTrue(LevelRangeVerifier.isPartOfWorkingLevel("0-3", 1));
        assertTrue(LevelRangeVerifier.isPartOfWorkingLevel("0-3", 3));
        assertFalse(LevelRangeVerifier.isPartOfWorkingLevel("0-3", 4));

        assertFalse(LevelRangeVerifier.isPartOfWorkingLevel("2;3;4", 1));
        assertTrue(LevelRangeVerifier.isPartOfWorkingLevel("2;3;4", 2));
        assertTrue(LevelRangeVerifier.isPartOfWorkingLevel("2;3;4", 3));
        assertTrue(LevelRangeVerifier.isPartOfWorkingLevel("2;3;4", 4));

        assertTrue(LevelRangeVerifier.isPartOfWorkingLevel("0;2-3", 0));
        assertTrue(LevelRangeVerifier.isPartOfWorkingLevel("0;2-3", 2));
        assertTrue(LevelRangeVerifier.isPartOfWorkingLevel("0;2-3", 3));
        assertFalse(LevelRangeVerifier.isPartOfWorkingLevel("0;2-3", 1));
        assertFalse(LevelRangeVerifier.isPartOfWorkingLevel("0;2-3", 4));
        assertFalse(LevelRangeVerifier.isPartOfWorkingLevel("0;2-3", -1));

        assertFalse(LevelRangeVerifier.isPartOfWorkingLevel(".", 4));
        assertFalse(LevelRangeVerifier.isPartOfWorkingLevel(";t-3", 4));
    }
}
