CREATE TABLE IF NOT EXISTS player_market.team_info
(
    id                uuid                     NOT NULL PRIMARY KEY,
    created_date_time timestamp with time zone NOT NULL,
    deleted           boolean                  NOT NULL DEFAULT false,
    name              varchar(255),
    short_name        varchar(255),
    website           varchar(255),
    email             varchar(255),
    club_colors       varchar(255),
    country           varchar(255)             NOT NULL,
    currency_code     varchar(255)             NOT NULL,
    updated_date_time timestamp with time zone NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS player_market.player_info
(
    id                    uuid                     NOT NULL PRIMARY KEY,
    created_date_time     timestamp with time zone NOT NULL,
    deleted               boolean                  NOT NULL DEFAULT false,
    name                  varchar(255),
    surname               varchar(255),
    position              varchar(255)             NOT NULL,
    jersey_number         integer,
    date_of_birth         varchar(255),
    nationality           varchar(255),
    contract_until        varchar(255),
    market_value          numeric(19, 2),
    market_value_currency varchar(255)             NOT NULL,
    updated_date_time     timestamp with time zone NOT NULL DEFAULT now(),
    team_info_id          uuid,
    CONSTRAINT fk_player_info_team FOREIGN KEY (team_info_id) REFERENCES player_market.team_info (id)
);
