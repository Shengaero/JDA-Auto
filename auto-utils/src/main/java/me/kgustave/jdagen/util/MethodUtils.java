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

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;

/**
 * @author Kaidan Gustave
 */
public final class MethodUtils
{
    /**
     * Determines whether the provided {@link javax.lang.model.element.ExecutableElement ExecutableElement}
     * is a getter for the provided {@link javax.lang.model.element.VariableElement VariableElement} on the
     * basis of naming conventions alone, more specifically that given a {@code field} whose name is
     * {@code "foo"}, this method will return {@code true} if and only if the provided {@code method} has a
     * name {@code "getFoo"}.
     *
     * <p>The keyword here is "alone", in it that this utility doesn't check for return type matches, or
     * modifiers. Notably, this method assumes that you have verified that the {@code field} is actually
     * a {@link javax.lang.model.element.ElementKind#FIELD field}, and the {@code method} is actually a
     * {@link javax.lang.model.element.ElementKind#METHOD method}.
     *
     * <p>It's highly recommended you check these other conditions to prevent processor issues.
     *
     * @param  method
     *         The ExecutableElement that will be checked in respect to the {@code field}.
     * @param  field
     *         The VariableElement that this will check the {@code method} for to determine if it's a getter.
     *
     * @return {@code true} if and only if the {@code method} matches getter method
     *         standard naming conventions with respect to the {@code field}.
     */
    public static boolean isGetterFor(ExecutableElement method, VariableElement field)
    {
        String methodName = method.getSimpleName().toString();
        String fieldName = field.getSimpleName().toString();

        System.out.println(methodName);
        System.out.println(fieldName);

        if(!methodName.startsWith("get"))
            return false;

        String methodSuffix = methodName.substring(3);
        String getterSuffix = Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);

        System.out.println(methodSuffix);
        System.out.println(getterSuffix);

        return getterSuffix.equals(methodSuffix);
    }
}
