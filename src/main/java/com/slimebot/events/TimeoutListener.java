package com.slimebot.events;

import com.slimebot.main.Main;
import com.slimebot.utils.Config;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.audit.ActionType;
import net.dv8tion.jda.api.audit.AuditLogEntry;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateTimeOutEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.simpleyaml.configuration.file.YamlFile;

import java.io.IOException;
import java.time.Instant;

public class TimeoutListener extends ListenerAdapter {

	@Override
	public void onGuildMemberUpdateTimeOut(GuildMemberUpdateTimeOutEvent event) {
		if(!event.getMember().isTimedOut()) return;

		for(AuditLogEntry entry : event.getGuild().retrieveAuditLogs().type(ActionType.MEMBER_UPDATE)) {

			String reason = entry.getReason();
			if (reason == null){
				reason = "N/A";
			}

			if(entry.getTargetId().equals(event.getMember().getId())) {
				EmbedBuilder embedBuilder = new EmbedBuilder()
						.setTitle("Du wurdest getimeouted")
						.setColor(Main.embedColor(event.getGuild().getId()))
						.setTimestamp(Instant.now())
						.setDescription("Du wurdest auf dem SlimeCloud Discord getimeouted")
						.addField("Grund:", reason, true)
						.addField("Teammitglied:", entry.getUser().getAsMention(), true);

				event.getUser().openPrivateChannel().flatMap(channel -> channel.sendMessageEmbeds(embedBuilder.build())).queue();

				EmbedBuilder embedBuilderLog = new EmbedBuilder()
						.setTitle("\"" + event.getMember().getEffectiveName() + "\"" + " wurde getimeouted")
						.setColor(Main.embedColor(event.getGuild().getId()))
						.setTimestamp(Instant.now())
						.addField("Grund:", reason, true)
						.addField("Wer: ", event.getMember().getAsMention(), true)
						.addField("Teammitglied:", entry.getUser().getAsMention(), true);

				YamlFile config = Config.getConfig(event.getGuild().getId(), "mainConfig");

				try {
					config.load();
				} catch(IOException e) {
					throw new RuntimeException(e);
				}

				event.getGuild().getChannelById(MessageChannel.class, config.getString("punishmentChannelID")).sendMessageEmbeds(embedBuilderLog.build()).queue();

				break;
			}
		}
	}
}
