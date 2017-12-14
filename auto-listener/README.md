# Auto-Listener

**Auto-Listener** is a highly simplified and intuitive annotation processor 
that automatically generates `EventListener` implementations for you!

### How does it work?

Auto-Listeners are generated via a single applied annotation: `@AutoListener`.<br>
When you apply this, an annotation processor will create a separate generated class
that serves as a wrapper for the source class. Each generated class creates an instance
of source class when constructed, and also implements `EventListener` to listen for
events, and delegates specific event types to methods in the instance it is wrapping.

Sounds confusing? Here's an example of what it looks like:

```java
// The value here is the name of the class that will be generated.
// You don't have to specify this. If you don't it will create a 
// generated class with the same name and the suffix "Listener".
@AutoListener("MyGeneratedListener")
public class MyListener 
{
    private final String command;
    private final String response;
    
    // Wait how are we going to handle constructor arguments??
    // Just wait everything will make sense in a bit!
    public MyListener(String command, String response)
    {
        this.command = command;
        this.response = response;
    }
    
    public String getCommand()
    {
        return command;
    }
    
    // This is an event method that will be invoked when a
    // MessageReceivedEvent is fired.
    public void onMsg(MessageReceivedEvent event)
    {
        if(event.getAuthor().isBot())
            return;
        
        if(event.getMessage().getRawContent().startsWith(command))
        {
            event.getChannel().sendMessage(response).queue();
        }
    }
}
```

Now we generate this all and it creates this:
```java
// This is pretty much the exact output of the source file
@Generated("me.kgustave.jdagen.autolistener.AutoListenerProcessor")
public class MyGeneratedListener implements EventListener {
  private final MyListener instance;
  
  // This also generates copy constructors that allow you
  // to maintain encapsulation of the original source file.
  public MyGeneratedListener(String command, String response) {
    instance = new MyListener(command, response);
  }
  
  // Instance methods maintain the
  // same name and delegate function
  // to the instance's version.
  public String getCommand() {
    return instance.getCommand();
  }
  
  @Override
  public void onEvent(Event event) {
    // As you can see, this checks if the Event is a
    // MessageReceivedEvent and then casts and invokes
    // the instance's "onMsg" method!
    if(event instanceof MessageReceivedEvent) {
      instance.onMsg((MessageReceivedEvent)event);
    }
  }
}
```

### Advantages of Using Auto-Listener

What is the advantages of using this?

+ **Simplicity**<br>
  Auto-Listener is highly simplified, intuitive, and the internal implementation 
  is minimized to only use exactly what needs to be used.

+ **No Bias**<br>
  Auto-Listener allows full and simple internal specification without any complicated
  compiler arguments, and *absolutely no bias*.
  If something isn't coming out correctly after compiling, it almost certainly has
  a configuration that will make it work.