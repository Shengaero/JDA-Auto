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
package me.kgustave.jdagen;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Kaidan Gustave
 */
@SuppressWarnings({"FieldCanBeLocal", "unused", "WeakerAccess"})
public abstract class ProcessorFrame extends AbstractProcessor
{
    protected Types types;
    protected Elements elements;
    protected Filer filer;
    protected Messager messager;

    protected final SourceVersion version;
    protected final Set<Class<? extends Annotation>> supported;

    protected ProcessorFrame(SourceVersion version)
    {
        this.version = version;
        this.supported = new HashSet<>();
    }

    @Override
    public SourceVersion getSupportedSourceVersion()
    {
        return version;
    }

    @Override
    public Set<String> getSupportedAnnotationTypes()
    {
        return supported.stream().map(Class::getCanonicalName).collect(Collectors.toSet());
    }

    /**
     * Initializer method for the {@link javax.annotation.processing.ProcessingEnvironment ProcessingEnvironment}.
     *
     * <p>Overriding methods should call {@code super} to prevent exceptions from being thrown.
     *
     * @param  processingEnv
     *         The Processing environment.
     */
    @Override
    public synchronized void init(ProcessingEnvironment processingEnv)
    {
        this.types = processingEnv.getTypeUtils();
        this.elements = processingEnv.getElementUtils();
        this.filer = processingEnv.getFiler();
        this.messager = processingEnv.getMessager();
    }
}
