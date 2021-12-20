(window.webpackJsonp=window.webpackJsonp||[]).push([[5],{117:function(e,t,n){"use strict";n.d(t,"a",(function(){return m}));var a=n(0),o=n.n(a);function i(e,t,n){return t in e?Object.defineProperty(e,t,{value:n,enumerable:!0,configurable:!0,writable:!0}):e[t]=n,e}function r(e,t){var n=Object.keys(e);if(Object.getOwnPropertySymbols){var a=Object.getOwnPropertySymbols(e);t&&(a=a.filter((function(t){return Object.getOwnPropertyDescriptor(e,t).enumerable}))),n.push.apply(n,a)}return n}function s(e){for(var t=1;t<arguments.length;t++){var n=null!=arguments[t]?arguments[t]:{};t%2?r(Object(n),!0).forEach((function(t){i(e,t,n[t])})):Object.getOwnPropertyDescriptors?Object.defineProperties(e,Object.getOwnPropertyDescriptors(n)):r(Object(n)).forEach((function(t){Object.defineProperty(e,t,Object.getOwnPropertyDescriptor(n,t))}))}return e}function c(e,t){if(null==e)return{};var n,a,o=function(e,t){if(null==e)return{};var n,a,o={},i=Object.keys(e);for(a=0;a<i.length;a++)n=i[a],t.indexOf(n)>=0||(o[n]=e[n]);return o}(e,t);if(Object.getOwnPropertySymbols){var i=Object.getOwnPropertySymbols(e);for(a=0;a<i.length;a++)n=i[a],t.indexOf(n)>=0||Object.prototype.propertyIsEnumerable.call(e,n)&&(o[n]=e[n])}return o}var u=o.a.createContext({}),l=function(e){var t=o.a.useContext(u),n=t;return e&&(n="function"==typeof e?e(t):s(s({},t),e)),n},p={inlineCode:"code",wrapper:function(e){var t=e.children;return o.a.createElement(o.a.Fragment,{},t)}},d=o.a.forwardRef((function(e,t){var n=e.components,a=e.mdxType,i=e.originalType,r=e.parentName,u=c(e,["components","mdxType","originalType","parentName"]),d=l(n),m=a,f=d["".concat(r,".").concat(m)]||d[m]||p[m]||i;return n?o.a.createElement(f,s(s({ref:t},u),{},{components:n})):o.a.createElement(f,s({ref:t},u))}));function m(e,t){var n=arguments,a=t&&t.mdxType;if("string"==typeof e||a){var i=n.length,r=new Array(i);r[0]=d;var s={};for(var c in t)hasOwnProperty.call(t,c)&&(s[c]=t[c]);s.originalType=e,s.mdxType="string"==typeof e?e:a,r[1]=s;for(var u=2;u<i;u++)r[u]=n[u];return o.a.createElement.apply(null,r)}return o.a.createElement.apply(null,n)}d.displayName="MDXCreateElement"},55:function(e,t,n){"use strict";n.r(t),n.d(t,"frontMatter",(function(){return r})),n.d(t,"metadata",(function(){return s})),n.d(t,"rightToc",(function(){return c})),n.d(t,"default",(function(){return l}));var a=n(2),o=n(6),i=(n(0),n(117)),r={id:"automation",title:"Automation",sidebar_label:"Automation & DevOps"},s={unversionedId:"concepts/automation",id:"concepts/automation",isDocsHomePage:!1,title:"Automation",description:"`nuts` has been designed and implemented with automation and devops philosophy in mind.",source:"@site/docs/concepts/automation.md",permalink:"/nuts/docs/concepts/automation",editUrl:"https://github.com/thevpc/nuts/edit/master/website/docs/concepts/automation.md",sidebar_label:"Automation & DevOps",sidebar:"someSidebar",previous:{title:"Aliases, Imports & Launchers",permalink:"/nuts/docs/concepts/aliases"},next:{title:"Command Line Arguments",permalink:"/nuts/docs/concepts/commandline"}},c=[{value:"Install Automation",id:"install-automation",children:[]},{value:"Structured Output",id:"structured-output",children:[]},{value:"Environment Sensitive",id:"environment-sensitive",children:[]},{value:"Multi Platform",id:"multi-platform",children:[]},{value:"Workspace Isolation",id:"workspace-isolation",children:[]},{value:"Security Mechanisms",id:"security-mechanisms",children:[]}],u={rightToc:c};function l(e){var t=e.components,n=Object(o.a)(e,["components"]);return Object(i.a)("wrapper",Object(a.a)({},u,n,{components:t,mdxType:"MDXLayout"}),Object(i.a)("p",null,Object(i.a)("inlineCode",{parentName:"p"},"nuts")," has been designed and implemented with automation and devops philosophy in mind."),Object(i.a)("p",null,Object(i.a)("inlineCode",{parentName:"p"},"nuts")," Application Framework infrastructure provides a seamless support process automation\nwith structured output, including json, xml, yaml, tson and so on. You can for instance call the\nPOSIX ls command and get the file list as ",Object(i.a)("inlineCode",{parentName:"p"},"json"),". You can then process this ",Object(i.a)("inlineCode",{parentName:"p"},"json"),"\nand extract meaningful information and pass it to the next command using standard pipe mechanism.\nThink of this as a general pattern for any and all commands you can run via nuts. "),Object(i.a)("p",null,"Besides, automation includes dynamic classloading of on-the-fly dependencies (remotely resolved and downloaded)\nto make usage of a feature you need such as installing a tomcat version that is compatible with the jre version you run."),Object(i.a)("p",null,"Automation requires also partitioning, isolation, sand-boxing, security reinforcements and portability. This is ensured by workspace feature that helps isolating the application dependencies from other applications, authentication and authorisation mechanisms to limit access to nuts configurations (and hence available repositories used for dependency resolution) and to system resources (running with or without elevated privileges) and finally environment adaptability to handle appropriate support for each architecture (x86_32,itanium_64,...), operating system (linux, windows,...), shell (bash, zsh,...), platform (java, dotnet, ...) and desktop environment."),Object(i.a)("h2",{id:"install-automation"},"Install Automation"),Object(i.a)("p",null,"TODO..."),Object(i.a)("h2",{id:"structured-output"},"Structured Output"),Object(i.a)("p",null,"TODO..."),Object(i.a)("h2",{id:"environment-sensitive"},"Environment Sensitive"),Object(i.a)("p",null,"TODO..."),Object(i.a)("h2",{id:"multi-platform"},"Multi Platform"),Object(i.a)("p",null,"TODO..."),Object(i.a)("h2",{id:"workspace-isolation"},"Workspace Isolation"),Object(i.a)("p",null,"TODO..."),Object(i.a)("h2",{id:"security-mechanisms"},"Security Mechanisms"),Object(i.a)("p",null,"TODO..."))}l.isMDXComponent=!0}}]);