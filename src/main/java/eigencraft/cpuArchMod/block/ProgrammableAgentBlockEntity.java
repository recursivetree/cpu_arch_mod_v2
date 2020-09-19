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
    private String infoLine = "";
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

    public void setInfoLine(String infoLine) {
        this.infoLine = infoLine;
        sync();
        markDirty();
    }

    @Override
    public void fromClientTag(CompoundTag compoundTag) {
        this.fromTag(getCachedState(),compoundTag);
    }

    @Override
    public CompoundTag toClientTag(CompoundTag compoundTag) {
        return this.toTag(compoundTag);
    }

    @Override
    public CompoundTag toTag(CompoundTag tag) {
        super.toTag(tag);
        tag.putString("displayName", displayName);
        tag.putInt("textColor", textColor);
        tag.putString("infoLine",infoLine);
        return tag;
    }

    @Override
    public void fromTag(BlockState state, CompoundTag tag) {
        super.fromTag(state, tag);
        displayName = tag.getString("displayName");
        textColor = tag.getInt("textColor");
        infoLine = tag.getString("infoLine");
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

        private void renderTextLine(String text,int color, float x, float y, float z, MatrixStack matrices){
            matrices.push();
            matrices.translate(x, y, z);
            matrices.multiply(this.dispatcher.camera.getRotation());
            matrices.scale(-0.025F, -0.025F, 0.025F);

            TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;

            float rx = (float) (-textRenderer.getWidth(text) / 2);

            textRenderer.draw(matrices, text, rx, 0, color);
            matrices.pop();
        }

        @Override
        public void render(ProgrammableAgentBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
            if (entity.infoLine.equals("")) {
                renderTextLine(entity.displayName, entity.textColor, 0.5f, 1.5f, 0.5f, matrices);
            } else {
                renderTextLine(entity.infoLine, entity.textColor, 0.5f, 1.5f, 0.5f, matrices);
                renderTextLine(entity.displayName, entity.textColor, 0.5f, 1.75f, 0.5f, matrices);
            }
        }
    }
}
