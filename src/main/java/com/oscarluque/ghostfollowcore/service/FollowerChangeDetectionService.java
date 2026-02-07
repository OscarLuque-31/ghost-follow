package com.oscarluque.ghostfollowcore.service;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oscarluque.ghostfollowcore.dto.follower.Follower;
import com.oscarluque.ghostfollowcore.dto.follower.FollowingList;
import com.oscarluque.ghostfollowcore.dto.follower.InstagramProfile;
import com.oscarluque.ghostfollowcore.dto.response.AnalysisResponse;
import com.oscarluque.ghostfollowcore.dto.utils.ZipDataResult;
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

    private static final String TARGET_FILE_NAME_FOLLOWERS = "followers_1.json";
    private static final String TARGET_FILE_NAME_FOLLOWING = "following.json";

    private final ObjectMapper objectMapper;
    private final FollowerAnalysisService followerAnalysisService;
    private final FollowingService followingService;

    public AnalysisResponse processFollowerFile(MultipartFile file, String accountName, String userEmail) throws IOException {
        log.info("Procesando ZIP para: {}", accountName);
        long startTime = System.currentTimeMillis();

        validateZipFile(file);

        ZipDataResult zipData = extractDataFromZip(file);

        if (zipData.getFollowers().isEmpty() && zipData.getFollowing().isEmpty()) {
            throw new IllegalArgumentException("El ZIP no contiene datos válidos de Instagram.");
        }

        log.info("Procesado en {} ms. Followers: {}, Following: {}",
                System.currentTimeMillis() - startTime,
                zipData.getFollowers().size(),
                zipData.getFollowing().size());

        followingService.processFollowingList(zipData.getFollowing(), userEmail);

        return followerAnalysisService.processNewFollowerList(zipData.getFollowers(), accountName, userEmail);
    }

    private void validateZipFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Archivo vacío.");
        }
        if (file.getOriginalFilename() == null && !file.getOriginalFilename().toLowerCase().endsWith(".zip")) {
            throw new IllegalArgumentException("Debe ser un archivo .zip");
        }
    }

    private ZipDataResult extractDataFromZip(MultipartFile file) throws IOException {
        ZipDataResult.ZipDataResultBuilder resultBuilder = ZipDataResult.builder();

        try (ZipInputStream zis = new ZipInputStream(file.getInputStream())) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                String name = entry.getName();

                if (name.endsWith(TARGET_FILE_NAME_FOLLOWERS)) {
                    try {
                        List<Follower> raw = objectMapper
                                .disable(JsonParser.Feature.AUTO_CLOSE_SOURCE)
                                .readValue(zis, new TypeReference<List<Follower>>() {});
                        resultBuilder.followers(flattenFollowers(raw));
                    } catch (Exception e) {
                        log.error("Error parseando followers", e);
                        throw new IllegalArgumentException("JSON de followers inválido.");
                    }

                } else if (name.endsWith(TARGET_FILE_NAME_FOLLOWING)) {
                    try {
                        FollowingList response = objectMapper
                                .disable(JsonParser.Feature.AUTO_CLOSE_SOURCE)
                                .readValue(zis, FollowingList.class);

                        if (response.getRelationships() != null) {
                            resultBuilder.following(response.getRelationships());
                        }
                    } catch (Exception e) {
                        log.error("Error parseando following", e);
                        throw new IllegalArgumentException("JSON de following inválido.");
                    }
                }
            }
        }
        return resultBuilder.build();
    }

    private List<InstagramProfile> flattenFollowers(List<Follower> wrappers) {
        List<InstagramProfile> profiles = new ArrayList<>();
        if (wrappers != null) {
            for (Follower wrapper : wrappers) {
                if (wrapper.getFollowerEntryList() != null) {
                    profiles.addAll(wrapper.getFollowerEntryList());
                }
            }
        }
        return profiles;
    }
}