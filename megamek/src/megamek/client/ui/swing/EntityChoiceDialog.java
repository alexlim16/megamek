package megamek.client.ui.swing;

import megamek.client.ui.swing.tooltip.UnitToolTip;
import megamek.common.Entity;
import megamek.common.annotations.Nullable;

import javax.swing.*;
import java.util.List;

/**
 * A modal dialog for choosing one or more Entities.
 */
public class EntityChoiceDialog extends AbstractChoiceDialog<Entity> {

    /** Creates new PlanetaryConditionsDialog and takes the conditions from the client's Game. */
    protected EntityChoiceDialog(JFrame frame, String message, String title,
                                 @Nullable List<Entity> targets, boolean isMultiSelect) {
        super(frame, message, title, targets, isMultiSelect);
    }

    @Override
    protected void detailLabel(JToggleButton button, Entity target)
    {
        button.setText("<html>" + UnitToolTip.getEntityTipBrief((Entity) target, null) + "</html>");
    }

    @Override
    protected void simpleLabel(JToggleButton button, Entity target) {
        button.setText("<html><b>" + target.getDisplayName() + "</b></html>");
    }

    public static @Nullable Entity showSingleChoiceDialog(JFrame frame, String message, String title, @Nullable List<Entity> targets) {
        EntityChoiceDialog dialog = new EntityChoiceDialog(frame, message, title, targets, false);

        boolean userOkay = dialog.showDialog();
        if (userOkay) {
            return dialog.getFirstChoice();
        }
        return null;
    }

    public static @Nullable List<Entity> showMultiChoiceDialog(JFrame frame, String message, String title, @Nullable List<Entity> targets) {
        EntityChoiceDialog dialog = new EntityChoiceDialog(frame, message, title, targets, true);

        boolean userOkay = dialog.showDialog();
        if (userOkay) {
            return dialog.getChoosen();
        }
        return null;
    }
}
