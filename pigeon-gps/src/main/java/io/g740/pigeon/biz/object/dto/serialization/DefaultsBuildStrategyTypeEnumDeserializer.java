package io.g740.pigeon.biz.object.dto.serialization;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import io.g740.pigeon.biz.share.constants.DefaultsBuildStrategyTypeEnum;

import java.io.IOException;

/**
 * @author bbottong
 */
public class DefaultsBuildStrategyTypeEnumDeserializer extends JsonDeserializer<DefaultsBuildStrategyTypeEnum> {
    @Override
    public DefaultsBuildStrategyTypeEnum deserialize(
            JsonParser jsonParser,
            DeserializationContext deserializationContext) throws IOException {
        if (jsonParser != null && jsonParser.getText() != null && !jsonParser.getText().isEmpty()) {
            String text = jsonParser.getText();

            return DefaultsBuildStrategyTypeEnum.valueOf(text);
        }

        return null;
    }
}
