package ru.practicum.ewm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Import;
import ru.practicum.ewm.client.EventClient;
import ru.practicum.ewm.client.UserClient;
import ru.practicum.stats.client.StatsClient;

@EnableFeignClients(
        clients = {
                UserClient.class,
                EventClient.class
        }
)
@Import(StatsClient.class)
@SpringBootApplication
public class EwmServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(
                EwmServiceApplication.class,
                args
        );
    }
}