package com.starshootercity.abilities;

import com.destroystokyo.paper.event.server.ServerTickEndEvent;
import com.starshootercity.Origin;
import com.starshootercity.OriginSwapper;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public interface ParticleAbility extends Ability {
    Particle getParticle();
    int getFrequency();
    default int getExtra() {
        return 0;
    }
    default Object getData() {
        return null;
    }

    class ParticleAbilityListener implements Listener {
        @EventHandler
        public void onServerTickEnd(ServerTickEndEvent event) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                Origin origin = OriginSwapper.getOrigin(player);
                if (origin == null) continue;
                for (Ability ability : origin.getAbilities()) {
                    if (ability instanceof ParticleAbility particleAbility) {
                        if (event.getTickNumber() % particleAbility.getFrequency() == 0) {
                            player.getWorld().spawnParticle(particleAbility.getParticle(), player.getLocation(), 1, 0.5, 1, 0.5, particleAbility.getExtra(), particleAbility.getData());
                        }
                    }
                }
            }
        }
    }
}
