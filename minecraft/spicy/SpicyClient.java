package spicy;

import java.awt.Font;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;

import com.thealtening.AltService;
import com.thealtening.AltService.EnumAltService;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Session;
import spicy.ClickGUI.Tab;
import spicy.chatCommands.Command;
import spicy.chatCommands.CommandManager;
import spicy.events.Event;
import spicy.events.EventType;
import spicy.events.listeners.EventChatmessage;
import spicy.events.listeners.EventKey;
import spicy.events.listeners.EventPlayerRenderUtilRender;
import spicy.events.listeners.EventRenderGUI;
import spicy.events.listeners.EventUpdate;
import spicy.files.AltInfo;
import spicy.files.Config;
import spicy.files.FileManager;
import spicy.fonts.FontManager;
import spicy.fonts.FontRenderer;
import spicy.modules.Module;
import spicy.modules.Module.Category;
import spicy.modules.player.Timer;
import spicy.modules.render.*;
import spicy.notifications.NotificationManager;
import spicy.ui.HUD;

public class SpicyClient {
	
	public static CopyOnWriteArrayList<Module> modules = new CopyOnWriteArrayList<Module>();
	
	public static HUD hud = new HUD();
	
	public static DiscordRP discord;
	
	public static AltInfo altInfo;
	
	public static Config config;
	
	public static AltService TheAltening = new AltService();
	
	public static CommandManager commandManager = new CommandManager();
	
	public static int originalGuiScale = Minecraft.getMinecraft().gameSettings.guiScale;
	
	public static String originalUsername = "Not Set";
	public static Boolean originalAccountOnline = false;
	
	public static void StartUp() {
		
		if (Minecraft.getMinecraft().getSession().getSessionType().equals(Session.Type.LEGACY)) {
			System.out.println("Not pinging server, this is an offline account");
			System.out.println("Please keep in mind that all this would send is your username and nothing else");
			originalAccountOnline = false;
			originalUsername = Minecraft.getMinecraft().getSession().getUsername();
		}else {
			System.out.println("Pinging the server, this is an online account");
			System.out.println("Please keep in mind that all this sends is your username and nothing else");
			originalAccountOnline = true;
			originalUsername = Minecraft.getMinecraft().getSession().getUsername();
			
			String url = "http://spicyclient.info/api/api.php?username=" + originalUsername + "&stat_type=ping";
		     URL obj = null;
			try {
				obj = new URL(url);
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		     HttpURLConnection con = null;
			try {
				con = (HttpURLConnection) obj.openConnection();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		     //add request header
		     con.setRequestProperty("User-Agent", "Mozilla/5.0");
		     int responseCode = 0;
			try {
				responseCode = con.getResponseCode();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		     System.out.println("\nSending 'GET' request to URL : " + url);
		     System.out.println("Response Code : " + responseCode);
		     BufferedReader in = null;
		     try {
				in = new BufferedReader(
				         new InputStreamReader(con.getInputStream()));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		     String inputLine;
		     StringBuffer response = new StringBuffer();
		     try {
				in.close();
			} catch (NullPointerException e) {
				// TODO: handle exception
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
		// Creates the font manager
		FontManager.getFontManager();
		// Sets the font renderer's font to the default font
		FontRenderer.setCurrentFont(FontManager.getFontManager().getUniFont("opensans"));
		
		// Creates a new config with the default values
		config = new Config("Default");
		loadConfig(config);
		
		Display.setTitle(config.clientName + config.clientVersion);
		
		// Used for thealtening api
		try {
			TheAltening.switchService(EnumAltService.MOJANG);
		} catch (NoSuchFieldException | IllegalAccessException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		// Code to load the AltInfo class
		try {
			altInfo = (AltInfo) FileManager.loadAltInfo(altInfo);
		} catch (IOException e) {
			
			e.printStackTrace();
		}
		if (altInfo == null) {
			
			altInfo = new AltInfo();
			altInfo.addAlt("Puro", "You dont have pawmission", false);
			altInfo.addAlt("Puro@alt.com", "You dont have pawmission", true);
			altInfo.addAlt("Puro@example.com", "You dont have pawmission", true);
			altInfo.addAlt("None of them work but this is how they would look in here", "You dont have pawmission", true);
			
			altInfo.alts.get(3).username = "Here are some sample accounts";
			altInfo.alts.get(2).username = "Puro";
			altInfo.alts.get(1).username = "Puro";
			altInfo.alts.get(0).username = "Puro";
			
			try {
				FileManager.saveAltInfo(altInfo);
			} catch (IOException e) {
				
				e.printStackTrace();
			}
		}
		
		// Start the file manager
		FileManager.init();
		
		// Start the discord rich presence
		discord = new DiscordRP();
		discord.start();
		
		spicy.modules.Module.CategoryList = Arrays.asList(Category.values());
		
		// Sets up the clickGui
		float catOffset = Minecraft.getMinecraft().fontRendererObj.FONT_HEIGHT + 7;
		for (Module.Category c : Module.CategoryList) {

			System.out.println("Setting up the " + c.name + " category for the click gui...");
			Tab temp = new Tab();
			temp.setName(c.name);
			temp.setCategory(c);
			temp.setX(30);
			temp.setY(10 + catOffset);
			temp.setOffsetX(0);
			temp.setOffsetY(0);
			spicy.ClickGUI.ClickGUI.tabs.add(temp);
			catOffset += Minecraft.getMinecraft().fontRendererObj.FONT_HEIGHT + 20;
			System.out.println("The " + c.name + " category has been set up");

		}
		
	}
	
	public static void shutdown() {
		
		discord.shutdown();
		
	}
	
	public static void keypress(int key) {
		
		spicy.SpicyClient.onEvent(new EventKey(key));
		
		for (Module m : modules) {
			if (m.getKey() == key) {
				m.toggle();
			}
		}
		
	}
	
	public static void onEvent(Event e) {
		
		if (Minecraft.getMinecraft().thePlayer == null || Minecraft.getMinecraft().theWorld == null) {
			return;
		}
		
		if (e instanceof EventChatmessage) {
			
			// Will check if the message is a command and if it is a command then will run it
			EventChatmessage chat = (EventChatmessage) e;
			if (chat.message.startsWith(commandManager.prefix)) {
				commandManager.runCommands(chat);
				chat.setCanceled(true);
			}
		}
		
		if (e instanceof EventKey) {
			
			EventKey key = (EventKey) e;
			if (key.key == Keyboard.KEY_PERIOD) {
				Minecraft.getMinecraft().displayGuiScreen(new GuiChat("."));
			}
			
		}
		
		for (Module m : modules) {
			
			//if (!m.toggled)
			//	continue;
			
			if (m.toggled || m instanceof ClickGUI) {
				
				boolean sendTwice = false;
				
				if (e.isPre()) {
					e.setType(EventType.BEFOREPRE);
					sendTwice = true;
				}
				else if (e.isPost()) {
					e.setType(EventType.BEFOREPOST);
					sendTwice = true;
				}
				
				if (sendTwice) {
					m.onEvent(e);
				}
				
				if (e.isBeforePre()) {
					e.setType(EventType.PRE);
				}
				else if (e.isBeforePost()) {
					e.setType(EventType.POST);
				}
				
				m.onEvent(e);
				
			}
			else if (!m.toggled || m instanceof ClickGUI) {
				
				boolean sendTwice = false;
				
				if (e.isPre()) {
					e.setType(EventType.BEFOREPRE);
					sendTwice = true;
				}
				else if (e.isPost()) {
					e.setType(EventType.BEFOREPOST);
					sendTwice = true;
				}
				
				if (sendTwice) {
					m.onEventWhenDisabled(e);
				}
				
				if (e.isBeforePre()) {
					e.setType(EventType.PRE);
				}
				else if (e.isBeforePost()) {
					e.setType(EventType.POST);
				}
				
				m.onEventWhenDisabled(e);
				
			}
			
		}
		
	}
	
	public static void onSettingChange(spicy.settings.SettingChangeEvent e) {
		
		for (Module m : modules) {
			
			m.onSettingChange(e);
			
		}
		
	}
	
	public static void loadConfig(Config c) {
		
		modules.clear();
		
		// Normal modules
		
		modules.add(c.tabgui);
		modules.add(c.clickgui);
		modules.add(c.killaura);
		modules.add(c.fly);
		modules.add(c.sprint);
		modules.add(c.bhop);
		modules.add(c.rainbowgui);
		modules.add(c.fullbright);
		modules.add(c.nofall);
		modules.add(c.keystrokes);
		modules.add(c.fastplace);
		modules.add(c.step);
		modules.add(c.noHead);
		modules.add(c.oldHitting);
		modules.add(c.noSlow);
		modules.add(c.owoifier);
		modules.add(c.chatBypass);
		modules.add(c.safewalk);
		modules.add(c.blockFly);
		modules.add(c.playerESP);
		modules.add(c.antiVoid);
		modules.add(c.longJump);
		modules.add(c.spider);
		modules.add(c.altManager);
		modules.add(c.timer);
		modules.add(c.antiKnockback);
		modules.add(c.back);
		modules.add(c.noClip);
		modules.add(c.blink);
		modules.add(c.autoClicker);
		modules.add(c.fastBreak);
		modules.add(c.inventoryManager);
		modules.add(c.tophat);
		modules.add(c.worldTime);
		modules.add(c.chestStealer);
		modules.add(c.noRotate);
		modules.add(c.skyColor);
		modules.add(c.reach);
		modules.add(c.csgoSpinbot);
		modules.add(c.yawAndPitchSpoof);
		modules.add(c.antibot);
		modules.add(c.pingSpoof);
		modules.add(c.killSults);
		modules.add(c.autoLog);
		modules.add(c.floofyFoxes);
		modules.add(c.jesus);
		modules.add(c.phase);
		modules.add(c.dougDimmadome);
		modules.add(c.criticals);
		modules.add(c.wtap);
		modules.add(c.triggerBot);
		modules.add(c.trail);
		modules.add(c.reachNotify);
		modules.add(c.hideName);
		modules.add(c.discordRichPresence);
		modules.add(c.autoArmor);
		modules.add(c.antiLava);
		modules.add(c.invWalk);
		modules.add(c.mike);
		
		for (Module temp : SpicyClient.modules) {
			
			temp.name = temp.name.replaceAll("\\s+","");
			
		}
		
		for (Module temp : SpicyClient.modules) {
			if (temp.additionalInformation.equalsIgnoreCase(""
					+ "")) {
				temp.additionalInformation = "";
			}
			
		}
		
		
		for (Module temp : SpicyClient.modules) {
			
			temp.ClickGuiExpanded = false;
			temp.expanded = false;
			temp.index = 0;
			
		}
	}
	
	public static List<Module> getModulesByCategory(Category c){
		
		List<Module> modules = new ArrayList<Module>();
		
		for (Module m : spicy.SpicyClient.modules) {
			
			if (m.category == c) {
				
				modules.add(m);
				
			}
			
		}
		
		return modules;
		
	}
	
	public static void setWindowIcons(InputStream in0, InputStream in1) {
		
		Class cls = null;
		try {
			cls = Class.forName("spicy.fonts.FontUtils");
		} catch (ClassNotFoundException e3) {
			e3.printStackTrace();
			return;
		}
		
		/*
		
		From the Minecraft source code
		
		inputstream = this.mcDefaultResourcePack.getInputStreamAssets(new ResourceLocation("icons/icon_16x16.png"));
        inputstream1 = this.mcDefaultResourcePack.getInputStreamAssets(new ResourceLocation("icons/icon_32x32.png"));
        
		 */
		
		InputStream temp0 = in0, temp1 = in1;
		
		System.out.println("Setting the icons...");
		
		in0 = cls.getResourceAsStream("/assets/minecraft/spicy/SpicyClientLogo128x128.png");
		in1 = cls.getResourceAsStream("/assets/minecraft/spicy/SpicyClientLogo256x256.png");
		
		if (in0 == null || in1 == null) {
			System.out.println("Failed to set icons");
			System.out.println("Restoring original icons...");
			
			in0 = temp0;
			in1 = temp1;
			
			System.out.println("Original icons restored");
		}else {
			System.out.println("Icons set");
		}
		
	}
	
}
