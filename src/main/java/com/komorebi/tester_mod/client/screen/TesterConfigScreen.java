package com.komorebi.tester_mod.client.screen;

import java.math.BigDecimal;

import com.komorebi.tester_mod.entity.tester.TesterEntity;
import com.komorebi.tester_mod.network.UpdateTesterConfigPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.neoforge.network.PacketDistributor;

public class TesterConfigScreen extends Screen {

    private static final int CONTENT_WIDTH = 220;
    private static final int FIELD_WIDTH = 100;

    private final TesterEntity tester;
    private boolean knockbackable;
    private boolean outputZeroDamage;
    private EditBox armorField;
    private EditBox armorToughnessField;
    private EditBox maxHealthField;
    private Button knockbackButton;
    private Button outputZeroDamageButton;
    private Component errorMessage;

    public TesterConfigScreen(TesterEntity tester) {
        super(Component.translatable("screen.tester_mod.config.title", tester.getName()));
        this.minecraft = Minecraft.getInstance();
        this.font = this.minecraft.font;
        this.tester = tester;
        this.knockbackable = tester.isKnockbackable();
        this.outputZeroDamage = tester.shouldOutputZeroDamage();
    }

    @Override
    protected void init() {
        int left = (this.width - CONTENT_WIDTH) / 2;
        int fieldX = left + CONTENT_WIDTH - FIELD_WIDTH;
        int top = Math.max(35, this.height / 2 - 90);

        this.knockbackButton = this.addRenderableWidget(Button.builder(
            this.knockbackButtonText(),
            button -> {
                this.knockbackable = !this.knockbackable;
                button.setMessage(this.knockbackButtonText());
            }
        ).bounds(left, top + 24, CONTENT_WIDTH, 20).build());

        this.outputZeroDamageButton = this.addRenderableWidget(Button.builder(
            this.outputZeroDamageButtonText(),
            button -> {
                this.outputZeroDamage = !this.outputZeroDamage;
                button.setMessage(this.outputZeroDamageButtonText());
            }
        ).bounds(left, top + 48, CONTENT_WIDTH, 20).build());

        this.armorField = this.createNumberField(
            fieldX,
            top + 78,
            "screen.tester_mod.config.armor",
            this.tester.getAttributeValue(Attributes.ARMOR)
        );
        this.armorToughnessField = this.createNumberField(
            fieldX,
            top + 102,
            "screen.tester_mod.config.armor_toughness",
            this.tester.getAttributeValue(Attributes.ARMOR_TOUGHNESS)
        );
        this.maxHealthField = this.createNumberField(
            fieldX,
            top + 126,
            "screen.tester_mod.config.max_health",
            this.tester.getAttributeValue(Attributes.MAX_HEALTH)
        );

        this.addRenderableWidget(Button.builder(
            Component.translatable("gui.done"),
            button -> this.applyConfiguration()
        ).bounds(left, top + 158, 106, 20).build());
        this.addRenderableWidget(Button.builder(
            Component.translatable("gui.cancel"),
            button -> this.onClose()
        ).bounds(left + 114, top + 158, 106, 20).build());
    }

    private EditBox createNumberField(int x, int y, String narrationKey, double value) {
        EditBox field = new EditBox(
            this.font,
            x,
            y,
            FIELD_WIDTH,
            20,
            Component.translatable(narrationKey)
        );
        field.setMaxLength(10);
        field.setFilter(input -> input.matches("\\d{0,4}(\\.\\d{0,3})?"));
        field.setValue(formatNumber(value));
        return this.addRenderableWidget(field);
    }

    private Component knockbackButtonText() {
        return Component.translatable(
            "screen.tester_mod.config.knockback",
            Component.translatable(this.knockbackable ? "options.on" : "options.off")
        );
    }

    private Component outputZeroDamageButtonText() {
        return Component.translatable(
            "screen.tester_mod.config.output_zero_damage",
            Component.translatable(this.outputZeroDamage ? "options.on" : "options.off")
        );
    }

    private void applyConfiguration() {
        try {
            double armor = Double.parseDouble(this.armorField.getValue());
            double armorToughness = Double.parseDouble(this.armorToughnessField.getValue());
            double maxHealth = Double.parseDouble(this.maxHealthField.getValue());

            if (!TesterEntity.isValidConfiguration(armor, armorToughness, maxHealth)) {
                this.errorMessage = Component.translatable("screen.tester_mod.config.error.range");
                return;
            }

            PacketDistributor.sendToServer(new UpdateTesterConfigPayload(
                this.tester.getId(),
                this.knockbackable,
                this.outputZeroDamage,
                armor,
                armorToughness,
                maxHealth
            ));
            this.onClose();
        } catch (NumberFormatException exception) {
            this.errorMessage = Component.translatable("screen.tester_mod.config.error.number");
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (this.tester.isRemoved()) {
            this.onClose();
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        int left = (this.width - CONTENT_WIDTH) / 2;
        int top = Math.max(35, this.height / 2 - 90);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, top, 0xFFFFFFFF);
        guiGraphics.drawString(
            this.font,
            Component.translatable("screen.tester_mod.config.armor"),
            left,
            top + 84,
            0xFFFFFFFF
        );
        guiGraphics.drawString(
            this.font,
            Component.translatable("screen.tester_mod.config.armor_toughness"),
            left,
            top + 108,
            0xFFFFFFFF
        );
        guiGraphics.drawString(
            this.font,
            Component.translatable("screen.tester_mod.config.max_health"),
            left,
            top + 132,
            0xFFFFFFFF
        );

        if (this.errorMessage != null) {
            guiGraphics.drawCenteredString(this.font, this.errorMessage, this.width / 2, top + 183, 0xFFFF5555);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private static String formatNumber(double value) {
        return BigDecimal.valueOf(value).stripTrailingZeros().toPlainString();
    }
}
