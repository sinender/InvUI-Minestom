package xyz.xenondevs.invui.util;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.ItemEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.inventory.StackSizeProvider;

import java.time.Duration;
import java.util.Arrays;
import java.util.Objects;

public class InventoryUtils {

    public static StackSizeProvider stackSizeProvider = ItemStack::maxStackSize;

    public static int addItemCorrectly(@NotNull Inventory inventory, @NotNull ItemStack itemStack) {
        return addItemCorrectly(inventory, itemStack, new boolean[inventory.getSize()]);
    }

    public static int addItemCorrectly(@NotNull Inventory inventory, @NotNull ItemStack itemStack, boolean @NotNull [] blockedSlots) {
        int maxStackSize = stackSizeProvider.getMaxStackSize(itemStack);
        int amountLeft = itemStack.amount();

        // add to partial stacks
        while (amountLeft > 0) {
            ItemStack partialStack = getFirstPartialStack(inventory, itemStack, blockedSlots);
            if (partialStack == null)
                break;

            int partialAmount = partialStack.amount();
            int addableAmount = Math.max(0, Math.min(amountLeft, maxStackSize - partialAmount));
            inventory.setItemStack(
                    Arrays.asList(inventory.getItemStacks()).indexOf(partialStack),
                    partialStack.withAmount(partialAmount + addableAmount)
            );
            amountLeft -= addableAmount;
        }

        // add to empty slots
        while (amountLeft > 0) {
            int emptySlot = getFirstEmptySlot(inventory, blockedSlots);
            if (emptySlot == -1)
                break;

            int addableAmount = Math.min(amountLeft, maxStackSize);

            ItemStack newStack = itemStack.builder().build();
            inventory.setItemStack(emptySlot, newStack.withAmount(addableAmount));

            amountLeft -= addableAmount;
        }

        return amountLeft;
    }

    @Nullable
    public static ItemStack getFirstPartialStack(@NotNull Inventory inventory, @NotNull ItemStack type) {
        return getFirstPartialStack(inventory, type, new boolean[inventory.getSize()]);
    }

    @Nullable
    public static ItemStack getFirstPartialStack(@NotNull Inventory inventory, @NotNull ItemStack type, boolean @NotNull [] blockedSlots) {
        int maxStackSize = stackSizeProvider.getMaxStackSize(type);

        ItemStack[] storageContents = inventory.getItemStacks();
        for (int i = 0; i < storageContents.length; i++) {
            if (blockedSlots[i] || isInvalidSlot(inventory, i))
                continue;

            ItemStack item = storageContents[i];
            if (type.isSimilar(item)) {
                int amount = item.amount();
                if (amount < maxStackSize)
                    return item;
            }
        }

        return null;
    }

    public static int getFirstEmptySlot(@NotNull Inventory inventory) {
        return getFirstEmptySlot(inventory, new boolean[inventory.getSize()]);
    }

    public static int getFirstEmptySlot(@NotNull Inventory inventory, boolean @NotNull [] blockedSlots) {
        ItemStack[] storageContents = inventory.getItemStacks();
        for (int i = 0; i < storageContents.length; i++) {
            if (blockedSlots[i] || isInvalidSlot(inventory, i))
                continue;

            ItemStack item = storageContents[i];
            if (ItemUtils.isEmpty(item))
                return i;
        }

        return -1;
    }

    private static boolean isInvalidSlot(@NotNull Inventory inventory, int slot) {
//        if (inventory instanceof CraftingInventory) {
//            // craft result slot
//            return slot == 0;
//        }

        return false;
    }

    public static Inventory createMatchingInventory(@NotNull Gui gui, @NotNull String title) {
        InventoryType type;

        if (gui.getWidth() == 9) type = null;
        else if (gui.getWidth() == 3 && gui.getHeight() == 3) type = InventoryType.WINDOW_3X3;
        else if (gui.getWidth() == 5 && gui.getHeight() == 1) type = InventoryType.HOPPER;
        else throw new UnsupportedOperationException("Invalid bounds of Gui");

        switch (gui.getSize()) {
            case 9 -> type = InventoryType.CHEST_1_ROW;
            case 18 -> type = InventoryType.CHEST_2_ROW;
            case 27 -> type = InventoryType.CHEST_3_ROW;
            case 36 -> type = InventoryType.CHEST_4_ROW;
            case 45 -> type = InventoryType.CHEST_5_ROW;
            case 54 -> type = InventoryType.CHEST_6_ROW;
        }

        return new Inventory(Objects.requireNonNullElse(type, InventoryType.CHEST_1_ROW), title);
    }

    public static boolean containsSimilar(@NotNull Inventory inventory, @Nullable ItemStack itemStack) {
        for (int i = 0; i < inventory.getSize(); i++) {
            ItemStack currentStack = ItemUtils.takeUnlessEmpty(inventory.getItemStack(i));

            if ((currentStack == null && itemStack == null)
                    || (currentStack != null && currentStack.isSimilar(itemStack))) return true;
        }

        return false;
    }

    public static void dropItemLikePlayer(@NotNull Player player, @NotNull ItemStack itemStack) {
        Pos location = player.getPosition();
        location = location.add(0, 1.5, 0); // not the eye location
        ItemEntity entity = new ItemEntity(itemStack);
        entity.setInstance(player.getInstance(), location);
        entity.setPickupDelay(Duration.ofSeconds(2));
        entity.setVelocity(location.direction().mul(0.35));
    }

}
