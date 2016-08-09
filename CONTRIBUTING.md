=======
[ This document is still a draft ]

Reporting an issue
==================
1. 


Submission of pull requests
===========================
1. Fork repository
2. Implement your new feature/fix in branch `development`
3. Update unit tests to ensure the test are passing and code coverage is at acceptable level 
4. Submit a pull request to be merged in the `development` of original repo including in description modification you made and reason for change

### *Pull requests are welcome!*

Developer Setup
===========================
- For the time being, the whole project is running Java 7. Sorry, no lambdas.

General Guidelines
===========================
* **Code style.**  Cyclomatic complexity must be kept down as well.
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
