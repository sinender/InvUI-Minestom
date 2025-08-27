package xyz.xenondevs.invui.item.impl;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.inventory.click.Click;
import net.minestom.server.timer.Task;
import net.minestom.server.timer.TaskSchedule;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.item.Item;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.window.AbstractWindow;

import java.time.Duration;

/**
 * An {@link Item} that automatically cycles through a predefined array of
 * {@link ItemProvider} at a predefined speed.
 */
public class AutoCycleItem extends AbstractItem {
    
    private final ItemProvider[] itemProviders;
    private final int period;
    private Task task;
    
    private int state;
    
    public AutoCycleItem(int period, ItemProvider... itemProviders) {
        this.itemProviders = itemProviders;
        this.period = period;
    }
    
    public void start() {
        if (task != null) task.cancel();
        task = MinecraftServer.getSchedulerManager().scheduleTask(this::cycle, TaskSchedule.immediate(), TaskSchedule.tick(period));
    }
    
    public void cancel() {
        task.cancel();
        task = null;
    }
    
    private void cycle() {
        state++;
        if (state == itemProviders.length) state = 0;
        notifyWindows();
    }
    
    @Override
    public ItemProvider getItemProvider() {
        return itemProviders[state];
    }
    
    @Override
    public void addWindow(AbstractWindow window) {
        super.addWindow(window);
        if (task == null) start();
    }
    
    @Override
    public void removeWindow(AbstractWindow window) {
        super.removeWindow(window);
        if (getWindows().isEmpty() && task != null) cancel();
    }

    @Override
    public void handleClick(@NotNull Click clickType, @NotNull Player player, @NotNull InventoryPreClickEvent event) {
        // empty
    }
    
}
