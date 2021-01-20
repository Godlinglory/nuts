package net.thevpc.nuts.runtime.core.format.text.stylethemes;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.bundles.parsers.StringReaderExt;
import net.thevpc.nuts.runtime.core.util.CoreCommonUtils;
import net.thevpc.nuts.runtime.core.util.CoreIOUtils;
import net.thevpc.nuts.runtime.core.util.CoreStringUtils;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class NutsTextFormatPropertiesTheme implements NutsTextFormatTheme {
    private Properties props = new Properties();
    private NutsWorkspace ws;

    public NutsTextFormatPropertiesTheme(String name,ClassLoader cls,NutsWorkspace ws){
        this.ws=ws;
        if(name.indexOf('/')>=0 || name.indexOf('\\')>=0){
            if(name.startsWith("classpath://")){
                if(cls==null){
                    cls=getClass().getClassLoader();//Thread.currentThread().getContextClassLoader();
                }
                String r = name.substring("classpath://".length());
                if(!r.startsWith("/")){
                    r="/"+r;
                }
                URL u=cls.getResource(r);
                if(u==null){
                    if(!r.endsWith(".ntf-theme")){
                        r+=".ntf-theme";
                        u=cls.getResource(r);
                    }
                }
                if(u==null){
                    throw new IllegalArgumentException("invalid theme: "+name);
                }
                try {
                    props.load(u.openStream());
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }
            if(CoreIOUtils.isURL(name)){
                try {
                    URL uu=new URL(name);
                    InputStream inStream = null;
                    inStream=uu.openStream();
                    if(inStream==null){
                        throw new IllegalArgumentException("invalid theme: "+name);
                    }
                    try {
                        props.load(inStream);
                    }finally {
                        inStream.close();
                    }
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }else{
                try (InputStream inStream=new FileInputStream(new File(name))){
                    props.load(inStream);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }
        }else {
            if(cls==null){
                cls=getClass().getClassLoader();//Thread.currentThread().getContextClassLoader();
            }
            URL u = cls.getResource("META-INF/ntf-themes/" + name + ".ntf-theme");
            if(u!=null){
                try {
                    InputStream inStream = null;
                    inStream=u.openStream();
                    if(inStream==null){
                        throw new IllegalArgumentException("invalid theme: "+name);
                    }
                    try {
                        props.load(inStream);
                    }finally {
                        inStream.close();
                    }
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }else {
                Path themeFile = Paths.get(ws.locations().getStoreLocation(
                        ws.id().parser().parse("net.thevpc.nuts:nuts-runtime#SHARED"),
                        NutsStoreLocation.CONFIG
                )).resolve("themes").resolve(name);
                if(Files.isRegularFile(themeFile)){
                    try (InputStream inStream=Files.newInputStream(themeFile)){
                        props.load(inStream);
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                }else{
                    throw new IllegalArgumentException("invalid theme: "+name);
                }
            }
        }
    }

    @Override
    public String getName() {
        String themeName = props.getProperty("theme-name");
        if (CoreStringUtils.isBlank(themeName)) {
            themeName = UUID.randomUUID().toString();
            props.put("theme-name", themeName);
        }
        return themeName;
    }

    private String getProp(NutsTextNodeStyleType t, int variant){
        String v = getProp(t, "" + variant);
        if(v!=null){
            return v;
        }
        v = getProp(t, "*");
        if(v!=null){
            StringReaderExt r=new StringReaderExt(v);
            StringBuilder sb=new StringBuilder();
            while(r.hasNext()){
                if(r.peekChar()=='*'){
                    if(r.hasNext(1) && r.peekChar(1)=='%') {
                        r.nextChar();
                        r.nextChar();
                        StringBuilder mod = new StringBuilder();
                        while (r.hasNext() && r.peekChar() >= '0' && r.peekChar() <= '9') {
                            mod.append(r.nextChar());
                        }
                        sb.append(variant % (Integer.parseInt(mod.toString())));
                    }else {
                        r.nextChar();
                        sb.append(variant);
                    }
                }else{
                    sb.append(r.nextChar());
                }
            }
            return sb.toString();
        }
        return v;
    }

    private int getVarVal(String n){
        String z = props.getProperty(n);
        if(z!=null){
            try {
                return Integer.parseInt(z);
            }catch (Exception ex){
                return 0;
            }
        }
        return 0;
    }
    private String getProp(NutsTextNodeStyleType t, String variant){
        String name=t.name();
        String s = props.getProperty(name + "(" + variant + ")");
        if(s==null){
            s = props.getProperty(name.toLowerCase() + "(" + variant + ")");
        }
        return s;
    }

    @Override
    public NutsTextNodeStyle[] toBasicStyles(NutsTextNodeStyle style) {
        return toBasicStyles(style,20);
    }

    public NutsTextNodeStyle[] toBasicStyles(NutsTextNodeStyle style,int maxLoop) {
        if(maxLoop<=0){
            throw new NutsIllegalArgumentException(ws,"invalid ntf theme for "+style.getType()+"("+style.getVariant()+"). infinite loop");
        }
        if(style.getType().basic()){
            return new NutsTextNodeStyle[]{style};
        }
        String s = getProp(style.getType(),style.getVariant());
        if(s==null){
            return new NutsTextNodeStyle[0];
        }
        List<NutsTextNodeStyle> ret=new ArrayList<>();
        for (String v : s.split(",")) {
            NutsTextNodeStyle[] ss = toBasicStyles(v, style.getVariant(),maxLoop-1);
            if(ss!=null){
                ret.addAll(Arrays.asList(ss));
            }
        }
        return ret.toArray(new NutsTextNodeStyle[0]);
    }

    public NutsTextNodeStyle[] toBasicStyles(String v, int defaultVariant,int maxLoop) {
        v=v.trim();
        int a=v.indexOf('(');
        if(a>0){
            int b=v.indexOf(')',a);
            if(b>0){
                String n=v.substring(a+1,b);
                String k=v.substring(0,a);
                if(n.equals("*")){
                    n=""+defaultVariant;
                }
                switch (k.toLowerCase()){
                    case "foreground":
                    case "foregroundcolor":
                        {
                        Integer ii=CoreCommonUtils.convertToInteger(n,null);
                        if(ii==null){
                            ii=getVarVal(n);
                        }
                        return new NutsTextNodeStyle[]{
                                NutsTextNodeStyle.foregroundColor(ii)
                        };
                    }
                    case "plain":{
                        return new NutsTextNodeStyle[]{};
                    }
                    case "foregroundtruecolor":{
                        Integer ii=CoreCommonUtils.convertToInteger(n,null);
                        if(ii==null){
                            ii=getVarVal(n);
                        }
                        return new NutsTextNodeStyle[]{
                                NutsTextNodeStyle.foregroundTrueColor(ii)
                        };
                    }
                    case "background":
                    case "backgroundcolor":{
                        Integer ii=CoreCommonUtils.convertToInteger(n,null);
                        if(ii==null){
                            ii=getVarVal(n);
                        }
                        return new NutsTextNodeStyle[]{
                                NutsTextNodeStyle.backgroundColor(ii)
                        };
                    }
                    case "backgroundtruecolor":{
                        Integer ii=CoreCommonUtils.convertToInteger(n,null);
                        if(ii==null){
                            ii=getVarVal(n);
                        }
                        return new NutsTextNodeStyle[]{
                                NutsTextNodeStyle.backgroundTrueColor(ii)
                        };
                    }

                    case "primary":{
                        Integer ii=CoreCommonUtils.convertToInteger(n,null);
                        if(ii==null){
                            ii=getVarVal(n);
                        }
                        return toBasicStyles(NutsTextNodeStyle.primary(ii),maxLoop);
                    }
                    case "secondary":{
                        Integer ii=CoreCommonUtils.convertToInteger(n,null);
                        if(ii==null){
                            ii=getVarVal(n);
                        }
                        return toBasicStyles(NutsTextNodeStyle.secondary(ii),maxLoop);
                    }
                    case "underlined":{
                        Integer ii=CoreCommonUtils.convertToInteger(n,null);
                        if(ii==null){
                            ii=getVarVal(n);
                        }
                        return toBasicStyles(NutsTextNodeStyle.underlined(),maxLoop);
                    }
                    case "bold":{
                        Integer ii=CoreCommonUtils.convertToInteger(n,null);
                        if(ii==null){
                            ii=getVarVal(n);
                        }
                        return toBasicStyles(NutsTextNodeStyle.bold(),maxLoop);
                    }
                    case "bool":{
                        Integer ii=CoreCommonUtils.convertToInteger(n,null);
                        if(ii==null){
                            ii=getVarVal(n);
                        }
                        return toBasicStyles(NutsTextNodeStyle.bool(ii),maxLoop);
                    }
                    case "blink":{
                        Integer ii=CoreCommonUtils.convertToInteger(n,null);
                        if(ii==null){
                            ii=getVarVal(n);
                        }
                        return toBasicStyles(NutsTextNodeStyle.blink(),maxLoop);
                    }
                    case "comments":{
                        Integer ii=CoreCommonUtils.convertToInteger(n,null);
                        if(ii==null){
                            ii=getVarVal(n);
                        }
                        return toBasicStyles(NutsTextNodeStyle.comments(ii),maxLoop);
                    }
                    case "config":{
                        Integer ii=CoreCommonUtils.convertToInteger(n,null);
                        if(ii==null){
                            ii=getVarVal(n);
                        }
                        return toBasicStyles(NutsTextNodeStyle.config(ii),maxLoop);
                    }
                    case "danger":{
                        Integer ii=CoreCommonUtils.convertToInteger(n,null);
                        if(ii==null){
                            ii=getVarVal(n);
                        }
                        return toBasicStyles(NutsTextNodeStyle.danger(ii),maxLoop);
                    }
                    case "date":{
                        Integer ii=CoreCommonUtils.convertToInteger(n,null);
                        if(ii==null){
                            ii=getVarVal(n);
                        }
                        return toBasicStyles(NutsTextNodeStyle.date(ii),maxLoop);
                    }
                    case "number":{
                        Integer ii=CoreCommonUtils.convertToInteger(n,null);
                        if(ii==null){
                            ii=getVarVal(n);
                        }
                        return toBasicStyles(NutsTextNodeStyle.number(ii),maxLoop);
                    }
                    case "error":{
                        Integer ii=CoreCommonUtils.convertToInteger(n,null);
                        if(ii==null){
                            ii=getVarVal(n);
                        }
                        return toBasicStyles(NutsTextNodeStyle.error(ii),maxLoop);
                    }
                    case "warn":{
                        Integer ii=CoreCommonUtils.convertToInteger(n,null);
                        if(ii==null){
                            ii=getVarVal(n);
                        }
                        return toBasicStyles(NutsTextNodeStyle.warn(ii),maxLoop);
                    }
                    case "version":{
                        Integer ii=CoreCommonUtils.convertToInteger(n,null);
                        if(ii==null){
                            ii=getVarVal(n);
                        }
                        return toBasicStyles(NutsTextNodeStyle.version(ii),maxLoop);
                    }
                    case "variable":{
                        Integer ii=CoreCommonUtils.convertToInteger(n,null);
                        if(ii==null){
                            ii=getVarVal(n);
                        }
                        return toBasicStyles(NutsTextNodeStyle.variable(ii),maxLoop);
                    }
                    case "input":{
                        Integer ii=CoreCommonUtils.convertToInteger(n,null);
                        if(ii==null){
                            ii=getVarVal(n);
                        }
                        return toBasicStyles(NutsTextNodeStyle.input(ii),maxLoop);
                    }
                    case "title":{
                        Integer ii=CoreCommonUtils.convertToInteger(n,null);
                        if(ii==null){
                            ii=getVarVal(n);
                        }
                        return toBasicStyles(NutsTextNodeStyle.title(ii),maxLoop);
                    }
                    case "success":{
                        Integer ii=CoreCommonUtils.convertToInteger(n,null);
                        if(ii==null){
                            ii=getVarVal(n);
                        }
                        return toBasicStyles(NutsTextNodeStyle.success(ii),maxLoop);
                    }
                    case "string":{
                        Integer ii=CoreCommonUtils.convertToInteger(n,null);
                        if(ii==null){
                            ii=getVarVal(n);
                        }
                        return toBasicStyles(NutsTextNodeStyle.string(ii),maxLoop);
                    }
                    case "striked":{
                        Integer ii=CoreCommonUtils.convertToInteger(n,null);
                        if(ii==null){
                            ii=getVarVal(n);
                        }
                        return toBasicStyles(NutsTextNodeStyle.striked(ii),maxLoop);
                    }
                    case "separator":{
                        Integer ii=CoreCommonUtils.convertToInteger(n,null);
                        if(ii==null){
                            ii=getVarVal(n);
                        }
                        return toBasicStyles(NutsTextNodeStyle.separator(ii),maxLoop);
                    }
                    case "reversed":{
                        Integer ii=CoreCommonUtils.convertToInteger(n,null);
                        if(ii==null){
                            ii=getVarVal(n);
                        }
                        return toBasicStyles(NutsTextNodeStyle.reversed(ii),maxLoop);
                    }
                    case "path":{
                        Integer ii=CoreCommonUtils.convertToInteger(n,null);
                        if(ii==null){
                            ii=getVarVal(n);
                        }
                        return toBasicStyles(NutsTextNodeStyle.path(ii),maxLoop);
                    }
                    case "option":{
                        Integer ii=CoreCommonUtils.convertToInteger(n,null);
                        if(ii==null){
                            ii=getVarVal(n);
                        }
                        return toBasicStyles(NutsTextNodeStyle.option(ii),maxLoop);
                    }
                    case "pale":{
                        Integer ii=CoreCommonUtils.convertToInteger(n,null);
                        if(ii==null){
                            ii=getVarVal(n);
                        }
                        return toBasicStyles(NutsTextNodeStyle.pale(ii),maxLoop);
                    }
                    case "operator":{
                        Integer ii=CoreCommonUtils.convertToInteger(n,null);
                        if(ii==null){
                            ii=getVarVal(n);
                        }
                        return toBasicStyles(NutsTextNodeStyle.operator(ii),maxLoop);
                    }
                    case "keyword":{
                        Integer ii=CoreCommonUtils.convertToInteger(n,null);
                        if(ii==null){
                            ii=getVarVal(n);
                        }
                        return toBasicStyles(NutsTextNodeStyle.keyword(ii),maxLoop);
                    }
                    case "italic":{
                        Integer ii=CoreCommonUtils.convertToInteger(n,null);
                        if(ii==null){
                            ii=getVarVal(n);
                        }
                        return toBasicStyles(NutsTextNodeStyle.italic(ii),maxLoop);
                    }
                    case "info":{
                        Integer ii=CoreCommonUtils.convertToInteger(n,null);
                        if(ii==null){
                            ii=getVarVal(n);
                        }
                        return toBasicStyles(NutsTextNodeStyle.info(ii),maxLoop);
                    }
                    case "fail":{
                        Integer ii=CoreCommonUtils.convertToInteger(n,null);
                        if(ii==null){
                            ii=getVarVal(n);
                        }
                        return toBasicStyles(NutsTextNodeStyle.fail(ii),maxLoop);
                    }
                }
            }
        }
        return new NutsTextNodeStyle[0];
    }
}
