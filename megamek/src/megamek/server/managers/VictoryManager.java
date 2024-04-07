package megamek.server.managers;

import megamek.common.Game;
import megamek.common.Player;
import megamek.common.Report;
import megamek.common.options.OptionsConstants;
import megamek.server.GameManager;
import megamek.server.victory.VictoryResult;

import java.util.Vector;

public class VictoryManager {

    private GameManager gameManager;
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
    }

    /**
     * Forces victory for the specified player, or his/her team at the end of the round.
     */
    public void forceVictory(Game game, Player victor) {
        game.setForceVictory(true);
        if (victor.getTeam() == Player.TEAM_NONE) {
            game.setVictoryPlayerId(victor.getId());
            game.setVictoryTeam(Player.TEAM_NONE);
        } else {
            game.setVictoryPlayerId(Player.PLAYER_NONE);
            game.setVictoryTeam(victor.getTeam());
        }

        Vector<Player> playersVector = game.getPlayersVector();
        for (int i = 0; i < playersVector.size(); i++) {
            Player player = playersVector.elementAt(i);
            player.setAdmitsDefeat(false);
        }
    }

    public boolean isPlayerForcedVictory(Game game) {
        // check game options
        if (!game.getOptions().booleanOption(OptionsConstants.VICTORY_SKIP_FORCED_VICTORY)) {
            return false;
        }

        if (!game.isForceVictory()) {
            return false;
        }

        for (Player player : game.getPlayersVector()) {
            if ((player.getId() == game.getVictoryPlayerId()) || ((player.getTeam() == game.getVictoryTeam())
                    && (game.getVictoryTeam() != Player.TEAM_NONE))) {
                continue;
            }

            if (!player.admitsDefeat()) {
                return false;
            }
        }

        return true;
    }
}
