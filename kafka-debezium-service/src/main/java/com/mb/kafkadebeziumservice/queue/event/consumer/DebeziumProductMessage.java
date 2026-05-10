package com.mb.kafkadebeziumservice.queue.event.consumer;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class DebeziumProductMessage {

    private Payload payload;

    @Getter
    @Setter
    @ToString
    public static class Payload {

        private ProductData before;
        private ProductData after;
        private Source source;
        private String op;
        @JsonProperty("ts_ms")
        private Long tsMs;

        @Getter
        @Setter
        @ToString
        public static class ProductData {
            private Integer id;
            private String description;
            private String weight;
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
