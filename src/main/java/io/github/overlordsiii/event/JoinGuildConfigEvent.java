package io.github.overlordsiii.event;

import static io.github.overlordsiii.Main.*;
import static io.github.overlordsiii.Main.SERVER_CONFIG_LISTS;

import java.nio.file.Paths;

import javax.annotation.Nonnull;


import io.github.overlordsiii.config.PropertiesHandler;
import io.github.overlordsiii.config.PropertiesHandler;
import net.dv8tion.jda.api.entities.ISnowflake;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.hooks.SubscribeEvent;

public class JoinGuildConfigEvent {

	@SubscribeEvent
	public void onGuildJoin(@Nonnull GuildJoinEvent event) {
		if (!SERVER_CONFIG_LISTS.containsKey(event.getGuild().getId())) {

			SERVER_CONFIG_LISTS.put(event.getGuild().getId(), PropertiesHandler
					.builder()
					.serverConfig()
					.setFileName(event.getGuild().getId())
					.addConfigOption("emoteID", "")
					.addConfigOption("communityQuotebookChannelID", "")
					.addConfigOption("hallOfFameChannelID", "")
					.addConfigOption("communityQuotebookRequiredStars", "3")
					.addConfigOption("hallOfFameRequiredStars", "8")
					//for sanity
					.addConfigOption("name", event.getGuild().getName())
					.build()
			);
		}

		
	}

}
