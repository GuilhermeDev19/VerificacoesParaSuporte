package com.locaweb.tools;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/header")
@CrossOrigin(origins = "*")
public class EmailHeaderController {

    private final EmailHeaderService headerService;

    public EmailHeaderController(EmailHeaderService headerService) {
        this.headerService = headerService;
    }

    @PostMapping("/analyze")
    public ResponseEntity<?> analisarCabecalho(@RequestBody Map<String, String> body) {
        String rawHeader = body.get("header");
        Map<String, Object> res = headerService.analisarCabecalho(rawHeader);
        if (res.containsKey("erro")) {
            return ResponseEntity.badRequest().body(res);
        }
        return ResponseEntity.ok(res);
    }
}
