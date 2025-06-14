package com.example.tridentmod;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public class MainMod implements ModInitializer {
    private static KeyBinding recallTridentKey;

    @Override
    public void onInitialize() {
        // Enregistrement de la touche R pour rappeler le trident
        recallTridentKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.tridentmod.recall", 
            InputUtil.Type.KEYSYM, 
            GLFW.GLFW_KEY_R, 
            "category.tridentmod.keys"));

        // À chaque tick serveur, on vérifie si la touche est pressée
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (PlayerEntity player : server.getPlayerManager().getPlayerList()) {
                if (player.getWorld().isClient) continue;

                if (recallTridentKey != null && recallTridentKey.wasPressed()) {
                    recallTridents(player);
                }
            }
        });
    }

    private void recallTridents(PlayerEntity player) {
        World world = player.getWorld();
        // Recherche les tridents appartenant au joueur dans un rayon de 50 blocs
        List<TridentEntity> tridents = world.getEntitiesByClass(TridentEntity.class, player.getBoundingBox().expand(50), t -> t.getOwner() == player && !t.isRemoved());

        for (TridentEntity trident : tridents) {
            if (!trident.isRemoved()) {
                // Remet un trident dans l'inventaire du joueur et supprime l'entité trident
                if (!world.isClient) {
                    ItemStack tridentItem = new ItemStack(net.minecraft.item.Items.TRIDENT);
                    if (!player.getInventory().insertStack(tridentItem)) {
                        player.dropItem(tridentItem, false);
                    }
                    trident.remove();
                }
            }
        }
    }
}
