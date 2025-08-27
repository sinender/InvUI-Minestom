package xyz.xenondevs.invui;

import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import net.minestom.server.MinecraftServer;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;


public class InvUI {
    
    private static InvUI instance;
    
    private final List<Runnable> disableHandlers = new ArrayList<>();

    private InvUI() {
        MinecraftServer.getSchedulerManager().buildShutdownTask(() -> {
            for (Runnable disableHandler : disableHandlers) {
                try {
                    disableHandler.run();
                } catch (Throwable t) {
                    getLogger().error("An error occurred while running a disable handler:", t);
                }
            }
        });
    }
    
    public static @NotNull InvUI getInstance() {
        return instance == null ? instance = new InvUI() : instance;
    }
    
    public @NotNull ComponentLogger getLogger() {
        return MinecraftServer.LOGGER;
    }
    
    public void addDisableHandler(@NotNull Runnable runnable) {
        disableHandlers.add(runnable);
    }
    
}
