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

import com.google.auto.service.AutoService;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;
import me.kgustave.jdagen.ProcessorFrame;
import net.dv8tion.jda.core.events.Event;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeKind;
import javax.lang.model.util.ElementFilter;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * @author Kaidan Gustave
 */
@AutoService(Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedAnnotationTypes("me.kgustave.jdagen.autolistener.AutoListener")
public final class AutoListenerProcessor extends ProcessorFrame
{
    static final String LISTENER_SUFFIX = "Listener";

    public AutoListenerProcessor()
    {
        super(SourceVersion.RELEASE_8);

        supported.add(AutoListener.class);
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv)
    {
        annotations.forEach(type -> {
            if(isAutoListener(type))
            {
                for(Element element : roundEnv.getElementsAnnotatedWith(type))
                {
                    if(element.getKind() != ElementKind.CLASS)
                        continue;
                    processElement((TypeElement) element);
                }
            }
        });
        return false;
    }

    private synchronized void processElement(TypeElement element)
    {
        AutoListenerGenerator generator = new AutoListenerGenerator(element, elements, messager, types);
        AutoListener autoListener = element.getAnnotation(AutoListener.class);

        for(ExecutableElement e : ElementFilter.methodsIn(element.getEnclosedElements()))
        {
            // Never populate private methods
            if(e.getModifiers().contains(Modifier.STATIC) || e.getModifiers().contains(Modifier.PRIVATE))
                continue;

            List<? extends VariableElement> params = e.getParameters();

            // Must have only one param
            if(params.size() != 1) {
                generator.addNonEventMethod(e);
                continue;
            }

            VariableElement param = params.get(0);

            // We check if it's a method, returns void, has a single parameter,
            // that the single parameter is a subtype of Event, and that the method
            // is not annotated with @NoEvent
            if(e.getKind() != ElementKind.METHOD ||
               e.getReturnType().getKind() != TypeKind.VOID ||
               params.size() != 1 ||
               !types.isSubtype(param.asType(), elements.getTypeElement(Event.class.getCanonicalName()).asType()) ||
                e.getAnnotation(NoEvent.class) != null)
            {
                generator.addNonEventMethod(e);
                continue;
            }

            Element paramType = types.asElement(param.asType());
            String packageName = elements.getPackageOf(paramType).getQualifiedName().toString();
            String className = packageName + "." + paramType.getSimpleName().toString();

            // Make sure that this is a valid event
            if(!packageName.startsWith("net.dv8tion.jda")) {
                generator.addNonEventMethod(e);
                messager.printMessage(Diagnostic.Kind.WARNING, "Discovered an event type with a package name other that " +
                                                               "doesn't correspond to the JDA Library packaging! This " +
                                                               "behavior is not allowed! If you must have this behavior " +
                                                               "please apply @NoEvent to "+e.getSimpleName());
                continue;
            }

            try {
                generator.addEventElement(Class.forName(className).asSubclass(Event.class), e);
            } catch(ClassNotFoundException ex) {
                messager.printMessage(Diagnostic.Kind.ERROR, "Could not find Event class for '"+className+"'!");
            } catch(ClassCastException ex) {
                messager.printMessage(Diagnostic.Kind.ERROR, ex.getMessage());
            }
        }

        // Catch any errors to make sure they're reported correctly.
        final TypeSpec spec;
        try {
            spec = generator.build(autoListener.value().isEmpty()? null : autoListener.value());
        } catch(Throwable e) {
            messager.printMessage(Diagnostic.Kind.ERROR, "An error occurred while processing "+
                                                         element.getSimpleName()+": "+e.getMessage(), element);
            return;
        }

        JavaFile.Builder fileBuilder = JavaFile.builder(elements.getPackageOf(element).getQualifiedName().toString(), spec);

        fileBuilder.addFileComment("Generated using JDA-Generators: auto-listener.\n")
                   .addFileComment("This file should not be modified.\n")
                   .addFileComment("Modifications will be removed upon recompilation!");

        JavaFile file = fileBuilder.build();

        try {
            file.writeTo(filer);
        } catch(IOException e) {
            messager.printMessage(Diagnostic.Kind.ERROR, "Could not write '"+file.packageName+"."+spec.name+"' to file!");
        }
    }

    private static boolean isAutoListener(TypeElement element)
    {
        return element.getQualifiedName().toString().equals(AutoListener.class.getCanonicalName())
            || element.getAnnotation(AutoListener.class) != null;
    }
}
