package com.example;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.server.command.CommandManager;
import net.minecraft.text.Text;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.command.CommandManager;
import net.minecraft.util.ActionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Objects;



public class EnderchestClient implements ClientModInitializer {
    static boolean enabled = true;
    private static final Logger LOGGER = LoggerFactory.getLogger(EnderchestClient.class);

    @Override
    public void onInitializeClient() {
        AttackBlockCallback.EVENT.register((PlayerEntity, World, Hand, BlockPos, Direction) -> { // breaking detection
            if (World.isClient) { // client
                var state = World.getBlockState(BlockPos); // get data n shit
                var block = state.getBlock();
                var enchantments = PlayerEntity.getMainHandStack().getEnchantments().toString();

                String blockName = block.getName().getString();
                if (Objects.equals(blockName, "Ender Chest") && (!enchantments.contains("minecraft:silk_touch"))) { // checks if breaking an enderchest and if you have silktouch
                    LOGGER.info("breaking " + blockName + " without silktouch. canceling");
                    if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
                        LOGGER.info(enchantments);
                        LOGGER.info(String.valueOf(PlayerEntity), World, Hand, BlockPos, Direction, blockName, enchantments); // logging data cuz why not
                        LOGGER.info(String.valueOf(enabled));
                    }
                    if (enabled) {
                        return ActionResult.FAIL; // breaking gets canceled
                    }
                }
            }
            return ActionResult.PASS; // breaking continues
        });

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommandManager.literal("togglesilkenderchest").executes(context -> {
                        enabled = !enabled; // toggle the feature
                        MinecraftClient.getInstance().player.sendMessage(Text.literal("Set only break enderchest with silk_touch to " + enabled), true);
                        return 1;
                    })
            );
        });
    }
}