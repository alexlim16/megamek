package megamek.server.managers;

import megamek.common.Game;
import megamek.common.Report;
import megamek.server.GameManager;
import megamek.server.victory.VictoryResult;

public class VictoryManager {

    GameManager gameManager;
    public VictoryManager(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    /**
     * Returns true if victory conditions have been met. Victory conditions are
     * when there is only one player left with mechs or only one team. will also
     * add some reports to reporting
     */
    public boolean victory(Game game) {
        VictoryResult vr = game.getVictoryResult();
        for (Report r : vr.processVictory(game)) {
            gameManager.addReport(r);
        }
        return vr.victory();
    }// end victory
}
