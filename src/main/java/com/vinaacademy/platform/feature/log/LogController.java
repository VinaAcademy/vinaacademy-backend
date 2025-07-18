package com.vinaacademy.platform.feature.log;

import com.vinaacademy.platform.feature.log.service.LogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/log")
@CrossOrigin("*")
public class LogController {

    @Autowired
    private LogService logService;


//    @CheckPrivilege(value = {PermissionEnum.LOG_VIEW})
//    @PostMapping("/search")
//    public ResponseEntity<Page<LogDto>> search(@RequestBody SearchRequest request) {
//        return ResponseEntity.ok(logService.search(request));
//    }
}