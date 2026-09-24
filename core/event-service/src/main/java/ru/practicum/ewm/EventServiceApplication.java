package ru.practicum.ewm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import ru.practicum.ewm.client.CommentClient;
import ru.practicum.ewm.client.RequestClient;
import ru.practicum.ewm.client.UserClient;

@EnableFeignClients(
        clients = {
                UserClient.class,
                RequestClient.class,
                CommentClient.class
        }
)
@SpringBootApplication
public class EventServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(
                EventServiceApplication.class,
                args
        );
    }
}