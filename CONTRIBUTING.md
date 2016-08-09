### *Pull requests are welcome!*

Developer Setup
------------------
- We are using [Lombok Plugin](https://projectlombok.org/download.html) to auto generate getters and setters for our API model objects. 
- To properly help you contribute, you need to follow instructions on setting up Lombok Plugin in your IDE (Eclipse/IntelliJ/etc), as [shown here](https://projectlombok.org/download.html).

General Guidelines
------------------

* **Code style.** For the time being, the whole project is running Java 7. Sorry, no lambdas. Cyclomatic complexity must be kept down as well.
this can be done in a variety of ways, but, for example, agressive pre-condition checking will eliminate branching.

== example: ==

```java
public void foo(Integer thing) {
    if(thing != null) {
       if(thing < 3) {
        if(thing > 0) {
            doSome(thing);
        }
        else { 
          throw new IllegalArgumentException();
        }
       } 
    }
}
```

simplifies to:

```java
public void foo(Integer thing) {
    if(thing == null) 
       throw new IllegalArgumentException();
    
    if(thing >= 3 || thing <= 0)
        throw new IllegalArgumentException();
    
    doSome(thing);
}
```

* **No warnings.** All generated code must compile. 
* **Portable!.** Your code and compile process cannot be platform dependent. This means that, for example, any reference 
to file paths must be platform agnostic