(window.webpackJsonp=window.webpackJsonp||[]).push([[29],{118:function(e,t,n){"use strict";n.d(t,"a",(function(){return i})),n.d(t,"b",(function(){return d}));var a=n(0),r=n.n(a);function c(e,t,n){return t in e?Object.defineProperty(e,t,{value:n,enumerable:!0,configurable:!0,writable:!0}):e[t]=n,e}function o(e,t){var n=Object.keys(e);if(Object.getOwnPropertySymbols){var a=Object.getOwnPropertySymbols(e);t&&(a=a.filter((function(t){return Object.getOwnPropertyDescriptor(e,t).enumerable}))),n.push.apply(n,a)}return n}function l(e){for(var t=1;t<arguments.length;t++){var n=null!=arguments[t]?arguments[t]:{};t%2?o(Object(n),!0).forEach((function(t){c(e,t,n[t])})):Object.getOwnPropertyDescriptors?Object.defineProperties(e,Object.getOwnPropertyDescriptors(n)):o(Object(n)).forEach((function(t){Object.defineProperty(e,t,Object.getOwnPropertyDescriptor(n,t))}))}return e}function s(e,t){if(null==e)return{};var n,a,r=function(e,t){if(null==e)return{};var n,a,r={},c=Object.keys(e);for(a=0;a<c.length;a++)n=c[a],t.indexOf(n)>=0||(r[n]=e[n]);return r}(e,t);if(Object.getOwnPropertySymbols){var c=Object.getOwnPropertySymbols(e);for(a=0;a<c.length;a++)n=c[a],t.indexOf(n)>=0||Object.prototype.propertyIsEnumerable.call(e,n)&&(r[n]=e[n])}return r}var b=r.a.createContext({}),p=function(e){var t=r.a.useContext(b),n=t;return e&&(n="function"==typeof e?e(t):l(l({},t),e)),n},i=function(e){var t=p(e.components);return r.a.createElement(b.Provider,{value:t},e.children)},u={inlineCode:"code",wrapper:function(e){var t=e.children;return r.a.createElement(r.a.Fragment,{},t)}},j=r.a.forwardRef((function(e,t){var n=e.components,a=e.mdxType,c=e.originalType,o=e.parentName,b=s(e,["components","mdxType","originalType","parentName"]),i=p(n),j=a,d=i["".concat(o,".").concat(j)]||i[j]||u[j]||c;return n?r.a.createElement(d,l(l({ref:t},b),{},{components:n})):r.a.createElement(d,l({ref:t},b))}));function d(e,t){var n=arguments,a=t&&t.mdxType;if("string"==typeof e||a){var c=n.length,o=new Array(c);o[0]=j;var l={};for(var s in t)hasOwnProperty.call(t,s)&&(l[s]=t[s]);l.originalType=e,l.mdxType="string"==typeof e?e:a,o[1]=l;for(var b=2;b<c;b++)o[b]=n[b];return r.a.createElement.apply(null,o)}return r.a.createElement.apply(null,n)}j.displayName="MDXCreateElement"},84:function(e,t,n){"use strict";n.r(t),n.d(t,"frontMatter",(function(){return o})),n.d(t,"metadata",(function(){return l})),n.d(t,"rightToc",(function(){return s})),n.d(t,"default",(function(){return p}));var a=n(2),r=n(6),c=(n(0),n(118)),o={id:"javadoc_Events",title:"Events",sidebar_label:"Events"},l={unversionedId:"javadocs/javadoc_Events",id:"javadocs/javadoc_Events",isDocsHomePage:!1,title:"Events",description:"`",source:"@site/docs/javadocs/Events.md",permalink:"/nuts/docs/javadocs/javadoc_Events",editUrl:"https://github.com/facebook/docusaurus/edit/master/website/docs/javadocs/Events.md",sidebar_label:"Events",sidebar:"someSidebar",previous:{title:"Exception",permalink:"/nuts/docs/javadocs/javadoc_Exception"},next:{title:"Input Output",permalink:"/nuts/docs/javadocs/javadoc_Input_Output"}},s=[{value:"\u2615 NutsInstallListener",id:"-nutsinstalllistener",children:[{value:"\u2699 Instance Methods",id:"-instance-methods",children:[]}]},{value:"\u2615 NutsRepositoryEvent",id:"-nutsrepositoryevent",children:[{value:"\ud83c\udf9b Instance Properties",id:"-instance-properties",children:[]}]},{value:"\u2615 NutsRepositoryListener",id:"-nutsrepositorylistener",children:[{value:"\u2699 Instance Methods",id:"-instance-methods-1",children:[]}]},{value:"\u2615 NutsUpdateEvent",id:"-nutsupdateevent",children:[{value:"\ud83c\udf9b Instance Properties",id:"-instance-properties-1",children:[]}]},{value:"\u2615 NutsWorkspaceEvent",id:"-nutsworkspaceevent",children:[{value:"\ud83c\udf9b Instance Properties",id:"-instance-properties-2",children:[]}]},{value:"\u2615 NutsWorkspaceListener",id:"-nutsworkspacelistener",children:[{value:"\u2699 Instance Methods",id:"-instance-methods-2",children:[]}]}],b={rightToc:s};function p(e){var t=e.components,n=Object(r.a)(e,["components"]);return Object(c.b)("wrapper",Object(a.a)({},b,n,{components:t,mdxType:"MDXLayout"}),Object(c.b)("pre",null,Object(c.b)("code",Object(a.a)({parentName:"pre"},{}),"     __        __           ___    ____  ____\n  /\\ \\ \\ _  __/ /______    /   |  / __ \\/  _/\n /  \\/ / / / / __/ ___/   / /| | / /_/ // /   \n/ /\\  / /_/ / /_(__  )   / ___ |/ ____// /       \n\\_\\ \\/\\__,_/\\__/____/   /_/  |_/_/   /___/  version 0.7.0\n")),Object(c.b)("h2",{id:"-nutsinstalllistener"},"\u2615 NutsInstallListener"),Object(c.b)("pre",null,Object(c.b)("code",Object(a.a)({parentName:"pre"},{className:"language-java"}),"public interface net.vpc.app.nuts.NutsInstallListener\n")),Object(c.b)("p",null," A class can implement the \\<code",">","NutsInstallListener\\</code",">"," interface when it\nwants to be informed of install artifacts actions."),Object(c.b)("p",null," \\@author vpc\n\\@since 0.5.4\n\\@category Events"),Object(c.b)("h3",{id:"-instance-methods"},"\u2699 Instance Methods"),Object(c.b)("h4",{id:"-oninstallevent"},"\u2699 onInstall(event)"),Object(c.b)("p",null,"This method is called whenever the observed workspace installs an artifact."),Object(c.b)("pre",null,Object(c.b)("code",Object(a.a)({parentName:"pre"},{className:"language-java"}),"void onInstall(NutsInstallEvent event)\n")),Object(c.b)("ul",null,Object(c.b)("li",{parentName:"ul"},Object(c.b)("strong",{parentName:"li"},"NutsInstallEvent event")," : event")),Object(c.b)("h4",{id:"-onuninstallevent"},"\u2699 onUninstall(event)"),Object(c.b)("p",null,"This method is called whenever the observed workspace uninstalls an artifact."),Object(c.b)("pre",null,Object(c.b)("code",Object(a.a)({parentName:"pre"},{className:"language-java"}),"void onUninstall(NutsInstallEvent event)\n")),Object(c.b)("ul",null,Object(c.b)("li",{parentName:"ul"},Object(c.b)("strong",{parentName:"li"},"NutsInstallEvent event")," : event")),Object(c.b)("h4",{id:"-onupdateevent"},"\u2699 onUpdate(event)"),Object(c.b)("p",null,"This method is called whenever the observed workspace updates an artifact."),Object(c.b)("pre",null,Object(c.b)("code",Object(a.a)({parentName:"pre"},{className:"language-java"}),"void onUpdate(NutsUpdateEvent event)\n")),Object(c.b)("ul",null,Object(c.b)("li",{parentName:"ul"},Object(c.b)("strong",{parentName:"li"},"NutsUpdateEvent event")," : event")),Object(c.b)("h2",{id:"-nutsrepositoryevent"},"\u2615 NutsRepositoryEvent"),Object(c.b)("pre",null,Object(c.b)("code",Object(a.a)({parentName:"pre"},{className:"language-java"}),"public interface net.vpc.app.nuts.NutsRepositoryEvent\n")),Object(c.b)("p",null," Repository Event\n\\@author vpc\n\\@since 0.5.4\n\\@category Events"),Object(c.b)("h3",{id:"-instance-properties"},"\ud83c\udf9b Instance Properties"),Object(c.b)("h4",{id:"-parent"},"\ud83d\udcc4\ud83c\udf9b parent"),Object(c.b)("p",null,"Parent repository when this event is about creating\na new repository with a parent one."),Object(c.b)("pre",null,Object(c.b)("code",Object(a.a)({parentName:"pre"},{className:"language-java"}),"[read-only] NutsRepository public parent\npublic NutsRepository getParent()\n")),Object(c.b)("h4",{id:"-propertyname"},"\ud83d\udcc4\ud83c\udf9b propertyName"),Object(c.b)("p",null,"event property name"),Object(c.b)("pre",null,Object(c.b)("code",Object(a.a)({parentName:"pre"},{className:"language-java"}),"[read-only] String public propertyName\npublic String getPropertyName()\n")),Object(c.b)("h4",{id:"-propertyoldvalue"},"\ud83d\udcc4\ud83c\udf9b propertyOldValue"),Object(c.b)("p",null,"event property old value"),Object(c.b)("pre",null,Object(c.b)("code",Object(a.a)({parentName:"pre"},{className:"language-java"}),"[read-only] Object public propertyOldValue\npublic Object getPropertyOldValue()\n")),Object(c.b)("h4",{id:"-propertyvalue"},"\ud83d\udcc4\ud83c\udf9b propertyValue"),Object(c.b)("p",null,"event property new value"),Object(c.b)("pre",null,Object(c.b)("code",Object(a.a)({parentName:"pre"},{className:"language-java"}),"[read-only] Object public propertyValue\npublic Object getPropertyValue()\n")),Object(c.b)("h4",{id:"-repository"},"\ud83d\udcc4\ud83c\udf9b repository"),Object(c.b)("p",null,"repository that fires this event or the new repository\nwhen creating a new one with parent."),Object(c.b)("pre",null,Object(c.b)("code",Object(a.a)({parentName:"pre"},{className:"language-java"}),"[read-only] NutsRepository public repository\npublic NutsRepository getRepository()\n")),Object(c.b)("h4",{id:"-session"},"\ud83d\udcc4\ud83c\udf9b session"),Object(c.b)("p",null,"current session"),Object(c.b)("pre",null,Object(c.b)("code",Object(a.a)({parentName:"pre"},{className:"language-java"}),"[read-only] NutsSession public session\npublic NutsSession getSession()\n")),Object(c.b)("h4",{id:"-workspace"},"\ud83d\udcc4\ud83c\udf9b workspace"),Object(c.b)("p",null,"current workspace"),Object(c.b)("pre",null,Object(c.b)("code",Object(a.a)({parentName:"pre"},{className:"language-java"}),"[read-only] NutsWorkspace public workspace\npublic NutsWorkspace getWorkspace()\n")),Object(c.b)("h2",{id:"-nutsrepositorylistener"},"\u2615 NutsRepositoryListener"),Object(c.b)("pre",null,Object(c.b)("code",Object(a.a)({parentName:"pre"},{className:"language-java"}),"public interface net.vpc.app.nuts.NutsRepositoryListener\n")),Object(c.b)("p",null," Created by vpc on 1/20/17."),Object(c.b)("p",null," \\@since 0.5.4\n\\@category Events"),Object(c.b)("h3",{id:"-instance-methods-1"},"\u2699 Instance Methods"),Object(c.b)("h4",{id:"-onaddrepositoryevent"},"\u2699 onAddRepository(event)"),Object(c.b)("pre",null,Object(c.b)("code",Object(a.a)({parentName:"pre"},{className:"language-java"}),"void onAddRepository(NutsRepositoryEvent event)\n")),Object(c.b)("ul",null,Object(c.b)("li",{parentName:"ul"},Object(c.b)("strong",{parentName:"li"},"NutsRepositoryEvent event")," : ")),Object(c.b)("h4",{id:"-onconfigurationchangedevent"},"\u2699 onConfigurationChanged(event)"),Object(c.b)("pre",null,Object(c.b)("code",Object(a.a)({parentName:"pre"},{className:"language-java"}),"void onConfigurationChanged(NutsRepositoryEvent event)\n")),Object(c.b)("ul",null,Object(c.b)("li",{parentName:"ul"},Object(c.b)("strong",{parentName:"li"},"NutsRepositoryEvent event")," : ")),Object(c.b)("h4",{id:"-ondeployevent"},"\u2699 onDeploy(event)"),Object(c.b)("pre",null,Object(c.b)("code",Object(a.a)({parentName:"pre"},{className:"language-java"}),"void onDeploy(NutsContentEvent event)\n")),Object(c.b)("ul",null,Object(c.b)("li",{parentName:"ul"},Object(c.b)("strong",{parentName:"li"},"NutsContentEvent event")," : ")),Object(c.b)("h4",{id:"-onpushevent"},"\u2699 onPush(event)"),Object(c.b)("pre",null,Object(c.b)("code",Object(a.a)({parentName:"pre"},{className:"language-java"}),"void onPush(NutsContentEvent event)\n")),Object(c.b)("ul",null,Object(c.b)("li",{parentName:"ul"},Object(c.b)("strong",{parentName:"li"},"NutsContentEvent event")," : ")),Object(c.b)("h4",{id:"-onremoverepositoryevent"},"\u2699 onRemoveRepository(event)"),Object(c.b)("pre",null,Object(c.b)("code",Object(a.a)({parentName:"pre"},{className:"language-java"}),"void onRemoveRepository(NutsRepositoryEvent event)\n")),Object(c.b)("ul",null,Object(c.b)("li",{parentName:"ul"},Object(c.b)("strong",{parentName:"li"},"NutsRepositoryEvent event")," : ")),Object(c.b)("h4",{id:"-onundeployevent"},"\u2699 onUndeploy(event)"),Object(c.b)("pre",null,Object(c.b)("code",Object(a.a)({parentName:"pre"},{className:"language-java"}),"void onUndeploy(NutsContentEvent event)\n")),Object(c.b)("ul",null,Object(c.b)("li",{parentName:"ul"},Object(c.b)("strong",{parentName:"li"},"NutsContentEvent event")," : ")),Object(c.b)("h2",{id:"-nutsupdateevent"},"\u2615 NutsUpdateEvent"),Object(c.b)("pre",null,Object(c.b)("code",Object(a.a)({parentName:"pre"},{className:"language-java"}),"public interface net.vpc.app.nuts.NutsUpdateEvent\n")),Object(c.b)("p",null," \\@author vpc\n\\@since 0.5.6\n\\@category Events"),Object(c.b)("h3",{id:"-instance-properties-1"},"\ud83c\udf9b Instance Properties"),Object(c.b)("h4",{id:"-force"},"\ud83d\udcc4\ud83c\udf9b force"),Object(c.b)("pre",null,Object(c.b)("code",Object(a.a)({parentName:"pre"},{className:"language-java"}),"[read-only] boolean public force\npublic boolean isForce()\n")),Object(c.b)("h4",{id:"-newvalue"},"\ud83d\udcc4\ud83c\udf9b newValue"),Object(c.b)("pre",null,Object(c.b)("code",Object(a.a)({parentName:"pre"},{className:"language-java"}),"[read-only] NutsDefinition public newValue\npublic NutsDefinition getNewValue()\n")),Object(c.b)("h4",{id:"-oldvalue"},"\ud83d\udcc4\ud83c\udf9b oldValue"),Object(c.b)("pre",null,Object(c.b)("code",Object(a.a)({parentName:"pre"},{className:"language-java"}),"[read-only] NutsDefinition public oldValue\npublic NutsDefinition getOldValue()\n")),Object(c.b)("h4",{id:"-session-1"},"\ud83d\udcc4\ud83c\udf9b session"),Object(c.b)("pre",null,Object(c.b)("code",Object(a.a)({parentName:"pre"},{className:"language-java"}),"[read-only] NutsSession public session\npublic NutsSession getSession()\n")),Object(c.b)("h4",{id:"-workspace-1"},"\ud83d\udcc4\ud83c\udf9b workspace"),Object(c.b)("pre",null,Object(c.b)("code",Object(a.a)({parentName:"pre"},{className:"language-java"}),"[read-only] NutsWorkspace public workspace\npublic NutsWorkspace getWorkspace()\n")),Object(c.b)("h2",{id:"-nutsworkspaceevent"},"\u2615 NutsWorkspaceEvent"),Object(c.b)("pre",null,Object(c.b)("code",Object(a.a)({parentName:"pre"},{className:"language-java"}),"public interface net.vpc.app.nuts.NutsWorkspaceEvent\n")),Object(c.b)("p",null," \\@author vpc\n\\@since 0.5.4\n\\@category Events"),Object(c.b)("h3",{id:"-instance-properties-2"},"\ud83c\udf9b Instance Properties"),Object(c.b)("h4",{id:"-propertyname-1"},"\ud83d\udcc4\ud83c\udf9b propertyName"),Object(c.b)("pre",null,Object(c.b)("code",Object(a.a)({parentName:"pre"},{className:"language-java"}),"[read-only] String public propertyName\npublic String getPropertyName()\n")),Object(c.b)("h4",{id:"-propertyoldvalue-1"},"\ud83d\udcc4\ud83c\udf9b propertyOldValue"),Object(c.b)("pre",null,Object(c.b)("code",Object(a.a)({parentName:"pre"},{className:"language-java"}),"[read-only] Object public propertyOldValue\npublic Object getPropertyOldValue()\n")),Object(c.b)("h4",{id:"-propertyvalue-1"},"\ud83d\udcc4\ud83c\udf9b propertyValue"),Object(c.b)("pre",null,Object(c.b)("code",Object(a.a)({parentName:"pre"},{className:"language-java"}),"[read-only] Object public propertyValue\npublic Object getPropertyValue()\n")),Object(c.b)("h4",{id:"-repository-1"},"\ud83d\udcc4\ud83c\udf9b repository"),Object(c.b)("pre",null,Object(c.b)("code",Object(a.a)({parentName:"pre"},{className:"language-java"}),"[read-only] NutsRepository public repository\npublic NutsRepository getRepository()\n")),Object(c.b)("h4",{id:"-session-2"},"\ud83d\udcc4\ud83c\udf9b session"),Object(c.b)("pre",null,Object(c.b)("code",Object(a.a)({parentName:"pre"},{className:"language-java"}),"[read-only] NutsSession public session\npublic NutsSession getSession()\n")),Object(c.b)("h4",{id:"-workspace-2"},"\ud83d\udcc4\ud83c\udf9b workspace"),Object(c.b)("pre",null,Object(c.b)("code",Object(a.a)({parentName:"pre"},{className:"language-java"}),"[read-only] NutsWorkspace public workspace\npublic NutsWorkspace getWorkspace()\n")),Object(c.b)("h2",{id:"-nutsworkspacelistener"},"\u2615 NutsWorkspaceListener"),Object(c.b)("pre",null,Object(c.b)("code",Object(a.a)({parentName:"pre"},{className:"language-java"}),"public interface net.vpc.app.nuts.NutsWorkspaceListener\n")),Object(c.b)("p",null," Created by vpc on 1/20/17."),Object(c.b)("p",null," \\@since 0.5.4\n\\@category Events"),Object(c.b)("h3",{id:"-instance-methods-2"},"\u2699 Instance Methods"),Object(c.b)("h4",{id:"-onaddrepositoryevent-1"},"\u2699 onAddRepository(event)"),Object(c.b)("pre",null,Object(c.b)("code",Object(a.a)({parentName:"pre"},{className:"language-java"}),"void onAddRepository(NutsWorkspaceEvent event)\n")),Object(c.b)("ul",null,Object(c.b)("li",{parentName:"ul"},Object(c.b)("strong",{parentName:"li"},"NutsWorkspaceEvent event")," : ")),Object(c.b)("h4",{id:"-onconfigurationchangedevent-1"},"\u2699 onConfigurationChanged(event)"),Object(c.b)("pre",null,Object(c.b)("code",Object(a.a)({parentName:"pre"},{className:"language-java"}),"void onConfigurationChanged(NutsWorkspaceEvent event)\n")),Object(c.b)("ul",null,Object(c.b)("li",{parentName:"ul"},Object(c.b)("strong",{parentName:"li"},"NutsWorkspaceEvent event")," : ")),Object(c.b)("h4",{id:"-oncreateworkspaceevent"},"\u2699 onCreateWorkspace(event)"),Object(c.b)("pre",null,Object(c.b)("code",Object(a.a)({parentName:"pre"},{className:"language-java"}),"void onCreateWorkspace(NutsWorkspaceEvent event)\n")),Object(c.b)("ul",null,Object(c.b)("li",{parentName:"ul"},Object(c.b)("strong",{parentName:"li"},"NutsWorkspaceEvent event")," : ")),Object(c.b)("h4",{id:"-onreloadworkspaceevent"},"\u2699 onReloadWorkspace(event)"),Object(c.b)("pre",null,Object(c.b)("code",Object(a.a)({parentName:"pre"},{className:"language-java"}),"void onReloadWorkspace(NutsWorkspaceEvent event)\n")),Object(c.b)("ul",null,Object(c.b)("li",{parentName:"ul"},Object(c.b)("strong",{parentName:"li"},"NutsWorkspaceEvent event")," : ")),Object(c.b)("h4",{id:"-onremoverepositoryevent-1"},"\u2699 onRemoveRepository(event)"),Object(c.b)("pre",null,Object(c.b)("code",Object(a.a)({parentName:"pre"},{className:"language-java"}),"void onRemoveRepository(NutsWorkspaceEvent event)\n")),Object(c.b)("ul",null,Object(c.b)("li",{parentName:"ul"},Object(c.b)("strong",{parentName:"li"},"NutsWorkspaceEvent event")," : ")),Object(c.b)("h4",{id:"-onupdatepropertyevent"},"\u2699 onUpdateProperty(event)"),Object(c.b)("pre",null,Object(c.b)("code",Object(a.a)({parentName:"pre"},{className:"language-java"}),"void onUpdateProperty(NutsWorkspaceEvent event)\n")),Object(c.b)("ul",null,Object(c.b)("li",{parentName:"ul"},Object(c.b)("strong",{parentName:"li"},"NutsWorkspaceEvent event")," : ")))}p.isMDXComponent=!0}}]);