package com.mb.kafkadebeziumservice.queue.event.consumer;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class DebeziumCustomerMessage {

    private Payload payload;

    @Getter
    @Setter
    @ToString
    public static class Payload {

        private CustomerData before;
        private CustomerData after;
        private Source source;
        private String op;
        @JsonProperty("ts_ms")
        private Long tsMs;

        @Getter
        @Setter
        @ToString
        public static class CustomerData {
            @JsonAlias({"id", "ID"})
            private Integer id;
            @JsonAlias({"first_name", "FIRST_NAME"})
            private String firstName;
            @JsonAlias({"last_name", "LAST_NAME"})
            private String lastName;
            @JsonAlias({"email", "EMAIL"})
            private String email;
        }

        @Getter
        @Setter
        @ToString
        public static class Source {
            private String version;
            private String connector;
            private String name;
            @JsonProperty("ts_ms")
            private Long tsMs;
            private String snapshot;
            private String db;
            private String schema;
            private String table;
            private String op;
        }
    }
}
