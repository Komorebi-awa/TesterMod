package com.komorebi.tester_mod.client.screen;

import com.komorebi.tester_mod.network.UpdateSelfTesterConfigPayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

public class SelfTesterConfigScreen extends Screen {

    private static final int CONTENT_WIDTH = 240;

    private boolean damageImmunity;
    private boolean outputZeroDamage;

    public SelfTesterConfigScreen(boolean damageImmunity, boolean outputZeroDamage) {
        super(Component.translatable("screen.tester_mod.self_config.title"));
        this.damageImmunity = damageImmunity;
        this.outputZeroDamage = outputZeroDamage;
    }

    @Override
    protected void init() {
        int left = (this.width - CONTENT_WIDTH) / 2;
        int top = Math.max(45, this.height / 2 - 60);

        this.addRenderableWidget(Button.builder(
            this.damageImmunityButtonText(),
            button -> {
                this.damageImmunity = !this.damageImmunity;
                button.setMessage(this.damageImmunityButtonText());
            }
        ).bounds(left, top + 24, CONTENT_WIDTH, 20).build());

        this.addRenderableWidget(Button.builder(
            this.outputZeroDamageButtonText(),
            button -> {
                this.outputZeroDamage = !this.outputZeroDamage;
                button.setMessage(this.outputZeroDamageButtonText());
            }
        ).bounds(left, top + 48, CONTENT_WIDTH, 20).build());

        this.addRenderableWidget(Button.builder(
            Component.translatable("gui.done"),
            button -> this.applyConfiguration()
        ).bounds(left, top + 80, 116, 20).build());
        this.addRenderableWidget(Button.builder(
            Component.translatable("gui.cancel"),
            button -> this.onClose()
        ).bounds(left + 124, top + 80, 116, 20).build());
    }

    private Component damageImmunityButtonText() {
        return Component.translatable(
            "screen.tester_mod.self_config.damage_immunity",
            Component.translatable(this.damageImmunity ? "options.on" : "options.off")
        );
    }

    private Component outputZeroDamageButtonText() {
        return Component.translatable(
            "screen.tester_mod.config.output_zero_damage",
            Component.translatable(this.outputZeroDamage ? "options.on" : "options.off")
        );
    }

    private void applyConfiguration() {
        PacketDistributor.sendToServer(new UpdateSelfTesterConfigPayload(
            this.damageImmunity,
            this.outputZeroDamage
        ));
        this.onClose();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        int top = Math.max(45, this.height / 2 - 60);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, top, 0xFFFFFFFF);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
