package net.sehales.scservercmds;

import net.sehales.scplayercmds.InvisibilityManager;
import net.sehales.secon.SeCon;

public class InvManager {

	private InvisibilityManager invManager;

	public InvManager() {
		invManager = (InvisibilityManager) SeCon.getAPI().getAddonAPI("InvisibilityManager");
	}

	public InvisibilityManager getInstance() {
		return invManager;
	}
}
