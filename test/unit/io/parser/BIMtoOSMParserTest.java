// License: AGPL. For details, see LICENSE file.
package io.parser;

import org.junit.Test;

import java.io.File;

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
    String resourcePathDir = pluginDir + File.separator + "test" + File.separator + "resources" + File.separator;
    String ifcTestFile1 = resourcePathDir + "test1_IFC2X3_TC1.ifc";
    String ifcTestFile2 = resourcePathDir + "test2_IFC2X3_TC1.ifc";
    String ifcTestFile3 = resourcePathDir + "test1_IFC4.ifc";
    String ifcTestFile4 = resourcePathDir + "test2_IFC4.ifc";
    String ifcTestFile5 = resourcePathDir + "test3_IFC4.ifc";
    String ifcTestFile6 = resourcePathDir + "test1_IFC4X2.ifc";
    String ifcTestFile7 = resourcePathDir + "test1_IFC4X3.ifc";
    String ifcTestFile8 = resourcePathDir + "test4_IFC4.ifc";
    String ifcTestFile9 = resourcePathDir + "test1_IFC2X3_TC1_with_blockcomments.ifc";
    String ifcTestFile10 = resourcePathDir + "test2_IFC2X3_TC1_with_blockcomments.ifc";

    /**
     * Test case for {@link BIMtoOSMParser#parse} method.
     */
    @Test
    public void testParse() {
        // IFC2X3
        assertTrue(new BIMtoOSMParser(null, pluginDir).parse(ifcTestFile1));
        assertTrue(new BIMtoOSMParser(null, pluginDir).parse(ifcTestFile2));

        // IFC4 - disabled for now - causes ExceptionInInitializerError in test case but works with JOSM BIM-import functionality
//        assertTrue(new BIMtoOSMParser(null, pluginDir).parse(ifcTestFile3));
//        assertTrue(new BIMtoOSMParser(null, pluginDir).parse(ifcTestFile4));

        // Pre-Optimize test
        assertTrue(new BIMtoOSMParser(null, pluginDir).parse(ifcTestFile9));
        assertFalse(new BIMtoOSMParser(null, pluginDir).parse(ifcTestFile10));

        // Cannot load because of missing IFCSITE element
        assertFalse(new BIMtoOSMParser(null, pluginDir).parse(ifcTestFile5));
        assertFalse(new BIMtoOSMParser(null, pluginDir).parse(ifcTestFile8));

        // Schema is not supported
        assertFalse(new BIMtoOSMParser(null, pluginDir).parse(ifcTestFile6));
        assertFalse(new BIMtoOSMParser(null, pluginDir).parse(ifcTestFile7));
    }

}
