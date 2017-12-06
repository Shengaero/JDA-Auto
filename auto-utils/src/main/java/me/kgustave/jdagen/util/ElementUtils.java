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
package me.kgustave.jdagen.util;

import javax.annotation.Nullable;
import javax.lang.model.element.*;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Types;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Kaidan Gustave
 */
@SuppressWarnings({"unused","WeakerAccess"})
public final class ElementUtils
{
    @Nullable
    public static ElementKind getKindOrNull(Element toCheck, ElementKind... kinds)
    {
        for(ElementKind kind : kinds)
        {
            if(toCheck.getKind() == kind)
            {
                return kind;
            }
        }
        return null;
    }

    @Nullable
    public static Element findGetter(List<? extends Element> members, Element element, Types typeUtils)
    {
        String name = element.getSimpleName().toString();
        for(Element member : members)
        {
            // Static is not allowed
            if(member.getModifiers().contains(Modifier.STATIC))
                continue;

            // Not a method, cannot be a getter
            if(member.getKind() != ElementKind.METHOD)
                continue;

            // Not named getX
            if(!member.getSimpleName().toString().equals("get"+name.toUpperCase().charAt(0)+name.substring(1)))
                continue;

            ExecutableElement exe = (ExecutableElement) member;

            // Has params
            if((exe.getParameters().size() > 0))
                continue;

            // Type is not the same as return type
            if(!typeUtils.isSameType(element.asType(), exe.getReturnType()))
                continue;

            // Is public
            if(member.getModifiers().contains(Modifier.PUBLIC))
                return member;
        }

        return null;
    }

    public static boolean hasModifier(Element element, Modifier modifier)
    {
        for(Modifier mod : element.getModifiers())
        {
            if(mod.equals(modifier))
            {
                return true;
            }
        }

        return false;
    }

    public static List<ExecutableElement> getConstructors(TypeElement clazz)
    {
        List<ExecutableElement> constructors = new ArrayList<>();

        for(ExecutableElement member : ElementFilter.constructorsIn(clazz.getEnclosedElements()))
        {
            // Only get public constructors
            if(hasModifier(member, Modifier.PUBLIC))
                constructors.add(member);
        }

        return constructors;
    }
}
