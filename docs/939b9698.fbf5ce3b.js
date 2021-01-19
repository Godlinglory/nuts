(window.webpackJsonp=window.webpackJsonp||[]).push([[33],{112:function(e,t,n){"use strict";n.d(t,"a",(function(){return b})),n.d(t,"b",(function(){return f}));var a=n(0),o=n.n(a);function r(e,t,n){return t in e?Object.defineProperty(e,t,{value:n,enumerable:!0,configurable:!0,writable:!0}):e[t]=n,e}function i(e,t){var n=Object.keys(e);if(Object.getOwnPropertySymbols){var a=Object.getOwnPropertySymbols(e);t&&(a=a.filter((function(t){return Object.getOwnPropertyDescriptor(e,t).enumerable}))),n.push.apply(n,a)}return n}function c(e){for(var t=1;t<arguments.length;t++){var n=null!=arguments[t]?arguments[t]:{};t%2?i(Object(n),!0).forEach((function(t){r(e,t,n[t])})):Object.getOwnPropertyDescriptors?Object.defineProperties(e,Object.getOwnPropertyDescriptors(n)):i(Object(n)).forEach((function(t){Object.defineProperty(e,t,Object.getOwnPropertyDescriptor(n,t))}))}return e}function s(e,t){if(null==e)return{};var n,a,o=function(e,t){if(null==e)return{};var n,a,o={},r=Object.keys(e);for(a=0;a<r.length;a++)n=r[a],t.indexOf(n)>=0||(o[n]=e[n]);return o}(e,t);if(Object.getOwnPropertySymbols){var r=Object.getOwnPropertySymbols(e);for(a=0;a<r.length;a++)n=r[a],t.indexOf(n)>=0||Object.prototype.propertyIsEnumerable.call(e,n)&&(o[n]=e[n])}return o}var l=o.a.createContext({}),p=function(e){var t=o.a.useContext(l),n=t;return e&&(n="function"==typeof e?e(t):c(c({},t),e)),n},b=function(e){var t=p(e.components);return o.a.createElement(l.Provider,{value:t},e.children)},d={inlineCode:"code",wrapper:function(e){var t=e.children;return o.a.createElement(o.a.Fragment,{},t)}},u=o.a.forwardRef((function(e,t){var n=e.components,a=e.mdxType,r=e.originalType,i=e.parentName,l=s(e,["components","mdxType","originalType","parentName"]),b=p(n),u=a,f=b["".concat(i,".").concat(u)]||b[u]||d[u]||r;return n?o.a.createElement(f,c(c({ref:t},l),{},{components:n})):o.a.createElement(f,c({ref:t},l))}));function f(e,t){var n=arguments,a=t&&t.mdxType;if("string"==typeof e||a){var r=n.length,i=new Array(r);i[0]=u;var c={};for(var s in t)hasOwnProperty.call(t,s)&&(c[s]=t[s]);c.originalType=e,c.mdxType="string"==typeof e?e:a,i[1]=c;for(var l=2;l<r;l++)i[l]=n[l];return o.a.createElement.apply(null,i)}return o.a.createElement.apply(null,n)}u.displayName="MDXCreateElement"},87:function(e,t,n){"use strict";n.r(t),n.d(t,"frontMatter",(function(){return i})),n.d(t,"metadata",(function(){return c})),n.d(t,"rightToc",(function(){return s})),n.d(t,"default",(function(){return p}));var a=n(2),o=n(6),r=(n(0),n(112)),i={id:"filesystem",title:"File system",sidebar_label:"File system"},c={unversionedId:"advanced/filesystem",id:"advanced/filesystem",isDocsHomePage:!1,title:"File system",description:"`",source:"@site/docs/advanced/filesystem.md",permalink:"/nuts/docs/advanced/filesystem",editUrl:"https://github.com/facebook/docusaurus/edit/master/website/docs/advanced/filesystem.md",sidebar_label:"File system",sidebar:"someSidebar",previous:{title:"Automation",permalink:"/nuts/docs/advanced/automation"},next:{title:"Portable Workspaces",permalink:"/nuts/docs/advanced/portableWorkspaces"}},s=[{value:"Store Locations",id:"store-locations",children:[]},{value:"Store Location Strategies",id:"store-location-strategies",children:[]},{value:"Custom Store Locations",id:"custom-store-locations",children:[{value:"Selecting strategies",id:"selecting-strategies",children:[]},{value:"Finer Customization",id:"finer-customization",children:[]}]}],l={rightToc:s};function p(e){var t=e.components,n=Object(o.a)(e,["components"]);return Object(r.b)("wrapper",Object(a.a)({},l,n,{components:t,mdxType:"MDXLayout"}),Object(r.b)("pre",null,Object(r.b)("code",Object(a.a)({parentName:"pre"},{}),"     __        __    \n  /\\ \\ \\ _  __/ /______\n /  \\/ / / / / __/ ___/\n/ /\\  / /_/ / /_(__  )\n\\_\\ \\/\\__,_/\\__/____/    version v0.8.1\n")),Object(r.b)("p",null,Object(r.b)("strong",{parentName:"p"},Object(r.b)("inlineCode",{parentName:"strong"},"nuts"))," manages multiple workspaces. It has a default one located at ~/.config/nuts (~ is the user home directory). Each workspace handles a database and files related to the installed applications. The workspace has a specific layout to store different types of files relatives to your applications. ",Object(r.b)("strong",{parentName:"p"},"Nuts")," is largely inspired by ",Object(r.b)("a",Object(a.a)({parentName:"p"},{href:"https://specifications.freedesktop.org/basedir-spec/basedir-spec-latest.html"}),"XDG Base Directory Specification")," and hence defines several  store locations for each file type. Such organization of folders is called Layout and is dependent on the current operating system, the layout strategy and any custom configuration."),Object(r.b)("h2",{id:"store-locations"},"Store Locations"),Object(r.b)("p",null,"Supported Store Locations are :\n",Object(r.b)("strong",{parentName:"p"},Object(r.b)("inlineCode",{parentName:"strong"},"nuts"))," File System defines the following folders :"),Object(r.b)("ul",null,Object(r.b)("li",{parentName:"ul"},Object(r.b)("strong",{parentName:"li"},"config")," : defines the base directory relative to which application specific configuration files should be stored."),Object(r.b)("li",{parentName:"ul"},Object(r.b)("strong",{parentName:"li"},"apps")," : defines the base directory relative to which application executable binaries should be stored "),Object(r.b)("li",{parentName:"ul"},Object(r.b)("strong",{parentName:"li"},"lib")," : defines the base directory relative to which application non executable binaries should be stored "),Object(r.b)("li",{parentName:"ul"},Object(r.b)("strong",{parentName:"li"},"var")," : defines the base directory relative to which application specific data files (other than config) should be stored"),Object(r.b)("li",{parentName:"ul"},Object(r.b)("strong",{parentName:"li"},"log")," : defines the base directory relative to which application specific log and trace files should be stored"),Object(r.b)("li",{parentName:"ul"},Object(r.b)("strong",{parentName:"li"},"temp")," : defines the base directory relative to which application specific temporary files should be stored"),Object(r.b)("li",{parentName:"ul"},Object(r.b)("strong",{parentName:"li"},"cache")," : defines the base directory relative to which application non-essential data and binary files should be stored to optimize bandwidth or performance"),Object(r.b)("li",{parentName:"ul"},Object(r.b)("strong",{parentName:"li"},"run")," : defines the base directory relative to which application-specific non-essential runtime files and other file objects (such as sockets, named pipes, ...) should be stored")),Object(r.b)("p",null,Object(r.b)("strong",{parentName:"p"},Object(r.b)("inlineCode",{parentName:"strong"},"nuts"))," defines such distinct folders (named Store Locations) for storing different types of application data according to your operating system."),Object(r.b)("p",null,"On Windows Systems the default locations are :"),Object(r.b)("pre",null,Object(r.b)("code",Object(a.a)({parentName:"pre"},{}),'    * apps     : "$HOME/AppData/Roaming/nuts/apps"\n    * lib      : "$HOME/AppData/Roaming/nuts/lib"\n    * config   : "$HOME/AppData/Roaming/nuts/config"\n    * var      : "$HOME/AppData/Roaming/nuts/var"\n    * log      : "$HOME/AppData/Roaming/nuts/log"\n    * temp     : "$HOME/AppData/Local/nuts/temp"\n    * cache    : "$HOME/AppData/Local/nuts/cache"\n    * run      : "$HOME/AppData/Local/nuts/run"\n')),Object(r.b)("p",null,"On Linux, Unix, MacOS and any POSIX System the default locations are :"),Object(r.b)("pre",null,Object(r.b)("code",Object(a.a)({parentName:"pre"},{}),'    * config   : "$HOME/.config/nuts"\n    * apps     : "$HOME/.local/share/nuts/apps"\n    * lib      : "$HOME/.local/share/nuts/lib"\n    * var      : "$HOME/.local/share/nuts/var"\n    * log      : "$HOME/.local/log/nuts"\n    * cache    : "$HOME/.cache/nuts"\n    * temp     : "$java.io.tmpdir/$username/nuts"\n    * run      : "/run/user/$USER_ID/nuts"\n')),Object(r.b)("p",null,"As an example, the configuration folder for the artifact net.vpc.app:netbeans-launcher#1.2.4 in the default workspace in a Linux environment is"),Object(r.b)("pre",null,Object(r.b)("code",Object(a.a)({parentName:"pre"},{}),"home/me/.config/nuts/default-workspace/config/id/net/vpc/app/netbeans-launcher/1.2.4/\n")),Object(r.b)("p",null,'And the log file "app.log" for the same artifact in the workspace named "personal" in a Windows environment is located at'),Object(r.b)("pre",null,Object(r.b)("code",Object(a.a)({parentName:"pre"},{}),"C:/Users/me/AppData/Roaming/nuts/log/nuts/personal/config/id/net/vpc/app/netbeans-launcher/1.2.4/app.log\n")),Object(r.b)("h2",{id:"store-location-strategies"},"Store Location Strategies"),Object(r.b)("p",null,"When you install any application using the ",Object(r.b)("strong",{parentName:"p"},Object(r.b)("inlineCode",{parentName:"strong"},"nuts"))," command a set of specific folders for the presented Store Locations are created. For that,\ntwo strategies exist : ",Object(r.b)("strong",{parentName:"p"},"Exploded strategy")," (the default) and ",Object(r.b)("strong",{parentName:"p"},"Standalone strategy"),".  "),Object(r.b)("p",null,"In ",Object(r.b)("strong",{parentName:"p"},"Exploded strategy"),"  ",Object(r.b)("strong",{parentName:"p"},Object(r.b)("inlineCode",{parentName:"strong"},"nuts"))," defines top level folders (in linux ~/.config for config Store Location etc), and then creates withing each top level Store Location a sub folder for the given application (or application version to be more specific). This helps putting all your config files in a SSD partition for instance and make ",Object(r.b)("strong",{parentName:"p"},"Nuts")," run faster. However if you are interested in the backup or roaming of your workspace, this may be not the best approach."),Object(r.b)("p",null,"The ",Object(r.b)("strong",{parentName:"p"},"Standalone strategy"),"   is indeed provided mainly for Roaming workspaces that can be shared, copied, moved to other locations. A single root folder will contain all of the Store Locations."),Object(r.b)("p",null,'As an example, in "Standalone Strategy", the configuration folder for the artifact net.vpc.app:netbeans-launcher#1.2.4 in the default workspace in a Linux environment is'),Object(r.b)("pre",null,Object(r.b)("code",Object(a.a)({parentName:"pre"},{}),"home/me/.config/nuts/default-workspace/config/id/net/vpc/app/netbeans-launcher/1.2.4/\n")),Object(r.b)("p",null,'And the log file "app.log" for the same artifact in the workspace named "personal" in the same Linux environment is located at'),Object(r.b)("pre",null,Object(r.b)("code",Object(a.a)({parentName:"pre"},{}),"/home/me/.config/nuts/default-workspace/log/id/net/vpc/app/netbeans-launcher/1.2.4/\n")),Object(r.b)("p",null,"You can see here that the following folder will contain ALL the data files of the workspace."),Object(r.b)("pre",null,Object(r.b)("code",Object(a.a)({parentName:"pre"},{}),"/home/me/.config/nuts/default-workspace\n")),Object(r.b)("p",null,"whereas in the ",Object(r.b)("strong",{parentName:"p"},"Exploded strategy"),' the Store Location are "exploded" into multiple root folders.'),Object(r.b)("h2",{id:"custom-store-locations"},"Custom Store Locations"),Object(r.b)("p",null,"Of course, your able to configure separately each Store Location to meet your needs."),Object(r.b)("h3",{id:"selecting-strategies"},"Selecting strategies"),Object(r.b)("p",null,"The following command will create an exploded workspace"),Object(r.b)("pre",null,Object(r.b)("code",Object(a.a)({parentName:"pre"},{}),"nuts -w my-workspace --exploded\n")),Object(r.b)("p",null,"The following comman will create an standalone workspace"),Object(r.b)("pre",null,Object(r.b)("code",Object(a.a)({parentName:"pre"},{}),"nuts -w my-workspace --standalone\n")),Object(r.b)("h3",{id:"finer-customization"},"Finer Customization"),Object(r.b)("p",null,"The following command will create an exploded workspace and moves all config files to the SSD partition folder /myssd/myconfig"),Object(r.b)("pre",null,Object(r.b)("code",Object(a.a)({parentName:"pre"},{}),"nuts -w my-workspace --system-config-home=/myssd/myconfig\n")),Object(r.b)("p",null,"You can type help for more details."),Object(r.b)("pre",null,Object(r.b)("code",Object(a.a)({parentName:"pre"},{}),"nuts help\n")))}p.isMDXComponent=!0}}]);