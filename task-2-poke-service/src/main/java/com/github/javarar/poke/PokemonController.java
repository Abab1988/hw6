package com.github.javarar.poke;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@RestController
public class PokemonController {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper mapper = new ObjectMapper();
    public static String URL = "https://pokeapi.co/api/v2/pokemon/";
    private final Random random = ThreadLocalRandom.current();

    @GetMapping("/getAll")
    @ResponseStatus(HttpStatus.OK)
    public List<PokemonDto> getAll(@RequestParam List<String> names) throws IOException {
        List<PokemonDto> response = new ArrayList<>();
        for (String name : names) {
            response.add(getPokemonInfo(name));
        }
        return response;
    }

    @GetMapping("/oneOf")
    public PokemonDto oneOf(@RequestParam List<String> names) throws IOException {
        String randomName = names.get(random.nextInt(names.size()));
        return getPokemonInfo(randomName);
    }

    @GetMapping("/getAllAsync")
    public List<PokemonDto> getAllParallel(@RequestParam List<String> names) throws ExecutionException, InterruptedException, JsonProcessingException {
        return getAllAsync(names);
    }

    @GetMapping("/getOneOfAsync")
    public PokemonDto anyOf(@RequestParam List<String> names) throws ExecutionException, InterruptedException {
        return getOneOfAsync(names);
    }

    private PokemonDto getOneOfAsync(List<String> names) throws ExecutionException, InterruptedException {
        List<CompletableFuture<PokemonDto>> futures = new ArrayList<>();
        for (String name : names) {
            futures.add(CompletableFuture.supplyAsync(() -> {
                try {
                    return getPokemonInfo(name);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }));
        }
        return (PokemonDto) CompletableFuture.anyOf(futures.toArray(new CompletableFuture[0])).get();
    }

    private List<PokemonDto> getAllAsync(List<String> names) throws ExecutionException, InterruptedException {
        List<CompletableFuture<PokemonDto>> futures = new ArrayList<>();
        for (String name : names) {
            futures.add(CompletableFuture.supplyAsync(() -> {
                try {
                    return getPokemonInfo(name);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }));
        }

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(
                        f -> futures.stream()
                            .map(CompletableFuture::join)
                            .collect(Collectors.toList())
                ).get();
    }

    private PokemonDto getPokemonInfo(String name) throws IOException {
        String response = restTemplate.getForEntity(URL + name, String.class).getBody();
        Map<String, Object> responseMap = mapper.readValue(response, Map.class);
        return getPokemon(responseMap);
    }

    private PokemonDto getPokemon(Map<String, Object> map) {
        List<String> abilitiesList = new ArrayList<>();
        List<Map<String, Object>> abilities = (List<Map<String, Object>>) map.get("abilities");

        for (Map<String, Object> a : abilities) {
            Map<String, Object> ability = (Map<String, Object>) a.get("ability");
            abilitiesList.add(ability.get("name").toString());

        }

        return new PokemonDto(
                map.get("name").toString(),
                Double.valueOf(map.get("height").toString()),
                Double.valueOf(map.get("weight").toString()),
                abilitiesList
        );
    }

}
