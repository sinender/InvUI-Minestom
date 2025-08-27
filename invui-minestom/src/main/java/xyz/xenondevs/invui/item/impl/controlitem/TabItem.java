package xyz.xenondevs.invui.item.impl.controlitem;

import net.minestom.server.entity.Player;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.inventory.click.Click;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.gui.AbstractTabGui;
import xyz.xenondevs.invui.gui.TabGui;

/**
 * Switches between tabs in a {@link AbstractTabGui}
 */
public abstract class TabItem extends ControlItem<TabGui> {
    
    private final int tab;
    
    public TabItem(int tab) {
        this.tab = tab;
    }

    @Override
    public void handleClick(@NotNull Click clickType, @NotNull Player player, @NotNull InventoryPreClickEvent event) {
        if (clickType instanceof Click.Left) getGui().setTab(tab);
    }
    
}
