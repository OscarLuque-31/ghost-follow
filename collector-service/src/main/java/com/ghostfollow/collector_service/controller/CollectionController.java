package com.ghostfollow.collector_service.controller;

import com.ghostfollow.collector_service.model.FollowerList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;

@RestController
public class CollectionController {

    private static final String RAW_TOPIC = "raw-follower-lists";

    @Autowired
    private KafkaTemplate<String, FollowerList> kafkaTemplate;

    @PostMapping("/trigger")
    public ResponseEntity<String> triggerCollection(){
        // SIMULACION DE DATOS RECOLECTADOS (a futuro será con scrapping)
        List<String> currentFollowers = Arrays.asList("oscar_dev","lauradiaz","pedrovigarita");

        FollowerList data = new FollowerList("monitored_account", currentFollowers, System.currentTimeMillis());

        kafkaTemplate.send(RAW_TOPIC, data.getAccountId(), data);

        return ResponseEntity.accepted().body("Collection proccess started for" + data.getAccountId() + ".");
    }
}
