package me.jjeda.houseserver.accounts;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

public class AccountSerializer extends JsonSerializer<Account> {

    @Override
    public void serialize(Account account, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartObject();
        gen.writeNumberField("id", account.getId());
        gen.writeStringField("email",account.getEmail());
        gen.writeStringField("displayName", account.getDisplayName());
//        gen.writeStringField("role",account.getRoles().toString());
        gen.writeEndObject();
    }
}
