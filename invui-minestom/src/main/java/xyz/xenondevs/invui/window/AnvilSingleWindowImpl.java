package xyz.xenondevs.invui.window;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.minestom.server.component.DataComponents;
import net.minestom.server.entity.Player;
import net.minestom.server.inventory.type.AnvilInventory;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.xenondevs.invui.gui.AbstractGui;
import xyz.xenondevs.invui.gui.TabGui;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * An {@link AbstractSingleWindow} that uses an {@link AnvilInventory} as the upper inventory.
 * <p>
 * Use the builder obtained by {@link AnvilWindow#single()}, to get an instance of this class.
 */
final class AnvilSingleWindowImpl extends AbstractSingleWindow implements AnvilWindow {
    
    private final AnvilInventory anvilInventory;
    
    public AnvilSingleWindowImpl(
        @NotNull Player player,
        @Nullable Component title,
        @NotNull AbstractGui gui,
        @Nullable List<@NotNull Consumer<@NotNull String>> renameHandlers,
        boolean closable
    ) {
        super(player, title, gui, null, closable, renameHandlers);

        anvilInventory = new AnvilInventory(title);
        inventory = anvilInventory;
    }
    
    @Override
    protected void setInvItem(int slot, ItemStack itemStack) {
        anvilInventory.setItemStack(slot, itemStack);
    }
    
    @Override
    protected void openInventory(@NotNull Player viewer) {
        viewer.openInventory(anvilInventory);
    }
    
    @Override
    public String getRenameText() {
        return LegacyComponentSerializer.legacyAmpersand().serialize(anvilInventory.getItemStack(2).get(DataComponents.CUSTOM_NAME));
    }
    
    public static final class BuilderImpl
        extends AbstractSingleWindow.AbstractBuilder<AnvilWindow, AnvilWindow.Builder.Single>
        implements AnvilWindow.Builder.Single
    {
        
        private List<Consumer<String>> renameHandlers;
        
        @Override
        public @NotNull BuilderImpl setRenameHandlers(@NotNull List<@NotNull Consumer<String>> renameHandlers) {
            this.renameHandlers = renameHandlers;
            return this;
        }
        
        @Override
        public @NotNull BuilderImpl addRenameHandler(@NotNull Consumer<String> renameHandler) {
            if (renameHandlers == null)
                renameHandlers = new ArrayList<>();
            
            renameHandlers.add(renameHandler);
            return this;
        }
        
        @Override
        public @NotNull AnvilWindow build(Player viewer) {
            if (viewer == null)
                throw new IllegalStateException("Viewer is not defined.");
            if (guiSupplier == null)
                throw new IllegalStateException("Gui is not defined.");
            
            var window = new AnvilSingleWindowImpl(
                viewer,
                title,
                (AbstractGui) guiSupplier.get(),
                renameHandlers,
                closeable
            );
            
            applyModifiers(window);
            
            return window;
        }
        
    }
    
}
