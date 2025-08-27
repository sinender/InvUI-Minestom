package xyz.xenondevs.invui.item.impl;

import net.minestom.server.entity.Player;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.inventory.click.Click;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.item.Item;
import xyz.xenondevs.invui.item.ItemProvider;

/**
 * An {@link Item} that will force a player to run a command or say something in the chat when clicked.
 */
public class CommandItem extends SimpleItem {
    
    private final String command;
    
    public CommandItem(@NotNull ItemProvider itemProvider, @NotNull String command) {
        super(itemProvider);
        this.command = command;
    }

    @Override
    public void handleClick(@NotNull Click clickType, @NotNull Player player, @NotNull InventoryPreClickEvent event) {
        player.sendMessage(command);
    }
    
}
