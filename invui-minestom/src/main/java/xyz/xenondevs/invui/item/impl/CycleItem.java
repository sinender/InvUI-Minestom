package xyz.xenondevs.invui.item.impl;

import net.minestom.server.entity.Player;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.inventory.click.Click;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.xenondevs.invui.item.Item;
import xyz.xenondevs.invui.item.ItemProvider;

import java.util.function.BiConsumer;

/**
 * An {@link Item} that cycles through a predefined array of {@link ItemProvider}s when clicked.
 */
public class CycleItem extends AbstractItem {
    
    private final ItemProvider[] states;
    
    private int state;
    
    public CycleItem(@NotNull ItemProvider... states) {
        this(0, states);
    }
    
    public CycleItem(int startState, @NotNull ItemProvider... states) {
        this.states = states;
        this.state = startState;
    }
    
    public static CycleItem withStateChangeHandler(BiConsumer<Player, Integer> stateChangeHandler, @NotNull ItemProvider... states) {
        return withStateChangeHandler(stateChangeHandler, 0, states);
    }
    
    public static CycleItem withStateChangeHandler(BiConsumer<Player, Integer> stateChangeHandler, int startState, @NotNull ItemProvider... states) {
        return new CycleItem(startState, states) {
            @Override
            protected void handleStateChange(@Nullable Player player, int state) {
                stateChangeHandler.accept(player, state);
            }
        };
    }
    
    @Override
    public ItemProvider getItemProvider() {
        return states[state];
    }

    @Override
    public void handleClick(@NotNull Click clickType, @NotNull Player player, @NotNull InventoryPreClickEvent event) {
        if (clickType instanceof Click.Left) cycle(player, true);
        else if (clickType instanceof Click.Right) cycle(player, false);
    }
    
    public void cycle(boolean forward) {
        cycle(null, forward);
    }
    
    private void cycle(@Nullable Player player, boolean forward) {
        if (forward) {
            if (++state == states.length) state = 0;
        } else {
            if (--state < 0) state = states.length - 1;
        }
        
        handleStateChange(player, state);
        notifyWindows();
    }
    
    protected void handleStateChange(@Nullable Player player, int state) {
        // empty
    }
    
    public int getState() {
        return state;
    }
    
}
