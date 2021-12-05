package net.thevpc.nuts.runtime.standalone.io.path.spi.htmlfs;

import net.thevpc.nuts.*;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ApacheReposHtmlfsParser implements HtmlfsParser {
    @Override
    public NutsSupported<List<String>> parseHtmlTomcat(byte[] bytes, NutsSession session) {
        int expected = 0;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(bytes)))) {
            String line = null;
            while ((line = br.readLine()) != null) {
                if (line.contains("<!DOCTYPE HTML PUBLIC")) {
                    expected++;
                } else if (line.contains("<h1>Index of ")) {
                    expected++;
                } else if (line.contains("<a href=\"?C=M;O=A\">Last modified</a>")) {
                    expected++;
                }
            }
        } catch (Exception e) {
            //ignore
        }
        if (expected < 2) {
            return toSupported(null);
        }
        List<String> found = new ArrayList<>();
        Pattern pattern = Pattern.compile("<img src=\"/icons/[a-z.]+\" alt=\"\\[[a-zA-Z ]+]\"> +<a href=\"(?<href>[^\"]+)\">(?<hname>[^>]+)</a> +(?<d>[^ ]+) (?<h>[^ ]+) +(?<s>[^ ]+)");
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(bytes)))) {
            String line = null;
            while ((line = br.readLine()) != null) {
                Matcher m = pattern.matcher(line);
                if (m.find()) {
                    found.add(m.group("href"));
                }
            }
        } catch (Exception e) {
            //ignore
        }
        return toSupported(found);
    }

    private NutsSupported<List<String>> toSupported(List<String> li) {
        if (li == null || li.isEmpty()) {
            return NutsSupported.invalid();
        }
        return NutsSupported.of(1, () -> li);
    }
}