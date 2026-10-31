package com.mb.modulith.shared.config;

import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationVersion;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.flyway.autoconfigure.FlywayMigrationInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * Runs Flyway once per module so module migrations never affect each other. Every module has its own location, its
 * own version line and its own schema history table, which lives inside the module's own schema
 * (for example {@code email.flyway_schema_history}).
 *
 * <p>Modules are declared in {@code application.yml}:
 * <pre>
 * app:
 *   flyway:
 *     modules:
 *       - name: school
 *         schema: school
 *         location: classpath:db/migration/school
 * </pre>
 *
 * <p>Boot's single, global Flyway is turned off ({@code spring.flyway.enabled=false}). Each module is registered as a
 * {@link FlywayMigrationInitializer}, so Boot makes the JPA {@code EntityManagerFactory} wait for all of them before
 * Hibernate validates the schema.
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(prefix = "app.flyway", name = "enabled", havingValue = "true", matchIfMissing = true)
public class ModuleFlywayConfig {

    @Bean
    @SuppressWarnings("java:S2259") // Suppress SonarQube warning about possible null return from Binder.get()
    static BeanDefinitionRegistryPostProcessor moduleFlywayRegistrar(Environment environment) {
        List<FlywayModule> modules = Binder.get(environment)
                .bind("app.flyway.modules", Bindable.listOf(FlywayModule.class))
                .orElseGet(List::of);
        boolean baselineOnMigrate = Binder.get(environment)
                .bind("app.flyway.baseline-on-migrate", Boolean.class)
                .orElse(true);
        return new ModuleFlywayRegistrar(modules, baselineOnMigrate);
    }

    /**
     * @param name     module name, used for the bean name
     * @param schema   schema that owns the module's tables and its Flyway history table
     * @param location Flyway location with the module's migration scripts
     */
    public record FlywayModule(String name, String schema, String location) {
    }

    @Slf4j
    private record ModuleFlywayRegistrar(List<FlywayModule> modules,
                                         boolean baselineOnMigrate) implements BeanDefinitionRegistryPostProcessor {

        /**
         * Highest {@code V*__*.sql} version in the module location; used when baselining an existing schema.
         */
        private static Optional<String> latestScriptVersion(String location) {
            String classpathFolder = location.startsWith("classpath:") ? location.substring("classpath:".length()) : location;
            if (classpathFolder.startsWith("/")) {
                classpathFolder = classpathFolder.substring(1);
            }
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            try {
                Resource[] resources = resolver.getResources("classpath*:" + classpathFolder + "/V*.sql");
                MigrationVersion latest = null;
                for (Resource resource : resources) {
                    String filename = resource.getFilename();
                    if (filename == null) {
                        continue;
                    }
                    MigrationVersion version = migrationVersionFromFilename(filename);
                    if (version != null && (latest == null || version.compareTo(latest) > 0)) {
                        latest = version;
                    }
                }
                return latest == null ? Optional.empty() : Optional.of(latest.getVersion());
            } catch (IOException ex) {
                throw new IllegalStateException("Could not read Flyway scripts at " + location, ex);
            }
        }

        private static MigrationVersion migrationVersionFromFilename(String filename) {
            if (!filename.startsWith("V") || !filename.endsWith(".sql")) {
                return null;
            }
            int separator = filename.indexOf("__");
            if (separator <= 1) {
                return null;
            }
            String versionToken = filename.substring(1, separator).replace('_', '.');
            return MigrationVersion.fromVersion(versionToken);
        }

        @Override
        public void postProcessBeanDefinitionRegistry(@NonNull BeanDefinitionRegistry registry) throws BeansException {
            BeanFactory beanFactory = (BeanFactory) registry;
            for (FlywayModule module : modules) {
                String beanName = module.name() + "FlywayMigrationInitializer";
                registry.registerBeanDefinition(beanName, BeanDefinitionBuilder
                        .genericBeanDefinition(FlywayMigrationInitializer.class, () -> new FlywayMigrationInitializer(createFlyway(beanFactory.getBean(DataSource.class), module)))
                        .getBeanDefinition());
                log.debug("Registered Flyway for module '{}' (schema={}, location={}).", module.name(), module.schema(), module.location());
            }
        }

        @Override
        public void postProcessBeanFactory(@NonNull ConfigurableListableBeanFactory beanFactory) throws BeansException {
            // Nothing to customize; bean definitions are registered above.
        }

        private Flyway createFlyway(DataSource dataSource, FlywayModule module) {
            var configuration = Flyway.configure()
                    .dataSource(dataSource)
                    .schemas(module.schema())
                    .defaultSchema(module.schema())
                    .locations(module.location())
                    .baselineOnMigrate(baselineOnMigrate);
            if (baselineOnMigrate) {
                latestScriptVersion(module.location())
                        .ifPresent(version -> {
                            configuration.baselineVersion(version);
                            log.debug("Module '{}' baseline version set to {} (latest script).", module.name(), version);
                        });
            }
            return configuration.load();
        }
    }
}
