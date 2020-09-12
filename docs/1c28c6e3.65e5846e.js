(window.webpackJsonp=window.webpackJsonp||[]).push([[9],{114:function(e,t,a){"use strict";a.r(t),a.d(t,"frontMatter",(function(){return O})),a.d(t,"metadata",(function(){return v})),a.d(t,"rightToc",(function(){return h})),a.d(t,"default",(function(){return g}));var n=a(2),i=a(6),r=a(0),s=a.n(r),c=a(118),l=a(126),o=a(120),b=a(63),p=a.n(b);const m=37,u=39;var d=function(e){const{block:t,children:a,defaultValue:n,values:i,groupId:c}=e,{tabGroupChoices:b,setTabGroupChoices:d}=Object(l.a)(),[j,O]=Object(r.useState)(n),[v,h]=Object(r.useState)(!1);if(null!=c){const e=b[c];null!=e&&e!==j&&i.some(t=>t.value===e)&&O(e)}const N=e=>{O(e),null!=c&&d(c,e)},g=[],f=e=>{e.metaKey||e.altKey||e.ctrlKey||h(!0)},w=()=>{h(!1)};return Object(r.useEffect)(()=>{window.addEventListener("keydown",f),window.addEventListener("mousedown",w)},[]),s.a.createElement("div",null,s.a.createElement("ul",{role:"tablist","aria-orientation":"horizontal",className:Object(o.a)("tabs",{"tabs--block":t})},i.map(({value:e,label:t})=>s.a.createElement("li",{role:"tab",tabIndex:0,"aria-selected":j===e,className:Object(o.a)("tabs__item",p.a.tabItem,{"tabs__item--active":j===e}),style:v?{}:{outline:"none"},key:e,ref:e=>g.push(e),onKeyDown:e=>{((e,t,a)=>{switch(a.keyCode){case u:((e,t)=>{const a=e.indexOf(t)+1;e[a]?e[a].focus():e[0].focus()})(e,t);break;case m:((e,t)=>{const a=e.indexOf(t)-1;e[a]?e[a].focus():e[e.length-1].focus()})(e,t)}})(g,e.target,e),f(e)},onFocus:()=>N(e),onClick:()=>{N(e),h(!1)},onPointerDown:()=>h(!1)},t))),s.a.createElement("div",{role:"tabpanel",className:"margin-vert--md"},r.Children.toArray(a).filter(e=>e.props.value===j)[0]))};var j=function(e){return s.a.createElement("div",null,e.children)},O={id:"installation",title:"Installation",sidebar_label:"Installation"},v={unversionedId:"get-started/installation",id:"get-started/installation",isDocsHomePage:!0,title:"Installation",description:"`",source:"@site/docs/get-started/installation.md",permalink:"/nuts/docs/",editUrl:"https://github.com/facebook/docusaurus/edit/master/website/docs/get-started/installation.md",sidebar_label:"Installation",sidebar:"someSidebar",previous:{title:"License",permalink:"/nuts/docs/intro/license"},next:{title:"Running Nuts",permalink:"/nuts/docs/get-started/running"}},h=[{value:"System Requirements",id:"system-requirements",children:[]},{value:"Installation",id:"installation",children:[]},{value:"Test Installation",id:"test-installation",children:[]},{value:"Run a command",id:"run-a-command",children:[]}],N={rightToc:h};function g(e){var t=e.components,r=Object(i.a)(e,["components"]);return Object(c.b)("wrapper",Object(n.a)({},N,r,{components:t,mdxType:"MDXLayout"}),Object(c.b)("pre",null,Object(c.b)("code",Object(n.a)({parentName:"pre"},{}),"     __        __    \n  /\\ \\ \\ _  __/ /______\n /  \\/ / / / / __/ ___/\n/ /\\  / /_/ / /_(__  )\n\\_\\ \\/\\__,_/\\__/____/    version v0.7.0\n")),Object(c.b)("h2",{id:"system-requirements"},"System Requirements"),Object(c.b)("p",null,"Here are all ",Object(c.b)("strong",{parentName:"p"},Object(c.b)("inlineCode",{parentName:"strong"},"nuts"))," requirements :"),Object(c.b)("ul",null,Object(c.b)("li",{parentName:"ul"},Object(c.b)("strong",{parentName:"li"},"Java")," : ",Object(c.b)("strong",{parentName:"li"},Object(c.b)("inlineCode",{parentName:"strong"},"nuts"))," requires a valid Java Runtime Environment (JRE) or Java Development Kit (JDK) version ",Object(c.b)("strong",{parentName:"li"},"8")," or above to execute."),Object(c.b)("li",{parentName:"ul"},Object(c.b)("strong",{parentName:"li"},"System Memory"),": ",Object(c.b)("strong",{parentName:"li"},Object(c.b)("inlineCode",{parentName:"strong"},"nuts"))," memory footprint is very little and has no minimum RAM requirements."),Object(c.b)("li",{parentName:"ul"},Object(c.b)("strong",{parentName:"li"},"Disk"),": 2.5Mo on the disk are required for the ",Object(c.b)("strong",{parentName:"li"},Object(c.b)("inlineCode",{parentName:"strong"},"nuts"))," installation itself. In addition to that, additional disk space will be used for your local Nuts workspace. The size of your local workspace will vary depending on usage but expect at least 500MB."),Object(c.b)("li",{parentName:"ul"},Object(c.b)("strong",{parentName:"li"},"Operating System"),": ",Object(c.b)("strong",{parentName:"li"},Object(c.b)("inlineCode",{parentName:"strong"},"nuts"))," is able to run on any java enabled Operating System including all recent versions of Windows, Linux and MacOS.")),Object(c.b)("p",null,"To check if you have a valid java installation type"),Object(c.b)("pre",null,Object(c.b)("code",Object(n.a)({parentName:"pre"},{className:"language-bash"}),"java -version\n")),Object(c.b)("p",null,"The result would be equivalent to the following. Just be sure the version is 1.8 or over. In this example,\nthe java version is 13.0.1"),Object(c.b)("pre",null,Object(c.b)("code",Object(n.a)({parentName:"pre"},{className:"language-bash"}),'$> java -version\njava version "13.0.1" 2019-10-15\nJava(TM) SE Runtime Environment (build 13.0.1+9)\nJava HotSpot(TM) 64-Bit Server VM (build 13.0.1+9, mixed mode, sharing)\n')),Object(c.b)("h2",{id:"installation"},"Installation"),Object(c.b)(d,{defaultValue:"linux",values:[{label:"Linux",value:"linux"},{label:"MacOS",value:"macos"},{label:"Windows",value:"windows"},{label:"*NIX wget",value:"wget"},{label:"*NIX curl",value:"curl"},{label:"Any Java enabled OS",value:"java"}],mdxType:"Tabs"},Object(c.b)(j,{value:"windows",mdxType:"TabItem"},Object(c.b)("p",null,"download ",Object(c.b)("a",Object(n.a)({parentName:"p"},{href:"https://github.com/thevpc/vpc-public-maven/raw/master/net/vpc/app/nuts/nuts/0.7.0/nuts-0.7.0.jar"}),"nuts-0.7.0.jar")),Object(c.b)("pre",null,Object(c.b)("code",Object(n.a)({parentName:"pre"},{}),"java -jar -y nuts-0.7.0.jar\n")),Object(c.b)("p",null,"On Windows systems, first launch will create a new ",Object(c.b)("strong",{parentName:"p"},Object(c.b)("inlineCode",{parentName:"strong"},"nuts"))," Menu (under Programs) and a couple of Desktop shortcuts to launch a configured command terminal."),Object(c.b)("ul",null,Object(c.b)("li",{parentName:"ul"},Object(c.b)("strong",{parentName:"li"},"nuts-cmd-0.7.0")," : this shortcut will open a configured command terminal. ",Object(c.b)("strong",{parentName:"li"},Object(c.b)("inlineCode",{parentName:"strong"},"nuts"))," command will be available as well\nas several nuts companion tools installed by ",Object(c.b)("strong",{parentName:"li"},"ndi")," by default"),Object(c.b)("li",{parentName:"ul"},Object(c.b)("strong",{parentName:"li"},"nuts-cmd"),"       : this shortcut will point to the last installed ",Object(c.b)("strong",{parentName:"li"},"nuts")," version, here 0.7.0  ")),Object(c.b)("p",null,"Any of these shortcuts will launch a nuts-aware terminal."),Object(c.b)("p",null,"Supported Windows systems include Window 7 and later."),Object(c.b)("div",{className:"admonition admonition-tip alert alert--success"},Object(c.b)("div",Object(n.a)({parentName:"div"},{className:"admonition-heading"}),Object(c.b)("h5",{parentName:"div"},Object(c.b)("span",Object(n.a)({parentName:"h5"},{className:"admonition-icon"}),Object(c.b)("svg",Object(n.a)({parentName:"span"},{xmlns:"http://www.w3.org/2000/svg",width:"12",height:"16",viewBox:"0 0 12 16"}),Object(c.b)("path",Object(n.a)({parentName:"svg"},{fillRule:"evenodd",d:"M6.5 0C3.48 0 1 2.19 1 5c0 .92.55 2.25 1 3 1.34 2.25 1.78 2.78 2 4v1h5v-1c.22-1.22.66-1.75 2-4 .45-.75 1-2.08 1-3 0-2.81-2.48-5-5.5-5zm3.64 7.48c-.25.44-.47.8-.67 1.11-.86 1.41-1.25 2.06-1.45 3.23-.02.05-.02.11-.02.17H5c0-.06 0-.13-.02-.17-.2-1.17-.59-1.83-1.45-3.23-.2-.31-.42-.67-.67-1.11C2.44 6.78 2 5.65 2 5c0-2.2 2.02-4 4.5-4 1.22 0 2.36.42 3.22 1.19C10.55 2.94 11 3.94 11 5c0 .66-.44 1.78-.86 2.48zM4 14h5c-.23 1.14-1.3 2-2.5 2s-2.27-.86-2.5-2z"})))),"tip")),Object(c.b)("div",Object(n.a)({parentName:"div"},{className:"admonition-content"}),Object(c.b)("p",{parentName:"div"},"Any of the created shortcuts for windows is a nuts-aware terminal.")))),Object(c.b)(j,{value:"linux",mdxType:"TabItem"},Object(c.b)("pre",null,Object(c.b)("code",Object(n.a)({parentName:"pre"},{}),"NDVER=0.7.0 && curl -OL https://github.com/thevpc/vpc-public-maven/raw/master\\\n/net/vpc/app/nuts/nuts/$NDVER/nuts-$NDVER.jar && java -jar \\\n      nuts-$NDVER.jar -zy\n")),Object(c.b)("p",null,'Linux Systems installation is based on bash shell. First launch will configure "~/.bashrc" so that ',Object(c.b)("strong",{parentName:"p"},"nuts")," and other companion tool commands will be available in any future terminal instances.\nUsing ",Object(c.b)("strong",{parentName:"p"},Object(c.b)("inlineCode",{parentName:"strong"},"nuts"))," on unix-like system should be seamless. A simple bash terminal (Gnome Terminal, KDE Konsole,...) is already a nuts-aware terminal."),Object(c.b)("p",null,"All Linux versions and distributions should work with or without XWindow (or equivalent). Graphical system is required only if you plan to run a gui application using ",Object(c.b)("strong",{parentName:"p"},"nuts"),".\nAll tests where performed on OpenSuse Tumbleweed."),Object(c.b)("div",{className:"admonition admonition-tip alert alert--success"},Object(c.b)("div",Object(n.a)({parentName:"div"},{className:"admonition-heading"}),Object(c.b)("h5",{parentName:"div"},Object(c.b)("span",Object(n.a)({parentName:"h5"},{className:"admonition-icon"}),Object(c.b)("svg",Object(n.a)({parentName:"span"},{xmlns:"http://www.w3.org/2000/svg",width:"12",height:"16",viewBox:"0 0 12 16"}),Object(c.b)("path",Object(n.a)({parentName:"svg"},{fillRule:"evenodd",d:"M6.5 0C3.48 0 1 2.19 1 5c0 .92.55 2.25 1 3 1.34 2.25 1.78 2.78 2 4v1h5v-1c.22-1.22.66-1.75 2-4 .45-.75 1-2.08 1-3 0-2.81-2.48-5-5.5-5zm3.64 7.48c-.25.44-.47.8-.67 1.11-.86 1.41-1.25 2.06-1.45 3.23-.02.05-.02.11-.02.17H5c0-.06 0-.13-.02-.17-.2-1.17-.59-1.83-1.45-3.23-.2-.31-.42-.67-.67-1.11C2.44 6.78 2 5.65 2 5c0-2.2 2.02-4 4.5-4 1.22 0 2.36.42 3.22 1.19C10.55 2.94 11 3.94 11 5c0 .66-.44 1.78-.86 2.48zM4 14h5c-.23 1.14-1.3 2-2.5 2s-2.27-.86-2.5-2z"})))),"tip")),Object(c.b)("div",Object(n.a)({parentName:"div"},{className:"admonition-content"}),Object(c.b)("p",{parentName:"div"},"Any bash terminal application is a nuts-aware terminal.")))),Object(c.b)(j,{value:"macos",mdxType:"TabItem"},Object(c.b)("pre",null,Object(c.b)("code",Object(n.a)({parentName:"pre"},{}),"NDVER=0.7.0 && curl -OL https://github.com/thevpc/vpc-public-maven/raw/master\\\n/net/vpc/app/nuts/nuts/$NDVER/nuts-$NDVER.jar && java -jar \\\n      nuts-$NDVER.jar -y\n")),Object(c.b)("p",null,"MacOS Systems installation is based on ",Object(c.b)("strong",{parentName:"p"},"bash"),' shell. First launch will configure "~/.bashrc" so that ',Object(c.b)("strong",{parentName:"p"},"nuts")," and other companion tool commands will be available in any future terminal instances.\nUsing ",Object(c.b)("strong",{parentName:"p"},Object(c.b)("inlineCode",{parentName:"strong"},"nuts"))," on MacOS system should be seamless. A simple bash terminal (MacOs Terminal App) is already a nuts-aware terminal."),Object(c.b)("div",{className:"admonition admonition-tip alert alert--success"},Object(c.b)("div",Object(n.a)({parentName:"div"},{className:"admonition-heading"}),Object(c.b)("h5",{parentName:"div"},Object(c.b)("span",Object(n.a)({parentName:"h5"},{className:"admonition-icon"}),Object(c.b)("svg",Object(n.a)({parentName:"span"},{xmlns:"http://www.w3.org/2000/svg",width:"12",height:"16",viewBox:"0 0 12 16"}),Object(c.b)("path",Object(n.a)({parentName:"svg"},{fillRule:"evenodd",d:"M6.5 0C3.48 0 1 2.19 1 5c0 .92.55 2.25 1 3 1.34 2.25 1.78 2.78 2 4v1h5v-1c.22-1.22.66-1.75 2-4 .45-.75 1-2.08 1-3 0-2.81-2.48-5-5.5-5zm3.64 7.48c-.25.44-.47.8-.67 1.11-.86 1.41-1.25 2.06-1.45 3.23-.02.05-.02.11-.02.17H5c0-.06 0-.13-.02-.17-.2-1.17-.59-1.83-1.45-3.23-.2-.31-.42-.67-.67-1.11C2.44 6.78 2 5.65 2 5c0-2.2 2.02-4 4.5-4 1.22 0 2.36.42 3.22 1.19C10.55 2.94 11 3.94 11 5c0 .66-.44 1.78-.86 2.48zM4 14h5c-.23 1.14-1.3 2-2.5 2s-2.27-.86-2.5-2z"})))),"tip")),Object(c.b)("div",Object(n.a)({parentName:"div"},{className:"admonition-content"}),Object(c.b)("p",{parentName:"div"},"Any bash terminal application is a nuts-aware terminal.")))),Object(c.b)(j,{value:"wget",mdxType:"TabItem"},Object(c.b)("pre",null,Object(c.b)("code",Object(n.a)({parentName:"pre"},{}),"  NDVER=0.7.0 && rm -f nuts-$NDVER.jar && wget https://github.com/thevpc/\\\nvpc-public-maven/raw/master/net/vpc/app/nuts/nuts/$NDVER/nuts-$NDVER.jar &&\\\n    java -jar nuts-$NDVER.jar -y\n")),Object(c.b)("div",{className:"admonition admonition-tip alert alert--success"},Object(c.b)("div",Object(n.a)({parentName:"div"},{className:"admonition-heading"}),Object(c.b)("h5",{parentName:"div"},Object(c.b)("span",Object(n.a)({parentName:"h5"},{className:"admonition-icon"}),Object(c.b)("svg",Object(n.a)({parentName:"span"},{xmlns:"http://www.w3.org/2000/svg",width:"12",height:"16",viewBox:"0 0 12 16"}),Object(c.b)("path",Object(n.a)({parentName:"svg"},{fillRule:"evenodd",d:"M6.5 0C3.48 0 1 2.19 1 5c0 .92.55 2.25 1 3 1.34 2.25 1.78 2.78 2 4v1h5v-1c.22-1.22.66-1.75 2-4 .45-.75 1-2.08 1-3 0-2.81-2.48-5-5.5-5zm3.64 7.48c-.25.44-.47.8-.67 1.11-.86 1.41-1.25 2.06-1.45 3.23-.02.05-.02.11-.02.17H5c0-.06 0-.13-.02-.17-.2-1.17-.59-1.83-1.45-3.23-.2-.31-.42-.67-.67-1.11C2.44 6.78 2 5.65 2 5c0-2.2 2.02-4 4.5-4 1.22 0 2.36.42 3.22 1.19C10.55 2.94 11 3.94 11 5c0 .66-.44 1.78-.86 2.48zM4 14h5c-.23 1.14-1.3 2-2.5 2s-2.27-.86-2.5-2z"})))),"tip")),Object(c.b)("div",Object(n.a)({parentName:"div"},{className:"admonition-content"}),Object(c.b)("p",{parentName:"div"},"Any bash terminal application is a nuts-aware terminal.")))),Object(c.b)(j,{value:"curl",mdxType:"TabItem"},Object(c.b)("pre",null,Object(c.b)("code",Object(n.a)({parentName:"pre"},{}),"  NDVER=0.7.0 && curl -OL https://github.com/thevpc/vpc-public-maven/raw/master\\\n/net/vpc/app/nuts/nuts/$NDVER/nuts-$NDVER.jar && java -jar \\\n      nuts-$NDVER.jar -y\n")),Object(c.b)("div",{className:"admonition admonition-tip alert alert--success"},Object(c.b)("div",Object(n.a)({parentName:"div"},{className:"admonition-heading"}),Object(c.b)("h5",{parentName:"div"},Object(c.b)("span",Object(n.a)({parentName:"h5"},{className:"admonition-icon"}),Object(c.b)("svg",Object(n.a)({parentName:"span"},{xmlns:"http://www.w3.org/2000/svg",width:"12",height:"16",viewBox:"0 0 12 16"}),Object(c.b)("path",Object(n.a)({parentName:"svg"},{fillRule:"evenodd",d:"M6.5 0C3.48 0 1 2.19 1 5c0 .92.55 2.25 1 3 1.34 2.25 1.78 2.78 2 4v1h5v-1c.22-1.22.66-1.75 2-4 .45-.75 1-2.08 1-3 0-2.81-2.48-5-5.5-5zm3.64 7.48c-.25.44-.47.8-.67 1.11-.86 1.41-1.25 2.06-1.45 3.23-.02.05-.02.11-.02.17H5c0-.06 0-.13-.02-.17-.2-1.17-.59-1.83-1.45-3.23-.2-.31-.42-.67-.67-1.11C2.44 6.78 2 5.65 2 5c0-2.2 2.02-4 4.5-4 1.22 0 2.36.42 3.22 1.19C10.55 2.94 11 3.94 11 5c0 .66-.44 1.78-.86 2.48zM4 14h5c-.23 1.14-1.3 2-2.5 2s-2.27-.86-2.5-2z"})))),"tip")),Object(c.b)("div",Object(n.a)({parentName:"div"},{className:"admonition-content"}),Object(c.b)("p",{parentName:"div"},"Any bash terminal application is a nuts-aware terminal.")))),Object(c.b)(j,{value:"java",mdxType:"TabItem"},Object(c.b)("pre",null,Object(c.b)("code",Object(n.a)({parentName:"pre"},{}),"  NDVER=0.7.0 && curl -OL https://github.com/thevpc/vpc-public-maven/raw/master\\\n/net/vpc/app/nuts/nuts/$NDVER/nuts-$NDVER.jar && java -jar \\\n      nuts-$NDVER.jar -y\n")),Object(c.b)("div",{className:"admonition admonition-tip alert alert--success"},Object(c.b)("div",Object(n.a)({parentName:"div"},{className:"admonition-heading"}),Object(c.b)("h5",{parentName:"div"},Object(c.b)("span",Object(n.a)({parentName:"h5"},{className:"admonition-icon"}),Object(c.b)("svg",Object(n.a)({parentName:"span"},{xmlns:"http://www.w3.org/2000/svg",width:"12",height:"16",viewBox:"0 0 12 16"}),Object(c.b)("path",Object(n.a)({parentName:"svg"},{fillRule:"evenodd",d:"M6.5 0C3.48 0 1 2.19 1 5c0 .92.55 2.25 1 3 1.34 2.25 1.78 2.78 2 4v1h5v-1c.22-1.22.66-1.75 2-4 .45-.75 1-2.08 1-3 0-2.81-2.48-5-5.5-5zm3.64 7.48c-.25.44-.47.8-.67 1.11-.86 1.41-1.25 2.06-1.45 3.23-.02.05-.02.11-.02.17H5c0-.06 0-.13-.02-.17-.2-1.17-.59-1.83-1.45-3.23-.2-.31-.42-.67-.67-1.11C2.44 6.78 2 5.65 2 5c0-2.2 2.02-4 4.5-4 1.22 0 2.36.42 3.22 1.19C10.55 2.94 11 3.94 11 5c0 .66-.44 1.78-.86 2.48zM4 14h5c-.23 1.14-1.3 2-2.5 2s-2.27-.86-2.5-2z"})))),"tip")),Object(c.b)("div",Object(n.a)({parentName:"div"},{className:"admonition-content"}),Object(c.b)("p",{parentName:"div"},"Any bash terminal application is a nuts-aware terminal."))))),Object(c.b)("p",null,"Yous should then see some log like the following :"),Object(c.b)("p",null,Object(c.b)("img",{alt:"install-log-example",src:a(151).default})),Object(c.b)("p",null,'As you can see, installation upon first launch, will also trigger installation of other optional programs called "companion tools".\nActually they are recommended helpful tools :'),Object(c.b)("ul",null,Object(c.b)("li",{parentName:"ul"},Object(c.b)("strong",{parentName:"li"},"ndi")," which stands for ",Object(c.b)("strong",{parentName:"li"},"Nuts Desktop Integration")," that helps configuring the desktop to better\ninteract with ",Object(c.b)("strong",{parentName:"li"},Object(c.b)("inlineCode",{parentName:"strong"},"nuts"))," by for instance creating shortcuts."),Object(c.b)("li",{parentName:"ul"},Object(c.b)("strong",{parentName:"li"},"nsh")," which stands for ",Object(c.b)("strong",{parentName:"li"},"Nuts Shell")," , a bash compatible shell implementation program that will run equally on linux an windows systems."),Object(c.b)("li",{parentName:"ul"},Object(c.b)("strong",{parentName:"li"},"nadmin")," an administration tool for ",Object(c.b)("strong",{parentName:"li"},Object(c.b)("inlineCode",{parentName:"strong"},"nuts"))," ")),Object(c.b)("div",{className:"admonition admonition-important alert alert--info"},Object(c.b)("div",Object(n.a)({parentName:"div"},{className:"admonition-heading"}),Object(c.b)("h5",{parentName:"div"},Object(c.b)("span",Object(n.a)({parentName:"h5"},{className:"admonition-icon"}),Object(c.b)("svg",Object(n.a)({parentName:"span"},{xmlns:"http://www.w3.org/2000/svg",width:"14",height:"16",viewBox:"0 0 14 16"}),Object(c.b)("path",Object(n.a)({parentName:"svg"},{fillRule:"evenodd",d:"M7 2.3c3.14 0 5.7 2.56 5.7 5.7s-2.56 5.7-5.7 5.7A5.71 5.71 0 0 1 1.3 8c0-3.14 2.56-5.7 5.7-5.7zM7 1C3.14 1 0 4.14 0 8s3.14 7 7 7 7-3.14 7-7-3.14-7-7-7zm1 3H6v5h2V4zm0 6H6v2h2v-2z"})))),"important")),Object(c.b)("div",Object(n.a)({parentName:"div"},{className:"admonition-content"}),Object(c.b)("p",{parentName:"div"},"After installation, you need to restart the terminal application for the configuration to take effet."))),Object(c.b)("h2",{id:"test-installation"},"Test Installation"),Object(c.b)("p",null,"To test installation the simplest way is to open a nuts-aware terminal and type : "),Object(c.b)("pre",null,Object(c.b)("code",Object(n.a)({parentName:"pre"},{}),"nuts --version\n")),Object(c.b)("p",null,"It should show a result in the format : nuts-api-version/nuts-impl-version"),Object(c.b)("pre",null,Object(c.b)("code",Object(n.a)({parentName:"pre"},{}),"00.7.0/0.7.0.0\n")),Object(c.b)("h2",{id:"run-a-command"},"Run a command"),Object(c.b)("p",null,"To run a command using ",Object(c.b)("strong",{parentName:"p"},"nuts")," just type"),Object(c.b)("pre",null,Object(c.b)("code",Object(n.a)({parentName:"pre"},{}),"nuts <command>\n")),Object(c.b)("p",null,"Several commands are available, and you still be able to run any java and non java application. More info is available in the ",Object(c.b)("strong",{parentName:"p"},Object(c.b)("inlineCode",{parentName:"strong"},"nuts"))," official ",Object(c.b)("a",Object(n.a)({parentName:"p"},{href:"https://github.com/thevpc/nuts/wiki"}),"wiki")," ."))}g.isMDXComponent=!0},118:function(e,t,a){"use strict";a.d(t,"a",(function(){return p})),a.d(t,"b",(function(){return d}));var n=a(0),i=a.n(n);function r(e,t,a){return t in e?Object.defineProperty(e,t,{value:a,enumerable:!0,configurable:!0,writable:!0}):e[t]=a,e}function s(e,t){var a=Object.keys(e);if(Object.getOwnPropertySymbols){var n=Object.getOwnPropertySymbols(e);t&&(n=n.filter((function(t){return Object.getOwnPropertyDescriptor(e,t).enumerable}))),a.push.apply(a,n)}return a}function c(e){for(var t=1;t<arguments.length;t++){var a=null!=arguments[t]?arguments[t]:{};t%2?s(Object(a),!0).forEach((function(t){r(e,t,a[t])})):Object.getOwnPropertyDescriptors?Object.defineProperties(e,Object.getOwnPropertyDescriptors(a)):s(Object(a)).forEach((function(t){Object.defineProperty(e,t,Object.getOwnPropertyDescriptor(a,t))}))}return e}function l(e,t){if(null==e)return{};var a,n,i=function(e,t){if(null==e)return{};var a,n,i={},r=Object.keys(e);for(n=0;n<r.length;n++)a=r[n],t.indexOf(a)>=0||(i[a]=e[a]);return i}(e,t);if(Object.getOwnPropertySymbols){var r=Object.getOwnPropertySymbols(e);for(n=0;n<r.length;n++)a=r[n],t.indexOf(a)>=0||Object.prototype.propertyIsEnumerable.call(e,a)&&(i[a]=e[a])}return i}var o=i.a.createContext({}),b=function(e){var t=i.a.useContext(o),a=t;return e&&(a="function"==typeof e?e(t):c(c({},t),e)),a},p=function(e){var t=b(e.components);return i.a.createElement(o.Provider,{value:t},e.children)},m={inlineCode:"code",wrapper:function(e){var t=e.children;return i.a.createElement(i.a.Fragment,{},t)}},u=i.a.forwardRef((function(e,t){var a=e.components,n=e.mdxType,r=e.originalType,s=e.parentName,o=l(e,["components","mdxType","originalType","parentName"]),p=b(a),u=n,d=p["".concat(s,".").concat(u)]||p[u]||m[u]||r;return a?i.a.createElement(d,c(c({ref:t},o),{},{components:a})):i.a.createElement(d,c({ref:t},o))}));function d(e,t){var a=arguments,n=t&&t.mdxType;if("string"==typeof e||n){var r=a.length,s=new Array(r);s[0]=u;var c={};for(var l in t)hasOwnProperty.call(t,l)&&(c[l]=t[l]);c.originalType=e,c.mdxType="string"==typeof e?e:n,s[1]=c;for(var o=2;o<r;o++)s[o]=a[o];return i.a.createElement.apply(null,s)}return i.a.createElement.apply(null,a)}u.displayName="MDXCreateElement"},120:function(e,t,a){"use strict";function n(e){var t,a,i="";if("string"==typeof e||"number"==typeof e)i+=e;else if("object"==typeof e)if(Array.isArray(e))for(t=0;t<e.length;t++)e[t]&&(a=n(e[t]))&&(i&&(i+=" "),i+=a);else for(t in e)e[t]&&(i&&(i+=" "),i+=t);return i}t.a=function(){for(var e,t,a=0,i="";a<arguments.length;)(e=arguments[a++])&&(t=n(e))&&(i&&(i+=" "),i+=t);return i}},125:function(e,t,a){"use strict";var n=a(0);const i=Object(n.createContext)(void 0);t.a=i},126:function(e,t,a){"use strict";var n=a(0),i=a(125);t.a=function(){const e=Object(n.useContext)(i.a);if(null==e)throw new Error("`useUserPreferencesContext` is used outside of `Layout` Component.");return e}},151:function(e,t,a){"use strict";a.r(t),t.default=a.p+"assets/images/install-log-example-1dd2410547d60576f9511ab1f66bc724.png"}}]);