package spicy;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;

import com.thealtening.AltService;
import com.thealtening.AltService.EnumAltService;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Session;
import spicy.ClickGUI.Tab;
import spicy.chatCommands.Command;
import spicy.chatCommands.CommandManager;
import spicy.events.Event;
import spicy.events.EventType;
import spicy.events.listeners.EventChatmessage;
import spicy.events.listeners.EventKey;
import spicy.events.listeners.EventMotion;
import spicy.files.AltInfo;
import spicy.files.Config;
import spicy.files.FileManager;
import spicy.modules.HudModule;
import spicy.modules.Module;
import spicy.modules.Module.Category;
import spicy.modules.combat.*;
import spicy.modules.memes.*;
import spicy.modules.movement.*;
import spicy.modules.player.*;
import spicy.modules.render.*;
import spicy.modules.world.*;
import spicy.ui.HUD;
import spicy.ui.NewAltManager;
import spicy.ui.customOpenGLWidgets.TextBox;
public class SpicyClient {
	
	public static CopyOnWriteArrayList<Module> modules = new CopyOnWriteArrayList<Module>();
	public static CopyOnWriteArrayList<HudModule> hudModules = new CopyOnWriteArrayList<HudModule>();
	
	public static HUD hud = new HUD();
	
	public static DiscordRP discord;
	
	public static AltInfo altInfo;
	
	public static Config config;
	
	public static ArrayList<Module> moduleObjectList = new ArrayList<Module>();
	
	public static AltService TheAltening = new AltService();
	
	public static CommandManager commandManager = new CommandManager();
	
	public static void StartUp() {
		
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
			temp.setX(Display.getWidth() / 5 - 50);
			temp.setY((Display.getHeight() / 5) + catOffset);
			temp.setOffsetX(0);
			temp.setOffsetY(0);
			spicy.ClickGUI.ClickGUI.tabs.add(temp);
			catOffset += Minecraft.getMinecraft().fontRendererObj.FONT_HEIGHT + 7;
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
		
		for (Module m : modules) {
			
			if (e instanceof EventChatmessage) {
				
				// Will check if the message is a command and if it is a command then will run it
				EventChatmessage chat = (EventChatmessage) e;
				if (chat.message.startsWith(commandManager.prefix)) {
					commandManager.runCommands(chat);
					chat.setCanceled(true);
				}
			}
			
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
			
		}
		
		for (HudModule m : hudModules) {
			
			if (m.toggled) {
				m.onEvent(e);
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
		
		// Hud modules
		
		hudModules.add(c.armorHud);
		
		for (Module temp : SpicyClient.modules) {
			
			temp.name = temp.name.replaceAll("\\s+","");
			
		}
		
		for (Module temp : SpicyClient.modules) {
			if (temp.additionalInformation.equalsIgnoreCase(""
					+ "")) {
				temp.additionalInformation = "";
			}
			
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
	
}
