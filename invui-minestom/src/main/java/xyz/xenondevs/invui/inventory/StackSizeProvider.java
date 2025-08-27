package xyz.xenondevs.invui.inventory;


import net.minestom.server.item.ItemStack;

public interface StackSizeProvider {
    
    int getMaxStackSize(ItemStack itemStack);
    
}
