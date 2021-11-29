package io.g740.pigeon.biz.object.dto.serialization;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import io.g740.pigeon.biz.share.constants.DfQueryGroupByFieldProcessingTypeEnum;

import java.io.IOException;

/**
 * @author bbottong
 */
public class DfQueryGroupByTypeEnumDeserializer extends JsonDeserializer<DfQueryGroupByFieldProcessingTypeEnum> {
    @Override
    public DfQueryGroupByFieldProcessingTypeEnum deserialize(
            JsonParser jsonParser,
            DeserializationContext deserializationContext) throws IOException {
        if (jsonParser != null && jsonParser.getText() != null && !jsonParser.getText().isEmpty()) {
            String text = jsonParser.getText();

            return DfQueryGroupByFieldProcessingTypeEnum.valueOf(text);
        }

        return null;
    }
}
