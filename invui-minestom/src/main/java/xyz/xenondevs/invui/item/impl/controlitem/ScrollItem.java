package xyz.xenondevs.invui.item.impl.controlitem;

import net.minestom.server.entity.Player;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.inventory.click.Click;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.gui.AbstractScrollGui;
import xyz.xenondevs.invui.gui.ScrollGui;

import java.util.HashMap;

/**
 * Scrolls in a {@link AbstractScrollGui}
 */
public abstract class ScrollItem extends ControlItem<ScrollGui<?>> {
    
    private final HashMap<Class<? extends Click>, Integer> scroll;
    
    public ScrollItem(int scrollLeftClick) {
        scroll = new HashMap<>();
        scroll.put(Click.Left.class, scrollLeftClick);
    }
    
    public ScrollItem(HashMap<Class<? extends Click>, Integer> scroll) {
        this.scroll = scroll;
    }

    @Override
    public void handleClick(@NotNull Click clickType, @NotNull Player player, @NotNull InventoryPreClickEvent event) {
        if (scroll.containsKey(clickType.getClass())) getGui().scroll(scroll.get(clickType));
    }
    
}
