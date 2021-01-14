package de.jeff_media.AngelChest;

import java.util.ArrayList;
import java.util.Scanner;
import java.util.UUID;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.util.Vector;

public class Hologram {

	final ArrayList<ArmorStand> armorStands;
	final ArrayList<UUID> armorStandUUIDs;
	final String text;
	final double lineOffset = -0.2D;
	double currentOffset = 0.0D;
	boolean usePapi = false;

	public Hologram(Block block, String text,Main main, AngelChest chest) {
		this(block.getLocation().add(new Vector(0.5,-0.5+main.getConfig().getDouble(Config.HOLOGRAM_OFFSET),0.5)),block,text,main,chest);
	}

	public void update(AngelChest chest) {
		Scanner scanner = new Scanner(text);
		int lineNumber = 0;
		while (scanner.hasNextLine()) {

			String line = scanner.nextLine();
			line = line.replaceAll("\\{time}", AngelChestCommandUtils.getTimeLeft(chest));
			if (line.equals("")) continue;

			if(usePapi) {
				line = PlaceholderAPI.setPlaceholders(Bukkit.getOfflinePlayer(chest.owner),line);
			}

			if(armorStands.get(lineNumber) !=null) {
				armorStands.get(lineNumber).setCustomName(line);
			} /*else if(armorStandUUIDs.get(lineNumber) != null) {

			}*/

			lineNumber++;
		}
	}

	public Hologram(Location location, Block block, String text, Main main, AngelChest chest) {

		if(Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
			usePapi = true;
		}

		main.debug("Creating hologram with text " + text + " at "+location.toString());
		this.text = text;

		armorStands = new ArrayList<>();
		armorStandUUIDs = new ArrayList<>();

		Scanner scanner = new Scanner(text);
		while (scanner.hasNextLine()) {
			String line = scanner.nextLine();
			line = line.replaceAll("\\{time}",AngelChestCommandUtils.getTimeLeft(chest));
			if(line.equals("")) continue;

			//plugin.hookUtils.hologramToBeSpawned=true;

			ArmorStand as = (ArmorStand) location.getWorld().spawnEntity(location.add(new Vector(0,lineOffset,0)), EntityType.ARMOR_STAND); // Spawn the ArmorStand
			armorStandUUIDs.add(as.getUniqueId());

			as.setGravity(false);
			as.setCanPickupItems(false);
			as.setCustomName(line);
			as.setCustomNameVisible(true);
			as.setVisible(false);

			armorStands.add(as);
			
			main.blockArmorStandCombinations.add(new BlockArmorStandCombination(block,as));
			
			currentOffset += lineOffset;

			//plugin.hookUtils.hologramToBeSpawned=false;

		}
		scanner.close();
	}
	
	public void destroy() {
		for(ArmorStand armorStand : armorStands.toArray(new ArmorStand[0])) {
			//System.out.println("DESTROYING ARMOR STAND @ " + armorStand.getLocation().toString());
			if(armorStand!=null) armorStand.remove();
			
			armorStands.remove(armorStand);

			
		}
	}

}