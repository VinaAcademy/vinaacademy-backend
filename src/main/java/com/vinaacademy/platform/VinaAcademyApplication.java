package com.vinaacademy.platform;

import com.vinaacademy.platform.feature.TestingDataService;
import com.vinaacademy.platform.feature.common.constant.AppConstants;
import java.util.TimeZone;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import vn.vinaacademy.common.exception.GlobalExceptionHandler;

@SpringBootApplication
@EnableDiscoveryClient
@ComponentScan(
    basePackages = {"vn.vinaacademy.common", "com.vinaacademy.platform"},
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = GlobalExceptionHandler.class
    )
)

public class VinaAcademyApplication {

  public static void main(String[] args) {
    TimeZone.setDefault(TimeZone.getTimeZone(AppConstants.TIME_ZONE));
    SpringApplication.run(VinaAcademyApplication.class, args);
  }

  @Bean
  public CommandLineRunner createTestingData(TestingDataService testingDataService) {
    return args -> {
      // Create testing data here
      testingDataService.createTestingAuthData(); // Create roles and users
      testingDataService.createSeedDataFromJson(); // Create categories and courses from JSON file
    };
  }
}
