package xyz.xenondevs.invui.item;


import net.minestom.server.entity.Player;
import net.minestom.server.event.inventory.InventoryPreClickEvent;

public class Click {
    
    private final Player player;
    private final net.minestom.server.inventory.click.Click clickType;
    private final InventoryPreClickEvent event;
    
    public Click(InventoryPreClickEvent event) {
        this.player = (Player) event.getPlayer();
        this.clickType = event.getClick();
        this.event = event;
    }
    
    public Player getPlayer() {
        return player;
    }
    
    public net.minestom.server.inventory.click.Click getClickType() {
        return clickType;
    }
    
    public InventoryPreClickEvent getEvent() {
        return event;
    }
    
}
