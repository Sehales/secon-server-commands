
package net.sehales.scservercmds;

import net.sehales.scplayercmds.InvisibilityManager;
import net.sehales.scplayercmds.PlayerCmdCollection;
import net.sehales.secon.addon.Addon;

public class InvManagerWrapper {
    
    private InvisibilityManager invManager;
    
    public InvManagerWrapper(Addon playerCmds) {
        ((PlayerCmdCollection) playerCmds).getInvManager();
    }
    
    public InvisibilityManager getInstance() {
        return invManager;
    }
}
