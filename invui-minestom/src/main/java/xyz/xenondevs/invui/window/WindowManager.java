package xyz.xenondevs.invui.window;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.event.inventory.InventoryCloseEvent;
import net.minestom.server.event.inventory.InventoryOpenEvent;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.event.item.PickupItemEvent;
import net.minestom.server.event.player.PlayerAnvilInputEvent;
import net.minestom.server.event.player.PlayerDeathEvent;
import net.minestom.server.event.player.PlayerDisconnectEvent;
import net.minestom.server.inventory.AbstractInventory;
import net.minestom.server.inventory.Inventory;
import org.jetbrains.annotations.Nullable;
import xyz.xenondevs.invui.InvUI;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Manages all {@link Window Windows} and provides methods for searching them.
 */
public class WindowManager {
    
    private static WindowManager instance;
    
    private final Map<AbstractInventory, AbstractWindow> windowsByInventory = new HashMap<>();
    private final Map<Player, AbstractWindow> windowsByPlayer = new HashMap<>();
    
    private WindowManager() {

        MinecraftServer.getGlobalEventHandler().addListener(InventoryPreClickEvent.class, event -> {
            AbstractWindow window = windowsByPlayer.get(event.getPlayer());

            if (window != null) {
                window.handleClickEvent(event);
            }
        });

        MinecraftServer.getGlobalEventHandler().addListener(InventoryCloseEvent.class, event -> {
            AbstractWindow window = windowsByPlayer.get(event.getPlayer());
            if (window != null) {
                window.handleCloseEvent(false);
            }
        });

        MinecraftServer.getGlobalEventHandler().addListener(InventoryOpenEvent.class, event -> {
            AbstractWindow window = windowsByPlayer.get(event.getPlayer());
            if (window != null) {
                window.handleOpenEvent(event);
            }
        });

        MinecraftServer.getGlobalEventHandler().addListener(PlayerDisconnectEvent.class, event -> {
            Player player = event.getPlayer();
            AbstractWindow window = windowsByPlayer.remove(player);
            if (window != null) {
                window.handleCloseEvent(true);
            }
        });

        MinecraftServer.getGlobalEventHandler().addListener(PlayerDeathEvent.class, event -> {
            Player player = event.getPlayer();
            AbstractWindow window = windowsByPlayer.remove(player);
            if (window != null) {
                window.handleViewerDeath(event);
            }
        });

        MinecraftServer.getGlobalEventHandler().addListener(PlayerAnvilInputEvent.class, event -> {
            Player player = event.getPlayer();
            AbstractWindow window = windowsByPlayer.get(player);
            if (window instanceof AnvilWindow) {
                window.handleAnvilInputEvent(event);
            }
        });

        MinecraftServer.getGlobalEventHandler().addListener(PickupItemEvent.class, event -> {
            if (!(event.getEntity() instanceof Player player)) return;
            AbstractWindow window = windowsByPlayer.get(player);
            if (window instanceof AbstractDoubleWindow) {
                event.setCancelled(true); // Prevent item pickup in double windows
            }
        });

        InvUI.getInstance().addDisableHandler(() -> new HashSet<>(windowsByPlayer.values()).forEach(AbstractWindow::close));
    }
    
    /**
     * Gets the {@link WindowManager} instance or creates a new one if there isn't one.
     *
     * @return The {@link WindowManager} instance
     */
    public static WindowManager getInstance() {
        return instance == null ? instance = new WindowManager() : instance;
    }
    
    /**
     * Adds an {@link AbstractWindow} to the list of windows.
     * This method is usually called by the {@link Window} itself.
     *
     * @param window The {@link AbstractWindow} to add
     */
    public void addWindow(AbstractWindow window) {
        windowsByInventory.put(window.getInventories()[0], window);
        windowsByPlayer.put(window.getViewer(), window);
    }
    
    /**
     * Removes an {@link AbstractWindow} from the list of windows.
     * This method is usually called by the {@link Window} itself.
     *
     * @param window The {@link AbstractWindow} to remove
     */
    public void removeWindow(AbstractWindow window) {
        windowsByInventory.remove(window.getInventories()[0]);
        windowsByPlayer.remove(window.getViewer());
    }
    
    /**
     * Finds the {@link Window} to an {@link Inventory}.
     *
     * @param inventory The {@link Inventory}
     * @return The {@link Window} that belongs to that {@link Inventory}
     */
    @Nullable
    public Window getWindow(Inventory inventory) {
        return windowsByInventory.get(inventory);
    }
    
    /**
     * Gets the {@link Window} the {@link Player} has currently open.
     *
     * @param player The {@link Player}
     * @return The {@link Window} the {@link Player} has currently open
     */
    @Nullable
    public Window getOpenWindow(Player player) {
        return windowsByPlayer.get(player);
    }
    
    /**
     * Gets a set of all open {@link Window Windows}.
     *
     * @return A set of all {@link Window Windows}
     */
    public Set<Window> getWindows() {
        return new HashSet<>(windowsByInventory.values());
    }
    
    /**
     * Gets a set of all open {@link Window Windows}.
     *
     * @deprecated Use {@link #getWindows()} instead
     */
    @Deprecated
    public Set<Window> getOpenWindows() {
        return getWindows();
    }
}
