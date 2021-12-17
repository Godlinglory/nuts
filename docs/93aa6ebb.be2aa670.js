(window.webpackJsonp=window.webpackJsonp||[]).push([[41],{115:function(e,t,a){"use strict";a.d(t,"a",(function(){return j}));var n=a(0),l=a.n(n);function r(e,t,a){return t in e?Object.defineProperty(e,t,{value:a,enumerable:!0,configurable:!0,writable:!0}):e[t]=a,e}function c(e,t){var a=Object.keys(e);if(Object.getOwnPropertySymbols){var n=Object.getOwnPropertySymbols(e);t&&(n=n.filter((function(t){return Object.getOwnPropertyDescriptor(e,t).enumerable}))),a.push.apply(a,n)}return a}function i(e){for(var t=1;t<arguments.length;t++){var a=null!=arguments[t]?arguments[t]:{};t%2?c(Object(a),!0).forEach((function(t){r(e,t,a[t])})):Object.getOwnPropertyDescriptors?Object.defineProperties(e,Object.getOwnPropertyDescriptors(a)):c(Object(a)).forEach((function(t){Object.defineProperty(e,t,Object.getOwnPropertyDescriptor(a,t))}))}return e}function b(e,t){if(null==e)return{};var a,n,l=function(e,t){if(null==e)return{};var a,n,l={},r=Object.keys(e);for(n=0;n<r.length;n++)a=r[n],t.indexOf(a)>=0||(l[a]=e[a]);return l}(e,t);if(Object.getOwnPropertySymbols){var r=Object.getOwnPropertySymbols(e);for(n=0;n<r.length;n++)a=r[n],t.indexOf(a)>=0||Object.prototype.propertyIsEnumerable.call(e,a)&&(l[a]=e[a])}return l}var u=l.a.createContext({}),p=function(e){var t=l.a.useContext(u),a=t;return e&&(a="function"==typeof e?e(t):i(i({},t),e)),a},O={inlineCode:"code",wrapper:function(e){var t=e.children;return l.a.createElement(l.a.Fragment,{},t)}},o=l.a.forwardRef((function(e,t){var a=e.components,n=e.mdxType,r=e.originalType,c=e.parentName,u=b(e,["components","mdxType","originalType","parentName"]),o=p(a),j=n,d=o["".concat(c,".").concat(j)]||o[j]||O[j]||r;return a?l.a.createElement(d,i(i({ref:t},u),{},{components:a})):l.a.createElement(d,i({ref:t},u))}));function j(e,t){var a=arguments,n=t&&t.mdxType;if("string"==typeof e||n){var r=a.length,c=new Array(r);c[0]=o;var i={};for(var b in t)hasOwnProperty.call(t,b)&&(i[b]=t[b]);i.originalType=e,i.mdxType="string"==typeof e?e:n,c[1]=i;for(var u=2;u<r;u++)c[u]=a[u];return l.a.createElement.apply(null,c)}return l.a.createElement.apply(null,a)}o.displayName="MDXCreateElement"},96:function(e,t,a){"use strict";a.r(t),a.d(t,"frontMatter",(function(){return c})),a.d(t,"metadata",(function(){return i})),a.d(t,"rightToc",(function(){return b})),a.d(t,"default",(function(){return p}));var n=a(2),l=a(6),r=(a(0),a(115)),c={id:"install-cmd",title:"Install Command",sidebar_label:"Install Command"},i={unversionedId:"cmd/install-cmd",id:"cmd/install-cmd",isDocsHomePage:!1,title:"Install Command",description:"A part from URL and path based executions, an artifact should be installed to be run. Installation can be auto fired when you first execute the artifact (you will be prompted to install the artifact) or manually using the install command. Note that when you run directly a jar file as a path or url, the artifact will not be installed!",source:"@site/docs/cmd/install.md",permalink:"/nuts/docs/cmd/install-cmd",editUrl:"https://github.com/thevpc/nuts/edit/master/website/docs/cmd/install.md",sidebar_label:"Install Command",sidebar:"someSidebar",previous:{title:"Info Command",permalink:"/nuts/docs/cmd/info-cmd"},next:{title:"License Command",permalink:"/nuts/docs/cmd/license-cmd"}},b=[{value:"Purpose",id:"purpose",children:[]}],u={rightToc:b};function p(e){var t=e.components,a=Object(l.a)(e,["components"]);return Object(r.a)("wrapper",Object(n.a)({},u,a,{components:t,mdxType:"MDXLayout"}),Object(r.a)("p",null,"A part from URL and path based executions, an artifact should be installed to be run. Installation can be auto fired when you first execute the artifact (you will be prompted to install the artifact) or manually using the ",Object(r.a)("strong",{parentName:"p"},"install")," command. Note that when you run directly a jar file as a path or url, the artifact will not be installed!\nto install an application just type"),Object(r.a)("pre",null,Object(r.a)("code",Object(n.a)({parentName:"pre"},{}),"nuts install <your-artifact-query-here>\n")),Object(r.a)("p",null,"For example"),Object(r.a)("pre",null,Object(r.a)("code",Object(n.a)({parentName:"pre"},{}),"nuts install net.vpc.app:netbeans-launcher#1.2.2\n")),Object(r.a)("p",null,"you may use any artifact query (see search command section) to install a command."),Object(r.a)("pre",null,Object(r.a)("code",Object(n.a)({parentName:"pre"},{}),"nuts install net.vpc.app:netbeans-*\n")),Object(r.a)("p",null,"if the artifact is already installed, you should use the force flag (--force)"),Object(r.a)("pre",null,Object(r.a)("code",Object(n.a)({parentName:"pre"},{}),"nuts install net.vpc.app:netbeans-launcher#1.2.2\n#this second time we have to force install\nnuts install -- force net.vpc.app:netbeans-launcher#1.2.2\n")),Object(r.a)("p",null,"One exception is when you want to switch between multiple versions installed to set the default one, you can omit the --force flag. Actually, when multiple version of the same artifact are installed all of them are executable directly by specifying the right version. When you specify no version, the default one is selected for you. And to make is simple, the default one is the last one you ran an install command for it."),Object(r.a)("pre",null,Object(r.a)("code",Object(n.a)({parentName:"pre"},{}),"me@linux:~> nuts install net.vpc.app:netbeans-launcher#1.2.2\nme@linux:~> nuts netbeans-launcher\n1.2.2\nme@linux:~> nuts install net.vpc.app:netbeans-launcher#1.2.1\nme@linux:~> nuts netbeans-launcher\n1.2.1\nme@linux:~> nuts install net.vpc.app:netbeans-launcher#1.2.2\n1.2.2\n")),Object(r.a)("p",null,"You can find all installed artifacts using 'nuts search --installed' command"),Object(r.a)("h2",{id:"purpose"},"Purpose"),Object(r.a)("p",null,"The install command is used to install or reinstall packages."),Object(r.a)("ul",null,Object(r.a)("li",{parentName:"ul"},"A+B  : read A main package and B dependencies"),Object(r.a)("li",{parentName:"ul"},"A+B? : ask, if confirmed, read A main package and B dependencies."),Object(r.a)("li",{parentName:"ul"},"require : deploy package as 'required'"),Object(r.a)("li",{parentName:"ul"},"install : deploy package as 'installed'"),Object(r.a)("li",{parentName:"ul"},"nothing : do nothing")),Object(r.a)("p",null,"The available strategies are"),Object(r.a)("ul",null,Object(r.a)("li",{parentName:"ul"},"require   : install the package and all of its dependencies as required class installed package"),Object(r.a)("li",{parentName:"ul"},"install   : install the package and all of its dependencies as first class installed package"),Object(r.a)("li",{parentName:"ul"},"reinstall : re-install or re-required the package and all of its dependencies"),Object(r.a)("li",{parentName:"ul"},"repair    : repair (re-install or re-required) the given dependency")),Object(r.a)("p",null,'"required class installed package" can be removed (uninstalled automatically by nuts when none\nof the depending package is nomore installed.'),Object(r.a)("table",null,Object(r.a)("thead",{parentName:"table"},Object(r.a)("tr",{parentName:"thead"},Object(r.a)("th",Object(n.a)({parentName:"tr"},{align:null}),"Status/Strategy -> Status"),Object(r.a)("th",Object(n.a)({parentName:"tr"},{align:null}),"REQUIRE"),Object(r.a)("th",Object(n.a)({parentName:"tr"},{align:null}),"INSTALL"),Object(r.a)("th",Object(n.a)({parentName:"tr"},{align:null}),"REINSTALL"),Object(r.a)("th",Object(n.a)({parentName:"tr"},{align:null}),"REPAIR"))),Object(r.a)("tbody",{parentName:"table"},Object(r.a)("tr",{parentName:"tbody"},Object(r.a)("td",Object(n.a)({parentName:"tr"},{align:null}),"NOT_INSTALLED"),Object(r.a)("td",Object(n.a)({parentName:"tr"},{align:null}),"REQUIRED"),Object(r.a)("td",Object(n.a)({parentName:"tr"},{align:null}),"INSTALLED"),Object(r.a)("td",Object(n.a)({parentName:"tr"},{align:null}),"INSTALLED?"),Object(r.a)("td",Object(n.a)({parentName:"tr"},{align:null}),"ERROR")),Object(r.a)("tr",{parentName:"tbody"},Object(r.a)("td",Object(n.a)({parentName:"tr"},{align:null}),"INSTALLED"),Object(r.a)("td",Object(n.a)({parentName:"tr"},{align:null}),"INSTALLED REQUIRED"),Object(r.a)("td",Object(n.a)({parentName:"tr"},{align:null}),"INSTALLED?"),Object(r.a)("td",Object(n.a)({parentName:"tr"},{align:null}),"INSTALLED"),Object(r.a)("td",Object(n.a)({parentName:"tr"},{align:null}),"INSTALLED")),Object(r.a)("tr",{parentName:"tbody"},Object(r.a)("td",Object(n.a)({parentName:"tr"},{align:null}),"INSTALLED REQUIRED"),Object(r.a)("td",Object(n.a)({parentName:"tr"},{align:null}),"INSTALLED REQUIRED"),Object(r.a)("td",Object(n.a)({parentName:"tr"},{align:null}),"INSTALLED REQUIRED"),Object(r.a)("td",Object(n.a)({parentName:"tr"},{align:null}),"INSTALLED REQUIRED"),Object(r.a)("td",Object(n.a)({parentName:"tr"},{align:null}),"INSTALLED REQUIRED")),Object(r.a)("tr",{parentName:"tbody"},Object(r.a)("td",Object(n.a)({parentName:"tr"},{align:null}),"REQUIRED"),Object(r.a)("td",Object(n.a)({parentName:"tr"},{align:null}),"REQUIRED"),Object(r.a)("td",Object(n.a)({parentName:"tr"},{align:null}),"INSTALLED REQUIRED"),Object(r.a)("td",Object(n.a)({parentName:"tr"},{align:null}),"REQUIRED"),Object(r.a)("td",Object(n.a)({parentName:"tr"},{align:null}),"REQUIRED")),Object(r.a)("tr",{parentName:"tbody"},Object(r.a)("td",Object(n.a)({parentName:"tr"},{align:null}),"INSTALLED OBSOLETE"),Object(r.a)("td",Object(n.a)({parentName:"tr"},{align:null}),"INSTALLED REQUIRED OBSOLETE"),Object(r.a)("td",Object(n.a)({parentName:"tr"},{align:null}),"INSTALLED"),Object(r.a)("td",Object(n.a)({parentName:"tr"},{align:null}),"INSTALLED"),Object(r.a)("td",Object(n.a)({parentName:"tr"},{align:null}),"INSTALLED")),Object(r.a)("tr",{parentName:"tbody"},Object(r.a)("td",Object(n.a)({parentName:"tr"},{align:null}),"INSTALLED REQUIRED OBSOLETE"),Object(r.a)("td",Object(n.a)({parentName:"tr"},{align:null}),"INSTALLED REQUIRED"),Object(r.a)("td",Object(n.a)({parentName:"tr"},{align:null}),"INSTALLED REQUIRED"),Object(r.a)("td",Object(n.a)({parentName:"tr"},{align:null}),"INSTALLED REQUIRED"),Object(r.a)("td",Object(n.a)({parentName:"tr"},{align:null}),"INSTALLED REQUIRED")),Object(r.a)("tr",{parentName:"tbody"},Object(r.a)("td",Object(n.a)({parentName:"tr"},{align:null}),"REQUIRED OBSOLETE"),Object(r.a)("td",Object(n.a)({parentName:"tr"},{align:null}),"REQUIRED OBSOLETE"),Object(r.a)("td",Object(n.a)({parentName:"tr"},{align:null}),"INSTALLED REQUIRED"),Object(r.a)("td",Object(n.a)({parentName:"tr"},{align:null}),"REQUIRED"),Object(r.a)("td",Object(n.a)({parentName:"tr"},{align:null}),"REQUIRED")))),Object(r.a)("table",null,Object(r.a)("thead",{parentName:"table"},Object(r.a)("tr",{parentName:"thead"},Object(r.a)("th",Object(n.a)({parentName:"tr"},{align:null}),"Status/Strategy -> action"),Object(r.a)("th",Object(n.a)({parentName:"tr"},{align:null}),"REQUIRE"),Object(r.a)("th",Object(n.a)({parentName:"tr"},{align:null}),"INSTALL"),Object(r.a)("th",Object(n.a)({parentName:"tr"},{align:null}),"REINSTALL"),Object(r.a)("th",Object(n.a)({parentName:"tr"},{align:null}),"REPAIR"))),Object(r.a)("tbody",{parentName:"table"},Object(r.a)("tr",{parentName:"tbody"},Object(r.a)("td",Object(n.a)({parentName:"tr"},{align:null}),"NOT_INSTALLED"),Object(r.a)("td",Object(n.a)({parentName:"tr"},{align:null}),"require+require"),Object(r.a)("td",Object(n.a)({parentName:"tr"},{align:null}),"install+require"),Object(r.a)("td",Object(n.a)({parentName:"tr"},{align:null}),"install+require?"),Object(r.a)("td",Object(n.a)({parentName:"tr"},{align:null}),"error")),Object(r.a)("tr",{parentName:"tbody"},Object(r.a)("td",Object(n.a)({parentName:"tr"},{align:null}),"INSTALLED"),Object(r.a)("td",Object(n.a)({parentName:"tr"},{align:null}),"nothing+nothing"),Object(r.a)("td",Object(n.a)({parentName:"tr"},{align:null}),"install+require?"),Object(r.a)("td",Object(n.a)({parentName:"tr"},{align:null}),"install+require"),Object(r.a)("td",Object(n.a)({parentName:"tr"},{align:null}),"install+nothing")),Object(r.a)("tr",{parentName:"tbody"},Object(r.a)("td",Object(n.a)({parentName:"tr"},{align:null}),"INSTALLED REQUIRED"),Object(r.a)("td",Object(n.a)({parentName:"tr"},{align:null}),"nothing+nothing"),Object(r.a)("td",Object(n.a)({parentName:"tr"},{align:null}),"install+require?"),Object(r.a)("td",Object(n.a)({parentName:"tr"},{align:null}),"install+require"),Object(r.a)("td",Object(n.a)({parentName:"tr"},{align:null}),"install+nothing")),Object(r.a)("tr",{parentName:"tbody"},Object(r.a)("td",Object(n.a)({parentName:"tr"},{align:null}),"REQUIRED"),Object(r.a)("td",Object(n.a)({parentName:"tr"},{align:null}),"nothing+nothing"),Object(r.a)("td",Object(n.a)({parentName:"tr"},{align:null}),"install+nothing"),Object(r.a)("td",Object(n.a)({parentName:"tr"},{align:null}),"require+require"),Object(r.a)("td",Object(n.a)({parentName:"tr"},{align:null}),"require+nothing")),Object(r.a)("tr",{parentName:"tbody"},Object(r.a)("td",Object(n.a)({parentName:"tr"},{align:null}),"INSTALLED OBSOLETE"),Object(r.a)("td",Object(n.a)({parentName:"tr"},{align:null}),"install+require"),Object(r.a)("td",Object(n.a)({parentName:"tr"},{align:null}),"install+require"),Object(r.a)("td",Object(n.a)({parentName:"tr"},{align:null}),"install+require"),Object(r.a)("td",Object(n.a)({parentName:"tr"},{align:null}),"install+nothing")),Object(r.a)("tr",{parentName:"tbody"},Object(r.a)("td",Object(n.a)({parentName:"tr"},{align:null}),"INSTALLED REQUIRED OBSOLETE"),Object(r.a)("td",Object(n.a)({parentName:"tr"},{align:null}),"install+require"),Object(r.a)("td",Object(n.a)({parentName:"tr"},{align:null}),"install+require"),Object(r.a)("td",Object(n.a)({parentName:"tr"},{align:null}),"install+require"),Object(r.a)("td",Object(n.a)({parentName:"tr"},{align:null}),"install+nothing")),Object(r.a)("tr",{parentName:"tbody"},Object(r.a)("td",Object(n.a)({parentName:"tr"},{align:null}),"REQUIRED OBSOLETE"),Object(r.a)("td",Object(n.a)({parentName:"tr"},{align:null}),"require+require"),Object(r.a)("td",Object(n.a)({parentName:"tr"},{align:null}),"install+require"),Object(r.a)("td",Object(n.a)({parentName:"tr"},{align:null}),"require+require"),Object(r.a)("td",Object(n.a)({parentName:"tr"},{align:null}),"require+nothing")))))}p.isMDXComponent=!0}}]);