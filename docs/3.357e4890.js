(window.webpackJsonp=window.webpackJsonp||[]).push([[3],{131:function(e,t,n){"use strict";n.d(t,"a",(function(){return p}));var r=n(0),a=n.n(r);function o(e,t,n){return t in e?Object.defineProperty(e,t,{value:n,enumerable:!0,configurable:!0,writable:!0}):e[t]=n,e}function c(e,t){var n=Object.keys(e);if(Object.getOwnPropertySymbols){var r=Object.getOwnPropertySymbols(e);t&&(r=r.filter((function(t){return Object.getOwnPropertyDescriptor(e,t).enumerable}))),n.push.apply(n,r)}return n}function s(e){for(var t=1;t<arguments.length;t++){var n=null!=arguments[t]?arguments[t]:{};t%2?c(Object(n),!0).forEach((function(t){o(e,t,n[t])})):Object.getOwnPropertyDescriptors?Object.defineProperties(e,Object.getOwnPropertyDescriptors(n)):c(Object(n)).forEach((function(t){Object.defineProperty(e,t,Object.getOwnPropertyDescriptor(n,t))}))}return e}function l(e,t){if(null==e)return{};var n,r,a=function(e,t){if(null==e)return{};var n,r,a={},o=Object.keys(e);for(r=0;r<o.length;r++)n=o[r],t.indexOf(n)>=0||(a[n]=e[n]);return a}(e,t);if(Object.getOwnPropertySymbols){var o=Object.getOwnPropertySymbols(e);for(r=0;r<o.length;r++)n=o[r],t.indexOf(n)>=0||Object.prototype.propertyIsEnumerable.call(e,n)&&(a[n]=e[n])}return a}var i=a.a.createContext({}),u=function(e){var t=a.a.useContext(i),n=t;return e&&(n="function"==typeof e?e(t):s(s({},t),e)),n},p=function(e){var t=u(e.components);return a.a.createElement(i.Provider,{value:t},e.children)},y={inlineCode:"code",wrapper:function(e){var t=e.children;return a.a.createElement(a.a.Fragment,{},t)}},d=a.a.forwardRef((function(e,t){var n=e.components,r=e.mdxType,o=e.originalType,c=e.parentName,i=l(e,["components","mdxType","originalType","parentName"]),p=u(n),d=r,f=p["".concat(c,".").concat(d)]||p[d]||y[d]||o;return n?a.a.createElement(f,s(s({ref:t},i),{},{components:n})):a.a.createElement(f,s({ref:t},i))}));d.displayName="MDXCreateElement"},132:function(e,t,n){"use strict";var r=n(2),a=n(0),o=n.n(a),c=n(120),s=n(119),l={plain:{backgroundColor:"#2a2734",color:"#9a86fd"},styles:[{types:["comment","prolog","doctype","cdata","punctuation"],style:{color:"#6c6783"}},{types:["namespace"],style:{opacity:.7}},{types:["tag","operator","number"],style:{color:"#e09142"}},{types:["property","function"],style:{color:"#9a86fd"}},{types:["tag-id","selector","atrule-id"],style:{color:"#eeebff"}},{types:["attr-name"],style:{color:"#c4b9fe"}},{types:["boolean","string","entity","url","attr-value","keyword","control","directive","unit","statement","regex","at-rule","placeholder","variable"],style:{color:"#ffcc99"}},{types:["deleted"],style:{textDecorationLine:"line-through"}},{types:["inserted"],style:{textDecorationLine:"underline"}},{types:["italic"],style:{fontStyle:"italic"}},{types:["important","bold"],style:{fontWeight:"bold"}},{types:["important"],style:{color:"#c4b9fe"}}]},i={Prism:n(20).a,theme:l};function u(e,t,n){return t in e?Object.defineProperty(e,t,{value:n,enumerable:!0,configurable:!0,writable:!0}):e[t]=n,e}function p(){return(p=Object.assign||function(e){for(var t=1;t<arguments.length;t++){var n=arguments[t];for(var r in n)Object.prototype.hasOwnProperty.call(n,r)&&(e[r]=n[r])}return e}).apply(this,arguments)}var y=/\r\n|\r|\n/,d=function(e){0===e.length?e.push({types:["plain"],content:"",empty:!0}):1===e.length&&""===e[0].content&&(e[0].empty=!0)},f=function(e,t){var n=e.length;return n>0&&e[n-1]===t?e:e.concat(t)},h=function(e,t){var n=e.plain,r=Object.create(null),a=e.styles.reduce((function(e,n){var r=n.languages,a=n.style;return r&&!r.includes(t)||n.types.forEach((function(t){var n=p({},e[t],a);e[t]=n})),e}),r);return a.root=n,a.plain=p({},n,{backgroundColor:null}),a};function g(e,t){var n={};for(var r in e)Object.prototype.hasOwnProperty.call(e,r)&&-1===t.indexOf(r)&&(n[r]=e[r]);return n}var m=function(e){function t(){for(var t=this,n=[],r=arguments.length;r--;)n[r]=arguments[r];e.apply(this,n),u(this,"getThemeDict",(function(e){if(void 0!==t.themeDict&&e.theme===t.prevTheme&&e.language===t.prevLanguage)return t.themeDict;t.prevTheme=e.theme,t.prevLanguage=e.language;var n=e.theme?h(e.theme,e.language):void 0;return t.themeDict=n})),u(this,"getLineProps",(function(e){var n=e.key,r=e.className,a=e.style,o=p({},g(e,["key","className","style","line"]),{className:"token-line",style:void 0,key:void 0}),c=t.getThemeDict(t.props);return void 0!==c&&(o.style=c.plain),void 0!==a&&(o.style=void 0!==o.style?p({},o.style,a):a),void 0!==n&&(o.key=n),r&&(o.className+=" "+r),o})),u(this,"getStyleForToken",(function(e){var n=e.types,r=e.empty,a=n.length,o=t.getThemeDict(t.props);if(void 0!==o){if(1===a&&"plain"===n[0])return r?{display:"inline-block"}:void 0;if(1===a&&!r)return o[n[0]];var c=r?{display:"inline-block"}:{},s=n.map((function(e){return o[e]}));return Object.assign.apply(Object,[c].concat(s))}})),u(this,"getTokenProps",(function(e){var n=e.key,r=e.className,a=e.style,o=e.token,c=p({},g(e,["key","className","style","token"]),{className:"token "+o.types.join(" "),children:o.content,style:t.getStyleForToken(o),key:void 0});return void 0!==a&&(c.style=void 0!==c.style?p({},c.style,a):a),void 0!==n&&(c.key=n),r&&(c.className+=" "+r),c}))}return e&&(t.__proto__=e),t.prototype=Object.create(e&&e.prototype),t.prototype.constructor=t,t.prototype.render=function(){var e=this.props,t=e.Prism,n=e.language,r=e.code,a=e.children,o=this.getThemeDict(this.props),c=t.languages[n];return a({tokens:function(e){for(var t=[[]],n=[e],r=[0],a=[e.length],o=0,c=0,s=[],l=[s];c>-1;){for(;(o=r[c]++)<a[c];){var i=void 0,u=t[c],p=n[c][o];if("string"==typeof p?(u=c>0?u:["plain"],i=p):(u=f(u,p.type),p.alias&&(u=f(u,p.alias)),i=p.content),"string"==typeof i){var h=i.split(y),g=h.length;s.push({types:u,content:h[0]});for(var m=1;m<g;m++)d(s),l.push(s=[]),s.push({types:u,content:h[m]})}else c++,t.push(u),n.push(i),r.push(0),a.push(i.length)}c--,t.pop(),n.pop(),r.pop(),a.pop()}return d(s),l}(void 0!==c?t.tokenize(r,c,n):[r]),className:"prism-code language-"+n,style:void 0!==o?o.root:{},getLineProps:this.getLineProps,getTokenProps:this.getTokenProps})},t}(a.Component),v=n(152),b=n.n(v),j=n(153),k=n.n(j),O=n(118),E={plain:{color:"#bfc7d5",backgroundColor:"#292d3e"},styles:[{types:["comment"],style:{color:"rgb(105, 112, 152)",fontStyle:"italic"}},{types:["string","inserted"],style:{color:"rgb(195, 232, 141)"}},{types:["number"],style:{color:"rgb(247, 140, 108)"}},{types:["builtin","char","constant","function"],style:{color:"rgb(130, 170, 255)"}},{types:["punctuation","selector"],style:{color:"rgb(199, 146, 234)"}},{types:["variable"],style:{color:"rgb(191, 199, 213)"}},{types:["class-name","attr-name"],style:{color:"rgb(255, 203, 107)"}},{types:["tag","deleted"],style:{color:"rgb(255, 85, 114)"}},{types:["operator"],style:{color:"rgb(137, 221, 255)"}},{types:["boolean"],style:{color:"rgb(255, 88, 116)"}},{types:["keyword"],style:{fontStyle:"italic"}},{types:["doctype"],style:{color:"rgb(199, 146, 234)",fontStyle:"italic"}},{types:["namespace"],style:{color:"rgb(178, 204, 214)"}},{types:["url"],style:{color:"rgb(221, 221, 221)"}}]},x=n(133),w=function(){var e=Object(O.a)().siteConfig.themeConfig.prism,t=void 0===e?{}:e,n=Object(x.a)().isDarkTheme,r=t.theme||E,a=t.darkTheme||r;return n?a:r},P=n(63),N=n.n(P),C=/{([\d,-]+)}/,T=function(e){void 0===e&&(e=["js","jsBlock","jsx","python","html"]);var t={js:{start:"\\/\\/",end:""},jsBlock:{start:"\\/\\*",end:"\\*\\/"},jsx:{start:"\\{\\s*\\/\\*",end:"\\*\\/\\s*\\}"},python:{start:"#",end:""},html:{start:"\x3c!--",end:"--\x3e"}},n=["highlight-next-line","highlight-start","highlight-end"].join("|"),r=e.map((function(e){return"(?:"+t[e].start+"\\s*("+n+")\\s*"+t[e].end+")"})).join("|");return new RegExp("^\\s*(?:"+r+")\\s*$")},S=/title=".*"/,D=function(e){var t=e.children,n=e.className,c=e.metastring,l=Object(O.a)().siteConfig.themeConfig.prism,u=void 0===l?{}:l,p=Object(a.useState)(!1),y=p[0],d=p[1],f=Object(a.useState)(!1),h=f[0],g=f[1];Object(a.useEffect)((function(){g(!0)}),[]);var v=Object(a.useRef)(null),j=[],E="",x=w();if(c&&C.test(c)){var P=c.match(C)[1];j=k.a.parse(P).filter((function(e){return e>0}))}c&&S.test(c)&&(E=c.match(S)[0].split("title=")[1].replace(/"+/g,""));var D=n&&n.replace(/language-/,"");!D&&u.defaultLanguage&&(D=u.defaultLanguage);var B=t.replace(/\n$/,"");if(0===j.length&&void 0!==D){for(var L,A="",I=function(e){switch(e){case"js":case"javascript":case"ts":case"typescript":return T(["js","jsBlock"]);case"jsx":case"tsx":return T(["js","jsBlock","jsx"]);case"html":return T(["js","jsBlock","html"]);case"python":case"py":return T(["python"]);default:return T()}}(D),R=t.replace(/\n$/,"").split("\n"),F=0;F<R.length;){var $=F+1,_=R[F].match(I);if(null!==_){switch(_.slice(1).reduce((function(e,t){return e||t}),void 0)){case"highlight-next-line":A+=$+",";break;case"highlight-start":L=$;break;case"highlight-end":A+=L+"-"+($-1)+","}R.splice(F,1)}else F+=1}j=k.a.parse(A),B=R.join("\n")}var W=function(){b()(B),d(!0),setTimeout((function(){return d(!1)}),2e3)};return o.a.createElement(m,Object(r.a)({},i,{key:String(h),theme:x,code:B,language:D}),(function(e){var t,n,a=e.className,c=e.style,l=e.tokens,i=e.getLineProps,u=e.getTokenProps;return o.a.createElement(o.a.Fragment,null,E&&o.a.createElement("div",{style:c,className:N.a.codeBlockTitle},E),o.a.createElement("div",{className:N.a.codeBlockContent},o.a.createElement("button",{ref:v,type:"button","aria-label":"Copy code to clipboard",className:Object(s.a)(N.a.copyButton,(t={},t[N.a.copyButtonWithTitle]=E,t)),onClick:W},y?"Copied":"Copy"),o.a.createElement("div",{tabIndex:0,className:Object(s.a)(a,N.a.codeBlock,(n={},n[N.a.codeBlockWithTitle]=E,n))},o.a.createElement("div",{className:N.a.codeBlockLines,style:c},l.map((function(e,t){1===e.length&&""===e[0].content&&(e[0].content="\n");var n=i({line:e,key:t});return j.includes(t+1)&&(n.className=n.className+" docusaurus-highlight-code-line"),o.a.createElement("div",Object(r.a)({key:t},n),e.map((function(e,t){return o.a.createElement("span",Object(r.a)({key:t},u({token:e,key:t})))})))}))))))}))},B=n(6),L=(n(64),n(65)),A=n.n(L),I=function(e){return function(t){var n,r=t.id,a=Object(B.a)(t,["id"]),c=Object(O.a)().siteConfig,l=(c=void 0===c?{}:c).themeConfig,i=(l=void 0===l?{}:l).navbar,u=(i=void 0===i?{}:i).hideOnScroll,p=void 0!==u&&u;return r?o.a.createElement(e,a,o.a.createElement("a",{"aria-hidden":"true",tabIndex:-1,className:Object(s.a)("anchor",(n={},n[A.a.enhancedAnchor]=!p,n)),id:r}),a.children,o.a.createElement("a",{"aria-hidden":"true",tabIndex:-1,className:"hash-link",href:"#"+r,title:"Direct link to heading"},"#")):o.a.createElement(e,a)}},R=n(66),F=n.n(R);t.a={code:function(e){var t=e.children;return"string"==typeof t?t.includes("\n")?o.a.createElement(D,e):o.a.createElement("code",e):t},a:function(e){return o.a.createElement(c.a,e)},pre:function(e){return o.a.createElement("div",Object(r.a)({className:F.a.mdxCodeBlock},e))},h1:I("h1"),h2:I("h2"),h3:I("h3"),h4:I("h4"),h5:I("h5"),h6:I("h6")}},152:function(e,t,n){"use strict";const r=(e,{target:t=document.body}={})=>{const n=document.createElement("textarea"),r=document.activeElement;n.value=e,n.setAttribute("readonly",""),n.style.contain="strict",n.style.position="absolute",n.style.left="-9999px",n.style.fontSize="12pt";const a=document.getSelection();let o=!1;a.rangeCount>0&&(o=a.getRangeAt(0)),t.append(n),n.select(),n.selectionStart=0,n.selectionEnd=e.length;let c=!1;try{c=document.execCommand("copy")}catch(s){}return n.remove(),o&&(a.removeAllRanges(),a.addRange(o)),r&&r.focus(),c};e.exports=r,e.exports.default=r},153:function(e,t){e.exports.parse=function(e){var t=e.split(",").map((function(e){return function(e){if(/^-?\d+$/.test(e))return parseInt(e,10);var t;if(t=e.match(/^(-?\d+)(-|\.\.\.?|\u2025|\u2026|\u22EF)(-?\d+)$/)){var n=t[1],r=t[2],a=t[3];if(n&&a){var o=[],c=(n=parseInt(n))<(a=parseInt(a))?1:-1;"-"!=r&&".."!=r&&"\u2025"!=r||(a+=c);for(var s=n;s!=a;s+=c)o.push(s);return o}}return[]}(e)}));return 0===t.length?[]:1===t.length?Array.isArray(t[0])?t[0]:t:t.reduce((function(e,t){return Array.isArray(e)||(e=[e]),Array.isArray(t)||(t=[t]),e.concat(t)}))}}}]);