package com.oscarluque.ghostfollowcore.service;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oscarluque.ghostfollowcore.dto.follower.FollowerWrapper;
import com.oscarluque.ghostfollowcore.dto.follower.InstagramProfile;
import com.oscarluque.ghostfollowcore.dto.response.AnalysisResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class FollowerChangeDetectionService {

    private static final String TARGET_FILE_NAME = "followers_1.json";

    private final ObjectMapper objectMapper;
    private final FollowerAnalysisService followerAnalysisService;

    public AnalysisResponse processFollowerFile(MultipartFile file, String accountName, String userEmail) throws IOException {
        log.info("INICIO: Procesando archivo para la cuenta: {}", accountName);
        long startTime = System.currentTimeMillis();

        validateZipFile(file);

        List<FollowerWrapper> rawWrappers = parseJsonFromZipStream(file);
        List<InstagramProfile> followers = extractProfiles(rawWrappers);

        validateFollowerList(followers, accountName);

        log.info("PARSEO COMPLETADO: {} seguidores procesados en {} ms.", followers.size(), System.currentTimeMillis() - startTime);

        return followerAnalysisService.processNewFollowerList(followers, accountName, userEmail);
    }

    private void validateZipFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("El archivo no puede estar vacío.");
        }
        String fileName = file.getOriginalFilename();
        if (fileName == null || !fileName.toLowerCase().endsWith(".zip")) {
            log.error("Intento de subida inválido: {}", fileName);
            throw new IllegalArgumentException("El archivo debe ser un .zip válido.");
        }
    }


    private List<FollowerWrapper> parseJsonFromZipStream(MultipartFile file) throws IOException {
        try (ZipInputStream zipInputStream = new ZipInputStream(file.getInputStream())) {
            ZipEntry zipEntry;

            while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                if (zipEntry.getName().endsWith(TARGET_FILE_NAME)) {
                    log.debug("📂 Archivo '{}' encontrado. Iniciando lectura stream...", TARGET_FILE_NAME);

                    return objectMapper
                            .disable(JsonParser.Feature.AUTO_CLOSE_SOURCE)
                            .readValue(zipInputStream, new TypeReference<List<FollowerWrapper>>() {});
                }
            }
        }

        log.error("El ZIP analizado no contenía '{}'", TARGET_FILE_NAME);
        throw new IllegalArgumentException("El ZIP no contiene el archivo '" + TARGET_FILE_NAME + "'.");
    }

    private List<InstagramProfile> extractProfiles(List<FollowerWrapper> wrappers) {
        List<InstagramProfile> profiles = new ArrayList<>();
        if (wrappers != null) {
            for (FollowerWrapper wrapper : wrappers) {
                if (wrapper.getFollowerEntryList() != null) {
                    profiles.addAll(wrapper.getFollowerEntryList());
                }
            }
        }
        return profiles;
    }

    private void validateFollowerList(List<InstagramProfile> followers, String accountName) {
        if (followers.isEmpty()) {
            log.warn("El archivo procesado para {} no contenía seguidores válidos.", accountName);
            throw new IllegalArgumentException("El archivo JSON no contiene seguidores o tiene un formato incorrecto.");
        }
    }
}