package megamek.server.managers;

import megamek.common.Game;
import megamek.common.Entity;
import megamek.common.IEntityRemovalConditions;
import megamek.common.Player;
import megamek.common.Report;
import megamek.server.GameManager;
import megamek.server.UnitStatusFormatter;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.Vector;

public class VictoryReportManager {
    protected GameManager gameManager;
    public VictoryReportManager(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    /**
     * Writes the victory report
     */
    public void prepareVictoryReport(Game game) {
        // remove carcasses to the graveyard
        Vector<Entity> toRemove = new Vector<>();
        for (Entity e : game.getEntitiesVector()) {
            if (e.isCarcass() && !e.isDestroyed()) {
                toRemove.add(e);
            }
        }
        for (Entity e : toRemove) {
            gameManager.destroyEntity(e, "crew death", false, true);
            game.removeEntity(e.getId(), IEntityRemovalConditions.REMOVE_SALVAGEABLE);
            e.setDestroyed(true);
        }

        gameManager.addReport(new Report(7000, Report.PUBLIC));

        // Declare the victor
        Report r = new Report(1210);
        r.type = Report.PUBLIC;
        if (game.getVictoryTeam() == Player.TEAM_NONE) {
            Player player = game.getPlayer(game.getVictoryPlayerId());
            if (null == player) {
                r.messageId = 7005;
            } else {
                r.messageId = 7010;
                r.add(player.getColorForPlayer());
            }
        } else {
            // Team victory
            r.messageId = 7015;
            r.add(game.getVictoryTeam());
        }
        gameManager.addReport(r);

        gameManager.bvReports(false);

        // List the survivors
        Iterator<Entity> survivors = game.getEntities();
        if (survivors.hasNext()) {
            gameManager.addReport(new Report(7023, Report.PUBLIC));
            while (survivors.hasNext()) {
                Entity entity = survivors.next();

                if (!entity.isDeployed()) {
                    continue;
                }

                gameManager.addReport(entity.victoryReport());
            }
        }
        // List units that never deployed
        Iterator<Entity> undeployed = game.getEntities();
        if (undeployed.hasNext()) {
            boolean wroteHeader = false;

            while (undeployed.hasNext()) {
                Entity entity = undeployed.next();

                if (entity.isDeployed()) {
                    continue;
                }

                if (!wroteHeader) {
                    gameManager.addReport(new Report(7075, Report.PUBLIC));
                    wroteHeader = true;
                }

                gameManager.addReport(entity.victoryReport());
            }
        }
        // List units that retreated
        Enumeration<Entity> retreat = game.getRetreatedEntities();
        if (retreat.hasMoreElements()) {
            gameManager.addReport(new Report(7080, Report.PUBLIC));
            while (retreat.hasMoreElements()) {
                Entity entity = retreat.nextElement();
                gameManager.addReport(entity.victoryReport());
            }
        }
        // List destroyed units
        Enumeration<Entity> graveyard = game.getGraveyardEntities();
        if (graveyard.hasMoreElements()) {
            gameManager.addReport(new Report(7085, Report.PUBLIC));
            while (graveyard.hasMoreElements()) {
                Entity entity = graveyard.nextElement();
                gameManager.addReport(entity.victoryReport());
            }
        }
        // List devastated units (not salvageable)
        Enumeration<Entity> devastated = game.getDevastatedEntities();
        if (devastated.hasMoreElements()) {
            gameManager.addReport(new Report(7090, Report.PUBLIC));

            while (devastated.hasMoreElements()) {
                Entity entity = devastated.nextElement();
                gameManager.addReport(entity.victoryReport());
            }
        }
        // Let player know about entitystatus.txt file
        gameManager.addReport(new Report(7095, Report.PUBLIC));
    }

    /**
     * Generates a detailed report for campaign use
     */
    public String getDetailedVictoryReport(Game game) {
        StringBuilder sb = new StringBuilder();

        Vector<Entity> vAllUnits = new Vector<>();
        for (Iterator<Entity> i = game.getEntities(); i.hasNext(); ) {
            vAllUnits.addElement(i.next());
        }

        for (Enumeration<Entity> i = game.getRetreatedEntities(); i.hasMoreElements(); ) {
            vAllUnits.addElement(i.nextElement());
        }

        for (Enumeration<Entity> i = game.getGraveyardEntities(); i.hasMoreElements(); ) {
            vAllUnits.addElement(i.nextElement());
        }

        for (Enumeration<Player> i = game.getPlayers(); i.hasMoreElements(); ) {
            // Record the player.
            Player p = i.nextElement();
            sb.append("++++++++++ ").append(p.getName()).append(" ++++++++++\n");

            // Record the player's alive, retreated, or salvageable units.
            for (int x = 0; x < vAllUnits.size(); x++) {
                Entity e = vAllUnits.elementAt(x);
                if (e.getOwner() == p) {
                    sb.append(UnitStatusFormatter.format(e));
                }
            }

            // Record the player's devastated units.
            Enumeration<Entity> devastated = game.getDevastatedEntities();
            if (devastated.hasMoreElements()) {
                sb.append("=============================================================\n");
                sb.append("The following utterly destroyed units are not available for salvage:\n");
                while (devastated.hasMoreElements()) {
                    Entity e = devastated.nextElement();
                    if (e.getOwner() == p) {
                        sb.append(e.getShortName());
                        for (int pos = 0; pos < e.getCrew().getSlotCount(); pos++) {
                            sb.append(", ").append(e.getCrew().getNameAndRole(pos)).append(" (")
                                    .append(e.getCrew().getGunnery()).append('/')
                                    .append(e.getCrew().getPiloting()).append(")\n");
                        }
                    }
                }
                sb.append("=============================================================\n");
            }
        }

        return sb.toString();
    }
}
