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

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import net.dv8tion.jda.core.AccountType;

import javax.annotation.Generated;
import javax.lang.model.element.*;
import java.util.List;

/**
 * @author Kaidan Gustave
 */
@SuppressWarnings("unused")
public class LoginClassFrame
{
    private final AccountType accountType;
    private final TypeElement base;
    private Element tokenElement = null;

    LoginClassFrame(AccountType accountType, TypeElement base)
    {
        this.accountType = accountType;
        this.base = base;
    }

    public AccountType getAccountType()
    {
        return accountType;
    }

    public Element getTokenElement()
    {
        return tokenElement;
    }

    public void setTokenElement(Element tokenElement)
    {
        if(tokenElement == null)
            throw new IllegalArgumentException("Cannot set token element null!");
        this.tokenElement = tokenElement;
    }

    public boolean hasTokenElement()
    {
        return tokenElement != null;
    }

    public void buildTypeSpec(TypeSpec.Builder builder)
    {
        builder.addAnnotation(AnnotationSpec.builder(Generated.class).addMember("value",
            CodeBlock.builder().add("\"me.kgustave.jdagen.autologin.AutoLoginProcessor\"").build()).build());

        // TODO Code Gen
        builder.addField(String.class, "token", Modifier.PRIVATE, Modifier.FINAL);

        MethodSpec.Builder conBuilder = MethodSpec.constructorBuilder();

        // The constructor should be private
        conBuilder.addModifiers(Modifier.PRIVATE);

        CodeBlock.Builder conCode = CodeBlock.builder();

        conCode.add("$T base = new $T", base, base);
        List<? extends TypeParameterElement> typeParams = base.getTypeParameters();

        if(!typeParams.isEmpty())
        {
            conCode.add("<");
            for(int i = 0; i < typeParams.size(); i++)
            {
                conCode.add("?");
                if(i != typeParams.size() - 1)
                    conCode.add(", ");
            }
            conCode.add(">");
        }

        conCode.addStatement("()"); // TODO Make sure to check for empty constructor

        addInitialized(conCode, "token", tokenElement);

        conBuilder.addCode(conCode.build());
        builder.addMethod(conBuilder.build());

        CodeBlock.Builder staticInit = CodeBlock.builder();
        staticInit.addStatement("JDALogin login = new JDALogin()"); // TODO Make output class configurable
        builder.addStaticBlock(staticInit.build());

        if(tokenElement.getKind() == ElementKind.METHOD)
            conCode.addStatement(tokenElement.getSimpleName().toString()+"()");
        else
            conCode.addStatement(tokenElement.getSimpleName().toString());
    }

    static void addInitialized(CodeBlock.Builder code, String field, Element element)
    {
        if(element == null)
            return;

        if(element.getKind() == ElementKind.FIELD)
            code.addStatement(field+" = base.$N", element.getSimpleName());
        else
            code.addStatement(field+" = base.$N()", element.getSimpleName());
    }
}
