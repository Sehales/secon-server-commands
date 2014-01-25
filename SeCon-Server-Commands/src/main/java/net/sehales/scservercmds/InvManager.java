
package net.sehales.scservercmds;

import net.sehales.scplayercmds.InvisibilityManager;
import net.sehales.secon.addon.Addon;

public class InvManager {
    
    private InvisibilityManager invManager;
    
    public InvManager(Addon playerCmds) {
        // invManager =
        playerCmds.getInvManager();
        ;
    }
    
    public InvisibilityManager getInstance() {
        return invManager;
    }
}
