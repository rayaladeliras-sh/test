package com.stubhub.domain.inventory.v2.DTO;

import java.io.IOException;

import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;

import com.stubhub.domain.inventory.v2.enums.*;

/**
 * Deserializer that converts ProductType string to an Enum
 * 
 * @author sadranly
 */
public class ProductTypeDeserializer extends JsonDeserializer<ProductType> 
{	
	@Override
    public ProductType deserialize(JsonParser parser, DeserializationContext context) 
    		throws IOException, JsonProcessingException 
    {
        return ProductType.fromString( parser.getText() );
    }
	
}
