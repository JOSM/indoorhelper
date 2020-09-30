// License: AGPL. For details, see LICENSE file.
package parser;

import io.parser.BIMtoOSMParser;
import org.junit.Test;

import java.io.File;
/**
 * Unit tests of {@link BIMtoOSMParser} class.
 * @author rebsc
 */
public class BIMtoOSMParserTest {

	/**
	 * Setup test.
	 */
//	@Rule
//	public JOSMTestRules test = new JOSMTestRules().preferences();

    String resourcePathDir = System.getProperty("user.dir") + File.separator + "test" + File.separator +"resources" + File.separator;
	String ifcTestFile1 = resourcePathDir + "test1_IFC2X3_TC1.ifc";
	String ifcTestFile2 = resourcePathDir + "test2_IFC2X3_TC1.ifc";
	String ifcTestFile3 = resourcePathDir + "test1_IFC4.ifc";
	String ifcTestFile4 = resourcePathDir + "test2_IFC4.ifc";
	String ifcTestFile5 = resourcePathDir + "test3_IFC4.ifc";
	String ifcTestFile8 = resourcePathDir + "test4_IFC4.ifc";
	String ifcTestFile6 = resourcePathDir + "test1_IFC4X2.ifc";
	String ifcTestFile7 = resourcePathDir + "test1_IFC4X3.ifc";


	@Test
	public void testParse() {
		// TODO fix test and define proper return type of BIMtoOSMParser().parse method

//		// for unit tests arguments of BIMtoOSMParser constructor set to null
//
//		// IFC2X3 should pass
//		new BIMtoOSMParser(null).parse(ifcTestFile1);
//		new BIMtoOSMParser(null).parse(ifcTestFile2);
//
//		// IFC4 should pass
//		new BIMtoOSMParser(null).parse(ifcTestFile3);
//		new BIMtoOSMParser(null).parse(ifcTestFile4);
//		new BIMtoOSMParser(null).parse(ifcTestFile5);
//
//		// other should not pass - no loading
//		new BIMtoOSMParser(null).parse(ifcTestFile6);
//		new BIMtoOSMParser(null).parse(ifcTestFile7);
//
//		// corrupted file, cannot be loaded
//		new BIMtoOSMParser(null).parse(ifcTestFile8);
	}

}
