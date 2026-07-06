package com.mb.notificationservice.config.type_safe;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jdbc.test.autoconfigure.DataJdbcTest;
import org.springframework.data.core.PropertyPath;
import org.springframework.data.domain.Sort;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJdbcTest(
        properties = {
                "spring.sql.init.mode=never",
                "spring.cloud.config.enabled=false",
                "spring.cloud.config.import-check.enabled=false"
        }
)
@Sql(scripts = "/schema.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class DataLoaderIntegrationTest {

    private final PersonRepository repository;
    private final JdbcAggregateTemplate template;

    @Autowired
    DataLoaderIntegrationTest(PersonRepository repository, JdbcAggregateTemplate template) {
        this.repository = repository;
        this.template = template;
    }

    @BeforeEach
    void setUp() {
        repository.deleteAll();
        new DataLoader(repository).run(); // Seed same 5 rows as production startup logic
    }

    @Test
    void run_shouldSeedFivePeople() {
        List<Person> people = repository.findAll();
        assertEquals(5, people.size());
    }

    @Test
    void simpleSortNewWay_shouldSortByLastNameAscending() {
        List<Person> sorted = repository.findAll(Sort.by(Person::lastName));

        List<String> lastNames = sorted.stream()
                .map(Person::lastName)
                .toList();

        assertEquals(List.of("Carter", "Cote", "Iberkleid", "Long", "Vega"), lastNames);
    }

    @Test
    void criteriaQueryNew_shouldFindOnlyVega() {
        List<Person> result = template.findAll(Query.query(Criteria.where(Person::lastName).is("Vega")), Person.class);

        assertEquals(1, result.size());
        assertEquals("Dan", result.getFirst().firstName());
        assertEquals("Vega", result.getFirst().lastName());
    }

    @Test
    void compositeNewWay_shouldFindOnlyUsPeople() {
        Query query = Query.query(Criteria.where(PropertyPath.of(Person::address).then(Address::country)).is("US"));

        List<Person> usPeople = template.findAll(query, Person.class);

        assertEquals(4, usPeople.size());
        assertTrue(usPeople.stream().allMatch(person -> person.address() != null));
        assertTrue(usPeople.stream().allMatch(person -> "US".equals(person.address().country())));
    }
}
