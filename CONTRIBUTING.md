# Contributing to Nuts

Thanks for your interest in nuts. 
Our goal is to leverage the power of Java, Maven and Gradle to build to build a rock solid package manager.

## Getting Started

Nuts's [open issues are here](https://github.com/thevpc/nuts/issues). 
In time, we'll tag issues that would make a good first pull request for new contributors. 
An easy way to get started helping the project is to *file an issue*. 
You can do that on the Nuts issues page by clicking on the green button at the right. 
Issues can include bugs to fix, features to add, or documentation that looks outdated.

For some tips on contributing to open source, this [post is helpful](http://blog.smartbear.com/programming/14-ways-to-contribute-to-open-source-without-being-a-programming-genius-or-a-rock-star/).

## Contributions

Nuts welcomes contributions from everyone.

Contributions to Nuts should be made in the form of GitHub pull requests. Each pull request will
be reviewed by a core contributor (someone with permission to land patches) and either landed in the
main tree or given feedback for changes that would be required.

## Compiling Nuts
```bash
git clone https://github.com/thevpc/nuts.git
cd nuts
mvn clean install
```

## Pull Request Checklist

- Branch from the master branch and, if needed, rebase to the current master
  branch before submitting your pull request. If it doesn't merge cleanly with
  master you may be asked to rebase your changes.

- Commits should be as small as possible, while ensuring that each commit is
  correct independently (i.e., each commit should compile and pass tests).

- Don't put sub-module updates in your pull request unless they are to landed
  commits.

- If your patch is not getting reviewed or you need a specific person to review
  it, you can @-reply a reviewer asking for a review in the pull request or a
  comment.

- Add tests relevant to the fixed bug or new feature.

---------------
## PREPARING DEV ENVIRONMENT

create a key:
```bash
gpg --gen-key
```

add in `~/.m2/settings.xml` the following

```xml
<profile>
      <id>ossrh</id>
      <activation>
        <activeByDefault>true</activeByDefault>
      </activation>
      <properties>
        <gpg.executable>gpg2</gpg.executable>
        <gpg.keyname>YOUR-KEY-438B05CFD2263E2EB91FD083C7E3C476060E40DD</gpg.keyname>
        <gpg.passphrase>YOUR PASSWORD</gpg.passphrase>
      </properties>
    </profile>        
```
---------------

This CONTRIBUTING.md file is adapted from the [DeepLearning4j CONTRIBUTING.md](https://alvinalexander.com/java/jwarehouse/deeplearning4j/CONTRIBUTING.md.shtml)
