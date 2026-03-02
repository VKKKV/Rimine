package io.github.vkkkv.rimine.mixin;

import io.github.vkkkv.rimine.core.RimeInputHandler;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EditBox.class)
public abstract class EditBoxMixin extends AbstractWidget {
  @Shadow @Final private Font font;
  @Shadow private int cursorPos;
  @Shadow private String value;
  @Shadow private int displayPos;

  public EditBoxMixin(int x, int y, int width, int height, Component message) {
    super(x, y, width, height, message);
  }

  @Inject(method = "renderWidget", at = @At("HEAD"))
  private void onRenderWidget(
      GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
    if (this.isFocused()) {
      int x = this.getX() + 4;
      int y = this.getY() + (this.height - 8) / 2;

      int start = Math.max(0, Math.min(this.displayPos, this.value.length()));
      int end = Math.max(start, Math.min(this.cursorPos, this.value.length()));
      String textBeforeCursor = this.value.substring(start, end);
      int cursorOffset = this.font.width(textBeforeCursor);

      RimeInputHandler.setCursorPosition(x + cursorOffset, y + 10);
    }
  }
}
