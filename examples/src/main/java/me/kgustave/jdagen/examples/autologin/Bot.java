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
package me.kgustave.jdagen.examples.autologin;

import me.kgustave.jdagen.autologin.JDALogin;
import me.kgustave.jdagen.autologin.settings.Token;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;

/**
 * @author Kaidan Gustave
 */
@JDALogin(type = AccountType.BOT, buildMode = JDALogin.Mode.ASYNC, loginClassName = "BotLogin")
public class Bot
{
    @Token private final String token;

    public Bot()
    {
        token = "token";
    }

    public String getToken()
    {
        return token;
    }

    @JDALogin.Main
    public void launch(JDA jda)
    {

    }
}
