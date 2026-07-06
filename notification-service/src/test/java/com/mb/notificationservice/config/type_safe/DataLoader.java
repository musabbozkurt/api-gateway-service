package com.mb.notificationservice.config.type_safe;

import org.jspecify.annotations.NonNull;
import org.springframework.boot.CommandLineRunner;

import java.util.List;

public class DataLoader implements CommandLineRunner {

    private final PersonRepository repository;

    public DataLoader(PersonRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(String @NonNull ... args) {
        repository.saveAll(List.of(
                new Person(null, "Dan", "Vega", new Address("Cleveland", "US")),
                new Person(null, "DaShaun", "Carter", new Address("Kansas City", "US")),
                new Person(null, "Josh", "Long", new Address("San Francisco", "US")),
                new Person(null, "Cora", "Iberkleid", new Address("New York City", "US")),
                new Person(null, "Michael", "Cote", new Address("Amsterdam", "NL"))
        ));
    }
}
