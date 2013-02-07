/*
 * Copyright 2012 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.forge.aesh.util;

import org.jboss.aesh.cl.CommandLine;
import org.jboss.aesh.cl.CommandLineParser;
import org.jboss.aesh.cl.OptionBuilder;
import org.jboss.aesh.cl.internal.ParameterInt;
import org.jboss.forge.aesh.ShellContext;
import org.jboss.forge.ui.Result;
import org.jboss.forge.ui.UICommand;
import org.jboss.forge.ui.UICommandMetadata;
import org.jboss.forge.ui.UIContext;
import org.jboss.forge.ui.UIInput;
import org.jboss.forge.ui.UIValidationContext;
import org.jboss.forge.ui.base.UICommandMetadataBase;
import org.jboss.forge.ui.impl.UIInputImpl;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author <a href="mailto:stale.pedersen@jboss.org">Ståle W. Pedersen</a>
 */
@Ignore
public class CommandLineUtilTest
{

   public CommandLineUtilTest(String name)
   {
   }

    @Test
   public void testGenerateParser() throws Exception
   {
      ShellContext context = new ShellContext(null);
      Foo1Command foo1 = new Foo1Command();
      foo1.initializeUI(context);
      CommandLineParser parser = CommandLineUtil.generateParser(foo1, context);

      assertEquals("foo1", parser.getParameters().get(0).getName());

      Foo2Command foo2 = new Foo2Command();
      foo2.initializeUI(context);
      parser = CommandLineUtil.generateParser(foo2, context);

      ParameterInt param = parser.getParameters().get(0);
      assertEquals("foo2", param.getName());
      assertEquals("str", param.findLongOption("str").getLongName());
      assertEquals("bool", param.findLongOption("bool").getLongName());

   }

    @Test
   public void testPopulateUIInputs()
   {
      UIInput<String> input1 = new UIInputImpl<String>("str", String.class);
      UIInput<Integer> input2 = new UIInputImpl<Integer>("int", Integer.class);
      UIInput<Boolean> input3 = new UIInputImpl<Boolean>("bool", Boolean.class);

      ShellContext context = new ShellContext(null);
      context.add(input1);
      context.add(input2);
      context.add(input3);

      ParameterInt param = new ParameterInt("test", "testing");
      param.addOption(new OptionBuilder().name('s').longName("str").description("yay").create());
      param.addOption(new OptionBuilder().name('i').longName("int").description("yay").create());
      param.addOption(new OptionBuilder().name('b').longName("bool").description("yay").hasValue(false).create());

      CommandLineParser clp = new CommandLineParser(param);
      CommandLine cl = clp.parse("test --str yay");

      CommandLineUtil.populateUIInputs(cl, context, null);
      assertEquals(input1.getValue(), "yay");
      assertNull(input2.getValue());
      assertNull(input3.getValue());

      cl = clp.parse("test --str yay --int 10");
      CommandLineUtil.populateUIInputs(cl, context, null);
      assertEquals(input1.getValue(), "yay");
      assertEquals(input2.getValue(), new Integer(10));
      assertNull(input3.getValue());

      cl = clp.parse("test --bool");
      CommandLineUtil.populateUIInputs(cl, context, null);
      assertEquals(input3.getValue(), Boolean.TRUE);
      assertNull(input1.getValue());
      assertNull(input2.getValue());
   }

    @Test
   public void testGenerateAndPopulate() throws Exception
   {
      ShellContext context = new ShellContext(null);
      Foo2Command foo2 = new Foo2Command();
      foo2.initializeUI(context);
      CommandLineParser clp = CommandLineUtil.generateParser(foo2, context);

      CommandLine cl = clp.parse("foo2 --str yay");
      CommandLineUtil.populateUIInputs(cl, context, null);

      assertEquals("yay", ((UIInput) context.findInput("str")).getValue());
   }

   private class Foo1Command implements UICommand
   {
      @Override
      public UICommandMetadata getMetadata()
      {
         return new UICommandMetadataBase("foo1", "bla");
      }

      @Override
      public boolean isEnabled(UIContext context)
      {
         return true;
      }

      @Override
      public void initializeUI(UIContext context) throws Exception
      {
      }

      @Override
      public void validate(UIValidationContext context)
      {
      }

      @Override
      public Result execute(UIContext context) throws Exception
      {
         return null;
      }
   }

   private class Foo2Command extends Foo1Command
   {
      private UIInput<String> str;
      private UIInput<Boolean> bool;

      @Override
      public UICommandMetadata getMetadata()
      {
         return new UICommandMetadataBase("foo2", "bla2");
      }

      @Override
      public void initializeUI(UIContext context) throws Exception
      {
         str = new UIInputImpl<String>("str", String.class);
         bool = new UIInputImpl<Boolean>("bool", Boolean.class);

         context.getUIBuilder().add(str);
         context.getUIBuilder().add(bool);
      }

   }
}
