package com.mb.modulith.commerce.data.filters;

import com.mb.modulith.commerce.data.model.QClient;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class ClientFilter implements Filter {
    private String telephone;
    private String firstName;
    private String lastName;
    private String email;
    private UUID clientId;

    @Override
    public Predicate toPredicate() {
        QClient client = QClient.client;
        BooleanExpression predicate = client.id.isNotNull().and(client.id.eq(clientId));

        if (telephone != null) {
            predicate = predicate.and(client.telephone.eq(telephone));
        }
        if (firstName != null) {
            predicate = predicate.and(client.firstName.isNotNull()).and(client.firstName.eq(firstName));
        }
        if (lastName != null) {
            predicate = predicate.and(client.lastName.isNotNull()).and(client.lastName.eq(lastName));
        }
        if (email != null) {
            predicate = predicate.and(client.email.isNotNull()).and(client.email.eq(email));
        }

        return predicate;
    }
}
