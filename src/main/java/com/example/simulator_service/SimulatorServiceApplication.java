package com.example.simulator_service;

import com.example.simulator_service.model.SwapRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;
import org.springframework.web.reactive.socket.client.WebSocketClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@SpringBootApplication
@Slf4j
public class SimulatorServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(SimulatorServiceApplication.class, args);
	}

	@Bean
	public CommandLineRunner simulatorRunner() {
		return args -> {
			Thread.sleep(2000); // Wait for consumer to start

			WebSocketClient client = new ReactorNettyWebSocketClient();
			ObjectMapper mapper = new ObjectMapper();
			List<String> staff = Arrays.asList("Anna", "Ivan", "Oleg", "Maria");
			Random random = new Random();

			URI url = new URI("ws://localhost:8080/ws/swaps");
			log.info(">>> SIMULATOR STARTED: Connecting to " + url);

			client.execute(url, session -> {
				// Generate event stream
				Flux<WebSocketMessage> simulationFlux = Flux.interval(Duration.ofMillis(300))
						.map(i -> {
							String from = staff.get(random.nextInt(staff.size()));
							String to = staff.get(random.nextInt(staff.size()));
							return new SwapRequest(
									UUID.randomUUID().toString(),
									from, to, "REQUEST",
									System.currentTimeMillis()
							);
						})
						.map(req -> {
							try {
								return mapper.writeValueAsString(req);
							} catch (Exception e) {
								return "";
							}
						})
						.map(session::textMessage);

				// Receive responses from consumer
				Mono<Void> receive = session.receive()
						.doOnNext(msg -> log.info("\n[CONSUMER RESPONSE]: " + msg.getPayloadAsText()))
						.then();

				return session.send(simulationFlux).and(receive);
			}).subscribe();
		};
	}
}