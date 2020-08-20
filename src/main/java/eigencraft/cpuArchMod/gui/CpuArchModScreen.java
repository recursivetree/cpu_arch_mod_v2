package eigencraft.cpuArchMod.gui;

import io.github.cottonmc.cotton.gui.GuiDescription;
import io.github.cottonmc.cotton.gui.client.CottonClientScreen;

public class CpuArchModScreen extends CottonClientScreen {
    public CpuArchModScreen(GuiDescription description) {
        super(description);
    }

    @Override
    public void onClose() {
        ((CpuArchModGuiDescription)getDescription()).onClose();
        super.onClose();
    }
}
