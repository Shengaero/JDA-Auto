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

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import net.dv8tion.jda.core.AccountType;

import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

/**
 * @author Kaidan Gustave
 */
@SuppressWarnings("unused")
public class LoginClassFrame
{
    private final AccountType accountType;
    private final TypeElement base;
    private Element tokenElement;


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
        this.tokenElement = tokenElement;
    }

    public void buildTypeSpec(TypeSpec.Builder builder)
    {
        builder.addField(String.class, "token", Modifier.PRIVATE, Modifier.FINAL);

        MethodSpec.Builder conBuilder = MethodSpec.constructorBuilder();

        CodeBlock.Builder conCode = CodeBlock.builder();

        conCode.add("");
    }
}
