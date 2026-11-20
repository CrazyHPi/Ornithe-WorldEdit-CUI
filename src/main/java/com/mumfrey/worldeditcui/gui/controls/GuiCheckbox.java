package com.mumfrey.worldeditcui.gui.controls;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;

/**
 * Super-simple implementation of a checkbox control
 *
 * @author Adam Mummery-Smith
 */
public class GuiCheckbox extends GuiButton
{
	public boolean checked;

	public GuiCheckbox(int controlId, int xPosition, int yPosition, String displayString)
	{
		super(controlId, xPosition, yPosition, Minecraft.getMinecraft().fontRenderer.getStringWidth(displayString) + 16, 12, displayString);
	}

	@Override
	public void drawButton(Minecraft minecraft, int mouseX, int mouseY, float partialTicks)
	{
		if (!this.visible)
		{
			return;
		}

		this.hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;

		drawRect(this.x, this.y, this.x + 12, this.y + 12, 0xFFA0A0A0);
		drawRect(this.x + 1, this.y + 1, this.x + 11, this.y + 11, 0xFF000000);

		if (this.checked)
		{
			drawRect(this.x + 2, this.y + 2, this.x + 10, this.y + 10, 0xFF66FF66);
		}

		int colour = 0xE0E0E0;
		if (!this.enabled)
		{
			colour = 0xA0A0A0;
		}
		else if (this.hovered)
		{
			colour = 0xFFFFA0;
		}

		this.drawString(minecraft.fontRenderer, this.displayString, this.x + 16, this.y + 2, colour);
	}
}