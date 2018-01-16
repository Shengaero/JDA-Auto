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
import me.kgustave.jdagen.commons.utils.ElementUtils;
import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.hooks.EventListener;

import javax.annotation.Generated;
import javax.annotation.Nullable;
import javax.annotation.processing.Messager;
import javax.lang.model.element.*;
import javax.lang.model.type.NoType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.*;

import static com.squareup.javapoet.TypeName.*;

/**
 * @author Kaidan Gustave
 */
@SuppressWarnings("CodeBlock2Expr")
class AutoListenerGenerator
{
    private final TypeElement original;
    private final Map<Class<? extends Event>, List<Element>> eventMap;
    private final List<ExecutableElement> nonEvents;
    private final Elements elements;
    private final Types types;

    // Provide messager for debugging only.
    @SuppressWarnings({"unused", "FieldCanBeLocal"})
    private final Messager messager;

    AutoListenerGenerator(TypeElement original, Elements elements, Messager messager, Types types)
    {
        this.original = original;
        this.eventMap = new HashMap<>();
        this.elements = elements;
        this.nonEvents = new ArrayList<>();
        this.types = types;

        this.messager = messager;
    }

    void addEventElement(Class<? extends Event> clazz, Element element)
    {
        List<Element> eventList = eventMap.getOrDefault(clazz, new ArrayList<>());

        eventList.add(element);

        eventMap.put(clazz, eventList);
    }

    void addNonEventMethod(ExecutableElement element)
    {
        nonEvents.add(element);
    }

    TypeSpec build(@Nullable String name)
    {
        TypeSpec.Builder builder = TypeSpec.classBuilder(
            name == null? original.getSimpleName().toString() + AutoListenerProcessor.LISTENER_SUFFIX : name
        );

        // Copy annotations, except @AutoListener
        original.getAnnotationMirrors().stream().filter(a -> {
            return !types.isSameType(a.getAnnotationType().asElement().asType(),
                elements.getTypeElement(AutoListener.class.getCanonicalName()).asType());
        }).forEach(annotation -> builder.addAnnotation(AnnotationSpec.get(annotation)));

        // Make public
        builder.addModifiers(Modifier.PUBLIC);

        // Add proper @Generated annotation
        builder.addAnnotation(AnnotationSpec.builder(Generated.class).addMember("value",
            CodeBlock.builder().add("\"me.kgustave.jdagen.autolistener.AutoListenerProcessor\"").build()).build());

        // Implement EventListener
        builder.addSuperinterface(EventListener.class);

        original.getInterfaces().forEach(inter -> builder.addSuperinterface(get(inter)));
        TypeMirror superclass = original.getSuperclass();
        if(superclass.getKind() != TypeKind.NONE && !(superclass instanceof NoType))
        {
            builder.superclass(get(superclass));
        }

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
        builder.addField(get(original.asType()), "instance", Modifier.PRIVATE, Modifier.FINAL);

        // Get constructors
        List<ExecutableElement> constructors = ElementUtils.getConstructors(original);

        // If there are no constructors, we need to add a default
        if(constructors.isEmpty()) {
            CodeBlock.Builder code = CodeBlock
                .builder()
                .addStatement("instance = new $T"+(typeVars.isEmpty() ? "()" : "<>()"), get(original.asType()));

            builder.addMethod(MethodSpec.constructorBuilder()
                                        .addModifiers(Modifier.PUBLIC)
                                        .addCode(code.build()).build());
        } else {
            for(ExecutableElement constructor : constructors)
            {
                MethodSpec.Builder constructorSpec = MethodSpec.constructorBuilder();
                List<? extends VariableElement> params = constructor.getParameters();

                // Reapply annotations
                constructor.getAnnotationMirrors().forEach(annotation ->
                    constructorSpec.addAnnotation(AnnotationSpec.get(annotation)));
                // Rethrow exceptions
                constructor.getThrownTypes().forEach(thrown ->
                    constructorSpec.addException(get(thrown)));

                CodeBlock.Builder code = CodeBlock.builder();

                // Javapoet will generate typeargs for us here
                code.add("instance = new $T(", get(original.asType()));

                for(int i = 0; i < params.size(); i++)
                {
                    VariableElement param = params.get(i);
                    Set<Modifier> modifiers = param.getModifiers();
                    Modifier[] mods = modifiers.toArray(new Modifier[modifiers.size()]);

                    constructorSpec.addParameter(get(param.asType()), param.getSimpleName().toString(), mods);
                    code.add(param.getSimpleName().toString());

                    if(i < params.size() - 1)
                        code.add(", ");
                }
                code.addStatement(")"); // End the statement, semicolon-newline

                // Generate the code
                constructorSpec.addCode(code.build());

                builder.addMethod(constructorSpec.addModifiers(constructor.getModifiers()).build());
            }
        }

        // Copy all public
        for(ExecutableElement nonEventMethod : nonEvents)
        {
            MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(nonEventMethod.getSimpleName().toString());

            List<? extends VariableElement> params = nonEventMethod.getParameters();

            nonEventMethod.getAnnotationMirrors().forEach(annotation ->
                methodBuilder.addAnnotation(AnnotationSpec.get(annotation)));
            nonEventMethod.getTypeParameters().forEach(param ->
                methodBuilder.addTypeVariable(TypeVariableName.get(param)));
            params.forEach(param ->
                methodBuilder.addParameter(ParameterSpec.get(param)));
            nonEventMethod.getThrownTypes().forEach(thrown ->
                methodBuilder.addException(get(thrown)));

            methodBuilder.returns(get(nonEventMethod.getReturnType()));

            CodeBlock.Builder code = CodeBlock.builder();

            // Make sure to only return for non-void methods
            if(nonEventMethod.getReturnType().getKind() != TypeKind.VOID)
                code.add("return ");

            code.add("instance."+nonEventMethod.getSimpleName().toString()+"(");

            for(int i = 0; i < params.size(); i++)
            {
                code.add(params.get(i).getSimpleName().toString());

                if(i < params.size() - 1)
                    code.add(", ");
            }

            methodBuilder.addModifiers(nonEventMethod.getModifiers());
            code.addStatement(")");
            methodBuilder.addCode(code.build());
            builder.addMethod(methodBuilder.build());
        }

        MethodSpec.Builder onEvent = MethodSpec
            .methodBuilder("onEvent")
            .addAnnotation(Override.class)
            .addModifiers(Modifier.PUBLIC)
            .addParameter(get(elements.getTypeElement(Event.class.getCanonicalName()).asType()), "event");

        CodeBlock.Builder block = CodeBlock.builder();

        eventMap.forEach((cla, methods) -> {
            block.add("if(event instanceof $T) {\n", get(elements.getTypeElement(cla.getCanonicalName()).asType()));
            block.indent();

            for(Element method : methods)
                block.addStatement("instance."+method.getSimpleName().toString()+"(("+cla.getSimpleName()+")event)");

            block.unindent();

            block.add("}\n");
        });

        onEvent.addCode(block.build());

        builder.addMethod(onEvent.build());

        return builder.build();
    }
}
