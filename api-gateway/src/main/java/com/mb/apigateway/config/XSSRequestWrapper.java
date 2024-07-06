package com.mb.apigateway.config;

import io.micrometer.core.instrument.util.IOUtils;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.regex.Pattern;

@Slf4j
public class XSSRequestWrapper extends HttpServletRequestWrapper {

    private static final Pattern[] patterns = new Pattern[]{
            // Characters
            Pattern.compile("<|(?i)&lt;"),
            Pattern.compile(">|(?i)&gt;"),
            Pattern.compile("(?i)&#39;(\\s+)\\)|'(\\s+)\\)"),
            // Script fragments
            Pattern.compile("<(?i)script>(.*?)</(?i)script>"),
            // src='...'
            Pattern.compile("(?i)src(\\s+)*=(.*?)", Pattern.MULTILINE | Pattern.DOTALL),
            Pattern.compile("(?i)style(.*?)=", Pattern.MULTILINE | Pattern.DOTALL),
            // lonely script tags
            Pattern.compile("</(?i)script>"),
            Pattern.compile("<(?i)script(.*?)>", Pattern.MULTILINE | Pattern.DOTALL),
            // eval(...)
            Pattern.compile("(?i)eval\\((.*?) \\)", Pattern.MULTILINE | Pattern.DOTALL),
            // expression(...)
            Pattern.compile("(?i)expression\\((.*?)\\)", Pattern.MULTILINE | Pattern.DOTALL),
            // javascript:...
            Pattern.compile("(?i)javascript:"),
            // vbscript:...
            Pattern.compile("(?i)vbscript:"),
            // on(...)=...
            Pattern.compile("(?i)onclick(.*?)=", Pattern.MULTILINE | Pattern.DOTALL),
            Pattern.compile("(?i)ondblclick(.*?)=", Pattern.MULTILINE | Pattern.DOTALL),
            Pattern.compile("(?i)onmousedown(.*?)=", Pattern.MULTILINE | Pattern.DOTALL),
            Pattern.compile("(?i)onmousemove(.*?)=", Pattern.MULTILINE | Pattern.DOTALL),
            Pattern.compile("(?i)onmouseover(.*?)=", Pattern.MULTILINE | Pattern.DOTALL),
            Pattern.compile("(?i)onmouseout(.*?)=", Pattern.MULTILINE | Pattern.DOTALL),
            Pattern.compile("(?i)onmouseup(.*?)=", Pattern.MULTILINE | Pattern.DOTALL),
            Pattern.compile("(?i)onkeydown(.*?)=", Pattern.MULTILINE | Pattern.DOTALL),
            Pattern.compile("(?i)onkeypress(.*?)=", Pattern.MULTILINE | Pattern.DOTALL),
            Pattern.compile("(?i)onkeyup(.*?)=", Pattern.MULTILINE | Pattern.DOTALL),
            Pattern.compile("(?i)onabort(.*?)=", Pattern.MULTILINE | Pattern.DOTALL),
            Pattern.compile("(?i)onload(.*?)=", Pattern.MULTILINE | Pattern.DOTALL),
            Pattern.compile("(?i)onresize(.*?)=", Pattern.MULTILINE | Pattern.DOTALL),
            Pattern.compile("(?i)onscroll(.*?)=", Pattern.MULTILINE | Pattern.DOTALL),
            Pattern.compile("(?i)onunload(.*?)=", Pattern.MULTILINE | Pattern.DOTALL),
            Pattern.compile("(?i)onsubmit(.*?)=", Pattern.MULTILINE | Pattern.DOTALL),
            Pattern.compile("(?i)onblur(.*?)=", Pattern.MULTILINE | Pattern.DOTALL),
            Pattern.compile("(?i)onchange(.*?)=", Pattern.MULTILINE | Pattern.DOTALL),
            Pattern.compile("(?i)onfocus(.*?)=", Pattern.MULTILINE | Pattern.DOTALL),
            Pattern.compile("(?i)onreset(.*?)=", Pattern.MULTILINE | Pattern.DOTALL),
            Pattern.compile("(?i)onselect(.*?)=", Pattern.MULTILINE | Pattern.DOTALL),
            Pattern.compile("(?i)onmoveon(.*?)=", Pattern.MULTILINE | Pattern.DOTALL),
    };

    private String body;

    XSSRequestWrapper(HttpServletRequest servletRequest) {
        super(servletRequest);
        try {
            body = IOUtils.toString(servletRequest.getInputStream(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.warn("Unable to create instance of XSSRequestWrapper", e);
        }

        checkXss(body);
    }

    private static String checkXss(String value) {
        String cleanXss = cleanXss(value);
        if (Objects.nonNull(cleanXss) && !cleanXss.equals(value)) {
            throw new RuntimeException("INVALID_DATA_FOUND");
        }
        return cleanXss;
    }

    static String cleanXss(String value) {
        if (value != null) {

            // Avoid null characters
            value = value.replaceAll("\0", "");

            // Remove all sections that match a pattern
            for (Pattern scriptPattern : patterns) {
                if (scriptPattern.matcher(value).find()) {
                    value = value.replaceAll(scriptPattern.pattern(), "");
                }
            }
        }
        return value;
    }

    @Override
    public String[] getParameterValues(String parameter) {
        String[] values = super.getParameterValues(parameter);

        if (values == null) {
            return new String[]{};
        }

        int count = values.length;
        String[] encodedValues = new String[count];
        for (int i = 0; i < count; i++) {
            encodedValues[i] = checkXss(values[i]);
        }

        return encodedValues;
    }

    @Override
    public String getParameter(String parameter) {
        String value = super.getParameter(parameter);

        return checkXss(value);
    }

    @Override
    public String getHeader(String name) {
        String value = super.getHeader(name);
        return checkXss(value);
    }

    @Override
    public ServletInputStream getInputStream() {
        if (body != null) {
            byte[] data = body.getBytes();
            return new ServletInputStream() {
                private int lastIndexRetrieved = -1;
                private ReadListener readListener = null;

                @Override
                public boolean isFinished() {
                    return (lastIndexRetrieved == data.length - 1);
                }

                @Override
                public boolean isReady() {
                    // This implementation will never block
                    // We also never need to call the readListener from this method, as this method will never return false
                    return isFinished();
                }

                @Override
                public void setReadListener(ReadListener readListener) {
                    this.readListener = readListener;
                    if (!isFinished()) {
                        try {
                            readListener.onDataAvailable();
                        } catch (IOException e) {
                            readListener.onError(e);
                        }
                    } else {
                        try {
                            readListener.onAllDataRead();
                        } catch (IOException e) {
                            readListener.onError(e);
                        }
                    }
                }

                @Override
                public int read() throws IOException {
                    int i;
                    if (!isFinished()) {
                        i = data[lastIndexRetrieved + 1];
                        lastIndexRetrieved++;
                        if (isFinished() && (readListener != null)) {
                            try {
                                readListener.onAllDataRead();
                            } catch (IOException ex) {
                                readListener.onError(ex);
                                throw ex;
                            }
                        }
                        return i;
                    } else {
                        return -1;
                    }
                }
            };
        }
        return null;
    }

    @Override
    public BufferedReader getReader() {
        return new BufferedReader(new InputStreamReader(getInputStream()));
    }
}
