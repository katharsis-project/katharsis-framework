Contributing to the katharsis project, and to any open-source project for that matter, can be a rewarding experience both in terms of the way you can help yourself and projects you are working on, as well as the countless number of others you may help with your contribution. 

Reporting Bugs & Requesting for Features  
=========================

Before you raise a new issue you must be able to narrow it  down to a minimal test case against a clean code base that can be used for reproduction when submitting the issue. And, naturally, to make sure it's not an "undiscovered feature" on your side :). Once you have a reproducable case in a clean code base please check the list of [project issues](https://github.com/katharsis-project/katharsis-framework/issues). There may already be a discussion in progress that you can contribute to or at least follow for a resolution. You should also ask in [gitter](https://gitter.im/katharsis-project) first. However, if you have discovered an issue then please follow these rules:  

1. Include **version of katharsis** you are using
2. Use labels to tell us in which **module** problem occurs
3. Wait for **feedback** from our developers in response to your issue. We are volunteers so please be patient.
4. Make a coffee and feel proud of yourself (or tea, or whatever the hot relaxing beverage in your part of the world is)!

Fixing Bugs & Extending the Library
===========================

So you have added a feature to a library or you have created something new. Maybe you just have some bit of code that you use all the time in your projects and now you want to give that to the community and let them enjoy the same benefits you have? Great! This is how open source projects begin, grow, thrive and reach new users. Here is how to proceed with a pull request:

1. [Fork katharsis-framework](https://github.com/katharsis-project/katharsis-framework#fork-destination-box) repository
2. Implement your new feature or fix on *dedicated* branch 
3. Don't forget to **update or add unit tests** and ensure they are passing
4. Submit a pull request to be merged into `master`. In your PR please refer to the issue number. Remember, enhancements are "issues" too. 
5. Your code will be reviewed by at least **two people** from our developers community
6. Wait for **feedback** and in next release you may see your code working on production

Join the Conversation
=====================

Katharsis gives you a number of ways to discuss features, express concerns or just chat about the project with other people just like you. Feel free to chat with us on [gitter](https://gitter.im/katharsis-project/katharsis-framework) or if your question is strictly involving usage of katharsis create new question on [StackOverflow](http://stackoverflow.com/questions/tagged/katharsis)

Other Ways to Contribute
======================

You don't necessarily have to write code to contribute. Telling someone in an effective way what a project is for and how to use it is just as important as providing the code for them to use. And it doesn't end there. From helping to maintain infrastructure to event planning, projects need help in many areas beyond the code.

Open source projects are small communities of like-minded individuals coming together over a common interest or skill set. As with any community, there are appropriate ways to conduct yourself when interacting with other members of that group. Treating others with respect is not just courteous, but it helps encourage new people that may have important contributions to feel comfortable enough to put themselves out there and take that first step toward getting involved.

Whether you're answering questions in a forum, chatting with someone at a conference or commenting on a section of someone's code, keep in mind that your words and your actions have real consequences. Treat others how you would like to be treated and keep in mind that we were all beginners at some point. When you encounter a community member that doesn't follow or understand the mores, just remember that someone once helped you and pass on the kindness.

### *Pull requests are welcome!*

Developer Setup
===========================
- Katharis 3.x uses Java 8. Katharsis 2.8.x uses java 7 and will only be supported by unpopular demand.  

General Guidelines
===========================
* **Code style.**  Cyclomatic complexity must be kept down as well.
this can be done in a variety of ways, but, for example, aggressive pre-condition checking will eliminate branching.

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
