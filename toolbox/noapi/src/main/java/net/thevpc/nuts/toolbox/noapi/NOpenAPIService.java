package net.thevpc.nuts.toolbox.noapi;

import net.thevpc.commons.md.*;
import net.thevpc.commons.md.asciidoctor.AsciiDoctorWriter;
import net.thevpc.nuts.*;
import org.asciidoctor.Asciidoctor;
import org.asciidoctor.OptionsBuilder;
import org.asciidoctor.SafeMode;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class NOpenAPIService {
    private NutsApplicationContext appContext;

    public NOpenAPIService(NutsApplicationContext appContext) {
        this.appContext = appContext;
    }

    public void run(String source, String target, boolean keep) {

//        Path path = Paths.get("/data/from-git/RapiPdf/docs/specs/maghrebia-api-1.1.2.yml");

        String targetType = "pdf";
        if (target == null) {
            target = addExtension(source, "pdf").toString();
            targetType = "pdf";
        } else if (target.equals(".pdf")) {
            target = addExtension(source, "pdf").toString();
            targetType = "pdf";
        } else if (target.equals(".adoc")) {
            target = addExtension(source, "adoc").toString();
            targetType = "adoc";
        }
        MdDocument md = toMarkdown(source);
        if (targetType.equals("adoc")) {
            writeAdoc(md, target,appContext.getSession().isPlainTrace());
        } else if (targetType.equals("pdf")) {
            String temp = null;
            if (keep) {
                temp = addExtension(source, "adoc").toString();
            } else {
                temp = appContext.getWorkspace().io().tmp()
                        .setSession(appContext.getSession())
                        .createTempFile("temp.adoc");
            }
            writeAdoc(md, temp,keep && appContext.getSession().isPlainTrace());
            if (new File(target).getParentFile() != null) {
                new File(target).getParentFile().mkdirs();
            }
            Asciidoctor asciidoctor = Asciidoctor.Factory.create();
            String outfile = asciidoctor.convertFile(new File(temp),
                    OptionsBuilder.options()
//                            .inPlace(true)
                            .backend("pdf")
                            .safe(SafeMode.UNSAFE)
                            .toFile(new File(target))
                    );
            if (appContext.getSession().isPlainTrace()) {
                    appContext.getSession().out().printf("generated pdf %s\n",
                            appContext.getWorkspace().formats().text().factory().styled(
                                    target, NutsTextNodeStyle.primary(4)
                            )
                    );
            }
            if(!keep){
                new File(temp).delete();
            }
        } else {
            throw new NutsIllegalArgumentException(appContext.getWorkspace(), "unsupported");
        }
    }

    private void writeAdoc(MdDocument md, String target,boolean trace) {
        try (MdWriter mw = new AsciiDoctorWriter(new FileWriter(target))) {
            mw.write(md);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
        if (trace) {
            appContext.getSession().out().printf("generated src %s\n",
                    appContext.getWorkspace().formats().text().factory().styled(
                            target, NutsTextNodeStyle.primary(4)
                    )
            );
        }
    }

    private Path addExtension(String source, String ext) {
        Path path = Paths.get(source);
        String n = path.getFileName().toString();
        if (n.endsWith(".json")) {
            n = n.substring(0, n.length() - ".json".length()) + "." + ext;
        } else if (n.endsWith(".yml")) {
            n = n.substring(0, n.length() - ".yml".length()) + "." + ext;
        } else if (n.endsWith(".yaml")) {
            n = n.substring(0, n.length() - ".yaml".length()) + "." + ext;
        } else {
            n = n + "." + ext;
        }
        return path.getParent().resolve(n);
    }

    private MdDocument toMarkdown(String source) {
        boolean json = false;
        try (BufferedReader r = Files.newBufferedReader(Paths.get(source))) {
            String t;
            while ((t = r.readLine()) != null) {
                t = t.trim();
                if (t.length() > 0) {
                    if (t.startsWith("{")) {
                        json = true;
                    }
                    break;
                }
            }
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
        try (InputStream inputStream = Files.newInputStream(Paths.get(source))) {
            return toMarkdown(inputStream, json);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    private MdDocument toMarkdown(InputStream inputStream, boolean json) {
        MdDocumentBuilder doc = new MdDocumentBuilder();
        doc.setProperty("headers", new String[]{
                ":source-highlighter: coderay",
                ":icons: font",
                ":icon-set: pf",
                ":doctype: book",
                ":toc:",
                ":toclevels: 3",
                ":appendix-caption: Appx",
                ":sectnums:",
                ":chapter-label:"
        });
        doc.setDate(LocalDate.now());
        doc.setSubTitle("RESTRICTED - INTERNAL");

        ItemFactory.Item obj = ItemFactory.itemOf(
                json ? appContext.getWorkspace().formats().element().setContentType(NutsContentType.JSON).parse(inputStream, NutsElement.class) :
                        new Yaml().load(inputStream)
        );
        MdSequenceBuilder all = MdFactory.seq();
        ItemFactory.Obj entries = obj.asObject();
        String documentTitle = entries.getObject("info").getString("title");
        doc.setTitle(documentTitle);
        String documentVersion = entries.getObject("info").getString("version");
        doc.setVersion(documentVersion);

        all.add(MdFactory.title(1, documentTitle));
        all.add(MdFactory.seq(
                MdFactory.text("API Reference")
        ));
        all.add(MdFactory.title(2, "INTRODUCTION"));
        all.add(MdFactory.text(entries.getObject("info").getString("description").trim()));
        all.add(MdFactory.title(3, "CONTACT"));
        all.add(MdFactory.table()
                .addColumns(
                        MdFactory.column().setName("NAME"),
                        MdFactory.column().setName("EMAIL"),
                        MdFactory.column().setName("URL")
                )
                .addRows(
                        MdFactory.row().addCells(
                                MdFactory.text(entries.getObject("info").getObject("contact").getString("name")),
                                MdFactory.text(entries.getObject("info").getObject("contact").getString("email")),
                                MdFactory.text(entries.getObject("info").getObject("contact").getString("url"))
                        )
                )
        );

        all.add(MdFactory.title(3, "SERVER LIST"));
        for (ItemFactory.Item srv : entries.getArray("servers")) {
            ItemFactory.Obj srvObj = (ItemFactory.Obj) srv;
            all.add(MdFactory.title(4, srvObj.getString("url")));
            all.add(MdFactory.text(srvObj.getString("description")));
            if (srvObj.get("variables").asObject().length() > 0) {
                MdTableBuilder mdTableBuilder = MdFactory.table().addColumns(
                        MdFactory.column().setName("NAME"),
                        MdFactory.column().setName("SPEC"),
                        MdFactory.column().setName("DESCRIPTION")
                );
                for (Map.Entry<String, ItemFactory.Item> variables : srvObj.get("variables").asObject()) {
                    mdTableBuilder.addRows(
                            MdFactory.row().addCells(
                                    MdFactory.text(variables.getKey()),
//                                MdFactory.text(variables.getValue().asObject().getString("enum")),
                                    MdFactory.text(variables.getValue().asObject().getString("default")),
                                    MdFactory.text(variables.getValue().asObject().getString("description"))
                            )
                    );
                }
                all.add(mdTableBuilder.build());
            }
        }

        if (!entries.getObject("components").getObject("headers").isEmpty()) {
            all.add(MdFactory.title(3, "HEADERS"));
            all.add(MdFactory.text("This section includes common Headers to be included in the incoming requests."));
            MdTableBuilder table = MdFactory.table()
                    .addColumns(
                            MdFactory.column().setName("NAME"),
                            MdFactory.column().setName("TYPE"),
                            MdFactory.column().setName("REQUIRED"),
                            MdFactory.column().setName("DESCRIPTION")
                    );


            for (Map.Entry<String, ItemFactory.Item> ee : entries.getObject("components").getObject("headers")) {
                table.addRows(
                        MdFactory.row().addCells(
                                MdFactory.code("", ee.getKey() + (
                                        ee.getValue().asObject().getBoolean("deprecated") ? " (DEPRECATED)" : ""
                                )),
                                MdFactory.code("", ee.getValue().asObject().getObject("schema").getString("type")),
                                MdFactory.text(ee.getValue().asObject().getBoolean("required") ? "required" : ""),
                                MdFactory.text(ee.getValue().asObject().getString("description"))
                        )
                );
            }
            all.add(table);
        }
        if (!entries.getObject("components").getObject("securitySchemes").isEmpty()) {
            all.add(MdFactory.title(3, "SECURITY AND AUTHENTICATION"));
            all.add(MdFactory.text("This section includes security configurations."));
            for (Map.Entry<String, ItemFactory.Item> ee : entries.getObject("components").getObject("securitySchemes")) {
                String type = ee.getValue().asObject().getString("type");
                switch (type) {
                    case "apiKey": {
                        all.add(MdFactory.title(4, ee.getKey() + " (Api Key)"));
                        all.add(MdFactory.text(ee.getValue().asObject().getString("description")));
                        all.add(MdFactory
                                .table().addColumns(
                                        MdFactory.column().setName("NAME"),
                                        MdFactory.column().setName("IN")
                                )
                                .addRows(MdFactory.row()
                                        .addCells(
                                                MdFactory.code("", ee.getValue().asObject().getString("name")),
                                                MdFactory.code("", ee.getValue().asObject().getString("in").toUpperCase())
                                        ))
                        );
                        break;
                    }
                    case "http": {
                        all.add(MdFactory.title(4, ee.getKey() + " (Http)"));
                        all.add(MdFactory.text(ee.getValue().asObject().getString("description")));
                        all.add(MdFactory
                                .table().addColumns(
                                        MdFactory.column().setName("SCHEME"),
                                        MdFactory.column().setName("BEARER")
                                )
                                .addRows(MdFactory.row()
                                        .addCells(
                                                MdFactory.text(ee.getValue().asObject().getString("scheme")),
                                                MdFactory.text(ee.getValue().asObject().getString("bearerFormat"))
                                        ))
                        );
                        break;
                    }
                    case "oauth2": {
                        all.add(MdFactory.title(4, ee.getKey() + " (Oauth2)"));
                        all.add(MdFactory.text(ee.getValue().asObject().getString("description")));
//                        all.add(MdFactory
//                                .table().addColumns(
//                                        MdFactory.column().setName("SCHEME"),
//                                        MdFactory.column().setName("BEARER")
//                                )
//                                .addRows(MdFactory.row()
//                                        .addCells(
//                                                MdFactory.text(ee.getValue().asObject().getString("scheme")),
//                                                MdFactory.text(ee.getValue().asObject().getString("bearerFormat"))
//                                        ))
//                        );
                        break;
                    }
                    case "openIdConnect": {
                        all.add(MdFactory.title(4, ee.getKey() + " (OpenId Connect)"));
                        all.add(MdFactory.text(ee.getValue().asObject().getString("description")));
                        all.add(MdFactory
                                .table().addColumns(
                                        MdFactory.column().setName("URL")
                                )
                                .addRows(MdFactory.row()
                                        .addCells(
                                                MdFactory.text(ee.getValue().asObject().getString("openIdConnectUrl"))
                                        ))
                        );
                        break;
                    }
                    default: {
                        all.add(MdFactory.title(4, ee.getKey() + " (" + type + ")"));
                        all.add(MdFactory.text(ee.getValue().asObject().getString("description")));
                    }
                }
            }
        }
        all.add(MdFactory.title(2, "API"));
        for (Map.Entry<String, ItemFactory.Item> path : entries.get("paths").asObject()) {
            String url = path.getKey();
            for (Map.Entry<String, ItemFactory.Item> ss : path.getValue().asObject()) {
                String method = ss.getKey();
                ItemFactory.Obj call = (ItemFactory.Obj) ss.getValue();
                all.add(MdFactory.title(3, method.toUpperCase() + " " + url));
                all.add(MdFactory.text(call.getString("summary")));
                all.add(
                        MdFactory.code("", "[" + method.toUpperCase() + "] " + url)
                );
                all.add(MdFactory.text(call.getString("description")));
                all.add(MdFactory.title(4, "REQUEST"));
                List<ItemFactory.Item> headerParameters = call.getArray("parameters").stream().filter(x -> x.asObject().getString("in").equals("header")).collect(Collectors.toList());
                List<ItemFactory.Item> queryParameters = call.getArray("parameters").stream().filter(x -> x.asObject().getString("in").equals("query")).collect(Collectors.toList());
                if (!headerParameters.isEmpty()) {
                    all.add(MdFactory.title(5, "HEADER PARAMETERS"));
                    MdTable tab = new MdTable(
                            new MdColumn[]{new MdColumn(MdFactory.text("NAME"), MdHorizontalAlign.LEFT),
                                    new MdColumn(MdFactory.text("TYPE"), MdHorizontalAlign.LEFT),
                                    new MdColumn(MdFactory.text("REQUIRED"), MdHorizontalAlign.LEFT),
                                    new MdColumn(MdFactory.text("DESCRIPTION"), MdHorizontalAlign.LEFT)},
                            headerParameters.stream().map(
                                    headerParameter -> new MdRow(
                                            new MdElement[]{
                                                    MdFactory.code("", headerParameter.asObject().getString("name")),
                                                    MdFactory.code("", headerParameter.asObject().getString("type")),
                                                    MdFactory.text(headerParameter.asObject().getBoolean("required") ? "required" : ""),
                                                    MdFactory.text(headerParameter.asObject().getString("description"))
                                            }, false
                                    )
                            ).toArray(MdRow[]::new)
                    );
                    all.add(tab);
                }
                if (!queryParameters.isEmpty()) {
                    all.add(MdFactory.title(5, "QUERY PARAMETERS"));
                    MdTable tab = new MdTable(
                            new MdColumn[]{new MdColumn(MdFactory.text("NAME"), MdHorizontalAlign.LEFT),
                                    new MdColumn(MdFactory.text("TYPE"), MdHorizontalAlign.LEFT),
                                    new MdColumn(MdFactory.text("REQUIRED"), MdHorizontalAlign.LEFT),
                                    new MdColumn(MdFactory.text("DESCRIPTION"), MdHorizontalAlign.LEFT)},
                            queryParameters.stream().map(
                                    headerParameter -> new MdRow(
                                            new MdElement[]{
                                                    MdFactory.code("", headerParameter.asObject().getString("name")),
                                                    MdFactory.code("", headerParameter.asObject().getString("type")),
                                                    MdFactory.text(headerParameter.asObject().getBoolean("required") ? "required" : ""),
                                                    MdFactory.text(headerParameter.asObject().getString("description"))
                                            }, false
                                    )
                            ).toArray(MdRow[]::new)
                    );
                    all.add(tab);
                }
                ItemFactory.Obj requestBody = call.getObject("requestBody");
                if (!requestBody.isEmpty()) {
                    boolean required = requestBody.getBoolean("required");
                    String desc = requestBody.getString("description");
                    ItemFactory.Obj r = requestBody.getObject("content");
                    for (Map.Entry<String, ItemFactory.Item> ii : r) {
                        all.add(MdFactory.title(5, "REQUEST BODY - " + ii.getKey() + (required ? " [required]" : "")));
                        all.add(MdFactory.text(desc));
                        all.add(MdFactory.code("javascript", toCode(ii.getValue(), "")));
                    }
                }

                all.add(MdFactory.title(4, "RESPONSE"));
                call.getObject("responses").stream()
                        .forEach(x -> {
                            String s = x.getKey();
                            ItemFactory.Item v = x.getValue();
                            all.add(MdFactory.title(5, "STATUS CODE - " + s));
                            all.add(MdFactory.text(v.asObject().getString("description")));
                            for (Map.Entry<String, ItemFactory.Item> content : v.asObject().getObject("content")) {
                                all.add(MdFactory.title(6, "RESPONSE MODEL - " + content.getKey()));
                                all.add(MdFactory.code("javascript", toCode(content.getValue(), "")));
                            }
                        });
            }
        }
        doc.setContent(all.build());
        return doc.build();
    }

    private String toCode(ItemFactory.Item o, String indent) {
        String descSep = " // ";
        if (o.isObject()) {
            if (o.asObject().get("schema").isObject()) {
                ItemFactory.Obj schema = o.asObject().getObject("schema");
                String t = schema.getString("type");
                if (t.equals("object")) {
                    StringBuilder sb = new StringBuilder("{");
                    for (Map.Entry<String, ItemFactory.Item> p : schema.getObject("properties")) {
                        sb.append("\n" + indent + "  " + p.getKey() + ": " + toCode(p.getValue(), indent + "  "));
                    }
                    sb.append("\n" + indent + "}");
                    ItemFactory.Item desc = o.asObject().get("description");
                    if (!desc.asString().isEmpty()) {
                        return sb + descSep + desc.asString();
                    }
                    return sb.toString();
                }
            } else if (o.asObject().get("type").isString()) {
                String t = o.asObject().get("type").asString();
                if (t.equals("object")) {
                    StringBuilder sb = new StringBuilder("{");
                    for (Map.Entry<String, ItemFactory.Item> p : o.asObject().getObject("properties")) {
                        sb.append("\n" + indent + "  " + p.getKey() + ": " + toCode(p.getValue(), indent + "  "));
                    }
                    sb.append("\n" + indent + "}");
                    ItemFactory.Item desc = o.asObject().get("description");
                    if (!desc.asString().isEmpty()) {
                        return sb + descSep + desc.asString();
                    }
                    return sb.toString();
                } else if (t.equals("boolean")) {
                    ItemFactory.Item ee = o.asObject().get("example");
                    if (!ee.isNull()) {
                        if (ee.isString()) {
                            return ee.asString();
                        }
                        return ee.asString();
                    }
                    ItemFactory.Item desc = o.asObject().get("description");
                    ItemFactory.Arr en = o.asObject().get("enum").asArray();
                    if (en.isEmpty()) {
                        String r = "boolean ALLOWED:" + en.stream().map(x -> x.isNull() ? "null" : x.asString()).collect(Collectors.joining(", "));
                        if (!desc.asString().isEmpty()) {
                            return r + descSep + desc.asString();
                        }
                        return r;
                    } else {
                        if (!desc.asString().isEmpty()) {
                            return "boolean" + descSep + desc.asString();
                        }
                        return "boolean";
                    }
                } else if (t.equals("string")) {
                    ItemFactory.Item ee = o.asObject().get("example");
                    if (!ee.isNull()) {
                        if (ee.isString()) {
                            return "\'" + ee.asString() + "\'";
                        }
                        return "\'" + ee.asString() + "\'";
                    }
                    ItemFactory.Item desc = o.asObject().get("description");
                    ItemFactory.Arr en = o.asObject().get("enum").asArray();
                    if (!en.isEmpty()) {
                        String r = "string ALLOWED:" + en.stream().map(x -> x.isNull() ? "null" : x.asString()).collect(Collectors.joining(", "));
                        if (!desc.asString().isEmpty()) {
                            return r + descSep + desc.asString();
                        }
                        return r;
                    } else {
                        if (!desc.asString().isEmpty()) {
                            return "string" + descSep + desc.asString();
                        }
                        return "string";
                    }
                } else if (t.equals("integer")) {
                    ItemFactory.Item desc = o.asObject().get("description");
                    ItemFactory.Arr en = o.asObject().get("enum").asArray();
                    if (!en.isEmpty()) {
                        String r = "integer ALLOWED:" + en.stream().map(x -> x.isNull() ? "null" : x.asString()).collect(Collectors.joining(", "));
                        if (!desc.asString().isEmpty()) {
                            return r + descSep + desc.asString();
                        }
                        return r;
                    } else {
                        if (!desc.asString().isEmpty()) {
                            return "integer" + descSep + desc.asString();
                        }
                        return "integer";
                    }
                }
            } else if (o.asObject().get("type").isNull()) {
                ItemFactory.Item desc = o.asObject().get("description");
                if (!desc.asString().isEmpty()) {
                    return "null" + "\n" + desc.asString();
                }
                return "null";
            }
        }
        return "";
    }


}
