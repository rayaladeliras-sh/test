package com.stubhub.domain.inventory.v2.DTO;

import java.io.IOException;

import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;

import com.stubhub.domain.inventory.common.entity.SaleMethod;

public class SaleMethodDeserilaizer extends JsonDeserializer<SaleMethod> {
	
	@Override
	public SaleMethod deserialize(JsonParser parser, DeserializationContext context) throws IOException, JsonProcessingException {
		return SaleMethod.getSaleMethod(parser.getText());
	}
	
}
