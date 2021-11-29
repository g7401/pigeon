package io.g740.pigeon.biz.object.dto.serialization;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import io.g740.pigeon.biz.share.constants.DsTypeEnum;

import java.io.IOException;

/**
 * @author bbottong
 */
public class DsTypeEnumDeserializer extends JsonDeserializer<DsTypeEnum> {
    @Override
    public DsTypeEnum deserialize(
            JsonParser jsonParser,
            DeserializationContext deserializationContext) throws IOException {
        if (jsonParser != null && jsonParser.getText() != null && !jsonParser.getText().isEmpty()) {
            String text = jsonParser.getText();

            return DsTypeEnum.valueOf(text);
        }

        return null;
    }
}
