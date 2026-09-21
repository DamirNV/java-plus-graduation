package ru.practicum.ewm.controller.internal;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewm.dto.UserShortDto;
import ru.practicum.ewm.mapper.UserMapper;
import ru.practicum.ewm.repository.UserRepository;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/users")
public class InternalUserController {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @GetMapping("/{userId}")
    public UserShortDto getUser(@PathVariable Long userId) {
        return userRepository.findById(userId)
                .map(userMapper::toShortDto)
                .orElse(null);
    }

    @GetMapping
    public List<UserShortDto> getUsers(@RequestParam List<Long> ids) {
        return userRepository.findAllById(ids).stream()
                .map(userMapper::toShortDto)
                .toList();
    }
}