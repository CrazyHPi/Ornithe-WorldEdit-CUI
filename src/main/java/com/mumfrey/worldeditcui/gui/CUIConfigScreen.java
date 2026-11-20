package com.mumfrey.worldeditcui.gui;

import com.mumfrey.worldeditcui.ModWorldEditCUI;
import com.mumfrey.worldeditcui.config.CUIConfiguration;
import com.mumfrey.worldeditcui.gui.controls.GuiCheckbox;
import com.mumfrey.worldeditcui.gui.controls.GuiColourButton;
import com.mumfrey.worldeditcui.gui.controls.GuiControl;
import com.mumfrey.worldeditcui.render.ConfiguredColour;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.resources.I18n;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

/**
 * Vanilla screen implementation of the WorldEditCUI configuration panel,
 * replaces the LiteLoader {@code ConfigPanel}.
 *
 */
public class CUIConfigScreen extends GuiScreen
{
	private static final int COLOUR_OPTION_BASE_ID = 100;
	private static final int DONE_BUTTON_ID = 9000;

	private static final int CONTROL_SPACING = 24;
	private static final int CONTROL_TOP = 80;
	private static final int CONTROLS_PADDING = 10;
	private static final int EXTRA_CONTROLS_SPACING = 16;
	private static final int EXTRA_CONTROLS_HEIGHT = EXTRA_CONTROLS_SPACING * 2;

	private static final int PANEL_WIDTH = 304;

	private static final int TITLE_TOP = 8;
	private static final int CONTENT_TOP = 28;
	private static final int BOTTOM_BAR = 32;

	private static final int SCROLL_STEP = 12;

	private final GuiScreen parent;

	private final List<GuiButton> colourControlList = new ArrayList<GuiButton>();
	private final List<GuiColourButton> colourButtonList = new ArrayList<GuiColourButton>();
	private final List<GuiCheckbox> checkboxList = new ArrayList<GuiCheckbox>();

	private GuiCheckbox chkPromiscuous, chkAlwaysOnTop, chkClearAll;

	private GuiButton activeControl;

	private int panelLeft;
	private int guiScale;
	private int colourButtonsBottom;
	private int contentHeight;
	private int scrollOffset;

	public CUIConfigScreen(GuiScreen parent)
	{
		this.parent = parent;
	}

	@Override
	public void initGui()
	{
		this.colourControlList.clear();
		this.colourButtonList.clear();
		this.checkboxList.clear();
		this.scrollOffset = 0;

		this.panelLeft = Math.max(0, (this.width - CUIConfigScreen.PANEL_WIDTH) / 2);
		ScaledResolution scaledresolution = new ScaledResolution(this.mc);
		this.guiScale = scaledresolution.getScaleFactor();
		GuiControl.setScreenSizeAndScale(this.width, this.height, this.guiScale);

		int nextId = 0;
		for (ConfiguredColour colour : ConfiguredColour.values())
		{
			this.colourControlList.add(new GuiColourButton(this.mc, nextId, this.panelLeft + 24, CUIConfigScreen.CONTROL_TOP + nextId * CUIConfigScreen.CONTROL_SPACING, 40, 20, colour));
			this.colourControlList.add(new GuiControl(this.mc, CUIConfigScreen.COLOUR_OPTION_BASE_ID + nextId, this.panelLeft + 234, CUIConfigScreen.CONTROL_TOP + nextId * CUIConfigScreen.CONTROL_SPACING, 60, 20, "Reset"));
			nextId++;
		}

		this.colourButtonsBottom = CUIConfigScreen.CONTROL_TOP + nextId * CUIConfigScreen.CONTROL_SPACING + CUIConfigScreen.EXTRA_CONTROLS_SPACING;

		this.checkboxList.add(this.chkPromiscuous = new GuiCheckbox(nextId, this.panelLeft + 24, 26, I18n.format("gui.options.compat.spammy")));
		this.checkboxList.add(this.chkAlwaysOnTop = new GuiCheckbox(nextId, this.panelLeft + 24, 42, I18n.format("gui.options.compat.ontop")));
		this.checkboxList.add(this.chkClearAll = new GuiCheckbox(nextId, this.panelLeft + 24, this.colourButtonsBottom + CUIConfigScreen.EXTRA_CONTROLS_SPACING, I18n.format("gui.options.extra.clearall")));

		for (GuiButton control : this.colourControlList)
		{
			if (control instanceof GuiColourButton)
			{
				this.colourButtonList.add((GuiColourButton)control);
			}
		}

		CUIConfiguration config = ModWorldEditCUI.INSTANCE.getController().getConfiguration();
		this.chkPromiscuous.checked = config.isPromiscuous();
		this.chkAlwaysOnTop.checked = config.isAlwaysOnTop();
		this.chkClearAll.checked = config.isClearAllOnKey();

		this.contentHeight = this.colourButtonsBottom + CUIConfigScreen.EXTRA_CONTROLS_HEIGHT + CUIConfigScreen.CONTROLS_PADDING;

		this.addButton(new GuiButton(CUIConfigScreen.DONE_BUTTON_ID, this.width / 2 - 100, this.height - 27, 200, 20, I18n.format("gui.done")));

		this.updateControlPositions();
	}

	private void updateControlPositions()
	{
		int offset = CUIConfigScreen.CONTENT_TOP - this.scrollOffset;
		int index = 0;
		for (GuiButton control : this.colourControlList)
		{
			control.y = offset + CUIConfigScreen.CONTROL_TOP + (index / 2) * CUIConfigScreen.CONTROL_SPACING;
			index++;
		}

		this.chkPromiscuous.y = offset + 26;
		this.chkAlwaysOnTop.y = offset + 42;
		this.chkClearAll.y = offset + this.colourButtonsBottom + CUIConfigScreen.EXTRA_CONTROLS_SPACING;
	}

	private int getViewTop()
	{
		return CUIConfigScreen.CONTENT_TOP;
	}

	private int getViewBottom()
	{
		return this.height - CUIConfigScreen.BOTTOM_BAR;
	}

	private int getMaxScroll()
	{
		int viewHeight = Math.max(0, this.getViewBottom() - this.getViewTop());
		return Math.max(0, this.contentHeight + CUIConfigScreen.CONTROLS_PADDING - viewHeight);
	}

	private boolean isPickerOpen()
	{
		for (GuiColourButton colourButton : this.colourButtonList)
		{
			if (colourButton.isPickerOpen())
			{
				return true;
			}
		}

		return false;
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks)
	{
		this.drawDefaultBackground();
		this.drawCenteredString(this.fontRenderer, I18n.format("wecui.options.title"), this.width / 2, CUIConfigScreen.TITLE_TOP, 0xFFFFFF);

		int offset = CUIConfigScreen.CONTENT_TOP - this.scrollOffset;

		this.enableScissor(0, this.getViewTop(), this.width, this.getViewBottom());

		this.drawString(this.fontRenderer, I18n.format("gui.options.compat.title"), this.panelLeft + 10, offset + CUIConfigScreen.CONTROLS_PADDING, 0xFFFFFF55);
		this.drawString(this.fontRenderer, I18n.format("gui.options.colours.title"), this.panelLeft + 10, offset + 64, 0xFFFFFF55);
		this.drawString(this.fontRenderer, I18n.format("gui.options.extra.title"), this.panelLeft + 10, offset + this.colourButtonsBottom, 0xFFFFFF55);

		for (GuiButton control : this.colourControlList)
		{
			control.drawButton(this.mc, mouseX, mouseY, partialTicks);
		}

		for (GuiCheckbox checkbox : this.checkboxList)
		{
			checkbox.drawButton(this.mc, mouseX, mouseY, partialTicks);
		}

		this.disableScissor();

		this.drawScrollBar();

		super.drawScreen(mouseX, mouseY, partialTicks);

		for (GuiColourButton colourButton : this.colourButtonList)
		{
			colourButton.drawPicker(this.mc, mouseX, mouseY, partialTicks);
		}
	}

	private void drawScrollBar()
	{
		int maxScroll = this.getMaxScroll();
		if (maxScroll <= 0)
		{
			return;
		}

		int barX = this.width - 8;
		int top = this.getViewTop();
		int bottom = this.getViewBottom();
		int trackHeight = bottom - top;

		drawRect(barX, top, barX + 4, bottom, 0x44000000);

		int thumbHeight = Math.max(20, trackHeight * trackHeight / (this.contentHeight + CUIConfigScreen.CONTROLS_PADDING));
		int thumbTop = top + (trackHeight - thumbHeight) * this.scrollOffset / maxScroll;

		drawRect(barX, thumbTop, barX + 4, thumbTop + thumbHeight, 0x88FFFFFF);
	}

	private void enableScissor(int x, int y, int x2, int y2)
	{
		GL11.glEnable(GL11.GL_SCISSOR_TEST);
		GL11.glScissor(x * this.guiScale, this.mc.displayHeight - y2 * this.guiScale, (x2 - x) * this.guiScale, (y2 - y) * this.guiScale);
	}

	private void disableScissor()
	{
		GL11.glDisable(GL11.GL_SCISSOR_TEST);
	}

	@Override
	public void handleMouseInput()
	{
		super.handleMouseInput();

		int dwheel = Mouse.getEventDWheel();
		if (dwheel != 0 && !this.isPickerOpen())
		{
			dwheel = dwheel > 0 ? 1 : -1;

			int newOffset = this.scrollOffset - dwheel * CUIConfigScreen.SCROLL_STEP;
			newOffset = Math.max(0, Math.min(this.getMaxScroll(), newOffset));

			if (newOffset != this.scrollOffset)
			{
				this.scrollOffset = newOffset;
				this.updateControlPositions();
			}
		}
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton)
	{
		if (mouseButton != 0)
		{
			super.mouseClicked(mouseX, mouseY, mouseButton);
			return;
		}

		if (this.isPickerOpen())
		{
			for (GuiColourButton colourButton : this.colourButtonList)
			{
				if (colourButton.mousePressed(this.mc, mouseX, mouseY))
				{
					this.activeControl = colourButton;
					return;
				}
			}

			// Clicking outside the picker dismisses it, do not let the same click
			// activate another control underneath.
			return;
		}

		if (mouseY >= this.getViewTop() && mouseY < this.getViewBottom())
		{
			for (GuiButton control : this.colourControlList)
			{
				if (control.mousePressed(this.mc, mouseX, mouseY))
				{
					this.activeControl = control;
					control.playPressSound(this.mc.getSoundHandler());
					this.actionPerformed(control);
					return;
				}
			}

			for (GuiCheckbox checkbox : this.checkboxList)
			{
				if (checkbox.mousePressed(this.mc, mouseX, mouseY))
				{
					this.activeControl = checkbox;
					checkbox.playPressSound(this.mc.getSoundHandler());
					this.actionPerformed(checkbox);
					return;
				}
			}

			return;
		}

		super.mouseClicked(mouseX, mouseY, mouseButton);
	}

	@Override
	protected void mouseReleased(int mouseX, int mouseY, int state)
	{
		if (this.activeControl != null)
		{
			this.activeControl.mouseReleased(mouseX, mouseY);
			this.activeControl = null;
		}

		super.mouseReleased(mouseX, mouseY, state);
	}

	@Override
	protected void actionPerformed(GuiButton control)
	{
		if (control.id == CUIConfigScreen.DONE_BUTTON_ID)
		{
			this.mc.displayGuiScreen(this.parent);
			return;
		}

		if (control instanceof GuiCheckbox)
		{
			GuiCheckbox checkbox = (GuiCheckbox)control;
			checkbox.checked = !checkbox.checked;
		}

		if (control.id >= CUIConfigScreen.COLOUR_OPTION_BASE_ID && control.id < CUIConfigScreen.COLOUR_OPTION_BASE_ID + ConfiguredColour.values().length)
		{
			ConfiguredColour lineColour = ConfiguredColour.values()[control.id - CUIConfigScreen.COLOUR_OPTION_BASE_ID];
			lineColour.setDefault();

			for (GuiColourButton colourButton : this.colourButtonList)
			{
				colourButton.updateColour(lineColour);
			}
		}
	}

	@Override
	protected void keyTyped(char typedChar, int keyCode)
	{
		if (keyCode == Keyboard.KEY_ESCAPE)
		{
			this.mc.displayGuiScreen(this.parent);
			return;
		}

		for (GuiColourButton colourButton : this.colourButtonList)
		{
			colourButton.keyTyped(typedChar, keyCode);
		}
	}

	@Override
	public void updateScreen()
	{
		super.updateScreen();

		for (GuiColourButton colourButton : this.colourButtonList)
		{
			colourButton.updateCursorCounter();
		}
	}

	@Override
	public void onGuiClosed()
	{
		for (GuiColourButton colourButton : this.colourButtonList)
		{
			colourButton.save();
		}

		CUIConfiguration config = ModWorldEditCUI.INSTANCE.getController().getConfiguration();
		config.setPromiscuous(this.chkPromiscuous.checked);
		config.setAlwaysOnTop(this.chkAlwaysOnTop.checked);
		config.setClearAllOnKey(this.chkClearAll.checked);
		config.save();
	}
}