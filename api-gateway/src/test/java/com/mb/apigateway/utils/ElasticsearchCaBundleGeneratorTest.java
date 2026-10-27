package com.mb.apigateway.utils;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class ElasticsearchCaBundleGeneratorTest {

    @TempDir
    private Path tempDir;

    private String previousProperty;

    @BeforeEach
    void setUp() {
        previousProperty = System.getProperty(ElasticsearchCaBundleGenerator.BUNDLE_PATH_PROPERTY);
    }

    @AfterEach
    void tearDown() {
        if (previousProperty == null) {
            System.clearProperty(ElasticsearchCaBundleGenerator.BUNDLE_PATH_PROPERTY);
        } else {
            System.setProperty(ElasticsearchCaBundleGenerator.BUNDLE_PATH_PROPERTY, previousProperty);
        }
    }

    @Test
    void bundlePath_usesConfiguredProperty() {
        Path configured = tempDir.resolve("custom.crt");
        System.setProperty(ElasticsearchCaBundleGenerator.BUNDLE_PATH_PROPERTY, configured.toString());

        assertThat(ElasticsearchCaBundleGenerator.bundlePath()).isEqualTo(configured);
    }

    @Test
    void bundlePath_defaultsToTmpDirWhenPropertyMissingOrBlank() {
        Path expected = Path.of(System.getProperty("java.io.tmpdir"), "es-ca.crt");

        System.clearProperty(ElasticsearchCaBundleGenerator.BUNDLE_PATH_PROPERTY);
        assertThat(ElasticsearchCaBundleGenerator.bundlePath()).isEqualTo(expected);

        System.setProperty(ElasticsearchCaBundleGenerator.BUNDLE_PATH_PROPERTY, "  ");
        assertThat(ElasticsearchCaBundleGenerator.bundlePath()).isEqualTo(expected);
    }

    @Test
    void generateIfMissing_keepsExistingBundle() throws IOException {
        Path target = tempDir.resolve("es-ca.crt");
        Files.writeString(target, "existing");
        System.setProperty(ElasticsearchCaBundleGenerator.BUNDLE_PATH_PROPERTY, target.toString());

        ElasticsearchCaBundleGenerator.generateIfMissing("https://localhost:1");

        assertThat(Files.readString(target)).isEqualTo("existing");
    }

    @Test
    void generateIfMissing_skipsNullAndNonHttpsUrls() {
        Path target = tempDir.resolve("es-ca.crt");
        System.setProperty(ElasticsearchCaBundleGenerator.BUNDLE_PATH_PROPERTY, target.toString());

        ElasticsearchCaBundleGenerator.generateIfMissing(null);
        ElasticsearchCaBundleGenerator.generateIfMissing("http://localhost:9200");

        assertThat(target).doesNotExist();
    }

    @Test
    void generateIfMissing_doesNotThrowOrCreateFileWhenHostUnreachable() throws IOException {
        Path target = tempDir.resolve("nested").resolve("es-ca.crt");
        System.setProperty(ElasticsearchCaBundleGenerator.BUNDLE_PATH_PROPERTY, target.toString());
        int closedPort;
        try (ServerSocket socket = new ServerSocket(0)) {
            closedPort = socket.getLocalPort();
        }

        assertThatCode(() -> ElasticsearchCaBundleGenerator.generateIfMissing("https://localhost:" + closedPort)).doesNotThrowAnyException();

        assertThat(target).doesNotExist();
    }

    @Test
    void generateIfMissing_doesNotThrowOnMalformedUrl() {
        Path target = tempDir.resolve("es-ca.crt");
        System.setProperty(ElasticsearchCaBundleGenerator.BUNDLE_PATH_PROPERTY, target.toString());

        assertThatCode(() -> ElasticsearchCaBundleGenerator.generateIfMissing("https://bad host:9200")).doesNotThrowAnyException();

        assertThat(target).doesNotExist();
    }
}
