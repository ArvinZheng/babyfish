<!DOCTYPE html>
<html>
    <head>
        <meta charset="utf-8" />
        <style type="text/css">
	        <!--
            body {
                font-family: Arial;
                margin: 0px;
            }
            body {
                font-size: 14px;
            }
            div.title {
                text-align: center;
                font-weight: bold;
                font-size: 40px;
                padding: 10px;
                background-color: rgb(0, 0, 100);
                color: white;
            }
            div.content {
                padding: 10px;
            }
            h1 {
                font-size: 18px;
                color: rgb(127, 63, 63);
                font-weight: bold;
            }
            h2 {
                font-size: 16px;
                color: rgb(127, 63, 63);
                font-weight: bold;
            }
            span.important {
                color: rgb(0, 127, 0);
                font-weight: bold;
            }
            div.author {
                font-size: 16px;
                font-weight: bold;
                text-align: right;
            }
            div.tab {
                margin: 20px;
                border-bottom: solid 1px rgb(0, 0, 127);
                font-size: 18px;
                font-weight: bold;
            }
            div.tab > a, div.tab > span {
                display: inline-block;
                border: solid 1px rgb(0, 0, 127);
                border-bottom: none;
                padding: 5px;
                border-radius: 5px 5px 0px 0px;
                cursor: default;
                text-decoration: none;
            }
            div.tab > a:hover {
                background-color: rgb(230, 230,255);
            }
            div.tab > a:visited {
                color: black;
            }
            div.tab > span {
                background-color: rgb(0, 0, 127) ! important;
                color: white ! important;
            }
            p.by-the-way {
                background-color: rgb(230, 230, 230);
                padding: 10px;
            }
			span.head {
				font-weight: bold;
				color: rgb(0, 0, 127);
			}
			-->
        </style>
    </head>
    <body>
        <div class="content">
            <h1>What is BabyFish?</h1>
            <ol>
                <li>
                    <h2>BabyFish is a Java framework for data model classes.</h2>
                    <p>
                        Generally speaking, program can be split to three layers: data accessing layer, 
                        business logic layer and Presentation layer, but there are some data model classes that 
                        do not belong to none of those layers, they are standalone and shared by every layer. 
                        Often, people call it "Entity Classes" too.
                    </p>
                    <p>
                        There are many technologies for those three layers, but unfortunately, the data model
                        classes that are shared by every layer are often neglected, people often think writing
                        some C-structure style classes with some getter and setter is enough. BabyFish tries to
                        try to change this phenomenon, it is a framework of data model classes.
                    </p>
                    <p>
                        In order to support powerful data model developing, babyfish supports two core functionalities
                    </p>
                    <ul>
                        <li>BabyFish Collection Framework(X Collection Framework + MA Collection Framework).</li>
                        <li><span class="important">ObjectModel</span>(ObjectModel4Java + ObjectMode4JPA).</li>
                    </ul>
                </li>
                <li>
                    <h2>BabyFish is an enhancement of JPA/Hibernate.</h2>
                    <p>
                        In order to adapt to the new data model technology, of course, every classic layer needs
                        to be improved. For first version, it only improved the data accessing layer because the
                        time is not enough.
                    </p>
                    <p>
                        In the first version, JPA/Hibernate is enhanced(
						BabyFish-1.0.0 Alpha is used to enhance JPA2.1/Hibernate-4.3.6.Final), 
						let's call it BabyFish-JPA/BabyFish-Hibernate.
                        it also merges some
                        advantages of 
                        <a href="https://msdn.microsoft.com/en-us/data/ee712907.aspx" target="_blank">
                            ADO.NET Entity Framework
                        </a>
                        into JPA/Hibernate.
                    </p>
                    <p>BabyFish-JPA/BabyFish-Hibernate supports these three core functionalities</p>
                    <ul>
                        <li>BabyFish JPA Criteria</li>
                        <li>Query Path</li>
                        <li>Distinct Limit Query</li>
                    </ul>
                </li>
            </ol>
            <p class="by-the-way">
                By the way, "BabyFish" is a Chinglish word, it means "Andrias davidianus". 
                Its voice sounds like the baby's cry, that why Chinese call it "BabyFish". ^&omega;^
            </p>
			<h1>Functionality tree</h1>
			<ul>
				<li><span class="head">Java Part</span></li>
				<ul>
					<li>
						<span class="head">Event combiner:</span> support .NET style event notification mechanism.
					</li>
					<li>
					    <span class="head">Typed-18N:</span> 
						support strong type "18N" which can report the I18N errors at compilation time.
					</li>
					<li>
					    <span class="head">X-Collection-Framework:</span> Enhance the Java Collection Framework. The most important functionality
						is "Unstable Collection Elements", element/key can be modified after it has been added into 
						set(map) because the corresponding sets/maps will be adjusted automatically when it's changed.
					</li>
					<li>
						<span class="head">MA-Collection-Framework:</span> Enhance the X-Collection-Framework to let collections support
						modification notification like the trigger of database. The most important functionality
						is "Bubbled Event", If the modification event has been triggered by
						iterator of view(eg: List.subList, SortedMap.subMap), it will bubbled
						to its parent view, util the original root collection triggered the event.
					</li>
					<li>
						<span class="head">ObjectModel4Java:</span> A powerful API to create Java model classes, 
						it supports the bidirectional association between objects.
						When one side bidirectional association is changed, the other side
						will be notified and adjusted automatically.
						User only need to declared some annotations and all the functionalities
						will be implemented by the byte-code generated dynamically in runtime.
					</li>
				</ul>
				<li><span class="head">ORM Part</li>
				<ul>
					<li>
						<span class="head">ObjectMode4JPA:</span> Enhance ObjectModel4Java. let ObjectModel support
						JPA entity classes.
						<ul>
							<li>
								Replace the lazy proxy and lazy collection of Hibernate to 
								support ObjectModel with laziness management.
							</li>
							<li>
								Support Maven plugin to change the byte-code of entity class
								at compilation time, programmer only need to write the simple
								JPA entity classes, and complex code for the ObjectMode4JPA 
								can be added into entity classes automatically during compilation.
							</li>
						</ul>
					</li>
					<li>
					    <span class="head">BabyFish-JPA-Criteria:</span> A smarter implementation of JPA Criteria,
						it supports more functionalities, and it can optimize the generated
						JPQL.
					</li>
					<li>
						<span class="head">QueryPath:</span> Its source code is generated by maven plugin at
						compilation time so that all the errors can be report at 
						compilation time. It can be decided by UI Layer dynamically,
						then dispatch it to Business Logic Layer, and finally dispatch
						it to the Data Access Layer. In Data Access Layer, it can
						<ul>
							<li>Fetch lazy associations dynamically with any depth and breadth</li>
							<li>Fetch lazy scalar associations(eg: Lob) dynamically</li>
							<li>Sort the query result or collection association dynamically with any depth and breadth</li>
						</ul>
					</li>
					<li>
						<span class="head">DistinctLimiQuery:</span> Enhance the Oracle Dialect of Hibernate to resolve
						a problem of Hibernate, In hibernate, when paging query(with firstResult/maxResults)
						contains collection fetches, hibernate has to query all the data and do the paging
						filter in memory. This functionality can resolve this problem when database is Oracle.
					</li>
				</ul>
			</ul>
            <h1>Why did the first version cost so long time(2008-2015)?</h1>
            <p>
                I started to develop this framework since Aug, 2008, the first version is coming so late because
            </p>
            <ul>
                <li>
                    I use my spare time to develop this framework, 
                    because I have to spend most of my time to engage in some boring works for salary to support myself.
                </li>
                <li>
                    The developping had been suspended for more than 2 years because of private affairs.
                </li>
                <li>
                    After I finished the main projects developing at the end of 2013, I need to create demo and deocument
                    by english, that cost me so much time because my English is not good.
                </li>
            </ul>
            <h1>license: LPGL3.0</h1>
            BabyFish uses the LGPL-3.0 license so that it can be used in commercial projects, 
            please see 
            <a href="http://opensource.org/licenses/LGPL-3.0" target="_blank">
                http://opensource.org/licenses/LGPL-3.0
            </a>
            to know more.
            <h1>Thanks</h1>
            Thank two great frameworks: 
            <a href="http://asm.ow2.org/" target="_blank">ASM</a> and 
            <a href="http://www.antlr.org/" target="_blank">ANTLR</a>
            <div class="author">
    	        <p>
    		        Tao Chen(&#38472;&#28059;)
    		        <a href="mailto:30338148@qq.com">30338148@qq.com</a>
    	        </p>
    	        <p>
    		        2015-10-11, ChengDu, China
    	        </p>
            </div>
        </div>
    </body>
</html>