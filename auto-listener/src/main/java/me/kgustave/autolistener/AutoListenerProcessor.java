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
package me.kgustave.autolistener;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.TypeSpec;
import me.kgustave.jdagen.ProcessorFrame;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import java.util.List;
import java.util.Set;

/**
 * @author Kaidan Gustave
 */
@AutoService(Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedAnnotationTypes({"me.kgustave.jdagen.autolistener.Listener",
                           "me.kgustave.jdagen.autolistener.ListenFor"})
public final class AutoListenerProcessor extends ProcessorFrame
{
    static final String LISTENER_SUFFIX = "__Auto_Listener";

    public AutoListenerProcessor()
    {
        super(SourceVersion.RELEASE_8);

        supported.add(AutoListener.class);
        supported.add(ListenFor.class);
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv)
    {
        annotations.forEach(type -> {
            if(isAutoListener(type)) {
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
        TypeSpec.Builder typeBuilder = TypeSpec.classBuilder(element.getSimpleName().toString());

        List<? extends Element> nestedElements = element.getEnclosedElements();

        for(Element nested : nestedElements)
        {
            if(nested.getKind() == ElementKind.FIELD) {
                FieldSpec.Builder fieldBuilder = FieldSpec.builder(nested.getClass(), nested.getSimpleName().toString(),
                    nested.getModifiers().toArray(new Modifier[nested.getModifiers().size()]));

                // Copy annotations
                nested.getAnnotationMirrors().forEach(am -> {
                });
            }
        }
    }

    private synchronized void processNestedField()
    {

    }

    private static boolean isAutoListener(TypeElement element) {
        return element.getQualifiedName().toString().equals(AutoListener.class.getCanonicalName())
            || element.getAnnotation(AutoListener.class) != null;
    }
}
