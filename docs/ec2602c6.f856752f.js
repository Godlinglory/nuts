(window.webpackJsonp=window.webpackJsonp||[]).push([[56],{109:function(e,t,n){"use strict";n.r(t),n.d(t,"frontMatter",(function(){return c})),n.d(t,"metadata",(function(){return i})),n.d(t,"rightToc",(function(){return l})),n.d(t,"default",(function(){return p}));var r=n(2),o=n(6),a=(n(0),n(117)),c={id:"help-cmd",title:"Help Command",sidebar_label:"Help Command"},i={unversionedId:"cmd/help-cmd",id:"cmd/help-cmd",isDocsHomePage:!1,title:"Help Command",description:"This command, as you should have guessed, show. help manual of your favorite command or of nuts it self.",source:"@site/docs/cmd/help-cmd.md",permalink:"/nuts/docs/cmd/help-cmd",editUrl:"https://github.com/thevpc/nuts/edit/master/website/docs/cmd/help-cmd.md",sidebar_label:"Help Command",sidebar:"someSidebar",previous:{title:"Fetch Command",permalink:"/nuts/docs/cmd/fetch-cmd"},next:{title:"Info Command",permalink:"/nuts/docs/cmd/info-cmd"}},l=[],s={rightToc:l};function p(e){var t=e.components,n=Object(o.a)(e,["components"]);return Object(a.a)("wrapper",Object(r.a)({},s,n,{components:t,mdxType:"MDXLayout"}),Object(a.a)("p",null,"This command, as you should have guessed, show. help manual of your favorite command or of nuts it self."),Object(a.a)("pre",null,Object(a.a)("code",Object(r.a)({parentName:"pre"},{}),"me@linux:~> nuts help version\nversion :\nnuts version\nnuts --version\nnuts -version\nnuts -v\n      show version and exit\n...\n")),Object(a.a)("p",null,"will show help of the ",Object(a.a)("strong",{parentName:"p"},"version")," command."),Object(a.a)("p",null,"Usually, all nuts commands support the '--help' option command that should also show this very same help.\nSo the latter command is equivalent to"),Object(a.a)("pre",null,Object(a.a)("code",Object(r.a)({parentName:"pre"},{}),"me@linux:~> nuts version --help\n")))}p.isMDXComponent=!0},117:function(e,t,n){"use strict";n.d(t,"a",(function(){return d}));var r=n(0),o=n.n(r);function a(e,t,n){return t in e?Object.defineProperty(e,t,{value:n,enumerable:!0,configurable:!0,writable:!0}):e[t]=n,e}function c(e,t){var n=Object.keys(e);if(Object.getOwnPropertySymbols){var r=Object.getOwnPropertySymbols(e);t&&(r=r.filter((function(t){return Object.getOwnPropertyDescriptor(e,t).enumerable}))),n.push.apply(n,r)}return n}function i(e){for(var t=1;t<arguments.length;t++){var n=null!=arguments[t]?arguments[t]:{};t%2?c(Object(n),!0).forEach((function(t){a(e,t,n[t])})):Object.getOwnPropertyDescriptors?Object.defineProperties(e,Object.getOwnPropertyDescriptors(n)):c(Object(n)).forEach((function(t){Object.defineProperty(e,t,Object.getOwnPropertyDescriptor(n,t))}))}return e}function l(e,t){if(null==e)return{};var n,r,o=function(e,t){if(null==e)return{};var n,r,o={},a=Object.keys(e);for(r=0;r<a.length;r++)n=a[r],t.indexOf(n)>=0||(o[n]=e[n]);return o}(e,t);if(Object.getOwnPropertySymbols){var a=Object.getOwnPropertySymbols(e);for(r=0;r<a.length;r++)n=a[r],t.indexOf(n)>=0||Object.prototype.propertyIsEnumerable.call(e,n)&&(o[n]=e[n])}return o}var s=o.a.createContext({}),p=function(e){var t=o.a.useContext(s),n=t;return e&&(n="function"==typeof e?e(t):i(i({},t),e)),n},u={inlineCode:"code",wrapper:function(e){var t=e.children;return o.a.createElement(o.a.Fragment,{},t)}},m=o.a.forwardRef((function(e,t){var n=e.components,r=e.mdxType,a=e.originalType,c=e.parentName,s=l(e,["components","mdxType","originalType","parentName"]),m=p(n),d=r,f=m["".concat(c,".").concat(d)]||m[d]||u[d]||a;return n?o.a.createElement(f,i(i({ref:t},s),{},{components:n})):o.a.createElement(f,i({ref:t},s))}));function d(e,t){var n=arguments,r=t&&t.mdxType;if("string"==typeof e||r){var a=n.length,c=new Array(a);c[0]=m;var i={};for(var l in t)hasOwnProperty.call(t,l)&&(i[l]=t[l]);i.originalType=e,i.mdxType="string"==typeof e?e:r,c[1]=i;for(var s=2;s<a;s++)c[s]=n[s];return o.a.createElement.apply(null,c)}return o.a.createElement.apply(null,n)}m.displayName="MDXCreateElement"}}]);