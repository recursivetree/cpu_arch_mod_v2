package eigencraft.cpuArchMod.gui;

import eigencraft.cpuArchMod.CpuArchModClient;
import eigencraft.cpuArchMod.networking.CpuArchModPackets;
import eigencraft.cpuArchMod.networking.PrgAgentConfigurationC2SPacket;
import io.github.cottonmc.cotton.gui.widget.*;
import io.github.cottonmc.cotton.gui.widget.data.Color;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public class ProgrammableAgentGUI extends CpuArchModGuiDescription {
    WGridPanel root = new WGridPanel();
    String currentScriptFileName;
    String currentScript;
    String errorLog;
    BlockPos pos;
    boolean resendScriptInfo = false;
    //File scriptsDirectory = new File(FabricLoader.getInstance().getConfigDirectory(), "cpu_arch_mod_scripts");
    private final List<WWidget> widgets = new ArrayList<>();

    public ProgrammableAgentGUI(String currentScriptFileName, String currentScript, String errorLog, BlockPos pos) {
        setRootPanel(root);
        this.pos = pos;
        this.currentScriptFileName = currentScriptFileName;
        this.currentScript = currentScript;
        this.errorLog = errorLog;
        root.setSize(180, 180);
        buildMainScreen();
    }

    protected void buildMainScreen() {
        removeElements();
        addElement(new WLabel(new LiteralText(currentScriptFileName)), 1, 0, 8, 1);
        WButton openFileSelectionScreen = new WButton(new TranslatableText("gui.cpu_arch_mod.select_script"));
        openFileSelectionScreen.setOnClick(this::buildFileSelectionScreen);
        addElement(openFileSelectionScreen, 6, 0, 4, 1);

        if (this.errorLog != null) {
            WButton openErrorMessageScreen = new WButton(new TranslatableText("gui.cpu_arch_mod.open_error_screen"));
            openErrorMessageScreen.setOnClick(this::buildErrorScreen);
            addElement(openErrorMessageScreen, 6, 1, 4, 1);
        }

        String[] elements = currentScript.split(System.lineSeparator());
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        int totalHeight = elements.length * textRenderer.fontHeight + 8;
        int maxWidth = 0;
        for (String element : elements) {
            int width = textRenderer.getWidth(element);
            if (width > maxWidth) {
                maxWidth = width;
            }
        }
        WText srcPreview = new WText(new LiteralText(currentScript));
        srcPreview.setSize(maxWidth + 8, totalHeight);
        WScrollPanel scrollPanel = new WScrollPanel(srcPreview);
        addElement(scrollPanel, 0, 2, 10, 8);
    }

    protected void buildErrorScreen() {
        removeElements();

        WButton back = new WButton(new TranslatableText("gui.cpu_arch_mod.back"));
        back.setOnClick(this::buildMainScreen);
        addElement(back, 0, 0, 3, 1);

        String[] elements = errorLog.split(System.lineSeparator());
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        int totalHeight = elements.length * textRenderer.fontHeight + 8;
        int maxWidth = 0;
        for (String element : elements) {
            int width = textRenderer.getWidth(element);
            if (width > maxWidth) {
                maxWidth = width;
            }
        }
        WText srcPreview = new WText(new LiteralText(errorLog));
        srcPreview.setSize(maxWidth + 8, totalHeight);
        WScrollPanel scrollPanel = new WScrollPanel(srcPreview);
        addElement(scrollPanel, 0, 2, 10, 8);
    }

    protected void buildFileSelectionScreen() {
        removeElements();

        WButton back = new WButton(new TranslatableText("gui.cpu_arch_mod.back"));
        back.setOnClick(this::buildMainScreen);
        addElement(back, 0, 0, 3, 1);

        WButton openDirectory = new WButton(new TranslatableText("gui.cpu_arch_mod.open_directory"));
        openDirectory.setOnClick(() -> Util.getOperatingSystem().open(CpuArchModClient.SCRIPT_MANAGER.getScriptsDirectory()));
        addElement(openDirectory, 5, 0, 5, 1);

        addElement(new WLabel(new TranslatableText("gui.cpu_arch_mod.available_scripts")), 0, 2, 10, 1);

        BiConsumer<String, FileListItem> configurator = (String file, FileListItem destination) -> {
            destination.fileName.setText(new LiteralText(file));
            destination.file = file;
            destination.select.setOnClick(() -> {
                try {
                    currentScript = CpuArchModClient.SCRIPT_MANAGER.readFile(destination.file);
                } catch (IOException e) {
                    return;
                }
                currentScriptFileName = destination.file;
                buildMainScreen();
                resendScriptInfo = true;
            });
        };
        WListPanel<String, FileListItem> files = new WListPanel<>(CpuArchModClient.SCRIPT_MANAGER.listAvailableFiles(), FileListItem::new, configurator);
        files.setListItemHeight(18);
        addElement(files, 0, 3, 10, 7);
    }

    protected void safeSettings() {
        PrgAgentConfigurationC2SPacket packet = new PrgAgentConfigurationC2SPacket(
                pos,resendScriptInfo,currentScript,currentScriptFileName,currentScriptFileName, Color.LIME_DYE.toRgb()
        );
        ClientSidePacketRegistry.INSTANCE.sendToServer(CpuArchModPackets.PROGRAMMABLE_AGENT_CONFIG_C2S_PACKET, packet.asPacketByteBuf());
    }

    @Override
    public void onClose() {
        safeSettings();
        super.onClose();
    }

    protected void addElement(WWidget widget, int x, int y, int width, int height) {
        root.add(widget, x, y, width, height);
        widgets.add(widget);
    }

    protected void removeElements() {
        for (WWidget widget : widgets) {
            root.remove(widget);
        }
    }

    private static class FileListItem extends WPlainPanel {
        private static final LiteralText defaultText = new LiteralText("internal error");
        WLabel fileName = new WLabel(defaultText);
        WButton select = new WButton(new TranslatableText("gui.cpu_arch_mod.select"));
        String file;

        public FileListItem() {
            add(fileName, 0, 2);
            add(select, 90, 0, 54, 18);
        }
    }
}
