package com.baizhihuaxin.betterdropping.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

@Mixin(PlayerEntity.class)
public abstract class PlayerTickMixin extends LivingEntity {
    @Unique
    private static int rightClickTick = 0;
    protected PlayerTickMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }
    @Inject(method = "tick",at=@At("HEAD"))
    private void tick(CallbackInfo ci){
        World world = this.getWorld();
        if(MinecraftClient.getInstance().mouse.wasRightButtonClicked()){
            if(rightClickTick == 0){
                Timer timer = new Timer();
                TimerTask task = new TimerTask() {
                    @Override
                    public void run() {
                        MinecraftClient client = MinecraftClient.getInstance();
                        HitResult hit = client.crosshairTarget;
                        if (hit != null) {
                            if (hit.getType().equals(HitResult.Type.BLOCK)) {
                                Vec3d vec3d = hit.getPos().offset(((BlockHitResult)hit).getSide(),0.25);
                                List<ItemEntity> entityList = world.getEntitiesByClass(ItemEntity.class, new Box(vec3d.x - 0.8, vec3d.y - 0.8, vec3d.z - 0.8, vec3d.x + 0.8, vec3d.y + 0.8, vec3d.z + 0.8), e -> e instanceof ItemEntity);
                                if (entityList.toArray().length > 0) {
                                    ItemEntity entity1 = entityList.getFirst();
                                    entity1.addCommandTag("canBePickedUp");
                                }
                            }
                        }
                    }
                };
                timer.schedule(task,50);
            }
            rightClickTick++;
        }
        else if(!MinecraftClient.getInstance().mouse.wasRightButtonClicked() && rightClickTick > 0){rightClickTick = 0;}
    }
}
