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
    public synchronized void init(ProcessingEnvironment processingEnv)
    {
        super.init(processingEnv);
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
        messager.printMessage(Diagnostic.Kind.NOTE, String.format("Processing %s", element.getQualifiedName().toString()));
        AutoListenerGenerator generator = new AutoListenerGenerator(element, elements, messager);

        List<ExecutableElement> members = ElementFilter.methodsIn(element.getEnclosedElements());

        for(ExecutableElement ele : members)
        {
            if(ele.getKind() != ElementKind.METHOD)
                continue;
            if(ele.getReturnType().getKind() != TypeKind.VOID)
                continue;
            if(!ele.getModifiers().contains(Modifier.PUBLIC))
                continue;

            List<? extends VariableElement> params = ele.getParameters();

            // Only methods with one parameter
            if(params.size() != 1)
                continue;

            VariableElement param = params.get(0);

            if(!types.isSubtype(param.asType(), elements.getTypeElement(Event.class.getCanonicalName()).asType()))
                continue;

            Element paramType = types.asElement(param.asType());

            String className = elements.getPackageOf(paramType).getQualifiedName().toString()
                               +"."+paramType.getSimpleName().toString();

            try {
                generator.addEventElement(Class.forName(className).asSubclass(Event.class), ele);
            } catch(ClassNotFoundException e) {
                messager.printMessage(Diagnostic.Kind.ERROR, "Could not find Event class for '"+className+"'!");
            } catch(ClassCastException e) {
                messager.printMessage(Diagnostic.Kind.ERROR, e.getMessage());
            }
        }

        TypeSpec spec = generator.build();

        JavaFile.Builder fileBuilder = JavaFile.builder(elements.getPackageOf(element).getQualifiedName().toString(), spec)
                                               .addFileComment("Generated using JDA-Generators: auto-listener.\n")
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
