package io.javabrains.moviecatalogservice.resources;

import io.javabrains.moviecatalogservice.models.Movie;
import io.javabrains.moviecatalogservice.models.Rating;
import io.javabrains.moviecatalogservice.models.UserRating;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.javabrains.moviecatalogservice.models.CatalogItem;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

@RestController
@RequestMapping("/catalog")
public class MovieCatalogResource {

  //This autowired tells spring that someone have a bean RestTemplate, please inject here.
  // MovieCatalogApplication has it with the annotation @Bean
  //@Autowired is a consumer of a resource produced by @Bean
  @Autowired
  private RestTemplate restTemplate;

  @Autowired
  private WebClient.Builder webClientBuilder;

  @RequestMapping("/{userId}")
  public List<CatalogItem> getCatalog(@PathVariable("userId") String userId) {

    // get all rated movie Ids
    // Uses the name of the project in Eureka server (ratings-data-service). This name was registered by the client project.
    UserRating ratings = restTemplate.getForObject("http://ratings-data-service/ratingsdata/users/" + userId, UserRating.class);

    return ratings.getUserRating().stream()
        .map(
            rating -> {
              // For each movie ID, call movie info service and get details
              Movie movie =
                  restTemplate.getForObject(
                      // Uses the name of the project in Eureka server (movie-info-service). This name was registered by the client project.
                      "http://movie-info-service/movies/" + rating.getMovieId(), Movie.class);
              // get them all together
              return new CatalogItem(movie.getName(), "Description", rating.getRating());
            })
        .collect(Collectors.toList());
  }
}

 /*
    REACTIVE OPTION
    return ratings.stream()
        .map(
            rating -> {
              Movie movie = webClientBuilder.build()
                  .get()
                  .uri("http://localhost:8082/movies/" + rating.getMovieId())
                  .retrieve()
                  .bodyToMono(Movie.class)
                  .block();
              return new CatalogItem(movie.getName(), "Description", rating.getRating());
            })
        .collect(Collectors.toList());

     */
