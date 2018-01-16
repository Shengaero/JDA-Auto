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
package me.kgustave.jdagen.autologin;

import net.dv8tion.jda.core.AccountType;

import java.lang.annotation.*;

/**
 * @author Kaidan Gustave
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface JDALogin
{
    AccountType type();

    Mode buildMode();

    String loginClassName() default "JDALogin";

    boolean instanceAccessor() default false;

    enum Mode
    {
        /**
         * Constant that causes JDALogin to generate building a {@link net.dv8tion.jda.core.JDA JDA}
         * instance <b>{@link net.dv8tion.jda.core.JDABuilder#buildBlocking() synchronously}</b>.
         */
        BLOCKING,

        /**
         * Constant that causes JDALogin to generate building a {@link net.dv8tion.jda.core.JDA JDA}
         * instance <b>{@link net.dv8tion.jda.core.JDABuilder#buildAsync() asynchronously}</b>.
         */
        ASYNC
    }

    /**
     * Marks a method to be used as the "main" method for the bot.
     * <br>This method must have one of the following sets of parameters:
     * <ul>
     *     <li>{@link net.dv8tion.jda.core.JDA JDA}</li>
     *     <li>{@code JDA}, {@code String[]}</li>
     *     <li>{@code JDA}, {@code String...}</li>
     * </ul>
     */
    @Documented
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.SOURCE)
    @interface Main { }
}
