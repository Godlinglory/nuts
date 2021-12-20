(window.webpackJsonp=window.webpackJsonp||[]).push([[49],{103:function(e,t,a){"use strict";a.r(t),a.d(t,"frontMatter",(function(){return s})),a.d(t,"metadata",(function(){return i})),a.d(t,"rightToc",(function(){return l})),a.d(t,"default",(function(){return p}));var n=a(2),r=a(6),o=(a(0),a(117)),s={id:"projects",title:"Repository Structure",sidebar_label:"Nuts Projects"},i={unversionedId:"contributing/projects",id:"contributing/projects",isDocsHomePage:!1,title:"Repository Structure",description:"Quick Lookup on sources organization",source:"@site/docs/contributing/projects.md",permalink:"/nuts/docs/contributing/projects",editUrl:"https://github.com/thevpc/nuts/edit/master/website/docs/contributing/projects.md",sidebar_label:"Nuts Projects",sidebar:"someSidebar",previous:{title:"Contributing",permalink:"/nuts/docs/contributing/contributing"}},l=[{value:"Quick Lookup on sources organization",id:"quick-lookup-on-sources-organization",children:[]},{value:"nuts projects",id:"nuts-projects",children:[]},{value:"Core Nuts projects",id:"core-nuts-projects",children:[{value:"<strong>nuts-builder</strong>",id:"nuts-builder",children:[]},{value:"<strong>nuts-api</strong>",id:"nuts-api",children:[]},{value:"<strong>nuts-runtime</strong>",id:"nuts-runtime",children:[]}]},{value:"Companion tools projects",id:"companion-tools-projects",children:[{value:"<strong>nsh</strong>",id:"nsh",children:[]}]},{value:"Toolbox projects",id:"toolbox-projects",children:[{value:"<strong>nversion</strong>",id:"nversion",children:[]},{value:"<strong>ndb</strong>",id:"ndb",children:[]},{value:"<strong>ntomcat</strong>",id:"ntomcat",children:[]},{value:"<strong>nmvn</strong>",id:"nmvn",children:[]},{value:"<strong>noapi</strong>",id:"noapi",children:[]},{value:"<strong>ncode</strong>",id:"ncode",children:[]},{value:"<strong>nwork</strong>",id:"nwork",children:[]},{value:"<strong>ntemplate</strong>",id:"ntemplate",children:[]},{value:"<strong>njob</strong>",id:"njob",children:[]},{value:"<strong>ndoc</strong>",id:"ndoc",children:[]},{value:"<strong>ndocusaurus</strong>",id:"ndocusaurus",children:[]},{value:"<strong>ntalk-agent</strong>",id:"ntalk-agent",children:[]},{value:"<strong>nclown</strong>",id:"nclown",children:[]},{value:"<strong>nserver</strong>",id:"nserver",children:[]},{value:"<strong>nwar</strong>",id:"nwar",children:[]},{value:"<strong>ndexer</strong>",id:"ndexer",children:[]}]},{value:"Library Projects",id:"library-projects",children:[{value:"<strong>nlib-tomcat-classloader</strong>",id:"nlib-tomcat-classloader",children:[]},{value:"<strong>nlib-servlet</strong>",id:"nlib-servlet",children:[]},{value:"<strong>nlib-template</strong>",id:"nlib-template",children:[]},{value:"<strong>nlib-talkagent</strong>",id:"nlib-talkagent",children:[]}]},{value:"Extensions",id:"extensions",children:[{value:"<strong>next-term</strong>",id:"next-term",children:[]}]},{value:"Other Projects",id:"other-projects",children:[]},{value:"Honorable mentions",id:"honorable-mentions",children:[{value:"<strong>netbeans-launcher</strong>",id:"netbeans-launcher",children:[]},{value:"<strong>pnote</strong>",id:"pnote",children:[]},{value:"<strong>upa-box</strong>",id:"upa-box",children:[]},{value:"<strong>vr-box</strong>",id:"vr-box",children:[]}]}],c={rightToc:l};function p(e){var t=e.components,a=Object(r.a)(e,["components"]);return Object(o.a)("wrapper",Object(n.a)({},c,a,{components:t,mdxType:"MDXLayout"}),Object(o.a)("h2",{id:"quick-lookup-on-sources-organization"},"Quick Lookup on sources organization"),Object(o.a)("p",null,"The repository is organized in several folders described here after:"),Object(o.a)("ul",null,Object(o.a)("li",{parentName:"ul"},Object(o.a)("strong",{parentName:"li"},"[.dir-template]")," : contains template files for generating ",Object(o.a)("inlineCode",{parentName:"li"},"README.md")," and ",Object(o.a)("inlineCode",{parentName:"li"},"METADATA")," (among other) files according to the current ",Object(o.a)("inlineCode",{parentName:"li"},"nuts")," development version"),Object(o.a)("li",{parentName:"ul"},Object(o.a)("strong",{parentName:"li"},"[core]"),"          : contains the core of ",Object(o.a)("inlineCode",{parentName:"li"},"nuts")," package manager (and the only required pieces for ",Object(o.a)("inlineCode",{parentName:"li"},"nuts")," to work). Practically this contains the Bootstrap (and API) project (called ",Object(o.a)("inlineCode",{parentName:"li"},"nuts"),") and the Runtime (Implementation) project (called ",Object(o.a)("inlineCode",{parentName:"li"},"nuts-runtime"),")"),Object(o.a)("li",{parentName:"ul"},Object(o.a)("strong",{parentName:"li"},"[docs]"),"          : contains a generated (using docusaurus) web site that is deployed to github pages (",Object(o.a)("a",Object(n.a)({parentName:"li"},{href:"https://thevpc.github.io/nuts/"}),"https://thevpc.github.io/nuts/"),")"),Object(o.a)("li",{parentName:"ul"},Object(o.a)("strong",{parentName:"li"},"[ext]"),"           : contains some ",Object(o.a)("inlineCode",{parentName:"li"},"nuts")," extensions/plugins. as an example it includes and extension for nuts terminal features implemented using ",Object(o.a)("inlineCode",{parentName:"li"},"jline")," library"),Object(o.a)("li",{parentName:"ul"},Object(o.a)("strong",{parentName:"li"},"[incubating]"),"    : ignore this for the moment :), it is a work in progress and an attempt to simplify ",Object(o.a)("inlineCode",{parentName:"li"},"nuts")," installation process and other frozen features. Still very embryonic."),Object(o.a)("li",{parentName:"ul"},Object(o.a)("strong",{parentName:"li"},"[lib]"),"           : contains a suite of libraries that are based on ",Object(o.a)("inlineCode",{parentName:"li"},"nuts")," and that can be used by other applications. This includes markdown parsers, ssh support, etc..."),Object(o.a)("li",{parentName:"ul"},Object(o.a)("strong",{parentName:"li"},"[test]"),"          : contains unit test projects"),Object(o.a)("li",{parentName:"ul"},Object(o.a)("strong",{parentName:"li"},"[toolbox]"),"       : contains a suite of applications that are based on ",Object(o.a)("inlineCode",{parentName:"li"},"nuts")," and that complement ",Object(o.a)("inlineCode",{parentName:"li"},"nuts")," features. This includes ",Object(o.a)("inlineCode",{parentName:"li"},"nsh")," the shell companion"),Object(o.a)("li",{parentName:"ul"},Object(o.a)("strong",{parentName:"li"},"[web-toolbox]"),"   : contains a suite of web applications that are based on ",Object(o.a)("inlineCode",{parentName:"li"},"nuts")," and that complement ",Object(o.a)("inlineCode",{parentName:"li"},"nuts")," features. This includes ",Object(o.a)("inlineCode",{parentName:"li"},"nwar"),", a servlet implementation to serve ",Object(o.a)("inlineCode",{parentName:"li"},"nuts")," workspaces."),Object(o.a)("li",{parentName:"ul"},Object(o.a)("strong",{parentName:"li"},"[website]"),"       : contains the sources of ",Object(o.a)("inlineCode",{parentName:"li"},"nuts"),"'s docusaurus based website.",Object(o.a)("ul",{parentName:"li"},Object(o.a)("li",{parentName:"ul"},Object(o.a)("strong",{parentName:"li"},"[website/.dir-template]"),"       : contains the effective sources of ",Object(o.a)("inlineCode",{parentName:"li"},"nuts"),"'s documentation (used to create the website as well). To be more precise, the website is built using a two steps process: first we pre-process the markdown files with template processing (using ",Object(o.a)("inlineCode",{parentName:"li"},"ntemplate"),") that will handle things like ",Object(o.a)("inlineCode",{parentName:"li"},"nuts")," version variables, documentation structure etc and hence prepare the ",Object(o.a)("inlineCode",{parentName:"li"},"docusaurus")," base folder. And then, we run ",Object(o.a)("inlineCode",{parentName:"li"},"docusaurus")," to generate the effective ",Object(o.a)("inlineCode",{parentName:"li"},"html"),"/",Object(o.a)("inlineCode",{parentName:"li"},"js")," for the statically compiled website.")))),Object(o.a)("h2",{id:"nuts-projects"},"nuts projects"),Object(o.a)("p",null,Object(o.a)("strong",{parentName:"p"},Object(o.a)("inlineCode",{parentName:"strong"},"nuts"))," repository is composed of several projects that can be organized in 5 categories"),Object(o.a)("ul",null,Object(o.a)("li",{parentName:"ul"},Object(o.a)("strong",{parentName:"li"},"Core ",Object(o.a)("inlineCode",{parentName:"strong"},"nuts"))," : These projects are the core/base of the ",Object(o.a)("strong",{parentName:"li"},Object(o.a)("inlineCode",{parentName:"strong"},"nuts"))," package manager"),Object(o.a)("li",{parentName:"ul"},Object(o.a)("strong",{parentName:"li"},"Companion Tools")," : These projects are applications and tools to install with ",Object(o.a)("strong",{parentName:"li"},Object(o.a)("inlineCode",{parentName:"strong"},"nuts"))," itself. Their installation are prompted at first install of ",Object(o.a)("strong",{parentName:"li"},"nuts")),Object(o.a)("li",{parentName:"ul"},Object(o.a)("strong",{parentName:"li"},"Toolbox")," : These projects are applications and tools built on top of ",Object(o.a)("strong",{parentName:"li"},Object(o.a)("inlineCode",{parentName:"strong"},"nuts"))," Application Framework and are of common interest"),Object(o.a)("li",{parentName:"ul"},Object(o.a)("strong",{parentName:"li"},"Lib")," : These projects are common libraries that can be used to enabled some ",Object(o.a)("strong",{parentName:"li"},Object(o.a)("inlineCode",{parentName:"strong"},"nuts"))," features in your application"),Object(o.a)("li",{parentName:"ul"},Object(o.a)("strong",{parentName:"li"},"Extension")," : These projects are add features to the nuts platform. on example is the ability to add JLine library support to have smarter terminals."),Object(o.a)("li",{parentName:"ul"},Object(o.a)("strong",{parentName:"li"},"Other")," : All other apps that doe no fit in the previous categories")),Object(o.a)("h2",{id:"core-nuts-projects"},"Core Nuts projects"),Object(o.a)("p",null,"Core ",Object(o.a)("strong",{parentName:"p"},Object(o.a)("inlineCode",{parentName:"strong"},"nuts"))," projects include ",Object(o.a)("strong",{parentName:"p"},"nuts-builder"),", ",Object(o.a)("strong",{parentName:"p"},"nuts-api")," (/core/nuts), ",Object(o.a)("strong",{parentName:"p"},"nuts-runtime")," (/core/nuts-runtime)."),Object(o.a)("h3",{id:"nuts-builder"},Object(o.a)("strong",{parentName:"h3"},"nuts-builder")),Object(o.a)("p",null,Object(o.a)("strong",{parentName:"p"},"nuts-builder")," is a meta project (parent maven pom project) that helps building all the other projects."),Object(o.a)("h3",{id:"nuts-api"},Object(o.a)("strong",{parentName:"h3"},"nuts-api")),Object(o.a)("p",null,Object(o.a)("strong",{parentName:"p"},"nuts-api"),' is the effective "nuts" only required dependency.\nIt defines the bootstrap application that is responsible of loading all necessary libraries for its execution.\n',Object(o.a)("strong",{parentName:"p"},"nuts-api")," starts to load ",Object(o.a)("strong",{parentName:"p"},"nuts-runtime")," which is responsible of implementing all features and interfaces declared by the ",Object(o.a)("strong",{parentName:"p"},"nuts-api")," library.\nThat implementation will handle further download, version dependency etc. Such architecture is considered to force loose coupling with nuts binaries.\n",Object(o.a)("strong",{parentName:"p"},"nuts-api")," is a very thin boostrapper : its size is about 300k. It can be used as a standalone application or as an embedded library."),Object(o.a)("h3",{id:"nuts-runtime"},Object(o.a)("strong",{parentName:"h3"},"nuts-runtime")),Object(o.a)("p",null,Object(o.a)("strong",{parentName:"p"},"nuts-runtime")," is the effective and standard implementation of ",Object(o.a)("strong",{parentName:"p"},"nuts-api"),".\n",Object(o.a)("strong",{parentName:"p"},"nuts-runtime")," has a faster update pace than ",Object(o.a)("strong",{parentName:"p"},"nuts-api"),".\nIt focuses on performance an compliance to the ",Object(o.a)("strong",{parentName:"p"},"nuts")," specifications declared by ",Object(o.a)("strong",{parentName:"p"},"nuts-api")," interfaces.\nYou are not required to add this dependency to your application if you want to embed ",Object(o.a)("strong",{parentName:"p"},"nuts"),".\nThe library will be loaded on the wire (if not yet present in the classpath of course).\n",Object(o.a)("strong",{parentName:"p"},"nuts-runtime")," is designed to have very few dependencies : ",Object(o.a)("strong",{parentName:"p"},"gson")," and ",Object(o.a)("strong",{parentName:"p"},"jansi"),"."),Object(o.a)("ul",null,Object(o.a)("li",{parentName:"ul"},Object(o.a)("strong",{parentName:"li"},"gson")," trivially is used to support json serialization : the main format used in ",Object(o.a)("strong",{parentName:"li"},"nuts")," to support configuration and descriptors. "),Object(o.a)("li",{parentName:"ul"},Object(o.a)("strong",{parentName:"li"},"jansi"),' is used to support terminal coloring and the "Nuts Text Format" (NTF), a simple text format (markdown like) that helps creating colorful terminal applications.')),Object(o.a)("h2",{id:"companion-tools-projects"},"Companion tools projects"),Object(o.a)("p",null,"Companion tools include mainly ",Object(o.a)("strong",{parentName:"p"},"nsh"),'\nThis application is implemented following the "',Object(o.a)("strong",{parentName:"p"},Object(o.a)("inlineCode",{parentName:"strong"},"nuts")),' Application Framework" and hence is dependent on ',Object(o.a)("strong",{parentName:"p"},"nuts-api")," library.\n",Object(o.a)("strong",{parentName:"p"},"nsh")," is a recommended for installation because it adds portable bash like features to the tool, however is is mandatory and may be ignored particularly when using ",Object(o.a)("strong",{parentName:"p"},"nuts-api")," as library."),Object(o.a)("h3",{id:"nsh"},Object(o.a)("strong",{parentName:"h3"},"nsh")),Object(o.a)("p",null,Object(o.a)("strong",{parentName:"p"},"nsh")," (for ",Object(o.a)("strong",{parentName:"p"},Object(o.a)("inlineCode",{parentName:"strong"},"nuts"))," shell) is simply a portable POSIX bash compatible implementation.\nIt supports all common builtin commands (ls, cd, rm, ...) and adds support to grep, ssh and scp in a seamless manner.\nIt also supports command line, scripts (including commons constructs with if, do, case, ...), pipes (|) and common bash syntax."),Object(o.a)("h2",{id:"toolbox-projects"},"Toolbox projects"),Object(o.a)("p",null,Object(o.a)("strong",{parentName:"p"},Object(o.a)("inlineCode",{parentName:"strong"},"nuts"))," comes with an array of tools out of the box you can install and play with. Here are some of them:"),Object(o.a)("h3",{id:"nversion"},Object(o.a)("strong",{parentName:"h3"},"nversion")),Object(o.a)("p",null,Object(o.a)("strong",{parentName:"p"},"nversion")," is a small tool that helps detecting files versions.\nIt supports jar, war, ear, dll and exe file versions. It opens a file and looks for it's version in its meta-data."),Object(o.a)("h3",{id:"ndb"},Object(o.a)("strong",{parentName:"h3"},"ndb")),Object(o.a)("p",null,Object(o.a)("strong",{parentName:"p"},"ndb")," is a companion tool to the relational databased. ",Object(o.a)("strong",{parentName:"p"},"mysql"),", ",Object(o.a)("strong",{parentName:"p"},"mariadb")," and ",Object(o.a)("strong",{parentName:"p"},"nderby")," servers are supported.\nThe main actions supported are backup and restore including push/pull mechanism from/to a couple of databases for synchronization.\nIt supports jdbc and ssh based access to remote mysql/mariadb installation."),Object(o.a)("h3",{id:"ntomcat"},Object(o.a)("strong",{parentName:"h3"},"ntomcat")),Object(o.a)("p",null,Object(o.a)("strong",{parentName:"p"},"ntomcat")," is a companion tool to the tomcat http server.\nThe main actions supported are start, stop, status, configure (http ports etc..) and deploy.\nIt supports as well installation of several versions of Tomcat and multi domain configuration for deployments."),Object(o.a)("h3",{id:"nmvn"},Object(o.a)("strong",{parentName:"h3"},"nmvn")),Object(o.a)("p",null,Object(o.a)("strong",{parentName:"p"},"nmvn")," is a companion tool to maven.\nIt supports installations of several versions of maven and running them seamlessly."),Object(o.a)("h3",{id:"noapi"},Object(o.a)("strong",{parentName:"h3"},"noapi")),Object(o.a)("p",null,Object(o.a)("strong",{parentName:"p"},"noapi")," (for Nuts OpenApi) is an OpenAPI documentation generator."),Object(o.a)("h3",{id:"ncode"},Object(o.a)("strong",{parentName:"h3"},"ncode")),Object(o.a)("p",null,Object(o.a)("strong",{parentName:"p"},"ncode")," is a small code search tool. It searches for files, file contents and classes within jars.\nYou can search for files than contains some text or jars that contain some class, or jars of a specific version of java."),Object(o.a)("h3",{id:"nwork"},Object(o.a)("strong",{parentName:"h3"},"nwork")),Object(o.a)("p",null,Object(o.a)("strong",{parentName:"p"},"nwork")," is a developer centered tool. ",Object(o.a)("strong",{parentName:"p"},"nwork")," is the tool we - maven users - need to check if the version of project we are working on is yet to be deployed to nexus or not. So basically it checks if the version is the same, and downloads the server's version and then compares binaries to local project's to check if we have missed to update the version in our pom.xml. I know I'm not the only one having pain with jar deployments to nexus. ",Object(o.a)("strong",{parentName:"p"},"nwork")," does other things as well to help me on on daily basis."),Object(o.a)("h3",{id:"ntemplate"},Object(o.a)("strong",{parentName:"h3"},"ntemplate")),Object(o.a)("p",null,Object(o.a)("strong",{parentName:"p"},"ntemplate")," is a file templating tool that replaces place-holders in the files with an evaluated expression. "),Object(o.a)("h3",{id:"njob"},Object(o.a)("strong",{parentName:"h3"},"njob")),Object(o.a)("p",null,Object(o.a)("strong",{parentName:"p"},"njob")," is a powerful terminal todo list"),Object(o.a)("h3",{id:"ndoc"},Object(o.a)("strong",{parentName:"h3"},"ndoc")),Object(o.a)("p",null,Object(o.a)("strong",{parentName:"p"},"ndoc")," is a javadoc generator. It supports standard format and adds markdown format."),Object(o.a)("h3",{id:"ndocusaurus"},Object(o.a)("strong",{parentName:"h3"},"ndocusaurus")),Object(o.a)("p",null,Object(o.a)("strong",{parentName:"p"},"ndocusaurus")," is a ",Object(o.a)("a",Object(n.a)({parentName:"p"},{href:"https://docusaurus.io"}),"Docusaurus 2")," toolbox that adds several features to the tool such as: "),Object(o.a)("ul",null,Object(o.a)("li",{parentName:"ul"},"templating (using ntemplate)"),Object(o.a)("li",{parentName:"ul"},"pdf generation")),Object(o.a)("h3",{id:"ntalk-agent"},Object(o.a)("strong",{parentName:"h3"},"ntalk-agent")),Object(o.a)("p",null,Object(o.a)("strong",{parentName:"p"},"ntalk-agent")," is a client-to-client communication broker used for sharing ",Object(o.a)("strong",{parentName:"p"},"nuts")," workspaces"),Object(o.a)("h3",{id:"nclown"},Object(o.a)("strong",{parentName:"h3"},"nclown")),Object(o.a)("p",null,Object(o.a)("strong",{parentName:"p"},"nclown")," is an angular web application frontend for ",Object(o.a)("strong",{parentName:"p"},Object(o.a)("inlineCode",{parentName:"strong"},"nuts")),". It helps navigating, searching and installing artifacts. It is intended to be a web admin tool as well."),Object(o.a)("h3",{id:"nserver"},Object(o.a)("strong",{parentName:"h3"},"nserver")),Object(o.a)("p",null,Object(o.a)("strong",{parentName:"p"},"nserver")," is a standalone application that runs a small http server that will expose a workspace as a remote repository to other ",Object(o.a)("strong",{parentName:"p"},"nuts")," installations. This is the simplest way to mirror a workspace and share artifacts between networked nodes."),Object(o.a)("h3",{id:"nwar"},Object(o.a)("strong",{parentName:"h3"},"nwar")),Object(o.a)("p",null,Object(o.a)("strong",{parentName:"p"},"nwar")," (for ",Object(o.a)("strong",{parentName:"p"},Object(o.a)("inlineCode",{parentName:"strong"},"nuts"))," Web Application Archive) is a web application that exposes ",Object(o.a)("strong",{parentName:"p"},"nserver")," as a war to be deployed on a more mature http server or container."),Object(o.a)("h3",{id:"ndexer"},Object(o.a)("strong",{parentName:"h3"},"ndexer")),Object(o.a)("p",null,Object(o.a)("strong",{parentName:"p"},"ndexer")," (for Indexer) is a lucene powered index for ",Object(o.a)("strong",{parentName:"p"},Object(o.a)("inlineCode",{parentName:"strong"},"nuts")),". It can be shared across multiple ",Object(o.a)("strong",{parentName:"p"},Object(o.a)("inlineCode",{parentName:"strong"},"nuts"))," workspaces and processes."),Object(o.a)("h2",{id:"library-projects"},"Library Projects"),Object(o.a)("p",null,"Library projects are several libraries that add ",Object(o.a)("strong",{parentName:"p"},Object(o.a)("inlineCode",{parentName:"strong"},"nuts"))," support in a particular environment or domain."),Object(o.a)("h3",{id:"nlib-tomcat-classloader"},Object(o.a)("strong",{parentName:"h3"},"nlib-tomcat-classloader")),Object(o.a)("p",null,'This is a must-have feature in your web application if deployed on Tomcat. It solves the following problem : a simple war application is surprisingly fat with too many jars (hundreds of Megas) you need to upload each time you change a single file or class in your web project. Basically all the jars included in the lib folder of the war are to be uploaded each time to the remote Tomcat server. The common solution is to use "provided" scope in maven and put your jars in Tomcat lib or ext folders. This is a bad approach if you are using a single Tomcat process for multiple applications. ',Object(o.a)("strong",{parentName:"p"},"nuts-tomcat-classloader")," simply uses ",Object(o.a)("strong",{parentName:"p"},"nuts")," to download libraries when the application is deployed based on the ",Object(o.a)("strong",{parentName:"p"},"pom.xml")," you provide and include them in the current web application class loader. Hence, the war becomes lighter than ever. ",Object(o.a)("strong",{parentName:"p"},"nuts")," cache mechanisms optimizes bandwidth and makes this more convenient by sharing the same jar files between applications depending on the same versions.\nAll you have to do is to add this library to your application and configure your ",Object(o.a)("strong",{parentName:"p"},"pom.xml")," accordingly."),Object(o.a)("h3",{id:"nlib-servlet"},Object(o.a)("strong",{parentName:"h3"},"nlib-servlet")),Object(o.a)("p",null,"Basically this is the simplest way to include ",Object(o.a)("strong",{parentName:"p"},"nserver")," into your web application."),Object(o.a)("h3",{id:"nlib-template"},Object(o.a)("strong",{parentName:"h3"},"nlib-template")),Object(o.a)("p",null,"This library provides helper methods to manipulate maven pom.xml and generate simple Java files while supporting ",Object(o.a)("strong",{parentName:"p"},Object(o.a)("inlineCode",{parentName:"strong"},"nuts"))," concepts. It is used in other tools that are meant to generate maven projects."),Object(o.a)("h3",{id:"nlib-talkagent"},Object(o.a)("strong",{parentName:"h3"},"nlib-talkagent")),Object(o.a)("p",null,"This library provides support for client to client communication"),Object(o.a)("h2",{id:"extensions"},"Extensions"),Object(o.a)("p",null,"Extensions provide extra feature to nuts."),Object(o.a)("h3",{id:"next-term"},Object(o.a)("strong",{parentName:"h3"},"next-term")),Object(o.a)("p",null,"This library provides rich terminal support (auto-complete, history) based on the JLine library"),Object(o.a)("h2",{id:"other-projects"},"Other Projects"),Object(o.a)("p",null,"Other projects you may encounter in the repository are WIP projects that may be continued or discontinued.\nThis includes : ",Object(o.a)("strong",{parentName:"p"},"nutsc")," (a native c bootstrapper) and ",Object(o.a)("strong",{parentName:"p"},"nuts-installer")," (a ",Object(o.a)("strong",{parentName:"p"},"nuts")," installer tool)"),Object(o.a)("h2",{id:"honorable-mentions"},"Honorable mentions"),Object(o.a)("p",null,"Although not included in this Git repository some other tools are based on ",Object(o.a)("strong",{parentName:"p"},Object(o.a)("inlineCode",{parentName:"strong"},"nuts"))," and hence are installable using ",Object(o.a)("inlineCode",{parentName:"p"},"nuts install the-app")," command. Those tools are published in other repositories."),Object(o.a)("h3",{id:"netbeans-launcher"},Object(o.a)("strong",{parentName:"h3"},"netbeans-launcher")),Object(o.a)("p",null,"this tool supports installation and launch of multiple netbeans instances in parallel. See ",Object(o.a)("a",Object(n.a)({parentName:"p"},{href:"https://github.com/thevpc/netbeans-launcher"}),"Netbeans Launcher GitHub Repository")),Object(o.a)("h3",{id:"pnote"},Object(o.a)("strong",{parentName:"h3"},"pnote")),Object(o.a)("p",null,"this tool is a multi purpose, developer oriented, Note taking application. See ",Object(o.a)("a",Object(n.a)({parentName:"p"},{href:"https://github.com/thevpc/pangaea-note"}),"Pangaea Note")),Object(o.a)("h3",{id:"upa-box"},Object(o.a)("strong",{parentName:"h3"},"upa-box")),Object(o.a)("p",null,"this tool supports creation of UPA aware projects. UPA is a non structured ORM for the Java Language. See ",Object(o.a)("a",Object(n.a)({parentName:"p"},{href:"https://github.com/thevpc/upa"}),"Netbeans Launcher GitHub Repository")),Object(o.a)("h3",{id:"vr-box"},Object(o.a)("strong",{parentName:"h3"},"vr-box")),Object(o.a)("p",null,"this tool supports creation of VR aware projects. VR is a web portal framework. See ",Object(o.a)("a",Object(n.a)({parentName:"p"},{href:"https://github.com/thevpc/vr"}),"Netbeans Launcher GitHub Repository")))}p.isMDXComponent=!0},117:function(e,t,a){"use strict";a.d(t,"a",(function(){return d}));var n=a(0),r=a.n(n);function o(e,t,a){return t in e?Object.defineProperty(e,t,{value:a,enumerable:!0,configurable:!0,writable:!0}):e[t]=a,e}function s(e,t){var a=Object.keys(e);if(Object.getOwnPropertySymbols){var n=Object.getOwnPropertySymbols(e);t&&(n=n.filter((function(t){return Object.getOwnPropertyDescriptor(e,t).enumerable}))),a.push.apply(a,n)}return a}function i(e){for(var t=1;t<arguments.length;t++){var a=null!=arguments[t]?arguments[t]:{};t%2?s(Object(a),!0).forEach((function(t){o(e,t,a[t])})):Object.getOwnPropertyDescriptors?Object.defineProperties(e,Object.getOwnPropertyDescriptors(a)):s(Object(a)).forEach((function(t){Object.defineProperty(e,t,Object.getOwnPropertyDescriptor(a,t))}))}return e}function l(e,t){if(null==e)return{};var a,n,r=function(e,t){if(null==e)return{};var a,n,r={},o=Object.keys(e);for(n=0;n<o.length;n++)a=o[n],t.indexOf(a)>=0||(r[a]=e[a]);return r}(e,t);if(Object.getOwnPropertySymbols){var o=Object.getOwnPropertySymbols(e);for(n=0;n<o.length;n++)a=o[n],t.indexOf(a)>=0||Object.prototype.propertyIsEnumerable.call(e,a)&&(r[a]=e[a])}return r}var c=r.a.createContext({}),p=function(e){var t=r.a.useContext(c),a=t;return e&&(a="function"==typeof e?e(t):i(i({},t),e)),a},u={inlineCode:"code",wrapper:function(e){var t=e.children;return r.a.createElement(r.a.Fragment,{},t)}},b=r.a.forwardRef((function(e,t){var a=e.components,n=e.mdxType,o=e.originalType,s=e.parentName,c=l(e,["components","mdxType","originalType","parentName"]),b=p(a),d=n,m=b["".concat(s,".").concat(d)]||b[d]||u[d]||o;return a?r.a.createElement(m,i(i({ref:t},c),{},{components:a})):r.a.createElement(m,i({ref:t},c))}));function d(e,t){var a=arguments,n=t&&t.mdxType;if("string"==typeof e||n){var o=a.length,s=new Array(o);s[0]=b;var i={};for(var l in t)hasOwnProperty.call(t,l)&&(i[l]=t[l]);i.originalType=e,i.mdxType="string"==typeof e?e:n,s[1]=i;for(var c=2;c<o;c++)s[c]=a[c];return r.a.createElement.apply(null,s)}return r.a.createElement.apply(null,a)}b.displayName="MDXCreateElement"}}]);