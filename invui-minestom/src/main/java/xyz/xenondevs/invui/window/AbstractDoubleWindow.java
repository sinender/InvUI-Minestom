package xyz.xenondevs.invui.window;

import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Player;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.event.player.PlayerDeathEvent;
import net.minestom.server.inventory.AbstractInventory;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.PlayerInventory;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import xyz.xenondevs.invui.gui.AbstractGui;
import xyz.xenondevs.invui.gui.SlotElement;
import xyz.xenondevs.invui.util.Pair;
import xyz.xenondevs.invui.util.SlotUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * A {@link Window} that uses both top and player {@link Inventory}.
 * <p>
 * Only in very rare circumstances should this class be used directly.
 * Instead, use {@link Window#split()} or {@link Window#merged()} to create such a {@link Window}.
 */
public abstract class AbstractDoubleWindow extends AbstractWindow {
    
    private final PlayerInventory playerInventory;
    private final ItemStack[] playerItems = new ItemStack[36];
    
    /**
     * The upper inventory of the window.
     */
    protected Inventory upperInventory;
    
    /**
     * Creates a new {@link AbstractDoubleWindow}.
     *
     * @param player         The player that views the window.
     * @param title          The title of the window.
     * @param size           The size of the window.
     * @param upperInventory The upper inventory of the window.
     * @param closeable      Whether the window is closeable.
     */
    public AbstractDoubleWindow(Player player, Component title, int size, Inventory upperInventory, boolean closeable) {
        super(player, title, size, closeable);
        this.upperInventory = upperInventory;
        this.playerInventory = player.getInventory();
    }

    public AbstractDoubleWindow(Player player, Component title, int size, Inventory upperInventory, boolean closeable, List<Consumer<String>> renameHandlers) {
        super(player, title, size, closeable, renameHandlers);
        this.upperInventory = upperInventory;
        this.playerInventory = player.getInventory();
    }
    
    @Override
    protected void initItems() {
        // init upper inventory
        for (int i = 0; i < upperInventory.getSize(); i++) {
            SlotElement element = getSlotElement(i);
            redrawItem(i, element, true);
        }
        
        // store and clear player inventory
        PlayerInventory inventory = getViewer().getInventory();
        for (int i = 0; i < 36; i++) {
            playerItems[i] = inventory.getItemStack(i);
            inventory.setItemStack(i, ItemStack.AIR);
        }
        
        // init player inventory
        for (int i = upperInventory.getSize(); i < upperInventory.getSize() + 36; i++) {
            SlotElement element = getSlotElement(i);
            redrawItem(i, element, true);
        }
    }
    
    @Override
    public @Nullable ItemStack @Nullable [] getPlayerItems() {
        if (isOpen())
            return playerItems;
        return null;
    }
    
    private void restorePlayerInventory() {
        PlayerInventory inventory = getViewer().getInventory();
        for (int i = 0; i < 36; i++) {
            inventory.setItemStack(i, playerItems[i]);
        }
    }
    
    @Override
    protected void redrawItem(int index, SlotElement element, boolean setItem) {
        super.redrawItem(index, element, setItem);
        if (getViewer().getOpenInventory() != null) {
            getViewer().getOpenInventory().update();
        }
    }
    
    @Override
    protected void setInvItem(int slot, ItemStack itemStack) {
        if (slot >= upperInventory.getSize()) {
            if (isOpen()) {
                int invSlot = SlotUtils.translateGuiToPlayerInv(slot - upperInventory.getSize());
                setPlayerInvItem(invSlot, itemStack);
            }
        } else setUpperInvItem(slot, itemStack);
    }
    
    /**
     * Places an {@link ItemStack} into the upper {@link Inventory}.
     *
     * @param slot      The slot in the upper {@link Inventory}.
     * @param itemStack The {@link ItemStack} to place.
     */
    protected void setUpperInvItem(int slot, ItemStack itemStack) {
        if (itemStack == null || itemStack.isAir()) {
            upperInventory.setItemStack(slot, ItemStack.AIR);
            return;
        }
        upperInventory.setItemStack(slot, itemStack);
    }
    
    /**
     * Places an {@link ItemStack} into the player {@link Inventory}.
     *
     * @param slot      The slot in the player {@link Inventory}.
     * @param itemStack The {@link ItemStack} to place.
     */
    protected void setPlayerInvItem(int slot, ItemStack itemStack) {
        if (itemStack == null || itemStack.isAir()) {
            playerInventory.setItemStack(slot, ItemStack.AIR);
            return;
        }
        playerInventory.setItemStack(slot, itemStack);
    }
    

    public void handleViewerDeath(PlayerDeathEvent event) {
        if (isOpen()) {
            List<ItemStack> drops = List.of(event.getPlayer().getInventory().getItemStacks());
            //TODO: Keep inventory?
//            if (!event.()) {
//                drops.clear();
//                Arrays.stream(playerItems)
//                    .filter(Objects::nonNull)
//                    .forEach(drops::add);
//            }
        }
    }
    
    @Override
    protected void handleOpened() {
        // Prevent players from receiving advancements from UI items
    }
    
    @Override
    protected void handleClosed() {
        restorePlayerInventory();
    }
    
    @Override
    public void handleClick(InventoryPreClickEvent event) {
        Pair<AbstractGui, Integer> clicked = getWhereClicked(event);
        clicked.getFirst().handleClick(clicked.getSecond(), (Player) event.getPlayer(), event.getClick(), event);
    }
    
    @Override
    public void handleItemShift(InventoryPreClickEvent event) {
        // empty, should not be called by the WindowManager
    }
    
    @Override
    public AbstractInventory[] getInventories() {
        return isOpen() ? new AbstractInventory[] {upperInventory, playerInventory} : new AbstractInventory[] {upperInventory};
    }
    
    /**
     * Gets the upper {@link Inventory} of the window.
     *
     * @return The upper {@link Inventory} of the window.
     */
    public Inventory getUpperInventory() {
        return upperInventory;
    }
    
    /**
     * Gets the player {@link Inventory} of the window.
     *
     * @return The player {@link Inventory} of the window.
     */
    public AbstractInventory getPlayerInventory() {
        return playerInventory;
    }
    
    /**
     * Gets the {@link AbstractGui} and the slot where the player clicked,
     * based on the given {@link InventoryPreClickEvent}.
     *
     * @param event The {@link InventoryPreClickEvent} that was triggered.
     * @return The {@link AbstractGui} and the slot where the player clicked.
     */
    protected abstract Pair<AbstractGui, Integer> getWhereClicked(InventoryPreClickEvent event);
    
}
