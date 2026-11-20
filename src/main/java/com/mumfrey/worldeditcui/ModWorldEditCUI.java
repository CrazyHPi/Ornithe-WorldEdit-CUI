package com.mumfrey.worldeditcui;

import io.netty.buffer.Unpooled;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.CPacketCustomPayload;
import net.ornithemc.osl.entrypoints.api.client.ClientModInitializer;
import net.ornithemc.osl.keybinds.api.KeybindEvents;
import net.ornithemc.osl.keybinds.api.KeybindRegistry;
import org.lwjgl.input.Keyboard;

import com.google.common.base.Charsets;
import com.mumfrey.worldeditcui.config.CUIConfiguration;
import com.mumfrey.worldeditcui.event.listeners.CUIListenerChannel;
import com.mumfrey.worldeditcui.event.listeners.CUIListenerWorldRender;

//todo config panel
//import com.mumfrey.worldeditcui.gui.CUIConfigPanel;

/**
 * Main Ornithe mod entry point.
 * Ported to Ornithe.
 *
 * @author CrazyH
 * @author Adam Mummery-Smith
 */
public class ModWorldEditCUI implements ClientModInitializer
{
	public static final Set<String> remotePluginChannels = new HashSet<>();
	public static ModWorldEditCUI INSTANCE;

	private static final int DELAYED_HELO_TICKS = 10;

	public static final String CHANNEL_WECUI = "WECUI";

	private WorldEditCUI controller;
	private WorldClient lastWorld;
	private EntityPlayerSP lastPlayer;

	private KeyBinding keyBindToggleUI = new KeyBinding("wecui.keys.toggle", Keyboard.KEY_NONE, "wecui.keys.category");
	private KeyBinding keyBindClearSel = new KeyBinding("wecui.keys.clear", Keyboard.KEY_NONE, "wecui.keys.category");
	private KeyBinding keyBindChunkBorder = new KeyBinding("wecui.keys.chunk", Keyboard.KEY_NONE, "wecui.keys.category");

	private boolean visible = true;
	private boolean alwaysOnTop = false;

	private CUIListenerWorldRender worldRenderListener;
	private CUIListenerChannel channelListener;

	private int delayedHelo = 0;


	public static void sendCUIPacket(PacketBuffer buffer)
	{
		CPacketCustomPayload packet = new CPacketCustomPayload(CHANNEL_WECUI, buffer);
		Minecraft.getMinecraft().player.connection.sendPacket(packet);
	}

	@Override
	public void initClient()
	{
        INSTANCE = this;

		KeybindEvents.REGISTER_KEYBINDS.register(() -> {
			KeybindRegistry.register(this.keyBindToggleUI);
			KeybindRegistry.register(this.keyBindClearSel);
			KeybindRegistry.register(this.keyBindChunkBorder);
		});
	}

	public void onInitCompleted(Minecraft minecraft)
	{
		this.controller = new WorldEditCUI();
		this.controller.initialise(minecraft);

		this.worldRenderListener = new CUIListenerWorldRender(this.controller, minecraft);
		this.channelListener = new CUIListenerChannel(this.controller);
	}


	public void onJoinGame()
	{
		remotePluginChannels.clear();

		this.visible = true;
		this.controller.getDebugger().debug("Joined game, sending initial handshake");
		this.helo();
	}


	private void helo()
	{
		PacketBuffer buffer = new PacketBuffer(Unpooled.buffer());
		String message = "v|" + WorldEditCUI.PROTOCOL_VERSION;
		buffer.writeBytes(message.getBytes(Charsets.UTF_8));
		sendCUIPacket(buffer);
	}

	// receiveMessage(Message message) is removed
	// it is for Liteloader's internal message API, not used

	public void onCustomPayload(PacketBuffer data)
	{
		try
		{
			int readableBytes = data.readableBytes();
			if (readableBytes > 0)
			{
				byte[] payload = new byte[readableBytes];
				data.readBytes(payload);
				this.channelListener.onMessage(new String(payload, Charsets.UTF_8));
			}
			else
			{
				this.controller.getDebugger().debug("Warning, invalid (zero length) payload received from server");
			}
		}
		catch (Exception ex) {}
	}


	public void onTick(Minecraft mc, boolean inGame, boolean clock)
	{
		CUIConfiguration config = this.controller.getConfiguration();
		
		if (inGame && mc.currentScreen == null)
		{
			
			if (this.keyBindToggleUI.isPressed())
			{
				if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT))
				{
					config.setAlwaysOnTop(!config.isAlwaysOnTop());
				}
				else
				{
					this.visible = !this.visible;
				}
			}
			
			if (this.keyBindClearSel.isPressed())
			{
				if (mc.player != null)
				{
					mc.player.sendChatMessage("//sel");
				}
				
				if (config.isClearAllOnKey())
				{
					this.controller.clearRegions();
				}
			}
			
			if (this.keyBindChunkBorder.isPressed())
			{
				this.controller.toggleChunkBorders();
			}
		}
		
		if (inGame && clock && this.controller != null)
		{
			this.alwaysOnTop = config.isAlwaysOnTop();
				
			if (mc.world != this.lastWorld || mc.player != this.lastPlayer)
			{
				this.lastWorld = mc.world;
				this.lastPlayer = mc.player;
				
				this.controller.getDebugger().debug("World change detected, sending new handshake");
				this.controller.clear();
				this.helo();
				this.delayedHelo = ModWorldEditCUI.DELAYED_HELO_TICKS;
				if (mc.player != null && config.isPromiscuous())
				{
					mc.player.sendChatMessage("/we cui"); //Tricks WE to send the current selection
				}
			}
			
			if (this.delayedHelo > 0)
			{
				this.delayedHelo--;
				if (this.delayedHelo == 0)
				{
					this.helo();
					if (remotePluginChannels.contains(CHANNEL_WECUI) && mc.player != null)
					{
						mc.player.sendChatMessage("/we cui");
					}
				}
			}
		}
	}


	public String getName()
	{
		return "WorldEditCUI";
	}


	//todo config panel
//	@Override
//	public Class<? extends ConfigPanel> getConfigPanelClass()
//	{
//		return CUIConfigPanel.class;
//	}


	public void onPostRenderEntities(float partialTicks)
	{
		if (this.visible && !this.alwaysOnTop)
		{
			RenderHelper.disableStandardItemLighting();
			this.worldRenderListener.onRender(partialTicks);
			RenderHelper.enableStandardItemLighting();
		}
	}


	public void onPostRender(float partialTicks)
	{
		if (this.visible && this.alwaysOnTop)
		{
			this.worldRenderListener.onRender(partialTicks);
		}
	}

	public WorldEditCUI getController()
	{
		return this.controller;
	}
}
