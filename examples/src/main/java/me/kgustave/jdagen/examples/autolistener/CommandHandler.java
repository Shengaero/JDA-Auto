/*
 * Copyright 2017 Kaidan Gustave
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package me.kgustave.jdagen.examples.autolistener;

import me.kgustave.jdagen.autolistener.AutoListener;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.util.concurrent.TimeUnit;

/**
 * @author Kaidan Gustave
 */
@AutoListener
public class CommandHandler
{
    // This method fires when an message is received
    public void onMessageSend(MessageReceivedEvent event)
    {
        if(event.getMessage().getContentRaw().startsWith("!ping"))
            handlePing(event);
        if(event.getMessage().getContentRaw().startsWith("!remind"))
            handleRemind(event);
    }


    // The methods below are ignored because they are private.

    private void handlePing(MessageReceivedEvent event)
    {
        event.getChannel().sendMessage("Pong!").queue();
    }

    private void handleRemind(MessageReceivedEvent event)
    {
        event.getChannel().sendMessage("I'll remind you in a bit!").queue(message ->
            event.getChannel().sendMessage(event.getAuthor().getAsMention() + " Here's your reminder!")
                 .queueAfter(20, TimeUnit.SECONDS));
    }
}
