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

    /**
     * Converts a source video into adaptive HLS (multiple variant playlists and a master playlist)
     * and generates a thumbnail image.
     *
     * This method removes any existing outputBaseDir, creates the HLS output structure, encodes the
     * input video into the configured set of variants (one directory per variant containing a
     * playlist and segments), and writes a top-level master.m3u8 referencing each variant. A single
     * thumbnail frame is also produced at timestamp 00:00:01 and written to thumbnailFilePath.
     *
     * @param inputFilePath     path to the source video file to be converted
     * @param outputBaseDir     directory where variant subdirectories and master.m3u8 will be written;
     *                          if it already exists it will be recursively deleted before use
     * @param thumbnailFilePath path where the generated thumbnail image will be saved
     * @return 0 on success
     * @throws IOException if filesystem operations fail
     * @throws InterruptedException if the FFmpeg/ffprobe subprocesses are interrupted
     * @throws RuntimeException if FFmpeg fails to produce any required variant (non-zero exit code)
     */
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
         * Converts a source video into adaptive HLS locally, uploads the resulting HLS directory and optional thumbnail to S3/MinIO, and cleans up local files.
         *
         * The method first invokes local HLS generation. If conversion succeeds it uploads the generated HLS directory under the prefix
         * "videos/hls/{videoId}" and, if present, uploads the thumbnail as "videos/thumbnails/{videoId}.jpg". Local output and thumbnail
         * files are removed in a finally block; cleanup failures are logged but do not propagate.
         *
         * @param inputFilePath     path to the source video file
         * @param outputBaseDir     temporary directory where HLS variants and playlists are generated
         * @param thumbnailFilePath path to the generated thumbnail image (may not exist)
         * @param videoId           UUID used to build the S3 key prefix (e.g., "videos/hls/{videoId}")
         * @return the S3/MinIO key prefix where the HLS files were uploaded (e.g., "videos/hls/{videoId}")
         * @throws IOException              if an I/O error occurs during upload or file operations
         * @throws InterruptedException     if the invoked ffmpeg/ffprobe process is interrupted
         * @throws RuntimeException         if the local FFmpeg HLS conversion fails (non-zero exit code)
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

    /**
     * Encodes the input video into a single HLS variant using ffmpeg, writing a playlist and TS segments into the given directory.
     *
     * The method runs the external `ffmpeg` process with the variant's resolution and bitrate settings, producing
     * a "playlist.m3u8" and segment files matching "segment_###.ts" inside {@code variantDir}.
     *
     * @param inputFilePath path to the source video file
     * @param variant      video variant (resolution and bitrates) to produce
     * @param variantDir   directory where the variant playlist and segments will be written
     * @return the ffmpeg process exit code (0 indicates success)
     * @throws IOException if starting the ffmpeg process or file path operations fail
     * @throws InterruptedException if the current thread is interrupted while waiting for ffmpeg to finish
     */
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
