package xyz.xenondevs.invui.window;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.event.inventory.InventoryOpenEvent;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.event.player.PlayerAnvilInputEvent;
import net.minestom.server.event.player.PlayerDeathEvent;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.inventory.click.Click;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.tag.Tag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.xenondevs.invui.gui.AbstractGui;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.gui.GuiParent;
import xyz.xenondevs.invui.gui.SlotElement;
import xyz.xenondevs.invui.inventory.CompositeInventory;
import xyz.xenondevs.invui.inventory.Inventory;
import xyz.xenondevs.invui.inventory.event.PlayerUpdateReason;
import xyz.xenondevs.invui.inventory.event.UpdateReason;
import xyz.xenondevs.invui.item.Item;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.util.ArrayUtils;
import xyz.xenondevs.invui.util.Pair;

import java.util.*;
import java.util.function.Consumer;

import static net.minestom.server.item.ItemStack.*;

/**
 * The abstract base class of all {@link Window} implementations.
 * <p>
 * Only in very rare circumstances should this class be used directly.
 * Instead, use the static builder functions in the
 * {@link Window} interfaces to create a new {@link Window}, such as
 * {@link Window#single()}.
 */
public abstract class AbstractWindow implements Window, GuiParent {

    private static final Key SLOT_KEY = Key.key("invui", "slot");

    private final Player viewer;
    private final UUID viewerUUID;
    private final SlotElement[] elementsDisplayed;
    private List<Runnable> openHandlers;
    private List<Runnable> closeHandlers;
    private List<Consumer<InventoryPreClickEvent>> outsideClickHandlers;
    private List<Consumer<String>> renameHandlers;
    private Component title;
    private boolean closeable;
    private boolean currentlyOpen;
    private boolean hasHandledClose;

    public AbstractWindow(Player viewer, Component title, int size, boolean closeable) {
        this.viewer = viewer;
        this.viewerUUID = viewer.getUuid();
        this.title = title;
        this.closeable = closeable;
        this.elementsDisplayed = new SlotElement[size];
    }

    public AbstractWindow(Player viewer, Component title, int size, boolean closeable, List<Consumer<String>> renameHandlers) {
        this.viewer = viewer;
        this.viewerUUID = viewer.getUuid();
        this.title = title;
        this.closeable = closeable;
        this.elementsDisplayed = new SlotElement[size];
        this.renameHandlers = renameHandlers;
    }

    /**
     * Redraws the current {@link SlotElement} at the given slot index.
     *
     * @param index The slot index.
     */
    protected void redrawItem(int index) {
        redrawItem(index, getSlotElement(index), false);
    }

    /**
     * Redraws the {@link SlotElement} at the given index.
     *
     * @param index   The slot index.
     * @param element The {@link SlotElement} at the index.
     * @param setItem Whether the {@link SlotElement} was newly set.
     */
    protected void redrawItem(int index, SlotElement element, boolean setItem) {
        // put ItemStack in inventory
        ItemStack itemStack;
        if (element == null || (element instanceof SlotElement.InventorySlotElement && element.getItemStack(this.viewer, getLang()) == null)) {
            ItemProvider background = getGuiAt(index).getFirst().getBackground();
            itemStack = background == null ? null : background.get(getLang());
        } else if (element instanceof SlotElement.LinkedSlotElement && element.getHoldingElement() == null) {
            ItemProvider background = null;

            List<Gui> guis = ((SlotElement.LinkedSlotElement) element).getGuiList();
            guis.add(0, getGuiAt(index).getFirst());

            for (int i = guis.size() - 1; i >= 0; i--) {
                background = guis.get(i).getBackground();
                if (background != null) break;
            }

            itemStack = background == null ? null : background.get(getLang());
        } else {
            SlotElement holdingElement = element.getHoldingElement();
            itemStack = holdingElement.getItemStack(this.viewer, getLang());

            if (holdingElement instanceof SlotElement.ItemSlotElement) {
                // This makes every item unique to prevent Shift-DoubleClick "clicking" multiple items at the same time.
                itemStack = itemStack.withTag(Tag.Byte("invui_slot"), (byte) index);
            }
        }
        if (itemStack == null) {
            itemStack = AIR;
        }
        setInvItem(index, itemStack);

        if (setItem) {
            // tell the previous item (if there is one) that this is no longer its window
            SlotElement previousElement = elementsDisplayed[index];
            if (previousElement instanceof SlotElement.ItemSlotElement) {
                SlotElement.ItemSlotElement itemSlotElement = (SlotElement.ItemSlotElement) previousElement;
                Item item = itemSlotElement.getItem();
                // check if the Item isn't still present on another index
                if (getItemSlotElements(item).size() == 1) {
                    // only if not, remove Window from list in Item
                    item.removeWindow(this);
                }
            } else if (previousElement instanceof SlotElement.InventorySlotElement) {
                SlotElement.InventorySlotElement invSlotElement = (SlotElement.InventorySlotElement) previousElement;
                Inventory inventory = invSlotElement.getInventory();
                // check if the InvUI-Inventory isn't still present on another index
                if (getInvSlotElements(invSlotElement.getInventory()).size() == 1) {
                    // only if not, remove Window from list in Inventory
                    inventory.removeWindow(this);
                }
            }

            if (element != null) {
                // tell the Item or InvUI-Inventory that it is being displayed in this Window
                SlotElement holdingElement = element.getHoldingElement();
                if (holdingElement instanceof SlotElement.ItemSlotElement) {
                    ((SlotElement.ItemSlotElement) holdingElement).getItem().addWindow(this);
                } else if (holdingElement instanceof SlotElement.InventorySlotElement) {
                    ((SlotElement.InventorySlotElement) holdingElement).getInventory().addWindow(this);
                }

                elementsDisplayed[index] = holdingElement;
            } else {
                elementsDisplayed[index] = null;
            }
        }
    }

    public void handleDragEvent(InventoryPreClickEvent event) {
        Player player = event.getPlayer();
        UpdateReason updateReason = new PlayerUpdateReason(player, event);
        if (!(event.getClick() instanceof Click.Drag)) {
            // the click is not a drag, so we don't handle it
            return;
        }
        Click.Drag dragClick = (Click.Drag) event.getClick();

        Map<Integer, ItemStack> newItems = new HashMap<>();
        for (int slot : dragClick.slots()) { // loop over all affected slots
            ItemStack itemStack = event.getInventory().getItemStack(slot);
            if (itemStack.material() == Material.AIR) itemStack = null;
            newItems.put(slot, itemStack);
        }

        player.getInventory().getCursorItem();
        int itemsLeft = player.getInventory().getCursorItem().amount();
        for (int rawSlot : dragClick.slots()) { // loop over all affected slots
            ItemStack currentStack = event.getInventory().getItemStack(rawSlot);
            if (currentStack.material() == Material.AIR) currentStack = null;

            // get the Gui at that slot and ask for permission to drag an Item there
            Pair<AbstractGui, Integer> pair = getGuiAt(rawSlot);
            if (pair != null && !pair.getFirst().handleItemDrag(updateReason, pair.getSecond(), currentStack, newItems.get(rawSlot))) {
                // the drag was cancelled
                int currentAmount = currentStack == null ? 0 : currentStack.amount();
                int newAmount = newItems.get(rawSlot).amount();

                itemsLeft += newAmount - currentAmount;
            }
        }

        // Redraw all items after the event so there won't be any Items that aren't actually there

        MinecraftServer.getSchedulerManager().scheduleNextProcess(() -> {
            dragClick.slots().forEach(rawSlot -> {
                if (getGuiAt(rawSlot) != null) redrawItem(rawSlot);
            });
        });

        // update the amount on the cursor
        ItemStack cursorStack = player.getInventory().getCursorItem();
        player.getInventory().setCursorItem(cursorStack.withAmount(itemsLeft));
    }

    public void handleClickEvent(InventoryPreClickEvent event) {
        if (event.getClick() instanceof Click.Drag) {
            // Handle drag events separately
            handleDragEvent(event);
            return;
        }

        if (event.getSlot() < 0) {
            // The player clicked outside the inventory
            if (outsideClickHandlers != null) {
                for (var handler : outsideClickHandlers) {
                    handler.accept(event);
                }
            }

            return;
        }

        if (Arrays.asList(getInventories()).contains(event.getInventory())) {
            // The inventory that was clicked is part of the open window
            handleClick(event);
        } else {
            if (event.getInventory().equals(event.getPlayer().getInventory())) {
                if (event.getPlayer().getInventory().getCursorItem() != AIR) {
                    handleItemShift(event);
                } else {
                    // The player clicked in their inventory, but not on an item
                    event.setCancelled(true);
                }
            }
        }
    }

    @SuppressWarnings("deprecation")
    public void handleCursorCollect(InventoryPreClickEvent event) {
        // cancel event as we do the collection logic ourselves
        event.setCancelled(true);

        Player player = (Player) event.getPlayer();

        // the template item stack that is used to collect similar items
        ItemStack template = player.getInventory().getCursorItem();

        // create a composite inventory consisting of all the gui's inventories and the player's inventory
        List<Inventory> inventories = getContentInventories();
        Inventory inventory = new CompositeInventory(inventories);

        // collect items from inventories until the cursor is full
        UpdateReason updateReason = new PlayerUpdateReason(player, event);
        int amount = inventory.collectSimilar(updateReason, template);

        player.getInventory().setCursorItem(template.withAmount(amount));
    }

    public void handleItemProviderUpdate(Item item) {
        getItemSlotElements(item).forEach((index, slotElement) ->
                redrawItem(index, slotElement, false));
    }

    public void handleInventoryUpdate(Inventory inventory) {
        getInvSlotElements(inventory).forEach((index, slotElement) ->
                redrawItem(index, slotElement, false));
    }

    protected Map<Integer, SlotElement> getItemSlotElements(Item item) {
        return ArrayUtils.findAllOccurrences(elementsDisplayed, element -> element instanceof SlotElement.ItemSlotElement
                && ((SlotElement.ItemSlotElement) element).getItem() == item);
    }

    protected Map<Integer, SlotElement> getInvSlotElements(Inventory inventory) {
        return ArrayUtils.findAllOccurrences(elementsDisplayed, element -> element instanceof SlotElement.InventorySlotElement
                && ((SlotElement.InventorySlotElement) element).getInventory() == inventory);
    }

    @Override
    public void open() {
        Player viewer = getViewer();
        if (currentlyOpen)
            throw new IllegalStateException("Window is already open");

        // call handleCloseEvent() close for currently open window
        AbstractWindow openWindow = (AbstractWindow) WindowManager.getInstance().getOpenWindow(viewer);
        if (openWindow != null) {
            openWindow.handleCloseEvent(true);
        }

        currentlyOpen = true;
        hasHandledClose = false;
        initItems();
        WindowManager.getInstance().addWindow(this);
        for (AbstractGui gui : getGuis()) gui.addParent(this);
        openInventory(viewer);
    }

    protected void openInventory(@NotNull Player viewer) {
        if (getInventories()[0] instanceof net.minestom.server.inventory.Inventory inv) {

            // Set the title of the inventory
            inv.setTitle(title);
            viewer.openInventory(inv);
            return;
        }

        net.minestom.server.inventory.Inventory inv = new net.minestom.server.inventory.Inventory(
                getInventories()[0] instanceof net.minestom.server.inventory.Inventory ?
                        ((net.minestom.server.inventory.Inventory) getInventories()[0]).getInventoryType() :
                        InventoryType.CHEST_3_ROW,
                title
        );
        viewer.openInventory(inv);
    }

    public void handleOpenEvent(InventoryOpenEvent event) {
        if (!event.getPlayer().equals(getViewer())) {
            event.setCancelled(true);
        } else {
            handleOpened();

            if (openHandlers != null) {
                openHandlers.forEach(Runnable::run);
            }
        }
    }

    @Override
    public void close() {
        Player viewer = getCurrentViewer();
        if (viewer != null) {
            handleCloseEvent(true);
            viewer.closeInventory();
        }
    }

    public void handleAnvilInputEvent(PlayerAnvilInputEvent event) {
        if (renameHandlers != null) {
            String renameText = event.getInput();
            renameHandlers.forEach(handler -> handler.accept(renameText));
        }

//        // Redraw the item in the output slot
//        redrawItem(2, getSlotElement(2), true);
//
//        // Set the cursor item to the output item
//        ItemStack outputItem = getSlotElement(2).getItemStack(viewer, getLang());
//        viewer.getInventory().setCursorItem(outputItem);
    }

    public void handleCloseEvent(boolean forceClose) {
        // handleCloseEvent might have already been called by close() or open() if the window was replaced by another one
        if (hasHandledClose)
            return;

        if (closeable || forceClose) {
            if (!currentlyOpen)
                throw new IllegalStateException("Window is already closed!");

            closeable = true;
            currentlyOpen = false;
            hasHandledClose = true;

            remove();
            handleClosed();

            if (closeHandlers != null) {
                closeHandlers.forEach(Runnable::run);
            }
        } else {
            MinecraftServer.getSchedulerManager().scheduleNextProcess(() -> openInventory(viewer));
        }
    }

    private void remove() {
        WindowManager.getInstance().removeWindow(this);

        Arrays.stream(elementsDisplayed)
                .filter(Objects::nonNull)
                .map(SlotElement::getHoldingElement)
                .forEach(slotElement -> {
                    if (slotElement instanceof SlotElement.ItemSlotElement) {
                        ((SlotElement.ItemSlotElement) slotElement).getItem().removeWindow(this);
                    } else if (slotElement instanceof SlotElement.InventorySlotElement) {
                        ((SlotElement.InventorySlotElement) slotElement).getInventory().removeWindow(this);
                    }
                });

        for (AbstractGui gui : getGuis()) gui.removeParent(this);
    }

    @Override
    public void changeTitle(@NotNull Component title) {
        this.title = title;
        Player currentViewer = getCurrentViewer();
        if (currentViewer != null && currentViewer.getOpenInventory() instanceof net.minestom.server.inventory.Inventory) {
            net.minestom.server.inventory.Inventory inv = (net.minestom.server.inventory.Inventory) currentViewer.getOpenInventory();
            inv.setTitle(title);
        }
    }

    @Override
    public void changeTitle(@NotNull String title) {
        changeTitle(LegacyComponentSerializer.legacyAmpersand().deserialize(title));
    }

    @Override
    public void setOpenHandlers(@Nullable List<@NotNull Runnable> openHandlers) {
        this.openHandlers = openHandlers;
    }

    @Override
    public void addOpenHandler(@NotNull Runnable openHandler) {
        if (openHandlers == null)
            openHandlers = new ArrayList<>();

        openHandlers.add(openHandler);
    }

    @Override
    public void setCloseHandlers(@Nullable List<@NotNull Runnable> closeHandlers) {
        this.closeHandlers = closeHandlers;
    }

    @Override
    public void addCloseHandler(@NotNull Runnable closeHandler) {
        if (closeHandlers == null)
            closeHandlers = new ArrayList<>();

        closeHandlers.add(closeHandler);
    }

    @Override
    public void removeCloseHandler(@NotNull Runnable closeHandler) {
        if (closeHandlers != null)
            closeHandlers.remove(closeHandler);
    }

    @Override
    public void setOutsideClickHandlers(@Nullable List<@NotNull Consumer<@NotNull InventoryPreClickEvent>> outsideClickHandlers) {
        this.outsideClickHandlers = outsideClickHandlers;
    }

    @Override
    public void addOutsideClickHandler(@NotNull Consumer<@NotNull InventoryPreClickEvent> outsideClickHandler) {
        if (this.outsideClickHandlers == null)
            this.outsideClickHandlers = new ArrayList<>();

        this.outsideClickHandlers.add(outsideClickHandler);
    }

    @Override
    public void removeOutsideClickHandler(@NotNull Consumer<@NotNull InventoryPreClickEvent> outsideClickHandler) {
        if (this.outsideClickHandlers != null)
            this.outsideClickHandlers.remove(outsideClickHandler);
    }

    @Override
    public @Nullable Player getCurrentViewer() {
        Set<Player> viewers = getInventories()[0].getViewers();
        return viewers.isEmpty() ? null : (Player) viewers.toArray()[0];
    }

    @Override
    public @NotNull Player getViewer() {
        return viewer;
    }

    public @NotNull String getLang() {
        return getViewer().getLocale().getLanguage();
    }

    @Override
    public @NotNull UUID getViewerUUID() {
        return viewerUUID;
    }

    @Override
    public boolean isCloseable() {
        return closeable;
    }

    @Override
    public void setCloseable(boolean closeable) {
        this.closeable = closeable;
    }

    @Override
    public boolean isOpen() {
        return currentlyOpen;
    }

    /**
     * Puts the given {@link ItemStack} into the inventory at the given slot.
     *
     * @param slot      The slot to put the item into.
     * @param itemStack The item to put into the inventory.
     */
    protected abstract void setInvItem(int slot, ItemStack itemStack);

    /**
     * Gets the {@link SlotElement} at the given index.
     *
     * @param index The index of the slot.
     * @return The {@link SlotElement} at the given index.
     */
    protected abstract SlotElement getSlotElement(int index);

    /**
     * Gets the {@link AbstractGui} at the given index.
     *
     * @param index The index of the slot.
     * @return The {@link AbstractGui} it's slot at that slot.
     */
    protected abstract Pair<AbstractGui, Integer> getGuiAt(int index);

    /**
     * Gets the {@link AbstractGui guis} displayed with this {@link Window},
     * does not contain the guis embedded in other guis.
     *
     * @return The guis displayed with this window.
     */
    protected abstract AbstractGui[] getGuis();

    /**
     * Gets the {@link net.minestom.server.inventory.AbstractInventory inventories} associated with this {@link Window}.
     *
     * @return The inventories associated with this window.
     */
    protected abstract net.minestom.server.inventory.AbstractInventory[] getInventories();

    /**
     * Gets the content {@link xyz.xenondevs.invui.inventory.Inventory inventories} associated with this
     * {@link Window}. These are not UI inventories, but actual inventories contained inside the {@link Gui},
     *
     * @return The content inventories associated with this window.
     */
    protected abstract List<xyz.xenondevs.invui.inventory.Inventory> getContentInventories();

    /**
     * Initializes the items in the {@link Window}.
     */
    protected abstract void initItems();

    /**
     * Handles the opening of the {@link Window}.
     */
    protected abstract void handleOpened();

    /**
     * Handles the closing of the {@link Window}.
     */
    protected abstract void handleClosed();

    /**
     * Handles a click in the {@link Window}.
     *
     * @param event The {@link InventoryPreClickEvent} that occurred.
     */
    protected abstract void handleClick(InventoryPreClickEvent event);

    /**
     * Handles an item-shift action in the {@link Window}.
     *
     * @param event The {@link InventoryPreClickEvent} that occurred.
     */
    protected abstract void handleItemShift(InventoryPreClickEvent event);

    /**
     * Handles the death of the viewer of the {@link Window}.
     *
     * @param event The {@link PlayerDeathEvent} that occurred.
     */
    public abstract void handleViewerDeath(PlayerDeathEvent event);

    /**
     * Builder for a {@link AbstractWindow}.
     * <p>
     * This class should only be used directly if you're creating a custom {@link AbstractBuilder} for a custom
     * {@link AbstractWindow} implementation. Otherwise, use the static builder functions in the {@link Window} interfaces,
     * such as {@link Window#single()} to obtain a builder.
     *
     * @param <W> The type of the window.
     * @param <S> The type of the builder.
     */
    @SuppressWarnings("unchecked")
    public static abstract class AbstractBuilder<W extends Window, S extends Window.Builder<W, S>> implements Window.Builder<W, S> {

        protected Player viewer;
        protected Component title;
        protected boolean closeable = true;
        protected List<Runnable> openHandlers;
        protected List<Runnable> closeHandlers;
        protected List<Consumer<InventoryPreClickEvent>> outsideClickHandlers;
        protected List<Consumer<W>> modifiers;

        @Override
        public @NotNull S setViewer(@NotNull Player viewer) {
            this.viewer = viewer;
            return (S) this;
        }

        @Override
        public @NotNull S setTitle(@NotNull Component title) {
            this.title = title;
            return (S) this;
        }


        @Override
        public @NotNull S setTitle(@NotNull String title) {
            this.title = LegacyComponentSerializer.legacyAmpersand().deserialize(title);
            return (S) this;
        }

        @Override
        public @NotNull S setCloseable(boolean closeable) {
            this.closeable = closeable;
            return (S) this;
        }

        @Override
        public @NotNull S setOpenHandlers(@Nullable List<@NotNull Runnable> openHandlers) {
            this.openHandlers = openHandlers;
            return (S) this;
        }

        @Override
        public @NotNull S addOpenHandler(@NotNull Runnable openHandler) {
            if (openHandlers == null)
                openHandlers = new ArrayList<>();

            openHandlers.add(openHandler);
            return (S) this;
        }

        @Override
        public @NotNull S setCloseHandlers(@Nullable List<@NotNull Runnable> closeHandlers) {
            this.closeHandlers = closeHandlers;
            return (S) this;
        }

        @Override
        public @NotNull S addCloseHandler(@NotNull Runnable closeHandler) {
            if (closeHandlers == null)
                closeHandlers = new ArrayList<>();

            closeHandlers.add(closeHandler);
            return (S) this;
        }

        @Override
        public @NotNull S setOutsideClickHandlers(@NotNull List<@NotNull Consumer<@NotNull InventoryPreClickEvent>> outsideClickHandlers) {
            this.outsideClickHandlers = outsideClickHandlers;
            return (S) this;
        }

        @Override
        public @NotNull S addOutsideClickHandler(@NotNull Consumer<@NotNull InventoryPreClickEvent> outsideClickHandler) {
            if (outsideClickHandlers == null)
                outsideClickHandlers = new ArrayList<>();

            outsideClickHandlers.add(outsideClickHandler);
            return (S) this;
        }

        @Override
        public @NotNull S setModifiers(@Nullable List<@NotNull Consumer<@NotNull W>> modifiers) {
            this.modifiers = modifiers;
            return (S) this;
        }

        @Override
        public @NotNull S addModifier(@NotNull Consumer<@NotNull W> modifier) {
            if (modifiers == null)
                modifiers = new ArrayList<>();

            modifiers.add(modifier);
            return (S) this;
        }

        protected void applyModifiers(W window) {
            if (openHandlers != null)
                window.setOpenHandlers(openHandlers);

            if (closeHandlers != null)
                window.setCloseHandlers(closeHandlers);

            if (outsideClickHandlers != null)
                window.setOutsideClickHandlers(outsideClickHandlers);

            if (modifiers != null)
                modifiers.forEach(modifier -> modifier.accept(window));
        }

        @Override
        public @NotNull W build() {
            return build(viewer);
        }

        @Override
        public void open(Player viewer) {
            build(viewer).open();
        }

        @SuppressWarnings("unchecked")
        @Override
        public @NotNull S clone() {
            try {
                var clone = (AbstractBuilder<W, S>) super.clone();
                if (title != null)
                    clone.title = Component.join(JoinConfiguration.noSeparators(), title);
                if (closeHandlers != null)
                    clone.closeHandlers = new ArrayList<>(closeHandlers);
                if (modifiers != null)
                    clone.modifiers = new ArrayList<>(modifiers);
                return (S) clone;
            } catch (CloneNotSupportedException e) {
                throw new AssertionError();
            }
        }

    }

}