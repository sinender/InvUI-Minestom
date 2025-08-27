package xyz.xenondevs.invui.item.builder;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.minestom.server.MinecraftServer;
import net.minestom.server.component.DataComponent;
import net.minestom.server.component.DataComponents;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.item.component.CustomModelData;
import net.minestom.server.item.component.EnchantmentList;
import net.minestom.server.item.enchant.Enchantment;
import net.minestom.server.utils.Unit;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.util.Pair;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Abstract base class for item builders.
 *
 * @param <S> Self reference, used for chaining.
 */
@SuppressWarnings("unchecked")
public abstract class AbstractItemBuilder<S> implements ItemProvider {
    
    /**
     * The {@link ItemStack} to use as a base.
     */
    protected ItemStack base;
    /**
     * The {@link Material} of the {@link ItemStack}.
     */
    protected Material material;

    /**
     * The amount of the {@link ItemStack}.
     */
    protected int amount = 1;
    /**
     * The damage value of the {@link ItemStack}
     */
    protected int damage;
    /**
     * The custom model data value of the {@link ItemStack}.
     */
    protected int customModelData;
    /**
     * The unbreakable state of the {@link ItemStack}.
     */
    protected Boolean unbreakable;
    /**
     * The display name of the {@link ItemStack}.
     */
    protected Component displayName;
    /**
     * The lore of the {@link ItemStack}.
     */
    protected List<Component> lore;
    /**
     * The enchantments of the {@link ItemStack}.
     */
    protected HashMap<Enchantment, Pair<Integer, Boolean>> enchantments;
    /**
     * Additional modifier functions to be run after building the {@link ItemStack}.
     */
    protected List<Function<ItemStack, ItemStack>> modifiers;
    
    /**
     * Constructs a new {@link AbstractItemBuilder} based on the given {@link Material}.
     *
     * @param material The {@link Material}
     */
    public AbstractItemBuilder(@NotNull Material material) {
        this.material = material;
    }
    
    /**
     * Constructs a new {@link AbstractItemBuilder} based on the given {@link Material} and amount.
     *
     * @param material The {@link Material}
     * @param amount   The amount
     */
    public AbstractItemBuilder(@NotNull Material material, int amount) {
        this.material = material;
        this.amount = amount;
    }
    
    /**
     * Constructs a new {@link AbstractItemBuilder} based on the give {@link ItemStack}.
     * This will keep the {@link ItemStack} and uses it's properties as a base for the builder.
     *
     * @param base The {@link ItemStack to use as a base}
     */
    public AbstractItemBuilder(@NotNull ItemStack base) {
        this.base = base.builder().build();
        this.amount = base.amount();
    }
    
    /**
     * Builds the {@link ItemStack}
     *
     * @return The {@link ItemStack}
     */
    @Contract(value = "_ -> new", pure = true)
    @Override
    public @NotNull ItemStack get(@Nullable String lang) {
        ItemStack itemStack;
        if (base != null) {
            itemStack = base.withAmount(amount);
        } else {
            itemStack = ItemStack.builder(material).amount(amount).build();
        }

        itemStack.with((builder) -> {
            builder.customName(displayName);
            builder.lore(lore);
            builder.set(DataComponents.DAMAGE, damage);
            builder.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(List.of(Integer.valueOf(customModelData).floatValue()), List.of(), List.of(), List.of()));

            // unbreakable state
            if (unbreakable != null) {
                builder.set(DataComponents.UNBREAKABLE, Unit.INSTANCE);
            } else {
                builder.remove(DataComponents.UNBREAKABLE);
            }

            // enchantments
            if (enchantments != null) {
                EnchantmentList enchantmentList = builder.build().get(DataComponents.ENCHANTMENTS) != null
                    ? builder.build().get(DataComponents.ENCHANTMENTS)
                    : EnchantmentList.EMPTY;
                for (Map.Entry<Enchantment, Pair<Integer, Boolean>> entry : enchantments.entrySet()) {
                    Enchantment enchantment = entry.getKey();
                    Pair<Integer, Boolean> pair = entry.getValue();
                    if (pair.getSecond()) {
                        enchantmentList = enchantmentList.with(MinecraftServer.getEnchantmentRegistry().getKey(enchantment), pair.getFirst());
                    } else {
                        enchantmentList = enchantmentList.remove(MinecraftServer.getEnchantmentRegistry().getKey(enchantment));
                    }
                }
                builder.set(DataComponents.ENCHANTMENTS, enchantmentList);
            }
        });
        
        // run modifiers
        if (modifiers != null) {
            for (Function<ItemStack, ItemStack> modifier : modifiers)
                itemStack = modifier.apply(itemStack);
        }
        
        return itemStack;
    }
    
    /**
     * Removes a lore line at the given index.
     *
     * @param index The index of the lore line to remove
     * @return The builder instance
     */
    @Contract("_ -> this")
    public @NotNull S removeLoreLine(int index) {
        if (lore != null) lore.remove(index);
        return (S) this;
    }
    
    /**
     * Clears the lore.
     *
     * @return The builder instance
     */
    @Contract("-> this")
    public @NotNull S clearLore() {
        if (lore != null) lore.clear();
        return (S) this;
    }
    
    /**
     * Gets the base {@link ItemStack} of this builder.
     *
     * @return The base {@link ItemStack}
     */
    public @Nullable ItemStack getBase() {
        return base;
    }
    
    /**
     * Gets the {@link Material} of this builder.
     *
     * @return The {@link Material}
     */
    public @Nullable Material getMaterial() {
        return material;
    }
    
    /**
     * Sets the {@link Material} of this builder.
     *
     * @param material The {@link Material}
     * @return The builder instance
     */
    @Contract("_ -> this")
    public @NotNull S setMaterial(@NotNull Material material) {
        this.material = material;
        return (S) this;
    }
    
    /**
     * Gets the amount.
     *
     * @return The amount
     */
    public int getAmount() {
        return amount;
    }
    
    /**
     * Sets the amount.
     *
     * @param amount The amount
     * @return The builder instance
     */
    @Contract("_ -> this")
    public @NotNull S setAmount(int amount) {
        this.amount = amount;
        return (S) this;
    }
    
    /**
     * Gets the damage value.
     *
     * @return The damage value
     */
    public int getDamage() {
        return damage;
    }
    
    /**
     * Sets the damage value.
     *
     * @param damage The damage value
     * @return The builder instance
     */
    @Contract("_ -> this")
    public @NotNull S setDamage(int damage) {
        this.damage = damage;
        return (S) this;
    }
    
    /**
     * Gets the custom model data value.
     *
     * @return The custom model data value
     */
    public int getCustomModelData() {
        return customModelData;
    }
    
    /**
     * Sets the custom model data value.
     *
     * @param customModelData The custom model data value
     * @return The builder instance
     */
    @Contract("_ -> this")
    public @NotNull S setCustomModelData(int customModelData) {
        this.customModelData = customModelData;
        return (S) this;
    }
    
    /**
     * Gets the unbreakable state, null for default.
     *
     * @return The unbreakable state
     */
    public @Nullable Boolean isUnbreakable() {
        return unbreakable;
    }
    
    /**
     * Sets the unbreakable state.
     *
     * @param unbreakable The unbreakable state
     * @return The builder instance
     */
    @Contract("_ -> this")
    public @NotNull S setUnbreakable(boolean unbreakable) {
        this.unbreakable = unbreakable;
        return (S) this;
    }
    
    /**
     * Gets the display name.
     *
     * @return The display name
     */
    public @Nullable Component getDisplayName() {
        return displayName;
    }
    
    /**
     * Sets the display name.
     *
     * @param displayName The display name
     * @return The builder instance
     */
    @Contract("_ -> this")
    public @NotNull S setDisplayName(String displayName) {
        this.displayName = LegacyComponentSerializer.legacyAmpersand().deserialize(displayName);
        return (S) this;
    }

    /**
     * Sets the display name.
     *
     * @param component The display name
     * @return The builder instance
     */
    @Contract("_ -> this")
    public @NotNull S setDisplayName(Component component) {
        this.displayName = component;
        return (S) this;
    }
    
    //<editor-fold desc="lore">
    
    /**
     * Gets the lore.
     *
     * @return The lore
     */
    public @Nullable List<Component> getLore() {
        return lore;
    }
    
    /**
     * Sets the lore.
     *
     * @param lore The lore
     * @return The builder instance
     */
    @Contract("_ -> this")
    public @NotNull S setLore(@NotNull List<@NotNull Component> lore) {
        this.lore = new ArrayList<>(lore);
        return (S) this;
    }

    /**
     * Adds lore lines.
     *
     * @param lines The lore lines
     * @return The builder instance
     */
    @Contract("_ -> this")
    public @NotNull S addLoreLines(@NotNull Component... lines) {
        if (lore == null) lore = new ArrayList<>();

        lore.addAll(Arrays.asList(lines));
        
        return (S) this;
    }
    
    /**
     * Adds lore lines.
     *
     * @param lines The lore lines
     * @return The builder instance
     */
    @Contract("_ -> this")
    public @NotNull S addLoreLines(@NotNull List<@NotNull Component> lines) {
        if (lore == null) lore = new ArrayList<>();

        lore.addAll(lines);
        
        return (S) this;
    }

    //<editor-fold desc="enchantments">
    
    /**
     * Gets the enchantments.
     *
     * @return The enchantments
     */
    public @Nullable HashMap<Enchantment, Pair<Integer, Boolean>> getEnchantments() {
        return enchantments;
    }
    
    /**
     * Sets the enchantments.
     *
     * @param enchantments The enchantments
     * @return The builder instance
     */
    @Contract("_ -> this")
    public @NotNull S setEnchantments(@NotNull HashMap<Enchantment, Pair<Integer, Boolean>> enchantments) {
        this.enchantments = enchantments;
        return (S) this;
    }
    
    /**
     * Adds an enchantment.
     *
     * @param enchantment            The enchantment
     * @param level                  The level
     * @param ignoreLevelRestriction Whether to ignore the level restriction
     * @return The builder instance
     */
    @Contract("_, _, _ -> this")
    public @NotNull S addEnchantment(Enchantment enchantment, int level, boolean ignoreLevelRestriction) {
        if (enchantments == null) enchantments = new HashMap<>();
        enchantments.put(enchantment, new Pair<>(level, ignoreLevelRestriction));
        return (S) this;
    }
    
    /**
     * Adds an enchantment.
     *
     * @param enchantment The enchantment
     * @return The builder instance
     */
    @Contract("_ -> this")
    public @NotNull S removeEnchantment(Enchantment enchantment) {
        if (enchantments != null) enchantments.remove(enchantment);
        return (S) this;
    }
    
    /**
     * Removes all enchantments.
     *
     * @return The builder instance
     */
    @Contract("-> this")
    public @NotNull S clearEnchantments() {
        if (enchantments != null) enchantments.clear();
        return (S) this;
    }
    //</editor-fold>
    
    //<editor-fold desc="modifiers">
    
    /**
     * Gets the configured modifier functions.
     *
     * @return The modifier functions
     */
    public @Nullable List<Function<ItemStack, ItemStack>> getModifiers() {
        return modifiers;
    }
    
    /**
     * Adds a modifier function, which will be run after building the {@link ItemStack}.
     *
     * @param modifier The modifier function
     * @return The builder instance
     */
    @Contract("_ -> this")
    public @NotNull S addModifier(Function<ItemStack, ItemStack> modifier) {
        if (modifiers == null) modifiers = new ArrayList<>();
        modifiers.add(modifier);
        return (S) this;
    }
    
    /**
     * Removes all modifier functions.
     *
     * @return The builder instance
     */
    @Contract("-> this")
    public @NotNull S clearModifiers() {
        if (modifiers != null) modifiers.clear();
        return (S) this;
    }
    //</editor-fold>
    
    /**
     * Clones this builder.
     *
     * @return The cloned builder
     */
    @SuppressWarnings("unchecked")
    @Contract(value = "-> new", pure = true)
    @Override
    public @NotNull S clone() {
        try {
            AbstractItemBuilder<S> clone = ((AbstractItemBuilder<S>) super.clone());
            if (base != null) clone.base = base.builder().build();
            if (lore != null) clone.lore = new ArrayList<>(lore);
            if (enchantments != null) clone.enchantments = new HashMap<>(enchantments);
            if (modifiers != null) clone.modifiers = new ArrayList<>(modifiers);
            
            return (S) clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e);
        }
    }
    
}
