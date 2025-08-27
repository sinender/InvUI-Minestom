package xyz.xenondevs.invui.item.builder;

import net.minestom.server.component.DataComponents;
import net.minestom.server.entity.PlayerSkin;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.item.component.HeadProfile;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public final class SkullBuilder extends AbstractItemBuilder<SkullBuilder> {
    private PlayerSkin playerSkin;

    @Deprecated
    public SkullBuilder(@NotNull UUID uuid) {
        this(PlayerSkin.fromUuid(uuid.toString()));
    }

    @Deprecated
    public SkullBuilder(@NotNull String username) {
        this(PlayerSkin.fromUsername(username));
    }
    
    /**
     * Create a {@link SkullBuilder} with a {@link PlayerSkin}.
     *
     * @param playerSkin The {@link PlayerSkin} to be applied to the skull.
     */
    public SkullBuilder(@NotNull PlayerSkin playerSkin) {
        super(Material.PLAYER_HEAD);
        this.playerSkin = playerSkin;
    }
    
    @Contract(value = "_ -> new", pure = true)
    @Override
    public @NotNull ItemStack get(@Nullable String lang) {
        ItemStack item = super.get(lang);
        item.with(DataComponents.PROFILE, new HeadProfile(playerSkin));
        
        return item;
    }
    
    @Contract("_ -> this")
    @Override
    public @NotNull SkullBuilder setMaterial(@NotNull Material material) {
        return this;
    }
    
    @Contract(value = "-> new", pure = true)
    @Override
    public @NotNull SkullBuilder clone() {
        return super.clone();
    }
    
}
