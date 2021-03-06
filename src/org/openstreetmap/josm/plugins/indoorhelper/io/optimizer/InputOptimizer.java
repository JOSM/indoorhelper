// License: AGPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.indoorhelper.io.optimizer;

import org.openstreetmap.josm.tools.Logging;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 * Class providing methods to optimize IFC files before loading them
 */
public class InputOptimizer {

    /**
     * Optimizes an ifc file following the given config parameters
     *
     * @param ifcFilepath to file to optimize
     * @return optimized file as {@link File}
     * @throws IOException
     */
    public static File optimizeIfcFile(Configuration config, String ifcFilepath) throws IOException {
        // TODO Maybe find a better way than creating a temporary file...
        File tempFile = File.createTempFile(getFileName(ifcFilepath) + "_optimized", ".ifc");
        tempFile.deleteOnExit();

        if (config.REMOVE_BLOCK_COMMENTS) {
            StringBuilder optimizedInput = new StringBuilder("");
            try {
                File file = new File(ifcFilepath);
                Scanner reader = new Scanner(file, StandardCharsets.UTF_8.name());
                while (reader.hasNextLine()) {
                    optimizedInput.append(reader.nextLine().replaceAll("/\\*.*?\\*/", "")).append("\n");
                }
                reader.close();
            } catch (FileNotFoundException e) {
                Logging.error(e.getMessage());
            }

            try (FileWriter writer = new FileWriter(tempFile)) {
                writer.write(String.valueOf(optimizedInput));
            }
        }
        return tempFile;
    }

    /**
     * Extracts the filename of path
     *
     * @param filepath to get name for
     * @return file name without suffix or default string if no valid file name
     */
    private static String getFileName(String filepath) {
        try {
            String[] parts = filepath.split(File.separator.equals("\\") ? "\\\\" : "/");
            return parts[parts.length - 1].split("\\.")[0];
        } catch (NullPointerException e) {
            return "indoorhelper_ifc_optimized";
        }
    }

    /**
     * Configuration class for file optimization tasks
     */
    public static class Configuration {
        public final boolean REMOVE_BLOCK_COMMENTS;

        public Configuration(boolean removeBlockCommands) {
            REMOVE_BLOCK_COMMENTS = removeBlockCommands;
        }
    }

}
