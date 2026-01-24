package com.oscarluque.ghostfollowcore.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oscarluque.ghostfollowcore.dto.follower.FollowerWrapper;
import com.oscarluque.ghostfollowcore.dto.follower.InstagramProfile;
import com.oscarluque.ghostfollowcore.dto.response.AnalysisResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
@RequiredArgsConstructor
public class FollowerChangeDetectionService {

    private static final Logger LOGGER = LoggerFactory.getLogger(FollowerChangeDetectionService.class);

    private final ObjectMapper objectMapper;
    private final FollowerAnalysisService followerAnalysisService;

    public AnalysisResponse processFollowerFile(MultipartFile file, String accountName, String userEmail) throws IOException {
        LOGGER.info("Iniciando procesamiento de archivo ZIP para la cuenta: {} (Usuario: {})", accountName, userEmail);

        String jsonContent = processZipFileToJson(file);

        List<FollowerWrapper> containerList = objectMapper.readValue(jsonContent, new TypeReference<>() {
        });

        List<InstagramProfile> allFollowers = new ArrayList<>();
        if (containerList != null) {
            for (FollowerWrapper wrapper : containerList) {
                if (wrapper.getFollowerEntryList() != null) {
                    allFollowers.addAll(wrapper.getFollowerEntryList());
                }
            }
        }

        LOGGER.info("Se han extraído {} seguidores del archivo JSON para la cuenta {}", allFollowers.size(), accountName);

        if (allFollowers.isEmpty()) {
            LOGGER.warn("El archivo procesado no contenía seguidores válidos para la cuenta {}", accountName);
            throw new IllegalArgumentException("El archivo JSON no contiene seguidores o tiene un formato incorrecto.");
        }

        return followerAnalysisService.processNewFollowerList(allFollowers, accountName, userEmail);
    }

    private String processZipFileToJson(MultipartFile file) throws IOException {
        if (file != null) {
            String fileName = file.getOriginalFilename();

            if (fileName == null || !fileName.toLowerCase().endsWith(".zip")) {
                LOGGER.error("Intento de subida con archivo incorrecto (no es ZIP): {}", fileName);
                throw new IllegalArgumentException("El archivo debe ser un .zip");
            }

            try (ZipInputStream zipInputStream = new ZipInputStream(file.getInputStream())) {
                ZipEntry zipEntry;

                while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                    if (zipEntry.getName().endsWith("followers_1.json")) {
                        LOGGER.debug("Archivo 'followers_1.json' encontrado dentro del ZIP.");

                        StringBuilder content = new StringBuilder();
                        BufferedReader reader = new BufferedReader(new InputStreamReader(zipInputStream, StandardCharsets.UTF_8));
                        String line;
                        while ((line = reader.readLine()) != null) {
                            content.append(line);
                        }

                        return content.toString();
                    }
                }
            }
        }

        LOGGER.error("El archivo ZIP no existe o no contiene 'followers_1.json'");
        throw new IllegalArgumentException("El ZIP no existe o no contiene el archivo 'followers_1.json'. Asegúrate de haber descargado los datos correctos de Instagram.");
    }
}