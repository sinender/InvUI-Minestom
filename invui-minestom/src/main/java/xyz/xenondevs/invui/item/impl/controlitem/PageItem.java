package xyz.xenondevs.invui.item.impl.controlitem;

import net.minestom.server.entity.Player;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.inventory.click.Click;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.gui.AbstractPagedGui;
import xyz.xenondevs.invui.gui.PagedGui;

/**
 * Switches between pages in a {@link AbstractPagedGui}
 */
public abstract class PageItem extends ControlItem<PagedGui<?>> {
    
    private final boolean forward;
    
    public PageItem(boolean forward) {
        this.forward = forward;
    }
    
    @Override
    public void handleClick(@NotNull Click clickType, @NotNull Player player, @NotNull InventoryPreClickEvent event) {
        if (clickType instanceof Click.Left) {
            if (forward) getGui().goForward();
            else getGui().goBack();
        }
    }
    
}
