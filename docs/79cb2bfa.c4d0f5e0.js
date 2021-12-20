(window.webpackJsonp=window.webpackJsonp||[]).push([[33],{117:function(e,t,n){"use strict";n.d(t,"a",(function(){return p}));var a=n(0),c=n.n(a);function r(e,t,n){return t in e?Object.defineProperty(e,t,{value:n,enumerable:!0,configurable:!0,writable:!0}):e[t]=n,e}function o(e,t){var n=Object.keys(e);if(Object.getOwnPropertySymbols){var a=Object.getOwnPropertySymbols(e);t&&(a=a.filter((function(t){return Object.getOwnPropertyDescriptor(e,t).enumerable}))),n.push.apply(n,a)}return n}function i(e){for(var t=1;t<arguments.length;t++){var n=null!=arguments[t]?arguments[t]:{};t%2?o(Object(n),!0).forEach((function(t){r(e,t,n[t])})):Object.getOwnPropertyDescriptors?Object.defineProperties(e,Object.getOwnPropertyDescriptors(n)):o(Object(n)).forEach((function(t){Object.defineProperty(e,t,Object.getOwnPropertyDescriptor(n,t))}))}return e}function l(e,t){if(null==e)return{};var n,a,c=function(e,t){if(null==e)return{};var n,a,c={},r=Object.keys(e);for(a=0;a<r.length;a++)n=r[a],t.indexOf(n)>=0||(c[n]=e[n]);return c}(e,t);if(Object.getOwnPropertySymbols){var r=Object.getOwnPropertySymbols(e);for(a=0;a<r.length;a++)n=r[a],t.indexOf(n)>=0||Object.prototype.propertyIsEnumerable.call(e,n)&&(c[n]=e[n])}return c}var d=c.a.createContext({}),m=function(e){var t=c.a.useContext(d),n=t;return e&&(n="function"==typeof e?e(t):i(i({},t),e)),n},s={inlineCode:"code",wrapper:function(e){var t=e.children;return c.a.createElement(c.a.Fragment,{},t)}},u=c.a.forwardRef((function(e,t){var n=e.components,a=e.mdxType,r=e.originalType,o=e.parentName,d=l(e,["components","mdxType","originalType","parentName"]),u=m(n),p=a,f=u["".concat(o,".").concat(p)]||u[p]||s[p]||r;return n?c.a.createElement(f,i(i({ref:t},d),{},{components:n})):c.a.createElement(f,i({ref:t},d))}));function p(e,t){var n=arguments,a=t&&t.mdxType;if("string"==typeof e||a){var r=n.length,o=new Array(r);o[0]=u;var i={};for(var l in t)hasOwnProperty.call(t,l)&&(i[l]=t[l]);i.originalType=e,i.mdxType="string"==typeof e?e:a,o[1]=i;for(var d=2;d<r;d++)o[d]=n[d];return c.a.createElement.apply(null,o)}return c.a.createElement.apply(null,n)}u.displayName="MDXCreateElement"},88:function(e,t,n){"use strict";n.r(t),n.d(t,"frontMatter",(function(){return o})),n.d(t,"metadata",(function(){return i})),n.d(t,"rightToc",(function(){return l})),n.d(t,"default",(function(){return m}));var a=n(2),c=n(6),r=(n(0),n(117)),o={id:"cmds",title:"Nuts Commands",sidebar_label:"Nuts Commands"},i={unversionedId:"cmd/cmds",id:"cmd/cmds",isDocsHomePage:!1,title:"Nuts Commands",description:"Nuts supports multiple types of commands (internal , external), multiple types of execution (embedded, spawn, and system execution) and multiple modes of execution (effective, dry).",source:"@site/docs/cmd/commands.md",permalink:"/nuts/docs/cmd/cmds",editUrl:"https://github.com/thevpc/nuts/edit/master/website/docs/cmd/commands.md",sidebar_label:"Nuts Commands",sidebar:"someSidebar",previous:{title:"License Command",permalink:"/nuts/docs/cmd/license-cmd"},next:{title:"Search Command",permalink:"/nuts/docs/cmd/search-cmds"}},l=[{value:"1.1.7 search command",id:"117-search-command",children:[]},{value:"1.1.8 fetch command",id:"118-fetch-command",children:[]},{value:"update and check-updates commands",id:"update-and-check-updates-commands",children:[]},{value:"deploy and push commands",id:"deploy-and-push-commands",children:[]}],d={rightToc:l};function m(e){var t=e.components,n=Object(c.a)(e,["components"]);return Object(r.a)("wrapper",Object(a.a)({},d,n,{components:t,mdxType:"MDXLayout"}),Object(r.a)("p",null,"Nuts supports multiple types of commands (internal , external), multiple types of execution (embedded, spawn, and system execution) and multiple modes of execution (effective, dry)."),Object(r.a)("p",null,"Internal Commands include:"),Object(r.a)("ul",null,Object(r.a)("li",{parentName:"ul"},"welcome"),Object(r.a)("li",{parentName:"ul"},"help"),Object(r.a)("li",{parentName:"ul"},"version"),Object(r.a)("li",{parentName:"ul"},"info"),Object(r.a)("li",{parentName:"ul"},"license"),Object(r.a)("li",{parentName:"ul"},"search"),Object(r.a)("li",{parentName:"ul"},"fetch"),Object(r.a)("li",{parentName:"ul"},"which"),Object(r.a)("li",{parentName:"ul"},"exec"),Object(r.a)("li",{parentName:"ul"},"install"),Object(r.a)("li",{parentName:"ul"},"uninstall"),Object(r.a)("li",{parentName:"ul"},"update"),Object(r.a)("li",{parentName:"ul"},"reinstall"),Object(r.a)("li",{parentName:"ul"},"check-updates"),Object(r.a)("li",{parentName:"ul"},"deploy"),Object(r.a)("li",{parentName:"ul"},"push")),Object(r.a)("h3",{id:"117-search-command"},"1.1.7 search command"),Object(r.a)("h3",{id:"118-fetch-command"},"1.1.8 fetch command"),Object(r.a)("p",null,Object(r.a)("strong",{parentName:"p"},"fetch")," command is used to download content of an artifact when you exactly know of it's nuts long id (which is required). This will download a cached version of the artifact in the local machine (the artifact passes to 'fetched' status)"),Object(r.a)("pre",null,Object(r.a)("code",Object(a.a)({parentName:"pre"},{}),"me@linux:~> nuts fetch net.vpc.app:netbeans-launcher#1.2.2\n")),Object(r.a)("h3",{id:"update-and-check-updates-commands"},"update and check-updates commands"),Object(r.a)("h3",{id:"deploy-and-push-commands"},"deploy and push commands"))}m.isMDXComponent=!0}}]);