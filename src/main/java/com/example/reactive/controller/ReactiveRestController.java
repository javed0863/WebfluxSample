package com.example.reactive.controller;

import com.example.reactive.model.Greeting;
import com.example.reactive.model.Person;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@RestController
public class ReactiveRestController {

    private static List<Person> personList = new ArrayList<>();
    static {
        personList.add(new Person(1, "John"));
        personList.add(new Person(2, "Jane"));
        personList.add(new Person(3, "Max"));
        personList.add(new Person(4, "Alex"));
        personList.add(new Person(5, "Aloy"));
        personList.add(new Person(6, "Sarah"));
    }

    @GetMapping("/person/{id}")
    public Person getPerson(@PathVariable int id, @RequestParam(defaultValue = "2") int delay)
            throws InterruptedException {
        Thread.sleep(delay * 1000);
        return personList.stream()
                .filter((person) -> person.getId() == id)
                .findFirst()
                .get();
    }

    @GetMapping("/rxPerson/{id}")
    private Mono<Person> getPersonById(@PathVariable int id) {
        return Mono.just(
                personList.stream()
                .filter((person) -> person.getId() == id)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Person not found !!"))
                );
    }

    @GetMapping("/rxPerson/all")
    private Flux<Person> getAllPersons() {
        return Flux.fromStream(personList.stream());
    }

    @GetMapping(value = "/greetings", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Greeting> greet(){
        Flux<Greeting> greetingFlux = Flux
                .<Greeting>generate(sink -> sink.next(new Greeting("Hello @" + Instant.now().toString())));
        Flux<Long> durationFlux = Flux.interval(Duration.ofSeconds(1));

        return Flux.zip(greetingFlux, durationFlux)
                .map(Tuple2::getT1);
    }

}
