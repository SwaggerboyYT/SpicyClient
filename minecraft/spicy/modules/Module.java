package spicy.modules;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.lwjgl.input.Keyboard;

import net.minecraft.client.Minecraft;
import spicy.SpicyClient;
import spicy.events.Event;
import spicy.settings.BooleanSetting;
import spicy.settings.KeybindSetting;
import spicy.settings.ModeSetting;
import spicy.settings.NumberSetting;
import spicy.settings.Setting;
import spicy.settings.SettingChangeEvent;
import spicy.settings.SettingChangeEvent.type;

public class Module {
	
	public String name;
	public String additionalInformation = "";
	public boolean toggled = false;
	public transient boolean expanded = false;
	public transient boolean ClickGuiExpanded = false;
	public transient int index;
	
	public boolean isToggled() {
		return toggled;
	}

	public KeybindSetting keycode = new KeybindSetting(0);
	public Category category;
	public static Minecraft mc = Minecraft.getMinecraft();
	
	// This was changed because it was causing crashes inside of the click gui
	// public List<Setting> settings = new ArrayList<Setting>();
	
	public transient CopyOnWriteArrayList<Setting> settings = new CopyOnWriteArrayList<Setting>();
	
	public Module(String name, int key, Category category) {
		this.name = name;
		keycode.code = key;
		this.category = category;
		this.addSettings(keycode);
	}
	
	public void addSettings(Setting... settings) {
		
		this.settings.addAll(Arrays.asList(settings));
		this.settings.sort(Comparator.comparing(s -> s == keycode ? 1 : 0));
		
		if (!this.settings.contains(keycode)) {
			this.settings.add(keycode);
			this.settings.sort(Comparator.comparing(s -> s == keycode ? 1 : 0));
		}
		
		for (Setting s : settings) {
			if (s instanceof BooleanSetting) {
				this.onSettingChange(new SettingChangeEvent(type.BOOLEAN, s));
			}
			else if (s instanceof NumberSetting) {
				this.onSettingChange(new SettingChangeEvent(type.NUMBER, s));
			}
			else if (s instanceof ModeSetting) {
				this.onSettingChange(new SettingChangeEvent(type.MODE, s));
			}
			else if (s instanceof KeybindSetting) {
				this.onSettingChange(new SettingChangeEvent(type.KEYBIND, s));
			}
		}
		
	}
	
	public boolean isEnabled() {
		return toggled;
	}
	
	public int getKey() {
		return keycode.code;
	}
	
	public void onEvent(Event e) {
		
	}
	
	public void onSettingChange(SettingChangeEvent e) {
		
	}
	
	public void toggle() {
		toggled = !toggled;
		if (toggled) {
			onEnable();
			
			if (SpicyClient.config.clickgui.sound.isEnabled()) {
				Minecraft.getMinecraft().thePlayer.playSound("random.click", (float) SpicyClient.config.clickgui.volume.getValue(), 0.6f);
			}
			
		}else {
			onDisable();
			
			if (SpicyClient.config.clickgui.sound.isEnabled()) {
				Minecraft.getMinecraft().thePlayer.playSound("random.click", (float) SpicyClient.config.clickgui.volume.getValue(), 0.4f);
			}
			
		}
	}
	
	public void onEnable() {
		
	}
	
	public void onDisable() {
		
	}
	
	public static Object findModule(String name) {
		for (Module m : SpicyClient.modules) {
			
			if (m.name.toLowerCase().equals(name.toLowerCase())) {
				return m;
			}
			
		}
		
		return null;
		
	}
	
	public static String getModuleName(Module m) {
		return m.name.replaceAll("\\s+","");
	}
	
	public void resetSettings() {
		this.settings.clear();
		this.addSettings();
	}
	
	public static List<Category> CategoryList;
	
	public enum Category{
		COMBAT("Combat"),
		MOVEMENT("Movement"),
		PLAYER("Player"),
		RENDER("Render"),
		MEMES("Memes"),
		BETA("Beta Modules"),
		WORLD("World");
		
		public String name;
		
		Category(String name){
			
			this.name = name;
			
		}
	}
	
}
