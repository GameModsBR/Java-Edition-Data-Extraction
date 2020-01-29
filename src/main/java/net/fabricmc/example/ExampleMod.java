package net.fabricmc.example;

import com.google.common.collect.ImmutableList;
import net.fabricmc.api.ModInitializer;
import net.minecraft.MinecraftVersion;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.state.property.Property;
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
	private void simpleStates() {
		HumanStringComparator humanStringComparator = new HumanStringComparator();
		SortedMap<String, SortedMap<String, SortedSet<String>>> states = new TreeMap<>(humanStringComparator);
		Registry.BLOCK.forEach(block -> {
			Identifier blockId = Registry.BLOCK.getId(block);
			ImmutableList<BlockState> mcStates = block.getStateManager().getStates();
			String name = blockId.toString();
			if (mcStates.isEmpty()) {
				states.put(name, Collections.emptySortedMap());
			} else {
				SortedMap<String, SortedSet<String>> registeredProperties = states.computeIfAbsent(name, k-> new TreeMap<>(humanStringComparator));
				for (BlockState mcState : mcStates) {
					for (Property<?> property : mcState.getProperties()) {
						SortedSet<String> registeredValues = registeredProperties.computeIfAbsent(property.getName().toLowerCase(), k -> new TreeSet<>(humanStringComparator));
						registeredValues.add(mcState.get(property).toString().toLowerCase());
					}
				}
			}
		});

		try(FileWriter fw = new FileWriter("block-states.ini"); BufferedWriter buffered = new BufferedWriter(fw)) {
			for (Map.Entry<String, SortedMap<String, SortedSet<String>>> topLevelEntry : states.entrySet()) {
				buffered.write("["+topLevelEntry.getKey()+"]");
				buffered.newLine();
				for (Map.Entry<String, SortedSet<String>> propertyEntry : topLevelEntry.getValue().entrySet()) {
					buffered.write(propertyEntry.getKey());
					buffered.write('=');
					buffered.write(String.join(",", propertyEntry.getValue()));
					buffered.newLine();
				}
				buffered.newLine();
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	//@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		System.out.println("Hello Fabric world!");

		simpleStates();

		SortedSet<String> simpleBlocks = new TreeSet<>();
		SortedSet<String> blockStates = new TreeSet<>();
		LinkedHashMap<String, String> properties = new LinkedHashMap<>();
		Registry.BLOCK.forEach(block -> {
			Identifier blockId = Registry.BLOCK.getId(block);
			properties.put(blockId.toString(), "1,0");
			BlockState defaultState = block.getDefaultState();
			ImmutableList<BlockState> states = block.getStateManager().getStates();
			if (states.size() == 1) {
				blockStates.add(blockId.toString());
				simpleBlocks.add(blockId.toString());
			} else {
				states.forEach(blockState -> {
					Set<String> stateProperties = new TreeSet<>();
					blockState.getProperties().forEach(property ->
							stateProperties.add(property.getName() + ':' + blockState.get(property).toString())
					);
					String key = blockId.toString() + ';' + String.join(";", stateProperties);
					properties.put(key, "1,0");
					blockStates.add(key + (defaultState.equals(blockState)? ";!" : ""));
				});
			}
		});

		writeProperties(properties, "block-states.properties");

		try (FileWriter fw = new FileWriter("block-states.txt"); BufferedWriter writer = new BufferedWriter(fw)) {
			for (String blockState : blockStates) {
				writer.write(blockState);
				writer.newLine();
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		try (FileWriter fw = new FileWriter("simple-blocks.txt"); BufferedWriter writer = new BufferedWriter(fw)) {
			for (String blockState : simpleBlocks) {
				writer.write(blockState);
				writer.newLine();
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

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
