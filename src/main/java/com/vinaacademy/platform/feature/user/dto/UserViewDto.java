package com.vinaacademy.platform.feature.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class UserViewDto extends UserDto{
	
	private long countCourseCreate;
	private long countCourseEnroll;
	private long countCourseEnrollComplete;

}
