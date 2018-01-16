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

import com.squareup.javapoet.*;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.exceptions.RateLimitedException;

import javax.annotation.Generated;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeKind;
import javax.security.auth.login.LoginException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Kaidan Gustave
 */
@SuppressWarnings("unused")
public class LoginClassFrame
{
    private static final TypeName JDA_TYPE = TypeName.get(JDA.class);
    private static final TypeName JDA_BUILDER_TYPE = TypeName.get(JDABuilder.class);

    private static final String BASE_INSTANCE = "base";
    private static final String BUILDER_INSTANCE = "builder";

    private static final String TOKEN_FIELD = "token";
    private static final String LISTENERS_FIELD = "listeners";

    private final AccountType accountType;
    private final TypeElement base;
    private final JDALogin jdaLogin;

    private Element tokenElement = null;
    private List<Element> listeners = new ArrayList<>();
    private ExecutableElement mainMethod = null;

    LoginClassFrame(AccountType accountType, TypeElement base, JDALogin jdaLogin)
    {
        this.accountType = accountType;
        this.base = base;
        this.jdaLogin = jdaLogin;
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

    public ExecutableElement getMainMethod()
    {
        return mainMethod;
    }

    public void setMainMethod(ExecutableElement mainMethod)
    {
        this.mainMethod = mainMethod;
    }

    public void addListener(Element listener)
    {
        this.listeners.add(listener);
    }

    public void buildTypeSpec(TypeSpec.Builder builder)
    {
        builder.addAnnotation(AnnotationSpec.builder(Generated.class).addMember("value",
            CodeBlock.builder().add("\"me.kgustave.jdagen.autologin.AutoLoginProcessor\"").build()).build());

        // TODO Code Gen
        builder.addField(String.class, TOKEN_FIELD, Modifier.PRIVATE, Modifier.FINAL);

        builder.addField(FieldSpec
            .builder(List.class, LISTENERS_FIELD, Modifier.PRIVATE, Modifier.FINAL)
            .initializer(CodeBlock
                .builder()
                .add("new $T<Object>()", ArrayList.class)
                .build()
            ).build());

        builder.addField(TypeName.get(base.asType()), BASE_INSTANCE, Modifier.PRIVATE, Modifier.FINAL);

        MethodSpec.Builder conBuilder = MethodSpec.constructorBuilder();

        // The constructor should be private
        conBuilder.addModifiers(Modifier.PRIVATE);

        CodeBlock.Builder conCode = CodeBlock.builder();

        conCode.add(BASE_INSTANCE + " = new $T", base);
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

        addInitialized(conCode, TOKEN_FIELD, tokenElement);

        for(Element listener : listeners)
        {
            final String call;
            if(listener.getKind() == ElementKind.FIELD)
                call = ".$N";
            else
                call = ".$N()";

            if(listener.asType().getKind() == TypeKind.ARRAY)
            {
                conCode.beginControlFlow("for($T listener : " + BASE_INSTANCE + call + ")", listener.getSimpleName())
                       .addStatement(LISTENERS_FIELD + ".add(listener)")
                       .endControlFlow();
            }
            // TODO Add iterable support
            else
            {
                conCode.addStatement(LISTENERS_FIELD + ".add(" + BASE_INSTANCE + call + ")", listener.getSimpleName());
            }
        }

        conBuilder.addCode(conCode.build());
        builder.addMethod(conBuilder.build());

        CodeBlock.Builder mainBlock = CodeBlock.builder();
        mainBlock.addStatement(
            String.format("%s login = new %s()", jdaLogin.loginClassName(), jdaLogin.loginClassName())
        );

        mainBlock.addStatement("$T " + BUILDER_INSTANCE + " = new $T($T.$L)",
            JDA_BUILDER_TYPE, JDA_BUILDER_TYPE,
            TypeName.get(AccountType.class), accountType.name()
        );

        // TODO Settings Gen
        mainBlock.addStatement(BUILDER_INSTANCE + ".setToken(login.token)");

        MethodSpec.Builder mainBuilder = MethodSpec.methodBuilder("main");
        mainBuilder.addModifiers(Modifier.PUBLIC, Modifier.STATIC).returns(TypeName.VOID);

        // Rethrow exceptions
        mainBuilder.addException(LoginException.class)
                   .addException(RateLimitedException.class);

        // Using buildBlocking
        if(jdaLogin.buildMode() == JDALogin.Mode.BLOCKING)
        {
            mainBuilder.addException(InterruptedException.class);
            mainBlock.addStatement("$T jda = " + BUILDER_INSTANCE + ".buildBlocking()", JDA_TYPE);
        }
        else
        {
            mainBlock.addStatement("$T jda = " + BUILDER_INSTANCE + ".buildAsync()", JDA_TYPE);
        }

        if(mainMethod != null)
        {
            mainBlock.addStatement(String.format("login." + BASE_INSTANCE + ".%s(jda)", mainMethod.getSimpleName().toString()));
        }

        builder.addMethod(mainBuilder.addCode(mainBlock.build()).build());

        if(tokenElement.getKind() == ElementKind.METHOD)
            conCode.addStatement(tokenElement.getSimpleName().toString()+"()");
        else
            conCode.addStatement(tokenElement.getSimpleName().toString());
    }

    private static void addInitialized(CodeBlock.Builder code, String field, Element element)
    {
        if(element == null)
            return;

        if(element.getKind() == ElementKind.FIELD)
            code.addStatement(field+" = " + BASE_INSTANCE + ".$N", element.getSimpleName());
        else
            code.addStatement(field+" = " + BASE_INSTANCE + ".$N()", element.getSimpleName());
    }
}
