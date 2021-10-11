(window.webpackJsonp=window.webpackJsonp||[]).push([[20],{113:function(e,t,n){"use strict";n.d(t,"a",(function(){return u})),n.d(t,"b",(function(){return d}));var a=n(0),r=n.n(a);function c(e,t,n){return t in e?Object.defineProperty(e,t,{value:n,enumerable:!0,configurable:!0,writable:!0}):e[t]=n,e}function o(e,t){var n=Object.keys(e);if(Object.getOwnPropertySymbols){var a=Object.getOwnPropertySymbols(e);t&&(a=a.filter((function(t){return Object.getOwnPropertyDescriptor(e,t).enumerable}))),n.push.apply(n,a)}return n}function l(e){for(var t=1;t<arguments.length;t++){var n=null!=arguments[t]?arguments[t]:{};t%2?o(Object(n),!0).forEach((function(t){c(e,t,n[t])})):Object.getOwnPropertyDescriptors?Object.defineProperties(e,Object.getOwnPropertyDescriptors(n)):o(Object(n)).forEach((function(t){Object.defineProperty(e,t,Object.getOwnPropertyDescriptor(n,t))}))}return e}function i(e,t){if(null==e)return{};var n,a,r=function(e,t){if(null==e)return{};var n,a,r={},c=Object.keys(e);for(a=0;a<c.length;a++)n=c[a],t.indexOf(n)>=0||(r[n]=e[n]);return r}(e,t);if(Object.getOwnPropertySymbols){var c=Object.getOwnPropertySymbols(e);for(a=0;a<c.length;a++)n=c[a],t.indexOf(n)>=0||Object.prototype.propertyIsEnumerable.call(e,n)&&(r[n]=e[n])}return r}var s=r.a.createContext({}),p=function(e){var t=r.a.useContext(s),n=t;return e&&(n="function"==typeof e?e(t):l(l({},t),e)),n},u=function(e){var t=p(e.components);return r.a.createElement(s.Provider,{value:t},e.children)},b={inlineCode:"code",wrapper:function(e){var t=e.children;return r.a.createElement(r.a.Fragment,{},t)}},m=r.a.forwardRef((function(e,t){var n=e.components,a=e.mdxType,c=e.originalType,o=e.parentName,s=i(e,["components","mdxType","originalType","parentName"]),u=p(n),m=a,d=u["".concat(o,".").concat(m)]||u[m]||b[m]||c;return n?r.a.createElement(d,l(l({ref:t},s),{},{components:n})):r.a.createElement(d,l({ref:t},s))}));function d(e,t){var n=arguments,a=t&&t.mdxType;if("string"==typeof e||a){var c=n.length,o=new Array(c);o[0]=m;var l={};for(var i in t)hasOwnProperty.call(t,i)&&(l[i]=t[i]);l.originalType=e,l.mdxType="string"==typeof e?e:a,o[1]=l;for(var s=2;s<c;s++)o[s]=n[s];return r.a.createElement.apply(null,o)}return r.a.createElement.apply(null,n)}m.displayName="MDXCreateElement"},74:function(e,t,n){"use strict";n.r(t),n.d(t,"frontMatter",(function(){return o})),n.d(t,"metadata",(function(){return l})),n.d(t,"rightToc",(function(){return i})),n.d(t,"default",(function(){return p}));var a=n(2),r=n(6),c=(n(0),n(113)),o={id:"search-cmds",title:"Search Command",sidebar_label:"Search Command"},l={unversionedId:"cmd/search-cmds",id:"cmd/search-cmds",isDocsHomePage:!1,title:"Search Command",description:"Artifact can be in multiple states. they can be",source:"@site/docs/cmd/serach-cmd.md",permalink:"/nuts/docs/cmd/search-cmds",editUrl:"https://github.com/thevpc/nuts/edit/master/website/docs/cmd/serach-cmd.md",sidebar_label:"Search Command",sidebar:"someSidebar",previous:{title:"Nuts Commands",permalink:"/nuts/docs/cmd/cmds"},next:{title:"Version Command",permalink:"/nuts/docs/cmd/version-cmd"}},i=[],s={rightToc:i};function p(e){var t=e.components,n=Object(r.a)(e,["components"]);return Object(c.b)("wrapper",Object(a.a)({},s,n,{components:t,mdxType:"MDXLayout"}),Object(c.b)("p",null,"Artifact can be in multiple states. they can be"),Object(c.b)("ul",null,Object(c.b)("li",{parentName:"ul"},"'unavailable' if no registered repository can serve that artifact"),Object(c.b)("li",{parentName:"ul"},"'available' if there is at least one repository that can serve that artifact"),Object(c.b)("li",{parentName:"ul"},"'fetched' if there is a repository that can serve the artifact from local machine. This happens either if the repository is a local one (for instance a folder repository) or the repository has already downloaded and cached the artifact"),Object(c.b)("li",{parentName:"ul"},"'installed' if the artifact is fetched and installed in the the machine."),Object(c.b)("li",{parentName:"ul"},"'installed default' if the artifact is installed and marked as default")),Object(c.b)("p",null,"To search for these artifacts status you will use the appropriate option flag with an artifact query.\nAn artifact query is a generalization of an artifact id where you may use wild cards and version intervals in it.\nThese are some examples of artifact queries."),Object(c.b)("pre",null,Object(c.b)("code",Object(a.a)({parentName:"pre"},{}),"# all artifacts that start with netbeans, whatever groupId they belong to\n# nuts search netbeans*\n\n# all artifacts that start with netbeans, whatever groupId they belong to. same as the latter.\n# nuts search *:netbeans*\n\n# all artifacts in the net.vpc.app groupId\n# nuts search net.vpc.*:*\n\n# all artifacts in the net.vpc.* groupId which includes all of net.vpc.app and net.vpc.app.example for instance.\n# nuts search net.vpc.*:*\n\n# all artifacts that start with netbeans and is available for windows operating system in x86_64 architecture\n# nuts search netbeans*?os=windows&arch=x86_64\n\n# all netbeans launcher version that are greater than 1.2.0 (excluding 1.2.0)\n# nuts search netbeans-launcher#]1.2.0,[\n\n# all netbeans launcher version that are greater than 1.2.0 (including 1.2.0)\n# nuts search netbeans-launcher#[1.2.0,[\n\n")),Object(c.b)("p",null,"You can then use the these flags to tighten your search :"),Object(c.b)("ul",null,Object(c.b)("li",{parentName:"ul"},"--installed (or -i) : search only for installed artifacts"),Object(c.b)("li",{parentName:"ul"},"--local     : search only for fetched artifacts"),Object(c.b)("li",{parentName:"ul"},"--remote    : search only for non fetched artifacts"),Object(c.b)("li",{parentName:"ul"},"--online    : search in installed then in local then in remote, stop when you first find a result."),Object(c.b)("li",{parentName:"ul"},"--anywhere  (or -a) : search in installed and local and remote, return all results.")),Object(c.b)("p",null,"You can also change the output layout using --long (or -l) flag"),Object(c.b)("pre",null,Object(c.b)("code",Object(a.a)({parentName:"pre"},{}),"me@linux:~> nuts search -i -l\nI-X 2019-08-26 09:53:53.141 anonymous vpc-public-maven net.vpc.app:netbeans-launcher#1.2.1\nIcX 2019-08-24 11:05:49.591 admin     maven-local      net.vpc.app.nuts.toolbox:nsh#0.5.7.0\nIcX 2019-08-24 11:05:58.143 admin     maven-local      net.vpc.app.nuts.toolbox:ndi#0.5.7.0\nIcX 2019-08-24 11:05:49.927 admin     maven-local      net.vpc.app.nuts.toolbox:nadmin#0.5.7.0\nI-x 2019-08-26 09:50:03.423 anonymous vpc-public-maven net.vpc.app:kifkif#1.3.3\n")),Object(c.b)("p",null,"you can even change the output format"),Object(c.b)("pre",null,Object(c.b)("code",Object(a.a)({parentName:"pre"},{}),"me@linux:~> nuts search -i -l --json\n")),Object(c.b)("pre",null,Object(c.b)("code",Object(a.a)({parentName:"pre"},{className:"language-json"}),'[\n{\n  "id": "vpc-public-maven://net.vpc.app:netbeans-launcher#1.2.1",\n  "descriptor": {\n    "id": "net.vpc.app:netbeans-launcher#1.2.1",\n    "parents": [],\n    "packaging": "jar",\n    "executable": true,\n    ...\n  }\n }\n]\n')),Object(c.b)("p",null,"Indeed, all of ",Object(c.b)("strong",{parentName:"p"},"nuts")," commands support the following formats : ",Object(c.b)("strong",{parentName:"p"},"plain"),", ",Object(c.b)("strong",{parentName:"p"},"json"),", ",Object(c.b)("strong",{parentName:"p"},"xml"),", ",Object(c.b)("strong",{parentName:"p"},"table")," and ",Object(c.b)("strong",{parentName:"p"},"tree")," because ",Object(c.b)("strong",{parentName:"p"},"nuts")," adds support to multi format output by default. You can switch to any of them for any command by adding the right option in ",Object(c.b)("strong",{parentName:"p"},"nuts")," (typically --plain, --json, --xml, --table and --tree). I know this is awesome!."),Object(c.b)("p",null,Object(c.b)("strong",{parentName:"p"},"search"),' is a very versatile command, you are welcome to run "nuts search --help" to get more information.'))}p.isMDXComponent=!0}}]);