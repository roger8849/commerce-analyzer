package co.edu.unal.migration.commerceanalyzer.controllers.rest;

import co.edu.unal.migration.commerceanalyzer.services.SensitivePropertiesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/sensitive")
public class SensitivePropertiesController {
    @Autowired
    private SensitivePropertiesService sensitivePropertiesService;

    @RequestMapping("/generate-properties")
    public ResponseEntity<String> searchWordInDirectory() throws IOException {
        sensitivePropertiesService.generateSensitiveFiles();
        return ResponseEntity.ok("Success");
    }
}
