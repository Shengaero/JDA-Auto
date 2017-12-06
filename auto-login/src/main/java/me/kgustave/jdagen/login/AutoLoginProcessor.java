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
package me.kgustave.jdagen.login;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.TypeSpec;
import me.kgustave.jdagen.ProcessorFrame;
import me.kgustave.jdagen.login.settings.Token;
import me.kgustave.jdagen.util.ElementUtils;

import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.util.List;
import java.util.Set;

/**
 * @author Kaidan Gustave
 */
@AutoService(Processor.class)
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

        LoginClassFrame frame = new LoginClassFrame(login.value(), baseClass);

        List<? extends Element> members = elements.getAllMembers(baseClass);

        for(Element member : members)
        {
            Token token = member.getAnnotation(Token.class);

            // Member is token marked
            if(token != null && member.getKind().isField())
            {
                // Public field
                if(member.getModifiers().contains(Modifier.PUBLIC))
                {
                    frame.setTokenElement(member);
                }
                else // Private field, look for getter
                {
                    Element getter = ElementUtils.findGetter(members, member, types);
                    if(getter == null)
                    {
                        messager.printMessage(Diagnostic.Kind.ERROR,
                            String.format(
                                "Member of class annotated with @Token (\"%s\") was inaccessible but had no getter!",
                                member.getSimpleName().toString()));
                    }
                    frame.setTokenElement(getter);
                }
            }
        }

        TypeSpec.Builder builder = TypeSpec.classBuilder("JDALogin");
    }
}
