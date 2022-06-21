package io.github.overlordsiii.event;

import javax.annotation.Nonnull;


import io.github.overlordsiii.Main;
import io.github.overlordsiii.config.PropertiesHandler;
import net.dv8tion.jda.api.events.guild.update.GuildUpdateNameEvent;
import net.dv8tion.jda.api.hooks.SubscribeEvent;

public class UpdateGuildNameEvent {

	@SubscribeEvent
	public void onGuildUpdateName(@Nonnull GuildUpdateNameEvent event){
		String name = event.getNewName();

		PropertiesHandler guildConfig = Main.SERVER_CONFIG_LISTS.get(event.getEntity().getId());

		guildConfig.setConfigOption("name", name);

		guildConfig.reload();

	}

}
