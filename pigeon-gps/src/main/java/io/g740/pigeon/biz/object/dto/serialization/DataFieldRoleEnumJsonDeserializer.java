package io.g740.pigeon.biz.object.dto.serialization;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import io.g740.pigeon.biz.share.constants.DataFieldRoleEnum;

import java.io.IOException;

public class DataFieldRoleEnumJsonDeserializer extends JsonDeserializer<DataFieldRoleEnum> {
    @Override
    public DataFieldRoleEnum deserialize(JsonParser jsonParser,
                                        DeserializationContext deserializationContext) throws IOException {
        if (jsonParser != null && jsonParser.getText() != null && !jsonParser.getText().isEmpty()) {
            String text = jsonParser.getText();

            return DataFieldRoleEnum.valueOf(text);
        }

        return null;
    }
}

