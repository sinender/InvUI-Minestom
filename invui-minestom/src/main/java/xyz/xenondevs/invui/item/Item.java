package xyz.xenondevs.invui.item;

import net.minestom.server.entity.Player;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.click.Click;
import net.minestom.server.inventory.click.ClickType;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.window.AbstractWindow;
import xyz.xenondevs.invui.window.Window;

import java.util.Set;

public interface Item {
    
    /**
     * Gets the {@link ItemProvider}.
     * This method gets called every time a {@link Window} is notified ({@link #notifyWindows()}).
     *
     * @return The {@link ItemProvider}
     * @throws UnsupportedOperationException if neither this method nor {@link #getItemProvider(Player)} is overridden
     */
    default ItemProvider getItemProvider() {
        throw new UnsupportedOperationException("Either getItemProvider() or getItemProvider(Player) must be overridden!");
    }
    
    /**
     * Gets the {@link ItemProvider} for a specific {@link Player}.
     * This method gets called every time a {@link Window} is notified ({@link #notifyWindows()}).
     * <p>The default implementation delegates to {@link #getItemProvider()}</p>
     *
     * @param viewer the viewer (provides context for rendering player-specific information in the item)
     * @return The {@link ItemProvider}
     */
    default ItemProvider getItemProvider(Player viewer) {
        return getItemProvider();
    }
    
    /**
     * Adds an {@link AbstractWindow} to the window set, telling the {@link Item} that it is
     * currently being displayed in that {@link AbstractWindow}.
     *
     * @param window The {@link AbstractWindow} the {@link Item} is currently displayed in.
     */
    void addWindow(AbstractWindow window);
    
    /**
     * Removes an {@link AbstractWindow} from the window set, telling the {@link Item} that it
     * is no longer being displayed in that {@link AbstractWindow}.
     *
     * @param window The {@link AbstractWindow} the {@link Item} is no longer displayed in.
     */
    void removeWindow(AbstractWindow window);
    
    /**
     * Gets an immutable {@link Set} of all the {@link Window}s where this
     * {@link Item} is displayed in.
     *
     * @return An immutable view of the {@link Set} of all the {@link Window}s
     * where this {@link Item} is displayed in.
     */
    Set<Window> getWindows();
    
    /**
     * Calls a refresh method on every {@link Window} in which this {@link Item} is displayed,
     * notifying them that the {@link ItemProvider} has been updated,
     * thus the {@link ItemStack} inside the {@link Window}'s {@link Inventory} should
     * be replaced.
     */
    void notifyWindows();
    
    /**
     * A method called if the {@link ItemStack} associated to this {@link Item}
     * has been clicked by a player.
     *
     * @param clickType The {@link ClickType} the {@link Player} performed.
     * @param player    The {@link Player} who clicked on the {@link ItemStack}.
     * @param event     The {@link InventoryPreClickEvent} associated with this click.
     */
    void handleClick(@NotNull Click clickType, @NotNull Player player, @NotNull InventoryPreClickEvent event);
    
}
