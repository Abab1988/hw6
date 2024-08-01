package com.github.javarar.poke;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(value = "testClient", url = "http://localhost:8080/")
public interface TestClient {

    @RequestMapping(method = RequestMethod.GET, value = "/getAll")
    List<PokemonDto> getAll(@RequestParam List<String> names);

    @RequestMapping(method = RequestMethod.GET, value = "/getAllAsync")
    List<PokemonDto> getAllAsync(@RequestParam List<String> names);

}
