package com.oscarluque.ghostfollowcore;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class GhostFollowCoreApplication {
    public static void main(String[] args) {
        SpringApplication.run(GhostFollowCoreApplication.class, args);
    }
}


