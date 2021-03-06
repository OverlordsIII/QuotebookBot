package io.github.overlordsiii.event;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.function.Function;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.github.overlordsiii.Main;
import io.github.overlordsiii.config.PropertiesHandler;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.SubscribeEvent;

public class ReactionAddedEvent {

	@SubscribeEvent
	public void onMessageReactionAdd(@Nonnull MessageReactionAddEvent event) {
		Guild guild;

		//checks reaction done in guild
		try {
			guild = event.getGuild();
		} catch (IllegalStateException e) {
			return;
		}

		if (event.getReactionEmote().isEmote() || !event.getReactionEmote().isEmoji()) return;

		PropertiesHandler guildConfig = Main.SERVER_CONFIG_LISTS.get(guild.getId());

		String reactionEmoteUnicode = event.getReactionEmote().getEmoji();

		String configReactionUnicode = guildConfig.getConfigOption("reaction-emoji", Function.identity());

		int communityQuotebookNumber = guildConfig.getConfigOption("community-quotebook-stars-number", Integer::parseInt);

		int hallOfFameNumber = guildConfig.getConfigOption("hall-of-fame-stars-number", Integer::parseInt);

		boolean hallOfFameEnabled = guildConfig.getConfigOption("hall-of-fame-enabled", Boolean::parseBoolean);

		MessageChannel communityQuotebookChannel = guildConfig.getConfigOption("community-quotebook-channel", s -> getChannelById(s, event.getGuild()));

		MessageChannel hallOfFameChannel = guildConfig.getConfigOption("hall-of-fame-channel", s -> getChannelById(s, event.getGuild()));

		if (!reactionEmoteUnicode.equals(configReactionUnicode)) {
			return;
		}

		event.retrieveMessage().queue(message -> {
			if (message == null || !message.getEmbeds().isEmpty()) {
				return;
			}

			if (message.getAuthor().isBot()) {
				return;
			}

			int starNum = Objects.requireNonNull(message.getReactionByUnicode(reactionEmoteUnicode)).getCount();

			// eligible
			if (starNum >= communityQuotebookNumber && communityQuotebookChannel != null) {
				MessageEmbed embed = createEmbed(message, starNum, reactionEmoteUnicode);
				communityQuotebookChannel.getHistory().retrievePast(100).queue(messages -> {
					Message sentMessage = null;
					for (Message message1 : messages) {
						if (!message1.getEmbeds().isEmpty()) {
							MessageEmbed embed1 = message1.getEmbeds().get(0);
							if (embed1.getDescription() != null && doesFieldContainMsgUrl(embed1.getDescription(), message.getJumpUrl())) {
								sentMessage = message1;
							}
						}
					}

					// message has already been sent
					if (sentMessage != null) {
						//edit the original message to update stars
						sentMessage.editMessageEmbeds(embed).queue();
					} else {
						communityQuotebookChannel.sendMessageEmbeds(embed).queue();
					}
				});
			}

			if (starNum >= hallOfFameNumber && hallOfFameEnabled && hallOfFameChannel != null) {
				MessageEmbed embed = createEmbed(message, starNum, reactionEmoteUnicode);
				hallOfFameChannel.getHistory().retrievePast(100).queue(messages -> {
					Message sentMessage = null;
					for (Message message1 : messages) {
						if (!message1.getEmbeds().isEmpty()) {
							MessageEmbed embed1 = message1.getEmbeds().get(0);
							if (embed1.getDescription() != null && doesFieldContainMsgUrl(embed1.getDescription(), message.getJumpUrl())) {
								sentMessage = message1;
							}
						}
					}

					// message has already been sent
					if (sentMessage != null) {
						//edit the original message to update stars
						sentMessage.editMessageEmbeds(embed).queue();
					} else {
						hallOfFameChannel.sendMessageEmbeds(embed).queue();
					}
				});
			}
		});
	}

	private static boolean doesFieldContainMsgUrl(String description, String urlToBeChecked) {
		if (description.isEmpty()) {
			return false;
		}

		return description.contains(urlToBeChecked);
	}

	@Nullable
	private static MessageChannel getChannelById(String s, Guild guild) {
		if (s.isEmpty()) {
			return null;
		}

		try {
			//ensure in the right format
			Long.parseLong(s);
		} catch (NumberFormatException e) {
			return null;
		}

		return guild.getTextChannelById(s);
	}

	private static MessageEmbed createEmbed(Message message, int starNum, String unicode) {
		EmbedBuilder builder = new EmbedBuilder();
		if (!message.getAttachments().isEmpty()) {
			Message.Attachment attachment = message.getAttachments().get(0);
			if (attachment.isImage()) {
				builder.setImage(attachment.getUrl());
			}
		}

		OffsetDateTime msgTime = message.getTimeCreated();

		builder
			.setAuthor(message.getAuthor().getAsTag(), null, message.getAuthor().getAvatarUrl())
			.appendDescription(message.getContentRaw())
			.appendDescription("\n")
			.appendDescription("\n")
			.appendDescription("[Click to jump to message!](" + message.getJumpUrl() + ")")
			.appendDescription("\n")
			.appendDescription(starNum + " " + unicode)
			.setFooter(msgTime.getMonthValue() + "/" + msgTime.getDayOfMonth() + "/" + msgTime.getYear());

		return builder.build();
	}

}
