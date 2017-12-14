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
package me.kgustave.jdagen.autologin.subprocessors;

import me.kgustave.jdagen.util.MethodUtils;

import javax.lang.model.element.*;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

/**
 * @author Kaidan Gustave
 */
@SuppressWarnings("SimplifiableIfStatement")
public abstract class LoginSubProcessor
{
    protected final Elements elements;
    protected final Types types;

    protected LoginSubProcessor(Elements elements, Types types)
    {
        this.elements = elements;
        this.types = types;
    }

    public Element process(Element element)
    {
        if(element.getModifiers().contains(Modifier.STATIC))
            return null;

        if(element instanceof VariableElement && element.getKind() == ElementKind.FIELD)
        {
            return process((VariableElement) element);
        }

        if(element instanceof ExecutableElement && element.getKind() == ElementKind.METHOD)
        {
            return process((ExecutableElement) element);
        }

        return null;
    }

    public Element process(VariableElement element)
    {
        if(element.getModifiers().contains(Modifier.PUBLIC))
            return element;

        for(ExecutableElement exe : ElementFilter.methodsIn(element.getEnclosingElement().getEnclosedElements()))
        {
            if(MethodUtils.isGetterFor(exe, element) && exe.getModifiers().contains(Modifier.PUBLIC))
            {
                return process(exe);
            }
        }

        return null;
    }

    public abstract Element process(ExecutableElement element);
}
