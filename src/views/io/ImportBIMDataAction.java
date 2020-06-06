package views.io;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;

import org.openstreetmap.josm.actions.JosmAction;

/**
 * Menu entry action for BIM import function.
 *
 * @author rebsc
 */
@SuppressWarnings("serial")
public class ImportBIMDataAction extends JosmAction{

	public ImportBIMDataAction(){
		super(tr("Import BIM file"), "dialogs/bim_small", null, null, false);
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		//TODO
	}
}
