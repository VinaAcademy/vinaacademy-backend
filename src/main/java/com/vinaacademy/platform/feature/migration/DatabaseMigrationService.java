package com.vinaacademy.platform.feature.migration;

import jakarta.annotation.PostConstruct;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Simple migration service to ensure required database extensions exist.
 *
 * <p>Currently ensures that the PostgreSQL {@code unaccent} extension is created so that
 * accent-insensitive search can be used in JPA specifications.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DatabaseMigrationService {

  private final DataSource dataSource;

  @PostConstruct
  public void migrate() {
    ensureUnaccentExtension();
  }

  private void ensureUnaccentExtension() {
    String checkSql =
        "SELECT 1 FROM pg_extension WHERE extname = 'unaccent'";
    String createSql = "CREATE EXTENSION IF NOT EXISTS unaccent";

    try (Connection connection = dataSource.getConnection();
        Statement statement = connection.createStatement()) {

      try (ResultSet rs = statement.executeQuery(checkSql)) {
        if (rs.next()) {
          log.info("PostgreSQL extension 'unaccent' already exists");
          return;
        }
      }

      log.info("PostgreSQL extension 'unaccent' is missing. Creating it now...");
      statement.executeUpdate(createSql);
      log.info("PostgreSQL extension 'unaccent' created successfully");

    } catch (SQLException ex) {
      log.error("Failed to ensure PostgreSQL 'unaccent' extension", ex);
      throw new RuntimeException("Unaccent extension initialization failed", ex);
    }
  }
}
