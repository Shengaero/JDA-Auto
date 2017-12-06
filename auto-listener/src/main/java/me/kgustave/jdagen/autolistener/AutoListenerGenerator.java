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

import com.squareup.javapoet.*;
import me.kgustave.jdagen.util.ElementUtils;
import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.hooks.EventListener;

import javax.annotation.Generated;
import javax.annotation.processing.Messager;
import javax.lang.model.element.*;
import javax.lang.model.util.Elements;
import java.util.*;

/**
 * @author Kaidan Gustave
 */
class AutoListenerGenerator
{
    private final TypeElement original;
    private final Map<Class<? extends Event>, List<Element>> eventMap;
    private final Elements elements;

    // Provide messager for debugging only.
    @SuppressWarnings({"unused", "FieldCanBeLocal"})
    private final Messager messager;

    AutoListenerGenerator(TypeElement original, Elements elements, Messager messager)
    {
        this.original = original;
        this.eventMap = new HashMap<>();
        this.elements = elements;
        this.messager = messager;
    }

    public void addEventElement(Class<? extends Event> clazz, Element element)
    {
        List<Element> eventList = eventMap.getOrDefault(clazz, new ArrayList<>());

        eventList.add(element);

        eventMap.put(clazz, eventList);
    }

    public TypeSpec build()
    {
        TypeSpec.Builder builder = TypeSpec.classBuilder(
            original.getSimpleName().toString()+AutoListenerProcessor.LISTENER_SUFFIX);

        // Make public
        builder.addModifiers(Modifier.PUBLIC);

        // Add proper @Generated annotation
        builder.addAnnotation(AnnotationSpec.builder(Generated.class).addMember(
            "value", CodeBlock.builder().add("\"me.kgustave.jdagen.autolistener.AutoListenerProcessor\"").build())
                                            .build());

        // Implement EventListener
        builder.addSuperinterface(EventListener.class);

        // We will need these later if there are any because the
        // original listener class may require type arguments
        List<TypeVariableName> typeVars = new ArrayList<>();

        // Copy Type Parameters
        for(TypeParameterElement type : original.getTypeParameters())
        {
            TypeVariableName var = TypeVariableName.get(type);
            typeVars.add(var);
            builder.addTypeVariable(var);
        }

        // Instance field for the original
        builder.addField(TypeName.get(original.asType()), "instance", Modifier.PRIVATE, Modifier.FINAL);

        // Get constructors
        List<ExecutableElement> constructors = ElementUtils.getConstructors(original);

        // If there are no constructors, we need to add a default
        if(constructors.isEmpty()) {
            CodeBlock.Builder code = CodeBlock
                .builder().addStatement("instance = new $T"+(typeVars.isEmpty() ? "()" : "<>()"),
                    TypeName.get(original.asType()));

            builder.addMethod(MethodSpec.constructorBuilder()
                                        .addModifiers(Modifier.PUBLIC)
                                        .addCode(code.build()).build());
        } else {
            for(ExecutableElement constructor : constructors)
            {
                MethodSpec.Builder constructorSpec = MethodSpec.constructorBuilder();
                List<? extends VariableElement> params = constructor.getParameters();

                CodeBlock.Builder code = CodeBlock.builder();

                code.add("instance = new $T(", TypeName.get(original.asType()));

                for(int i = 0; i < params.size(); i++)
                {
                    VariableElement param = params.get(i);

                    TypeName paramTypeName = TypeName.get(param.asType());

                    Set<Modifier> modifiers = param.getModifiers();

                    Modifier[] mods = modifiers.toArray(new Modifier[modifiers.size()]);

                    constructorSpec.addParameter(paramTypeName, param.getSimpleName().toString(), mods);

                    code.add(param.getSimpleName().toString());

                    if(i < params.size() - 1)
                        code.add(", ");
                }

                code.add(");\n");

                constructorSpec.addCode(code.build());

                builder.addMethod(constructorSpec.addModifiers(Modifier.PUBLIC).build());
            }
        }

        MethodSpec.Builder onEvent = MethodSpec
            .methodBuilder("onEvent")
            .addAnnotation(Override.class)
            .addModifiers(Modifier.PUBLIC)
            .addParameter(TypeName.get(elements.getTypeElement(Event.class.getCanonicalName()).asType()), "event");

        CodeBlock.Builder block = CodeBlock.builder();

        eventMap.forEach((cla, methods) -> {
            block.add("if(event instanceof $T) {\n",
                TypeName.get(elements.getTypeElement(cla.getCanonicalName()).asType()));
            block.indent();

            for(Element method : methods)
            {
                block.addStatement("instance."+method.getSimpleName().toString()+"(("+cla.getSimpleName()+")event)");
            }

            block.unindent();

            block.add("}\n");
        });

        onEvent.addCode(block.build());

        builder.addMethod(onEvent.build());

        return builder.build();
    }
}
