package com.ing.fmjavaguild.web;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JobCreateRequest {
    private String jobName;
    private Map<String, Object> properties;
}
