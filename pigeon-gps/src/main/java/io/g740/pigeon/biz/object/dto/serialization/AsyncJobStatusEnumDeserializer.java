package io.g740.pigeon.biz.object.dto.serialization;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import io.g740.commons.constants.AsyncJobStatusEnum;

import java.io.IOException;

/**
 * @author bbottong
 */
public class AsyncJobStatusEnumDeserializer extends JsonDeserializer<AsyncJobStatusEnum> {
    @Override
    public AsyncJobStatusEnum deserialize(
            JsonParser jsonParser,
            DeserializationContext deserializationContext) throws IOException {
        if (jsonParser != null && jsonParser.getText() != null && !jsonParser.getText().isEmpty()) {
            String text = jsonParser.getText();

            return AsyncJobStatusEnum.valueOf(text);
        }

        return null;
    }
}
