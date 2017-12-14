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
 * Marks a {@code class} to generate a companion source file that handles and redirects events
 * properly for it.
 *
 * <p>Generated files implement {@link net.dv8tion.jda.core.hooks.EventListener EventListener}.
 * Methods are also automatically targeted based on a set of properties and traits similar
 * to methods found in {@link net.dv8tion.jda.core.hooks.ListenerAdapter ListenerAdapter}:
 *
 * <ul>
 *     <li>The method must be {@code public}.</li>
 *     <li>The method must return {@code void}.</li>
 *     <li>The method must have a single parameter that is a
 *     subclass of {@link net.dv8tion.jda.core.events.Event Event}.</li>
 *     <li>The method is not marked with a  {@link NoEvent @NoEvent}
 *     annotation.</li>
 * </ul>
 *
 * <b>Note:</b> The generated source file will have a name in the format of {@code XListener}
 * where {@code X} is the name of the class that has this annotation applied, unless
 * {@link AutoListener#value()} is specified.
 *
 * @since  1.0
 * @author Kaidan Gustave
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface AutoListener
{
    /**
     * Target name of the generated source.
     * <br>Developers may choose to implement this personally to avoid
     * naming conflicts with previously existing or future resources/classes.
     * <br>If left unset or provided blank, the generated class will default
     * to {@code XListener} where {@code X} is the name of the class this
     * annotation is applied to.
     *
     * @return The generated class name, or blank if it's default.
     */
    String value() default "";
}
