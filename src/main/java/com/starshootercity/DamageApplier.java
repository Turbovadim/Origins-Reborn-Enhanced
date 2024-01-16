package com.starshootercity;

import net.minecraft.world.damagesource.DamageSources;
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftEntity;
import org.bukkit.entity.Entity;

public class DamageApplier {
    public static void damage(Entity entity, DamageSource source, float amount) {
        net.minecraft.world.entity.Entity NMSEntity = ((CraftEntity) entity).getHandle();
        NMSEntity.hurt(source.getSource(NMSEntity), amount);
    }

    @SuppressWarnings("unused")
    public enum DamageSource {
        FREEZING(DamageSources::freeze),
        DRY_OUT(DamageSources::dryOut),
        DROWN(DamageSources::drown),
        FALL(DamageSources::fall),
        MELTING(sources -> sources.melting),
        POISON(sources -> sources.poison),
        CRAMMING(DamageSources::cramming),
        DRAGON_BREATH(DamageSources::dragonBreath),
        CACTUS(DamageSources::cactus),
        FLY_INTO_WALL(DamageSources::flyIntoWall);

        private final DamageSourceGetter sourceGetter;

        public net.minecraft.world.damagesource.DamageSource getSource(net.minecraft.world.entity.Entity entity) {
            return sourceGetter.get(entity.damageSources());
        }

        DamageSource(DamageSourceGetter source) {
            this.sourceGetter = source;
        }
    }

    private interface DamageSourceGetter {
        net.minecraft.world.damagesource.DamageSource get(DamageSources sources);
    }
}
