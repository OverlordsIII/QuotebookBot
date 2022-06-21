package io.github.overlordsiii;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import javax.security.auth.login.LoginException;

import io.github.overlordsiii.command.ConfigCommand;
import io.github.overlordsiii.config.PropertiesHandler;
import io.github.overlordsiii.event.JoinGuildConfigEvent;
import io.github.overlordsiii.event.ReactionAddedEvent;
import io.github.overlordsiii.event.UpdateGuildNameEvent;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.hooks.AnnotatedEventManager;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Main {
	public static final Logger LOGGER = LogManager.getLogger("QuotebookBot");

	public static JDA JDA;

	// user should put token in here
	public static final PropertiesHandler TOKEN = PropertiesHandler.builder()
		.setFileName("token.properties")
		.addConfigOption("token", "")
		.build();

	public static final PropertiesHandler CONFIG = PropertiesHandler.builder()
		.setFileName("quotebook-bot.properties")
		.addConfigOption("statusType", OnlineStatus.ONLINE.toString())
		.addConfigOption("activityType", Activity.ActivityType.PLAYING.toString())
		.addConfigOption("activityText", "Quoting People")
		.addConfigOption("createdCommands", "false")
		.build();

	public static final Map<String, PropertiesHandler> SERVER_CONFIG_LISTS = new HashMap<>();

	public static void main(String[] args) throws InterruptedException {
		try {
			JDA = JDABuilder
				.createDefault(TOKEN.getConfigOption("token", Function.identity()))
				.enableIntents(GatewayIntent.GUILD_MESSAGE_REACTIONS)
				.setEventManager(new AnnotatedEventManager())
				// all guild event triggers
				.addEventListeners(new JoinGuildConfigEvent(), new UpdateGuildNameEvent())
				// config slash command
				.addEventListeners(new ConfigCommand())
				// checks when reaction added
				.addEventListeners(new ReactionAddedEvent())
				.setActivity(Activity.of(CONFIG.getConfigOption("activityType", Activity.ActivityType::valueOf), CONFIG.getConfigOption("activityText", Function.identity())))
				.setStatus(CONFIG.getConfigOption("statusType", OnlineStatus::valueOf))
				.build();
		} catch (LoginException e) {
			LOGGER.error("Error while initializing JDA Bridge!");
			e.printStackTrace();
		}
		if (!CONFIG.getConfigOption("createdCommands", Boolean::valueOf)) {
			JDA.awaitReady()
				.updateCommands()
				.addCommands(Commands.slash("reaction-unicode", "Sets the unicode of the reaction for quotebook")
					.addOption(OptionType.STRING, "unicode", "Unicode of the reaction (slash commands don't have)"))
				.addCommands(Commands.slash("community-quotebook-channel", "Sets the channel for community quotebook submissions to be put")
					.addOption(OptionType.CHANNEL, "channel", "Community Quotebook Channel"))
				.addCommands(Commands.slash("hall-of-fame-channel", "Sets the channel for hall of fame quotes to be put")
					.addOption(OptionType.CHANNEL, "channel", "Hall of Fame Channel"))
				.addCommands(Commands.slash("hall-of-fame-stars-number", "Sets reactions needed for Hall of Fame")
					.addOption(OptionType.INTEGER, "number", "Number of reactions needed"))
				.addCommands(Commands.slash("community-quotebook-stars-number", "Sets reactions needed for community quotebook")
					.addOption(OptionType.INTEGER, "number", "Number of reactions needed"))
				.addCommands(Commands.slash("mod-role", "Sets the moderator role so those who have that role can use quotebook commands")
					.addOption(OptionType.ROLE, "role", "Role for Moderator"))
				.addCommands(Commands.slash("hall-of-fame-enabled", "Enables the Hall of Fame channel")
					.addOption(OptionType.BOOLEAN, "enabled", "Whether hall of fame is turned on or off"))
				.queue();

			CONFIG.setConfigOption("createdCommands", "true");
			CONFIG.reload();
		}

		JDA.awaitReady().getGuilds().forEach(guild -> {
			if (!SERVER_CONFIG_LISTS.containsKey(guild.getId())) {

				SERVER_CONFIG_LISTS.put(guild.getId(), PropertiesHandler
					.builder()
					.serverConfig()
					.setFileName(guild.getId() + ".properties")
					//set reaction ID default to star reaction
					.addConfigOption("reaction-unicode", "⭐")
					.addConfigOption("community-quotebook-channel", "")
					.addConfigOption("hall-of-fame-channel", "")
					.addConfigOption("hall-of-fame-enabled", "true")
					.addConfigOption("community-quotebook-stars-number", "3")
					.addConfigOption("hall-of-fame-stars-number", "8")
					.addConfigOption("mod-role", "")
					//for sanity
					.addConfigOption("name", guild.getName())
					.build()
				);
			}
		});



		SERVER_CONFIG_LISTS.values().forEach(System.out::println);
	}

}
