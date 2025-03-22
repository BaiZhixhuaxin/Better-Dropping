package com.baizhihuaxin.betterdropping.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.Ownable;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stat.Stats;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin extends Entity implements Ownable {
    @Shadow public abstract ItemStack getStack();

    @Shadow @Nullable private UUID owner;

    @Shadow private int pickupDelay;

    @Shadow @Nullable private Entity thrower;

    public ItemEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }
    @Inject(method = "onPlayerCollision",at=@At("HEAD"), cancellable = true)
    private void modify(PlayerEntity player, CallbackInfo ci){
        if (!this.getWorld().isClient) {
            ItemStack itemStack = this.getStack();
            Item item = itemStack.getItem();
            int i = itemStack.getCount();
            if (this.getCommandTags().contains("canBePickedUp") || (this.thrower != null && !this.thrower.isPlayer()) || this.thrower == null){
                if (this.pickupDelay == 0 && (this.owner == null || this.owner.equals(player.getUuid())) && player.getInventory().insertStack(itemStack)) {
                    player.sendPickup(this, i);
                    if (itemStack.isEmpty()) {
                        this.discard();
                        itemStack.setCount(i);
                    }

                    player.increaseStat(Stats.PICKED_UP.getOrCreateStat(item), i);
                    player.triggerItemPickedUpByEntityCriteria((ItemEntity) (Object) this);
                }
            }
        }
        ci.cancel();
    }
}
