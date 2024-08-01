package com.github.javarar.poke;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT,
        properties = {
                "server.port=8080",
                "management.server.port=9042"
        }
)

public class PokeServiceApplicationTests {

    @Autowired
    TestClient client;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void checkGetAllTest() throws JsonProcessingException {
        String expected ="[{\"name\":\"pikachu\",\"height\":4.0,\"weight\":60.0,\"abilities\":[\"static\",\"lightning-rod\"]}]";
        List<PokemonDto> real = client.getAll(List.of("pikachu"));
        Assertions.assertEquals(expected, objectMapper.writeValueAsString(real));
    }

    @Test
    void checkGetAllAsyncTest() throws JsonProcessingException {
        String expected ="[{\"name\":\"pikachu\",\"height\":4.0,\"weight\":60.0,\"abilities\":[\"static\",\"lightning-rod\"]}]";
        List<PokemonDto> real = client.getAllAsync(List.of("pikachu"));
        Assertions.assertEquals(expected, objectMapper.writeValueAsString(real));
    }


}
