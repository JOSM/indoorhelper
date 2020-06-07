// License: GPL. For details, see LICENSE file.
package controller.io;

/**
 * Listener handles import actions from file chooser.
 * @author rebsc
 *
 */
public interface ImportEventListener {
	void onBIMImport(String filepath);
}