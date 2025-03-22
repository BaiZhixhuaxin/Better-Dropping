package com.baizhihuaxin.betterdropping.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.ItemEntityRenderer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.random.Random;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

import static net.minecraft.client.render.entity.ItemEntityRenderer.getSeed;
import static net.minecraft.client.render.entity.ItemEntityRenderer.renderStack;

@Mixin(ItemEntityRenderer.class)
public abstract class ItemEntityRendererMixin extends EntityRenderer<ItemEntity> {
    protected ItemEntityRendererMixin(EntityRendererFactory.Context ctx) {
        super(ctx);
    }
    @Unique
    private final ItemRenderer itemRenderer = MinecraftClient.getInstance().getItemRenderer();
    @Unique
    private final net.minecraft.util.math.random.Random random = Random.create();
    @Inject(method = "render(Lnet/minecraft/entity/ItemEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",at=@At("HEAD"), cancellable = true)
    public void render(ItemEntity itemEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci) {
        matrixStack.push();
        ItemStack itemStack = itemEntity.getStack();
        this.random.setSeed(getSeed(itemStack));
        BakedModel bakedModel = this.itemRenderer.getModel(itemStack, itemEntity.getWorld(), (LivingEntity)null, itemEntity.getId());
        boolean bl = bakedModel.hasDepth();
        itemEntity.getItemAge();
        float k = bakedModel.getTransformation().getTransformation(ModelTransformationMode.GROUND).scale.y();
        matrixStack.translate(0.0F, 0.1F * k, 0.0F);
        UUID uuid = itemEntity.getUuid();
        long mostSigBits = uuid.getMostSignificantBits();
        long leastSigBits = uuid.getLeastSignificantBits();
        long seed = mostSigBits ^ leastSigBits;
        java.util.Random uuidRandom = new java.util.Random(seed);
        matrixStack.multiply(RotationAxis.POSITIVE_Y.rotation((float) (uuidRandom.nextFloat() * 2 * Math.PI)));
        matrixStack.multiply(RotationAxis.POSITIVE_X.rotation((float) (Math.PI / 2)));
        renderStack(this.itemRenderer, matrixStack, vertexConsumerProvider, i, itemStack, bakedModel, bl, this.random);
        matrixStack.pop();
        super.render(itemEntity, f, g, matrixStack, vertexConsumerProvider, i);
        ci.cancel();
    }
}

