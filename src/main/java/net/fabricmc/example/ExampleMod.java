package net.fabricmc.example;

import net.fabricmc.api.ModInitializer;
import net.minecraft.MinecraftVersion;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ExampleMod implements ModInitializer {
	//@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		System.out.println("Hello Fabric world!");
		LinkedHashMap<String, String> properties = new LinkedHashMap<>();
		Registry.BLOCK.forEach(block -> {
			Identifier blockId = Registry.BLOCK.getId(block);
			properties.put(blockId.toString(), "1,0");
			block.getStateManager().getStates().forEach(blockState -> {
				Set<String> stateProperties = Collections.newSetFromMap(new TreeMap<>());
				blockState.getProperties().forEach(property ->
						stateProperties.add(property.getName() + ':' + blockState.get(property).toString())
				);
				if (stateProperties.size() > 0) {
					properties.put(blockId.toString() + ';' + String.join(";", stateProperties), "1,0");
				}
			});
		});

		writeProperties(properties, "block-states.properties");

		properties.clear();
		Registry.ITEM.forEach(item -> {
			Identifier itemId = Registry.ITEM.getId(item);
			properties.put(itemId.toString(), "1,~");
		});

		writeProperties(properties, "items.properties");

		properties.clear();
		Registry.ENTITY_TYPE.forEach(entityType -> {
			Identifier entityId = Registry.ENTITY_TYPE.getId(entityType);
			properties.put(entityId.toString(), "1");
		});

		writeProperties(properties, "entity-ids.properties");

		properties.clear();
		Registry.STATUS_EFFECT.forEach(statusEffect -> {
			Identifier effectId = Objects.requireNonNull(Registry.STATUS_EFFECT.getId(statusEffect));
			properties.put(effectId.toString(), "1");
		});
		writeProperties(properties, "status-effect-ids.properties");

		properties.clear();
		Registry.STATUS_EFFECT.forEach(statusEffect -> {
			int effectId = Registry.STATUS_EFFECT.getRawId(statusEffect);
			properties.put(Integer.toString(effectId),
					Objects.requireNonNull(Registry.STATUS_EFFECT.getId(statusEffect))
							.toString().replaceAll("^minecraft:", "")
			);
		});
		writeProperties(properties, "status-effect-java-ids.properties");


		properties.clear();
		Registry.ENTITY_TYPE.forEach(entityType -> {
			Identifier id = Registry.ENTITY_TYPE.getId(entityType);
			String key = id.toString();
			String[] parts = key.replace("minecraft:", "").split("_");
			for (int i = 0; i < parts.length; i++) {
				parts[i] = Character.toUpperCase(parts[i].charAt(0)) + parts[i].substring(1);
			}
			properties.put(key, String.join("", parts));
		});
		writeProperties(properties, "entities.properties");
		
		properties.clear();
		Registry.ENCHANTMENT.forEach(enchantment -> {
			Identifier id = Objects.requireNonNull(Registry.ENCHANTMENT.getId(enchantment));
			properties.put(id.toString(), "1");
		});
		writeProperties(properties, "enchantments.properties");

		properties.clear();
		Registry.BIOME.forEach(biome -> {
			int rawId = Registry.BIOME.getRawId(biome);
			Identifier id = Objects.requireNonNull(Registry.BIOME.getId(biome));
			properties.put(rawId+"-"+id.toString().replace("minecraft:", ""), "0");
		});
		writeProperties(properties, "biomes.properties");
		System.exit(0);
	}

	private void writeProperties(LinkedHashMap<String, String> properties, String fileName) {
		try (FileWriter fw = new FileWriter(fileName);
			 BufferedWriter writer = new BufferedWriter(fw) ) {
			writer.write("# Generated on Minecraft "
							+ MinecraftVersion.create().getName()
							+ " at "
							+ new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z").format(new Date()));
			writer.newLine();
			properties.forEach( (k, v) -> {
				try {
					writer.write(k.replace(':', '-').replaceFirst("^minecraft-", ""));
					writer.write('=');
					writer.write(v);
					writer.newLine();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			});
			writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
