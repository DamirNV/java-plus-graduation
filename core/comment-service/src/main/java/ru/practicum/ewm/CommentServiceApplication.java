package ru.practicum.ewm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import ru.practicum.ewm.client.EventClient;
import ru.practicum.ewm.client.UserClient;

@EnableFeignClients(
        clients = {
                UserClient.class,
                EventClient.class
        }
)
@SpringBootApplication(
        scanBasePackages = {
                "ru.practicum.ewm",
                "ru.practicum.stats.client"
        }
)
public class CommentServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(
                CommentServiceApplication.class,
                args
        );
    }
}