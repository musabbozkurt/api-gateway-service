package com.mb.springconfigserver;

import com.mb.springconfigserver.base.BaseTestContainersTest;
import com.mb.springconfigserver.config.KafkaIntegrationConfiguration;
import com.mb.springconfigserver.config.PostgresIntegrationConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@AutoConfigureMockMvc
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = {PostgresIntegrationConfiguration.class, KafkaIntegrationConfiguration.class}
)
class SpringConfigServerIntegrationTests extends BaseTestContainersTest {

    private static final String APP_NAME = "test-app";
    private static final String OTHER_APP_NAME = "other-app";
    private static final String PROFILE = "default";
    private static final String LABEL = "main";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // -------------------------------------------------------------------------
    // GET /{application}/{profile}
    // -------------------------------------------------------------------------

    @Test
    @Order(1)
    void getConfig_ShouldReturnProperties_WhenApplicationAndProfileAreValid() throws Exception {
        // Arrange
        // Act
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/{application}/{profile}", APP_NAME, PROFILE)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                // Assertions
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value(APP_NAME))
                .andExpect(jsonPath("$.propertySources").isArray())
                .andExpect(jsonPath("$.propertySources[0].source['app.feature.enabled']").value("true"))
                .andExpect(jsonPath("$.propertySources[0].source['app.timeout']").value("30"))
                .andExpect(jsonPath("$.propertySources[0].source['app.message']").value("hello from config"));
    }

    @Test
    @Order(2)
    void getConfig_ShouldReturnOnlyOwnProperties_WhenDifferentApplicationIsRequested() throws Exception {
        // Arrange
        // Act
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/{application}/{profile}", OTHER_APP_NAME, PROFILE)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                // Assertions
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value(OTHER_APP_NAME))
                .andExpect(jsonPath("$.propertySources[0].source['other.setting']").value("other-value"))
                .andExpect(jsonPath("$.propertySources[0].source['app.feature.enabled']").doesNotExist());
    }

    @Test
    @Order(3)
    void getConfig_ShouldReturnEmptyPropertySources_WhenApplicationDoesNotExist() throws Exception {
        // Arrange
        // Act
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/{application}/{profile}", "non-existent-app", PROFILE)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                // Assertions
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value("non-existent-app"))
                .andExpect(jsonPath("$.propertySources").isEmpty());
    }

    // -------------------------------------------------------------------------
    // GET /{application}/{profile}/{label}
    // -------------------------------------------------------------------------

    @Test
    @Order(4)
    void getConfig_ShouldReturnProperties_WhenApplicationProfileAndLabelAreValid() throws Exception {
        // Arrange
        // Act
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/{application}/{profile}/{label}", APP_NAME, PROFILE, LABEL)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                // Assertions
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value(APP_NAME))
                .andExpect(jsonPath("$.label").value(LABEL))
                .andExpect(jsonPath("$.propertySources[0].source['app.feature.enabled']").value("true"))
                .andExpect(jsonPath("$.propertySources[0].source['app.timeout']").value("30"))
                .andExpect(jsonPath("$.propertySources[0].source['app.message']").value("hello from config"));
    }

    // -------------------------------------------------------------------------
    // Actuator health
    // -------------------------------------------------------------------------

    @Test
    @Order(5)
    void actuatorHealth_ShouldReturnUp_WhenServiceIsRunning() throws Exception {
        // Arrange
        // Act
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/actuator/health")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                // Assertions
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value("UP"));
    }

    // -------------------------------------------------------------------------
    // Custom JDBC repository – isolation verification
    // -------------------------------------------------------------------------

    @Test
    @Order(6)
    void getConfig_ShouldReturnPropertiesIsolatedPerApplication_WhenMultipleApplicationsExist() throws Exception {
        // Verify test-app does NOT contain other-app's properties
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/{application}/{profile}", APP_NAME, PROFILE)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.propertySources[0].source['other.setting']").doesNotExist());

        // Verify other-app does NOT contain test-app's properties
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/{application}/{profile}", OTHER_APP_NAME, PROFILE)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.propertySources[0].source['app.timeout']").doesNotExist());
    }

    // -------------------------------------------------------------------------
    // Spring Cloud Bus – /actuator/busrefresh
    // -------------------------------------------------------------------------

    @Test
    @Order(7)
    void busRefresh_ShouldReturn204_WhenEventIsPublishedSuccessfully() throws Exception {
        // Arrange
        // Act
        // POST /actuator/busrefresh broadcasts a RefreshRemoteApplicationEvent over Kafka (springCloudBus topic).
        // A 204 No Content response means the event was accepted and published without error.
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/actuator/busrefresh")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                // Assertions
                .andExpect(status().isNoContent());
    }

    @Test
    @Order(8)
    void busRefresh_ShouldReturn204_WhenDestinationFilterIsProvided() throws Exception {
        // Arrange
        // Act
        // The optional ?destination= parameter scopes the refresh event to a specific service/instance.
        // Config server must still publish the event and return 204 regardless of the destination value.
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/actuator/busrefresh")
                        .param("destination", APP_NAME + ":**")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                // Assertions
                .andExpect(status().isNoContent());
    }

    @Test
    @Order(9)
    void busRefresh_ShouldServeUpdatedValue_WhenPropertyIsChangedInDatabase() throws Exception {
        // Arrange – verify the original value is served before the change
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/{application}/{profile}", APP_NAME, PROFILE)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.propertySources[0].source['app.timeout']").value("30"));

        // Simulate a runtime config change directly in the database (as an operator would do)
        jdbcTemplate.update("UPDATE config_server_schema.properties SET PROP_VALUE = ? WHERE APPLICATION = ? AND PROP_KEY = ?", "60", APP_NAME, "app.timeout");

        // Act – trigger bus refresh so all connected services are notified to reload their config
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/actuator/busrefresh")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNoContent());

        // Assertions – config server must now serve the updated value from the database
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/{application}/{profile}", APP_NAME, PROFILE)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.propertySources[0].source['app.timeout']").value("60"));

        // Teardown – restore original value so other tests are not affected
        jdbcTemplate.update("UPDATE config_server_schema.properties SET PROP_VALUE = ? WHERE APPLICATION = ? AND PROP_KEY = ?", "30", APP_NAME, "app.timeout");
    }
}
