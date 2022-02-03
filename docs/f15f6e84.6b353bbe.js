(window.webpackJsonp=window.webpackJsonp||[]).push([[61],{115:function(e,t,a){"use strict";a.r(t);var n=a(2),l=a(0),r=a.n(l),i=a(140),c=a(126),s=a(124),m=a(122),o=a(125),u=a(49),g=a.n(u),d=[{title:r.a.createElement(r.a.Fragment,null,"Download Installer"),imageUrl:"img/run.png",description:r.a.createElement(r.a.Fragment,null,r.a.createElement("p",null,"Download a graphical installer that will make it simple to install nuts package manager. After downloading the installer, just double click the file and follow the installation wizard instructions. A valid ",r.a.createElement("img",{src:"/static/img/java.png",alt:"",width:"16"})," java 1.8+ runtime is required."),r.a.createElement("ul",null,r.a.createElement("li",null,r.a.createElement("img",{src:"/static/img/java.png",alt:"",width:"32"})," \xa0",r.a.createElement(s.a,{className:Object(i.a)("button button--secondary--lg b2 ",g.a.getStarted),href:"https://thevpc.net/nuts-installer.jar",target:"_blank"},"Portable Installer")),r.a.createElement("li",null,r.a.createElement("img",{src:"/static/img/linux.png",alt:"",width:"32"})," \xa0",r.a.createElement(s.a,{className:Object(i.a)("button button--secondary--lg b2 ",g.a.getStarted),href:"https://thevpc.net/nuts-installer-linux-x64",target:"_blank"},"Linux x64 Installer")),r.a.createElement("li",null,r.a.createElement("img",{src:"/static/img/windows.png",alt:"",width:"32"})," \xa0",r.a.createElement(s.a,{className:Object(i.a)("button button--secondary--lg b2 ",g.a.getStarted),href:"https://thevpc.net/nuts-installer-windows-x64.exe",target:"_blank"},"Windows x64 Installer")),r.a.createElement("li",null,r.a.createElement("img",{src:"/static/img/macos.png",alt:"",width:"32"})," \xa0",r.a.createElement(s.a,{className:Object(i.a)("button button--secondary--lg b2 ",g.a.getStarted),href:"https://thevpc.net/nuts-installer-macos-x64",target:"_blank",disabled:!0},"MacOS x64 Installer"))))},{title:r.a.createElement(r.a.Fragment,null,"Download Raw Jar Package"),imageUrl:"img/jar2.png",description:r.a.createElement(r.a.Fragment,null,r.a.createElement("p",null,"Download raw jar file to perform installation using your favourite shell. After downloading the installer, follow the documentation to install the package manager. Use 'Portable' version for production and 'Preview' for all other cases. A valid ",r.a.createElement("img",{src:"/static/img/java.png",alt:"",width:"16"})," java 1.8+ runtime is required."),r.a.createElement("ul",null,r.a.createElement("li",null,r.a.createElement("img",{src:"/static/img/java.png",alt:"",width:"32"})," \xa0",r.a.createElement(s.a,{className:Object(i.a)("button button--secondary--lg b2 ",g.a.getStarted),href:"https://thevpc.net/nuts-stable.jar",target:"_blank"},"Stable 0.8.3 Jar")),r.a.createElement("li",null,r.a.createElement("img",{src:"/static/img/java.png",alt:"",width:"32"})," \xa0",r.a.createElement(s.a,{className:Object(i.a)("button button--secondary b3",g.a.getStarted),href:"https://thevpc.net/nuts-preview.jar",target:"_blank"},"Preview 0.8.3 Jar"))))},,{title:r.a.createElement(r.a.Fragment,null,"Install Manually"),imageUrl:"img/terminal.png",description:r.a.createElement(r.a.Fragment,null,r.a.createElement("p",null,"Use one commandline to download and install Nuts package manager with the help of cUrl command. Use 'Portable' version for production and 'Preview' version for all other cases. A valid ",r.a.createElement("img",{src:"/static/img/java.png",alt:"",width:"16"})," java 1.8+ runtime is required."),r.a.createElement(s.a,{className:Object(i.a)("button button--secondary--lg b2 ",g.a.getStarted)},"Stable"),r.a.createElement("pre",null,"curl -sOL https://repo.maven.apache.org/maven2/net/thevpc/nuts/nuts/0.8.3/nuts-0.8.3.jar -o nuts.jar && java -jar nuts.jar -Zy"),r.a.createElement(s.a,{className:Object(i.a)("button button--secondary b3",g.a.getStarted)},"Preview"),r.a.createElement("pre",null,"curl -sOL https://thevpc.net/nuts-preview.jar -o nuts.jar && java -jar nuts.jar -Zy"))}];function p(e){var t=e.imageUrl,a=e.title,n=e.description,l=Object(o.a)(t);return r.a.createElement("div",{className:Object(i.a)("col col--4",g.a.feature)},l&&r.a.createElement("div",{className:"text--center"},r.a.createElement("img",{className:g.a.featureImage,src:l,alt:a})),r.a.createElement("h3",null,a),r.a.createElement("p",null,n))}t.default=function(){var e=Object(m.a)().siteConfig,t=void 0===e?{}:e;return r.a.createElement(c.a,{title:t.title+", the Java Package Manager",description:"Description will go into a meta tag in <head />"},r.a.createElement("header",{className:Object(i.a)("hero hero--primary",g.a.heroBanner)},r.a.createElement("div",{className:"container"},r.a.createElement("h1",{className:"hero__title"},r.a.createElement("img",{src:"/static/img/nuts-icon.png",alt:"",width:"64"}),t.title),r.a.createElement("p",{className:"hero__subtitle"},t.tagline),r.a.createElement("img",{src:"https://thevpc.net/nuts/images/pixel.gif?q=nuts-gsite",alt:""}),"Choose your download format...")),r.a.createElement("main",null,d&&d.length>0&&r.a.createElement("section",{className:g.a.features},r.a.createElement("div",{className:"container"},r.a.createElement("div",{className:"row"},d.map((function(e,t){return r.a.createElement(p,Object(n.a)({key:t},e))})))))))}},140:function(e,t,a){"use strict";function n(e){var t,a,l="";if("string"==typeof e||"number"==typeof e)l+=e;else if("object"==typeof e)if(Array.isArray(e))for(t=0;t<e.length;t++)e[t]&&(a=n(e[t]))&&(l&&(l+=" "),l+=a);else for(t in e)e[t]&&(l&&(l+=" "),l+=t);return l}t.a=function(){for(var e,t,a=0,l="";a<arguments.length;)(e=arguments[a++])&&(t=n(e))&&(l&&(l+=" "),l+=t);return l}}}]);