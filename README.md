# What is BabyFish?

###1. BabyFish is a Java framework for data model classes.

Generally speaking, program can be splitted to three layers: data accessing layer, business logic layer and presentation layer, but there are some data model classes that do not belong to none of those layers, they are standalone and shared by every layer. Often, people call it "Entity Classes" too.

There are many technologies for those three layers, but unfortunately, the data model classes that are shared by every layer are often neglected, people often think writing some C-structure style classes with some getter and setter is enough. BabyFish tries to try to change this phenomenon, it is a framework of data model classes.

In order to support powerful data model developping, babyfish supports two core functionalities

* BabyFish Collection Framework(X Collection Framework + MA Collection Framework).
* ObjectModel(ObjectModel4Java + ObjectMode4JPA).

###2. BabyFish is an enhancement of JPA/Hibernate.

In order to adapt to the new data model technology, of course, every classic layer needs to be improved. For first version, it only improved the data accessing layer because the time is not enough.

In the first version, JPA/Hibernate is enhanced(
BabyFish-1.0.0 Alpha is used to enhance JPA2.1/Hibernate-4.3.6.Final), 
let's call it BabyFish-JPA/BabyFish-Hibernate. 
it also merges some advantages of ADO.NET Entity Framework into JPA/Hibernate.

BabyFish-JPA/BabyFish-Hibernate supports these three core functionalities

* BabyFish JPA Criteria
* Query Path
* Distinct Limit Query

> By the way, "BabyFish" is a Chinglish word, it means "Andrias
> davidianus". Its voice sounds like the baby's cry, that why Chinese
> call it "BabyFish". ^��^

# Why the first version cost so long time(2008-2015)?

I started to develop this framework since Aug, 2008, the first version is coming so late because

* I use my spare time to develop this framework, because I have to spend most of my time to engage in some boring works for salary to support myself.
* The developping had been suspended for more than 2 years because of private affairs.
* After I finished the main projects developping at the end of 2013, I need to create demo and deocument by english, that cost me so much time because my English is not good.

# License: LPGL3.0

BabyFish uses the LGPL-3.0 license so that it can be used in commercial projects, please see [http://opensource.org/licenses/LGPL-3.0](http://opensource.org/licenses/LGPL-3.0) to know more.

# Thanks
Thank two great frameworks: [ASM](http://asm.ow2.org/) and [ANTLR](http://www.antlr.org)

# About me
Tao Chen(Chinese Name: &#38472;&#28059;); [babyfish-ct@163.com](mailto:babyfish-ct@163.com)