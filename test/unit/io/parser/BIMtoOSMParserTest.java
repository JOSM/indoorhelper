// License: AGPL. For details, see LICENSE file.
package io.parser;

import org.junit.Test;
import org.openstreetmap.josm.TestUtils;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests of {@link BIMtoOSMParser} class.
 *
 * @author rebsc
 */
public class BIMtoOSMParserTest {

    /**
     * Setup test.
     */
//    @Rule
//    public JOSMTestRules test = new JOSMTestRules().preferences();

    String pluginDir = System.getProperty("user.dir");
    String resourcePathDir = TestUtils.getTestDataRoot();

    /**
     * Test case for {@link BIMtoOSMParser#parse} method.
     */
    @Test
    public void testParse() {
        // IFC2X3
        assertParseTrue("test1_IFC2X3_TC1.ifc");
        assertParseTrue("test2_IFC2X3_TC1.ifc");

        // IFC4 - disabled for now - causes ExceptionInInitializerError in test case but works with JOSM BIM-import functionality
//        assertParseTrue("test1_IFC4.ifc");
//        assertParseTrue("test2_IFC4.ifc");

        // Pre-Optimize test
        assertParseTrue("test1_IFC2X3_TC1_with_blockcomments.ifc");
        assertParseFalse("test2_IFC2X3_TC1_with_blockcomments.ifc");

        // Cannot load because of missing IFCSITE element
        assertParseFalse("test3_IFC4.ifc");
        assertParseFalse("test4_IFC4.ifc");

        // Schema is not supported
        assertParseFalse("test1_IFC4X2.ifc");
        assertParseFalse("test1_IFC4X3.ifc");
    }

    private void assertParseFalse(String filename) {
        String ifcTestFile = resourcePathDir + filename;
        assertFalse(new BIMtoOSMParser(null, pluginDir).parse(ifcTestFile), ifcTestFile);
    }

    private void assertParseTrue(String filename) {
        String ifcTestFile = resourcePathDir + filename;
        assertTrue(new BIMtoOSMParser(null, pluginDir).parse(ifcTestFile), ifcTestFile);
    }
}
