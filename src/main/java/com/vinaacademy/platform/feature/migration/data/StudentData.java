package com.vinaacademy.platform.feature.migration.data;

import com.vinaacademy.platform.feature.user.entity.User;
import com.vinaacademy.platform.feature.user.role.entity.Role;
import lombok.experimental.UtilityClass;
import net.datafaker.Faker;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@UtilityClass
public class StudentData {

    private static final Faker FAKER = new Faker();

    public static List<User> generateRandomStudents(int maxRandomStudents, Role studentRole) {
        int actualCount = ThreadLocalRandom.current().nextInt(1, maxRandomStudents + 1);
        return IntStream.range(0, actualCount)
                .mapToObj(i -> {
                    String uniqueSuffix = UUID.randomUUID().toString().substring(0, 8);
                    String firstName = FAKER.name().firstName().toLowerCase();

                    String username = firstName + "." + uniqueSuffix;
                    String email = firstName + "." + uniqueSuffix + "@vnacademy.io.vn";

                    return User.builder()
                            .username(username)
                            .password("$2a$12$c8xwXNrvHAKNP/Yzirb8MOV/iKnTU3J/aUqC2uCH8E3FmUJ4MUIy.")
                            .email(email)
                            .enabled(true)
                            .roles(Set.of(studentRole))
                            .build();
                })
                .collect(Collectors.toList());
    }
}
