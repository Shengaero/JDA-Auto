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
package me.kgustave.jdagen.autolistener;

import java.lang.annotation.*;

/**
 * Marks a method that might be considered a listener method to avoid targeting when events
 * are fired in the generated {@link net.dv8tion.jda.core.hooks.EventListener EventListener}.
 *
 * <p>An example of this behavior might look like this:
 *
 * <pre><code>
 *     {@link me.kgustave.jdagen.autolistener.AutoListener}
 *     public void MyListener
 *     {
 *         // This method will be fired when a message is received.
 *         public void handleMessages(MessageReceivedEvent event)
 *         {
 *             // code
 *         }
 *
 *         // This method will not be fired when a message is received
 *        {@literal @NoEvent}
 *         public void handleExternalMessages(MessageReceivedEvent event)
 *         {
 *             // code
 *         }
 *     }
 * </code></pre>
 *
 * Note this is not necessary for {@code private} methods or methods with more than one
 * parameter, as only {@code public} methods that return {@code void} with a single parameter
 * whose type is a subclass of {@link net.dv8tion.jda.core.events.Event Event} are eligible
 * to be used as auto-listener methods.
 *
 * @author Kaidan Gustave
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface NoEvent {}
