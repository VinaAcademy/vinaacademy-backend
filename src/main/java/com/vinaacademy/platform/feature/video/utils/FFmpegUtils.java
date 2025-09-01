package com.vinaacademy.platform.feature.video.utils;

import com.vinaacademy.platform.feature.storage.service.S3Service;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

@Slf4j
public class FFmpegUtils {

    private static final List<VideoVariant> variants = Arrays.asList(
            new VideoVariant("480p", "854x480", "800k", "96k"),
            new VideoVariant("720p", "1280x720", "1500k", "128k"),
            new VideoVariant("1080p", "1920x1080", "3000k", "192k")
    );

    public static int convertToAdaptiveHLS(Path inputFilePath, Path outputBaseDir, Path thumbnailFilePath) throws IOException, InterruptedException {
        if (Files.exists(outputBaseDir)) {
            deleteDirectoryRecursively(outputBaseDir);
        }
        Files.createDirectories(outputBaseDir);

        StringBuilder masterPlaylistBuilder = new StringBuilder();

        for (VideoVariant variant : variants) {
            Path variantDir = outputBaseDir.resolve(variant.name());
            Files.createDirectories(variantDir);

            int exitCode = convertToVariantHLS(inputFilePath, variant, variantDir);

            if (exitCode != 0) {
                throw new RuntimeException("FFmpeg failed for " + variant.name());
            }

            masterPlaylistBuilder.append("#EXT-X-STREAM-INF:BANDWIDTH=")
                    .append(variant.getBandwidthEstimate()).append(",RESOLUTION=")
                    .append(variant.resolution()).append("\n")
                    .append(variant.name()).append("/playlist.m3u8\n");
        }

        // Tạo master.m3u8
        Path masterPlaylist = outputBaseDir.resolve("master.m3u8");
        String masterContent = "#EXTM3U\n" + masterPlaylistBuilder;
        Files.writeString(masterPlaylist, masterContent);

        tryGenerateThumbnailAtTimestamp(inputFilePath, thumbnailFilePath, "00:00:01");

//        generateThumbnailAtHalfway(inputFilePath, thumbnailFilePath);
//        log.info("Thumbnail generated at: {}", thumbnailFilePath);
        return 0;
    }

    /**
     * Convert video to adaptive HLS and upload to MinIO
     * @param inputFilePath input video file
     * @param outputBaseDir temporary output directory
     * @param thumbnailFilePath thumbnail file path
     * @param s3Service S3 service for uploading to MinIO
     * @param videoId video UUID for S3 key prefix
     * @return MinIO path prefix for the uploaded HLS files
     */
    public static String convertToAdaptiveHLSAndUpload(Path inputFilePath, Path outputBaseDir, Path thumbnailFilePath, 
                                                      com.vinaacademy.platform.feature.storage.service.S3Service s3Service, 
                                                      java.util.UUID videoId) throws IOException, InterruptedException {
        // First convert to HLS locally
        int exitCode = convertToAdaptiveHLS(inputFilePath, outputBaseDir, thumbnailFilePath);
        if (exitCode != 0) {
            throw new RuntimeException("FFmpeg conversion failed with exit code: " + exitCode);
        }

        try {
            // Upload HLS directory to MinIO
            String s3KeyPrefix = "videos/hls/" + videoId.toString();
            s3Service.uploadDirectory(s3KeyPrefix, outputBaseDir);
            
            // Upload thumbnail to MinIO
            if (Files.exists(thumbnailFilePath)) {
                String thumbnailKey = "videos/thumbnails/" + videoId.toString() + ".jpg";
                s3Service.uploadFile(thumbnailKey, thumbnailFilePath, "image/jpeg");
            }
            
            log.info("Successfully uploaded HLS files and thumbnail to MinIO for video: {}", videoId);
            return s3KeyPrefix;
            
        } finally {
            // Clean up local files
            try {
                deleteDirectoryRecursively(outputBaseDir);
                Files.deleteIfExists(thumbnailFilePath);
                log.debug("Cleaned up local HLS files for video: {}", videoId);
            } catch (Exception e) {
                log.warn("Failed to clean up local files for video {}: {}", videoId, e.getMessage());
            }
        }
    }

    private static int convertToVariantHLS(Path inputFilePath, VideoVariant variant, Path variantDir) throws IOException, InterruptedException {
        String playlistPath = variantDir.resolve("playlist.m3u8").toString().replace("\\", "/");
        String segmentPattern = variantDir.resolve("segment_%03d.ts").toString().replace("\\", "/");

        ProcessBuilder pb = new ProcessBuilder(
                "ffmpeg",
                "-i", inputFilePath.toString(),
                "-vf", "scale=" + variant.resolution(),
                "-c:v", "libx264",
                "-b:v", variant.videoBitrate(),
                "-c:a", "aac",
                "-b:a", variant.audioBitrate(),
                "-hls_time", "4",
                "-hls_list_size", "0",
                "-hls_segment_filename", segmentPattern,
                "-f", "hls",
                playlistPath
        );

        pb.inheritIO();
        Process process = pb.start();
        return process.waitFor();
    }

    public static void generateThumbnailAtHalfway(Path videoPath, Path outputImagePath) throws IOException, InterruptedException {
        double duration = getVideoDurationInSeconds(videoPath);
        double halfway = duration / 2;

        String timestamp = String.format("00:%02d:%02d", (int) (halfway / 60), (int) (halfway % 60));

        int exitCode = tryGenerateThumbnailAtTimestamp(videoPath, outputImagePath, timestamp);
        if (exitCode != 0) {
            throw new RuntimeException("Failed to generate thumbnail at halfway (" + timestamp + ")");
        }
    }

    private static int tryGenerateThumbnailAtTimestamp(Path input, Path output, String timestamp) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(
                "ffmpeg",
                "-ss", timestamp,
                "-i", input.toString(),
                "-vframes", "1",
                "-f", "image2",
                "-q:v", "2",
                output.toString()
        );
        pb.inheritIO();
        Process process = pb.start();
        return process.waitFor();
    }


    public static double getVideoDurationInSeconds(Path videoPath) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(
                "ffprobe",
                "-v", "error",
                "-select_streams", "v:0",
                "-show_entries", "format=duration",
                "-of", "default=noprint_wrappers=1:nokey=1",
                videoPath.toString()
        );

        pb.redirectErrorStream(true);
        Process process = pb.start();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line = reader.readLine();
            int exitCode = process.waitFor();
            if (exitCode != 0 || line == null) {
                throw new RuntimeException("Failed to get video duration");
            }
            return Double.parseDouble(line);
        }
    }


    public static void deleteDirectoryRecursively(Path path) throws IOException {
        if (Files.exists(path)) {
            try (Stream<Path> walk = Files.walk(path)) {
                walk.sorted(Comparator.reverseOrder())
                    .forEach(p -> {
                        try {
                            Files.delete(p);
                        } catch (IOException e) {
                            log.error("Delete failed: {}", p, e);
                        }
                    });
            }
        }
    }

    public record VideoVariant(String name, String resolution, String videoBitrate, String audioBitrate) {
        public int getBandwidthEstimate() {
            // Rough estimate for bandwidth
            int videoKbps = Integer.parseInt(videoBitrate.replace("k", ""));
            int audioKbps = Integer.parseInt(audioBitrate.replace("k", ""));
            return (videoKbps + audioKbps) * 1024;
        }
    }
}
