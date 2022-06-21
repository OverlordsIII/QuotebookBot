package io.github.overlordsiii.command;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;

import javax.annotation.Nonnull;

import io.github.overlordsiii.Main;
import io.github.overlordsiii.config.PropertiesHandler;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.SubscribeEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

public class ConfigCommand {

	private static final Map<String, String> KEY_TO_OPTION = fillMap(new HashMap<>(), map -> {
		map.put("reaction-unicode", "unicode");
		map.put("community-quotebook-channel", "channel");
		map.put("hall-of-fame-channel", "channel");
		map.put("hall-of-fame-stars-number", "number");
		map.put("community-quotebook-stars-number", "number");
		map.put("mod-role", "role");
		map.put("hall-of-fame-enabled", "enabled");
	});

	@SubscribeEvent
	public void onSlashCommandInteraction(@Nonnull SlashCommandInteractionEvent event) {
		if (event.getGuild() == null) {
			event.reply("You must run this slash command in a guild.").queue();
			return;
		}

		PropertiesHandler guildConfig = Main.SERVER_CONFIG_LISTS.get(event.getGuild().getId());

		String configKey = event.getName();

		String configKeyLower = configKey.toLowerCase(Locale.ROOT);

		OptionMapping mapping = event.getInteraction().getOption(KEY_TO_OPTION.get(configKey));

		if (mapping == null) {
			event.reply("Please run the `" + configKey + "` command with the appropriate option").queue();
			return;
		}

		// no mod role yet
		if (guildConfig.getConfigOption("mod-role", String::isEmpty)) {
			if (!event.getMember().hasPermission(Permission.ADMINISTRATOR)) {
				event.reply("You will need an admin to set the mod-role command (`/mod-role`) in order to use this command.").queue();
				return;
			}
		} else {
			Role modRole = guildConfig.getConfigOption("mod-role", event.getGuild()::getRoleById);
			if (!event.getMember().getRoles().contains(modRole)) {
				event.reply("You do not have the " + modRole.getAsMention() + " role that is needed to run this command.").queue();
				return;
			}
		}

		//long config
		if (configKeyLower.contains("id")) {
			long idValue = mapping.getAsLong();
			guildConfig.setConfigOption(configKey, String.valueOf(idValue));
			guildConfig.reload();
			Main.SERVER_CONFIG_LISTS.replace(event.getGuild().getId(), guildConfig);
			event.reply("Set config value `" + configKey + "` to `" + idValue + "`").queue();
		} else if (configKeyLower.contains("channel")) {
			MessageChannel channelValue = mapping.getAsMessageChannel();
			if (channelValue != null) {
				guildConfig.setConfigOption(configKey, channelValue.getId());
				guildConfig.reload();
				Main.SERVER_CONFIG_LISTS.replace(event.getGuild().getId(), guildConfig);
				event.reply("Set config value `" + configKey + "` to channel " + channelValue.getAsMention()).queue();
			} else {
				event.reply("Please run the `" + configKey + "` command with the appropriate channel option").queue();
			}
		} else if (configKeyLower.contains("number")) {
			int idValue = mapping.getAsInt();
			guildConfig.setConfigOption(configKey, String.valueOf(idValue));
			guildConfig.reload();
			Main.SERVER_CONFIG_LISTS.replace(event.getGuild().getId(), guildConfig);
			event.reply("Set config value `" + configKey + "` to `" + idValue + "`").queue();
		} else if (configKeyLower.contains("enable")) {
			boolean idValue = mapping.getAsBoolean();
			guildConfig.setConfigOption(configKey, String.valueOf(idValue));
			guildConfig.reload();
			Main.SERVER_CONFIG_LISTS.replace(event.getGuild().getId(), guildConfig);
			event.reply("Set config value `" + configKey + "` to `" + idValue + "`").queue();
		} else if (configKeyLower.contains("role")) {
			Role roleValue = mapping.getAsRole();
			guildConfig.setConfigOption(configKey, roleValue.getId());
			guildConfig.reload();
			Main.SERVER_CONFIG_LISTS.replace(event.getGuild().getId(), guildConfig);
			event.reply("Set config value `" + configKey + "` to the role `" + roleValue.getName() + "`").queue();
		} else {
			String configValue = mapping.getAsString();
			guildConfig.setConfigOption(configKey, configValue);
			guildConfig.reload();
			Main.SERVER_CONFIG_LISTS.replace(event.getGuild().getId(), guildConfig);
			event.reply("Set config value `" + configKey + "` to `" + configValue + "`").queue();
		}
	}

	private static <T> T fillMap(T object, Consumer<T> initializer) {
		initializer.accept(object);
		return object;
	}

}
