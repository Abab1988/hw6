package com.github.javarar.poke;

import java.util.List;

public record PokemonDto(
        String name,
        Double height,
        Double weight,
        List<String> abilities) {
}
