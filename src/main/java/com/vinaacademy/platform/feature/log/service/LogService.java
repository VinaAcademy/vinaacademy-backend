package com.vinaacademy.platform.feature.log.service;

public interface LogService {
    void log(String name, String event, Object oldData, Object newData);

    void log(String name, String event, String description, Object oldData, Object newData);

//    Page<LogDto> search(SearchRequest request);

}
