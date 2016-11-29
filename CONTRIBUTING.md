# Katharsis Contribution Guidelines

Contributing to the Katharsis project, or to any open-source project for that matter, can be a rewarding experience both in terms of the way you can help yourself and projects you are working on, as well as the countless number of others you may help with your contribution. 

## Developer Quickstart

- For the time being, the whole project is running Java 7. Sorry, no lambdas.
- Install the Katharsis [code formatters for your IDE][formatters]. Please do not submit formatting related changes.
- Read the sections below on Pull Requests and Code Style.

## New Bugs \& Features

Before reporting a bug or requesting a new feature:

* Check the [project issues][issues]. There may already be a discussion in progress that you can contribute to or at
   least follow for a resolution.
* Pop into the [gitter][gitter] chat room and get some support or feedback from the community.

If you want to open a new bug report, please narrow it down to a minimal test case against a clean code base that
can be used for reproduction when submitting the issue.

Then:

1. Always include the information in the [issue template](ISSUE_TEMPLATE.md)
2. Use labels to tell us in which **module** problem occurs
3. Wait for **feedback** from our developers in response to your issue. We are volunteers so please be patient.
4. Make a coffee and feel proud of yourself (or tea, or whatever the hot relaxing beverage in your part of the world is)!

## Pull Requests

1. [Fork katharsis-framework](https://github.com/katharsis-project/katharsis-framework#fork-destination-box) repository
2. Implement your new feature or fix on branch `develop`
3. Don't forget to **update or add unit tests** and ensure they are passing
In your PR please refer to the issue number. 
6. Wait for **feedback** and in next release you may see your code working on production

* Make sure there is a ticket in the [bug tracker][issues], and refer to it in the PR.
  Remember, enhancements are "issues" too. 
* Make sure you use the [code formatters provided here][formatters] and have them applied to your changes.
  Don’t submit any formatting related changes.
* Make sure you submit test cases that back your changes. Make sure these tests pass.
* Try to reuse existing test sample code (domain classes). Try not to amend existing test cases but
  create new ones dedicated to the changes you’re making to the codebase.
* If introducing new features or changing existing ones, submit a corresponding PR in the [katharsis docs repo][docs].
* Your code will be reviewed by at least **two people** from our developer community


## Join the Conversation

Katharsis gives you a number of ways to discuss features, express concerns or just chat about the project with other
members in the community. Join us on [gitter](https://gitter.im/katharsis-project/katharsis-framework)
or if your question is strictly involving usage of katharsis create new question on
[StackOverflow](http://stackoverflow.com/questions/tagged/katharsis).

## Other Ways to Contribute

You don't necessarily have to write code to contribute. Telling someone in an effective way what a project is for
and how to use it is just as important as providing the code for them to use. And it does not end there. From helping
to maintain infrastructure to event planning, projects need help in many areas beyond the code.

Open source projects are small communities of like-minded individuals coming together over a common interest or
skill set. As with any community, there are appropriate ways to conduct yourself when interacting with other members
of that group. Treating others with respect is not just courteous, but it helps encourage new people that may have
important contributions to feel comfortable enough to put themselves out there and take that first step toward getting
involved.

Whether you're answering questions in a forum, chatting with someone at a conference or commenting on a section
of someone's code, keep in mind that your words and your actions have real consequences. Treat others how you would
like to be treated and keep in mind that we were all beginners at some point. When you encounter a community member
that doesn't follow or understand the mores, just remember that someone once helped you and pass on the kindness.

## Code style

**Install the Katharsis [code formatters for your IDE][formatters].** Please do not submit formatting related changes.

This section contains some stuff that the IDE formatters do not enforce. Try to keep track of these as well.

* Make sure, your IDE uses .* imports for all static ones.
* Eclipse users should [activate Save Actions][eclipse-save] to format sources on save and organize imports.
* **No warnings.** All generated code must compile. 
* **Portable!.** Your code and compile process cannot be platform dependent. This means that, for example, any reference 
to file paths must be platform agnostic
* Keep cyclomatic complexity to a minimum.This can be done in a variety of ways, but, in general adhere to the following
  *  aggressive pre-condition checking to eliminate branching. Example:
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


[issues]: https://github.com/katharsis-project/katharsis-framework/issues
[gitter]: https://gitter.im/katharsis-project
[docs]: https://github.com/katharsis-project/katharsis-docs
[formatters]: https://github.com/Ramblurr/katharsis-etc
[eclipse-save]: http://www.eclipseonetips.com/2009/12/13/automatically-format-and-cleanup-code-every-time-you-save/
