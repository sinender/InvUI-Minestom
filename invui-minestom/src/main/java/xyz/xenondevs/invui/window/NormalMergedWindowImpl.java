package xyz.xenondevs.invui.window;

import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Player;
import net.minestom.server.inventory.InventoryType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.xenondevs.invui.gui.AbstractGui;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.inventory.Inventory;

/**
 * An {@link AbstractMergedWindow} that uses a chest inventory as the upper inventory
 * and the player inventory as the lower inventory.
 * <p>
 * Use the builder obtained by {@link Window#merged()}, to get an instance of this class.
 */
final class NormalMergedWindowImpl extends AbstractMergedWindow {

    public NormalMergedWindowImpl(
            @NotNull Player player,
            @Nullable Component title,
            @NotNull AbstractGui gui,
            boolean closeable
    ) {
        super(player, title, gui, createInventory(gui, title), closeable);
    }

    private static net.minestom.server.inventory.Inventory createInventory(Gui gui, @Nullable Component title) {
        if (gui.getWidth() != 9)
            throw new IllegalArgumentException("Gui width has to be 9");


        InventoryType type = switch (gui.getHeight()) {
            case 5 -> InventoryType.CHEST_5_ROW;
            case 6 -> InventoryType.CHEST_6_ROW;
            default -> null;
        };

        if (type == null)
            throw new IllegalArgumentException("Gui height has to be 5 or 6");

        return new net.minestom.server.inventory.Inventory(type, title == null ? Component.empty() : title);
    }

    public static final class BuilderImpl
            extends AbstractSingleWindow.AbstractBuilder<Window, Window.Builder.Normal.Merged>
            implements Window.Builder.Normal.Merged {

        @Override
        public @NotNull Window build(Player viewer) {
            if (viewer == null)
                throw new IllegalStateException("Viewer is not defined.");
            if (guiSupplier == null)
                throw new IllegalStateException("Gui is not defined.");

            var window = new NormalMergedWindowImpl(
                    viewer,
                    title,
                    (AbstractGui) guiSupplier.get(),
                    closeable
            );

            applyModifiers(window);

            return window;
        }
    }

}
