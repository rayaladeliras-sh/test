package com.stubhub.domain.inventory.v2.DTO;

import java.io.IOException;

import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;

import com.stubhub.domain.inventory.v2.enums.*;

/**
 * Deserializer that converts a String to a Operation enumeration type 
 * 
 * @author sadranly
 */
public class OperationDeserializer extends JsonDeserializer<Operation> 
{	
	@Override
    public Operation deserialize(JsonParser parser, DeserializationContext context) 
    		throws IOException, JsonProcessingException 
    {
        return Operation.fromString( parser.getText() );
    }
	
}
