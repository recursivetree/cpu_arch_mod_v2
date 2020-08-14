package eigencraft.cpuArchMod.block;

import eigencraft.cpuArchMod.CpuArchMod;
import io.github.cottonmc.cotton.gui.widget.data.Color;
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ProgrammableAgentBlockEntity extends BlockEntity implements BlockEntityClientSerializable {
    private String displayName = "unconfigured";
    private int textColor = Color.ORANGE_DYE.toRgb();

    public ProgrammableAgentBlockEntity() {
        super(CpuArchMod.PROGRAMMABLE_AGENT_BLOCK_ENTITY);
        markDirty();
    }

    public void setAppearance(String name, int color) {
        this.displayName = name;
        this.textColor = color;
        sync();
        markDirty();
    }

    @Override
    public void fromClientTag(CompoundTag compoundTag) {
        displayName = compoundTag.getString("displayName");
        textColor = compoundTag.getInt("textColor");
    }

    @Override
    public CompoundTag toClientTag(CompoundTag compoundTag) {
        compoundTag.putString("displayName", displayName);
        compoundTag.putInt("textColor", textColor);
        return compoundTag;
    }

    @Override
    public CompoundTag toTag(CompoundTag tag) {
        super.toTag(tag);
        tag.putString("displayName", displayName);
        tag.putInt("textColor", textColor);
        return tag;
    }

    @Override
    public void fromTag(BlockState state, CompoundTag tag) {
        super.fromTag(state, tag);
        displayName = tag.getString("displayName");
        textColor = tag.getInt("textColor");
    }

    @Override
    public void setLocation(World world, BlockPos pos) {
        super.setLocation(world, pos);
        if (!world.isClient) {
            sync();
        }
    }

    public static class ProgrammableAgentRenderer extends BlockEntityRenderer<ProgrammableAgentBlockEntity> {

        public ProgrammableAgentRenderer(BlockEntityRenderDispatcher dispatcher) {
            super(dispatcher);
        }

        @Override
        public void render(ProgrammableAgentBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
            String text = entity.displayName;

            matrices.push();
            matrices.translate(0.5, 1.5, 0.5);
            matrices.multiply(this.dispatcher.camera.getRotation());
            matrices.scale(-0.025F, -0.025F, 0.025F);

            TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;

            float x = (float) (-textRenderer.getWidth(text) / 2);

            //textRenderer.draw(text, x, 0, Color.BLACK.toRgb(), false, matrices.peek().getModel(), vertexConsumers, false, 0, light);
            textRenderer.draw(matrices, text, x, 0, entity.textColor);
            matrices.pop();
        }
    }
}
