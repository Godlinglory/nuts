(window.webpackJsonp=window.webpackJsonp||[]).push([[29],{103:function(e,t,n){"use strict";n.d(t,"a",(function(){return d})),n.d(t,"b",(function(){return m}));var r=n(0),o=n.n(r);function i(e,t,n){return t in e?Object.defineProperty(e,t,{value:n,enumerable:!0,configurable:!0,writable:!0}):e[t]=n,e}function a(e,t){var n=Object.keys(e);if(Object.getOwnPropertySymbols){var r=Object.getOwnPropertySymbols(e);t&&(r=r.filter((function(t){return Object.getOwnPropertyDescriptor(e,t).enumerable}))),n.push.apply(n,r)}return n}function l(e){for(var t=1;t<arguments.length;t++){var n=null!=arguments[t]?arguments[t]:{};t%2?a(Object(n),!0).forEach((function(t){i(e,t,n[t])})):Object.getOwnPropertyDescriptors?Object.defineProperties(e,Object.getOwnPropertyDescriptors(n)):a(Object(n)).forEach((function(t){Object.defineProperty(e,t,Object.getOwnPropertyDescriptor(n,t))}))}return e}function c(e,t){if(null==e)return{};var n,r,o=function(e,t){if(null==e)return{};var n,r,o={},i=Object.keys(e);for(r=0;r<i.length;r++)n=i[r],t.indexOf(n)>=0||(o[n]=e[n]);return o}(e,t);if(Object.getOwnPropertySymbols){var i=Object.getOwnPropertySymbols(e);for(r=0;r<i.length;r++)n=i[r],t.indexOf(n)>=0||Object.prototype.propertyIsEnumerable.call(e,n)&&(o[n]=e[n])}return o}var s=o.a.createContext({}),u=function(e){var t=o.a.useContext(s),n=t;return e&&(n="function"==typeof e?e(t):l(l({},t),e)),n},d=function(e){var t=u(e.components);return o.a.createElement(s.Provider,{value:t},e.children)},b={inlineCode:"code",wrapper:function(e){var t=e.children;return o.a.createElement(o.a.Fragment,{},t)}},p=o.a.forwardRef((function(e,t){var n=e.components,r=e.mdxType,i=e.originalType,a=e.parentName,s=c(e,["components","mdxType","originalType","parentName"]),d=u(n),p=r,m=d["".concat(a,".").concat(p)]||d[p]||b[p]||i;return n?o.a.createElement(m,l(l({ref:t},s),{},{components:n})):o.a.createElement(m,l({ref:t},s))}));function m(e,t){var n=arguments,r=t&&t.mdxType;if("string"==typeof e||r){var i=n.length,a=new Array(i);a[0]=p;var l={};for(var c in t)hasOwnProperty.call(t,c)&&(l[c]=t[c]);l.originalType=e,l.mdxType="string"==typeof e?e:r,a[1]=l;for(var s=2;s<i;s++)a[s]=n[s];return o.a.createElement.apply(null,a)}return o.a.createElement.apply(null,n)}p.displayName="MDXCreateElement"},83:function(e,t,n){"use strict";n.r(t),n.d(t,"frontMatter",(function(){return a})),n.d(t,"metadata",(function(){return l})),n.d(t,"rightToc",(function(){return c})),n.d(t,"default",(function(){return u}));var r=n(2),o=n(6),i=(n(0),n(103)),a={id:"troubleshooting",title:"Troubleshooting",sidebar_label:"Troubleshooting"},l={unversionedId:"info/troubleshooting",id:"info/troubleshooting",isDocsHomePage:!1,title:"Troubleshooting",description:"Whenever installation fails, it is more likely there is a mis-configuration or invalid libraries bundles used. You may have to options",source:"@site/docs/info/troubleshooting.md",permalink:"/nuts/docs/info/troubleshooting",editUrl:"https://github.com/facebook/docusaurus/edit/master/website/docs/info/troubleshooting.md",sidebar_label:"Troubleshooting",sidebar:"someSidebar",previous:{title:"Running Nuts",permalink:"/nuts/docs/info/running"},next:{title:"Command Line Arguments",permalink:"/nuts/docs/concepts/commandline"}},c=[{value:"recover mode",id:"recover-mode",children:[]},{value:"newer mode",id:"newer-mode",children:[]},{value:"reset mode",id:"reset-mode",children:[]},{value:"kill mode",id:"kill-mode",children:[]},{value:"After invoking reset mode",id:"after-invoking-reset-mode",children:[]}],s={rightToc:c};function u(e){var t=e.components,n=Object(o.a)(e,["components"]);return Object(i.b)("wrapper",Object(r.a)({},s,n,{components:t,mdxType:"MDXLayout"}),Object(i.b)("p",null,"Whenever installation fails, it is more likely there is a mis-configuration or invalid libraries bundles used. You may have to options\nto circumvent this which are two levels or workspace reinitialization."),Object(i.b)("h2",{id:"recover-mode"},"recover mode"),Object(i.b)("p",null,Object(i.b)("strong",{parentName:"p"},"recover mode")," will apply best efforts to correct configuration without losing them. It will delete all cached data and\nlibraries for them to be downloaded later and searches for a valid nuts installation binaries to run (it will actually\ndo a forced update). To run nuts in recover mode type :"),Object(i.b)("pre",null,Object(i.b)("code",Object(r.a)({parentName:"pre"},{}),"nuts --recover\n")),Object(i.b)("h2",{id:"newer-mode"},"newer mode"),Object(i.b)("p",null,Object(i.b)("strong",{parentName:"p"},"newer mode")," will apply best efforts to reload cached files and libraries. to run nuts in 'newer mode' type:"),Object(i.b)("pre",null,Object(i.b)("code",Object(r.a)({parentName:"pre"},{}),"nuts -N\n")),Object(i.b)("h2",{id:"reset-mode"},"reset mode"),Object(i.b)("p",null,Object(i.b)("strong",{parentName:"p"},"reset mode")," will apply all efforts to correct configuration by, actually, ",Object(i.b)("strong",{parentName:"p"},"deleting")," them\n(and all of workspace files!!) to create a new fresh workspace. This is quite a radical action to run. Do not ever\ninvoke this unless your are really knowing what you are doing.\nTo run nuts in reset mode type :"),Object(i.b)("pre",null,Object(i.b)("code",Object(r.a)({parentName:"pre"},{}),"nuts --reset\n")),Object(i.b)("h2",{id:"kill-mode"},"kill mode"),Object(i.b)("p",null,Object(i.b)("strong",{parentName:"p"},"kill mode")," is a special variant of reset mode where workspace will not be recreated after deletion.\nThis can be achieved by using a combination of reset mode and --skip-boot (-Q)option. Do not ever\ninvoke it unless you are really knowing what you are doing. To run nuts in reset mode type :"),Object(i.b)("p",null,"To run nuts in prune mode type :"),Object(i.b)("pre",null,Object(i.b)("code",Object(r.a)({parentName:"pre"},{}),"nuts --reset -Q\n")),Object(i.b)("h2",{id:"after-invoking-reset-mode"},"After invoking reset mode"),Object(i.b)("p",null,"After invoking reset mode, nuts commands (installed by nuts settings) will not be available anymore.\nyou should use the jar based invocation at least once to reinstall these commands."),Object(i.b)("pre",null,Object(i.b)("code",Object(r.a)({parentName:"pre"},{}),"java -jar nuts-0.5.7.jar\n")))}u.isMDXComponent=!0}}]);