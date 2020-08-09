package eigencraft.cpuArchMod.gui;

import eigencraft.cpuArchMod.CpuArchMod;
import io.github.cottonmc.cotton.gui.client.LightweightGuiDescription;
import io.github.cottonmc.cotton.gui.widget.*;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;

public class ProgrammableAgentGUI extends LightweightGuiDescription {
    WGridPanel root = new WGridPanel();
    private List<WWidget> widgets = new ArrayList<>();
    String currentScriptFileName;
    String currentScript;
    BlockPos pos;
    File scriptsDirectory = new File(FabricLoader.getInstance().getConfigDirectory(),"cpu_arch_mod_scripts");

    public ProgrammableAgentGUI(String currentScriptFileName, String currentScript, BlockPos pos) {
        setRootPanel(root);
        this.pos = pos;
        this.currentScriptFileName = currentScriptFileName;
        this.currentScript = currentScript;
        root.setSize(180, 180);
        buildMainScreen();
    }

    protected void buildMainScreen(){
        removeElements();
        addElement(new WLabel(new LiteralText(currentScriptFileName)),1,0,8,1);
        WButton openFileSelectionScreen = new WButton(new TranslatableText("gui.cpu_arch_mod.select_script"));
        openFileSelectionScreen.setOnClick(new Runnable() {
            @Override
            public void run() {
                buildFileSelectionScreen();
            }
        });
        addElement(openFileSelectionScreen,6,0,4,1);

        String[] elements = currentScript.split(System.lineSeparator());
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        int totalHeight = elements.length* textRenderer.fontHeight +8;
        int maxWidth = 0;
        for (int i = 0; i < elements.length; i++) {
            int width = textRenderer.getWidth(elements[i]);
            if (width>maxWidth){
                maxWidth = width;
            }
        }
        WText srcPreview = new WText(new LiteralText(currentScript));
        srcPreview.setSize(maxWidth+8,totalHeight);
        WScrollPanel scrollPanel = new WScrollPanel(srcPreview);
        addElement(scrollPanel,0,2,10,8);
    }

    protected void buildFileSelectionScreen(){
        removeElements();

        WButton back = new WButton(new TranslatableText("gui.cpu_arch_mod.back"));
        back.setOnClick(new Runnable() {
            @Override
            public void run() {
                buildMainScreen();
            }
        });
        addElement(back,0,0,3,1);

        WButton openDirectory = new WButton(new TranslatableText("gui.cpu_arch_mod.open_directory"));
        openDirectory.setOnClick(new Runnable() {
            @Override
            public void run() {
                Util.getOperatingSystem().open(scriptsDirectory);
            }
        });
        addElement(openDirectory,5,0,5,1);

        addElement(new WLabel(new TranslatableText("gui.cpu_arch_mod.available_scripts")),0,2,10,1);

        BiConsumer<File, FileListItem> configurator = (File file, FileListItem destination) -> {
            destination.fileName.setText(new LiteralText(file.getName()));
            destination.file = file;
            destination.select.setOnClick(new Runnable() {
                @Override
                public void run() {
                    try {
                        currentScript = new String(Files.readAllBytes(destination.file.toPath()));
                    } catch (IOException e) {
                        return;
                    }
                    currentScriptFileName = destination.file.getName();
                    buildMainScreen();
                    safeSettings();
                }
            });
        };
        File[] fileNames = scriptsDirectory.listFiles();
        WListPanel files = new WListPanel(Arrays.asList(fileNames),FileListItem::new,configurator);
        files.setListItemHeight(18);
        addElement(files,0,3,10,7);
    }

    private static class FileListItem extends WPlainPanel{
        private static LiteralText defaultText = new LiteralText("internal error");
        WLabel fileName = new WLabel(defaultText);
        WButton select = new WButton(new TranslatableText("gui.cpu_arch_mod.select"));
        File file;
        public FileListItem(){
            add(fileName,0,2);
            add(select,90,0,54,18);
        }
    }

    protected void safeSettings(){
        PacketByteBuf passedData = new PacketByteBuf(Unpooled.buffer());
        passedData.writeBlockPos(this.pos);
        passedData.writeString(currentScriptFileName);
        passedData.writeString(currentScript);
        // Send packet to server to change the block for us
        ClientSidePacketRegistry.INSTANCE.sendToServer(CpuArchMod.PROGRAMMABLE_AGENT_SAFE_CONFIG_C2S_PACKET, passedData);
    }

    protected void addElement(WWidget widget,int x, int y, int width, int height){
        root.add(widget,x,y,width,height);
        widgets.add(widget);
    }

    protected void removeElements(){
        for (WWidget widget:widgets){
            root.remove(widget);
        }
    }
}
