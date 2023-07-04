package com.stubhub.domain.inventory.v2.DTO;

import java.io.IOException;

import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;

import com.stubhub.domain.inventory.common.entity.SplitOption;

public class SplitOptionDeserializer extends JsonDeserializer<SplitOption>{
	 
	@Override
	public SplitOption deserialize(JsonParser parser, DeserializationContext context) throws IOException, JsonProcessingException {
		return SplitOption.getSplitOption(parser.getText());
	}
	
}
