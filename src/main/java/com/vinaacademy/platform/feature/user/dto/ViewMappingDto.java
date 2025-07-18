package com.vinaacademy.platform.feature.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ViewMappingDto {
	public long countCourseCreate;
	public long countCourseEnroll;
	public long countCourseEnrollComplete;
} 
