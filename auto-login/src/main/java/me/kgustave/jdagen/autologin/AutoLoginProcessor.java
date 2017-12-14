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

import com.google.auto.service.AutoService;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;
import me.kgustave.jdagen.ProcessorFrame;
import me.kgustave.jdagen.autologin.settings.Token;
import me.kgustave.jdagen.autologin.subprocessors.TokenProcessor;

import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.util.ElementFilter;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author Kaidan Gustave
 */
@SuppressWarnings("unused")
@AutoService(Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedAnnotationTypes("me.kgustave.jdagen.autologin.JDALogin")
public final class AutoLoginProcessor extends ProcessorFrame
{
    private static boolean hasGeneratedLoginClass = false;

    public AutoLoginProcessor()
    {
        super(SourceVersion.RELEASE_8);

        supported.add(JDALogin.class);
    }

    @Override
    @SuppressWarnings("LoopStatementThatDoesntLoop")
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv)
    {
        for(TypeElement type : annotations)
        {
            Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(type);
            if(elements.size() > 1)
                messager.printMessage(Diagnostic.Kind.MANDATORY_WARNING, "Discovered more than one annotated element!");
            for(Element element : elements)
            {
                if(hasGeneratedLoginClass)
                {
                    messager.printMessage(Diagnostic.Kind.MANDATORY_WARNING,
                        "Another class annotated with @JDALogin was discovered! " +
                        "Only one class annotated with this may be present at compile time!");
                    break;
                }
                generateLoginClass((TypeElement) element, roundEnv);
                hasGeneratedLoginClass = true;
                break;
            }

            break;
        }

        return false;
    }

    private void generateLoginClass(TypeElement baseClass, RoundEnvironment roundEnv)
    {
        // Make sure it's a class, and not an interface
        if(baseClass.getKind() != ElementKind.CLASS)
        {
            messager.printMessage(Diagnostic.Kind.ERROR,
                "@JDALogin annotation was attached to a TypeElement that is not a class!");
            return;
        }

        // Make sure this is not private or abstract so we can create an instance
        for(Modifier modifier : baseClass.getModifiers())
        {
            switch(modifier)
            {
                case ABSTRACT:
                    messager.printMessage(Diagnostic.Kind.ERROR,
                        String.format("@JDALogin annotation was attached to a class that is %s!", "abstract"));
                    return;
                case PRIVATE:
                    messager.printMessage(Diagnostic.Kind.ERROR,
                        String.format("@JDALogin annotation was attached to a class that is %s!", "private"));
                    return;
            }
        }

        JDALogin login = baseClass.getAnnotation(JDALogin.class);

        List<Element> members = findRelevantMembers(baseClass);

        LoginClassFrame frame = new LoginClassFrame(login.value(), baseClass);
        TokenProcessor tokenProc = new TokenProcessor(elements, types);

        for(Element member : members)
        {
            processToken(member, frame, tokenProc);
        }

        TypeSpec.Builder builder = TypeSpec.classBuilder("JDALogin");

        frame.buildTypeSpec(builder);

        TypeSpec typeSpec = builder.build();

        JavaFile.Builder fileBuilder = JavaFile.builder(elements.getPackageOf(baseClass)
                                                                .getQualifiedName().toString(), typeSpec);

        fileBuilder.addFileComment("Generated using JDA-Generators: auto-listener.\n")
                   .addFileComment("This file should not be modified.\n")
                   .addFileComment("Modifications will be removed upon recompilation!");

        JavaFile file = fileBuilder.build();

        try {
            file.writeTo(filer);
        } catch(IOException e) {
            messager.printMessage(Diagnostic.Kind.ERROR, "Failed to write "+typeSpec.name+" to file!");
        }
    }

    private void processToken(Element element, LoginClassFrame frame, TokenProcessor tokenProc)
    {
        // Don't set token twice
        if(frame.hasTokenElement())
            return;

        // No @Token annotation
        if(element.getAnnotation(Token.class) == null)
            return;

        Element result = tokenProc.process(element);

        if(result != null && !frame.hasTokenElement())
            frame.setTokenElement(result);
    }

    private static List<Element> findRelevantMembers(TypeElement clazzElement)
    {
        List<Element> relevant = new ArrayList<>();

        relevant.addAll(ElementFilter.fieldsIn(clazzElement.getEnclosedElements()));
        relevant.addAll(ElementFilter.methodsIn(clazzElement.getEnclosedElements()));

        return relevant;
    }
}
