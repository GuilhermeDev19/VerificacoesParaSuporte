package com.locaweb.tools;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.Map;

@RestController
@RequestMapping("/api/ssl")
public class SslController {

    @Autowired
    private SslService sslService;

    @PostMapping("/generate-csr")
    public Map<String, String> gerarCsr(@RequestBody Map<String, String> payload) {
        return sslService.gerarCsrEKey(
                payload.getOrDefault("domain", ""),
                payload.getOrDefault("organization", ""),
                payload.getOrDefault("department", ""),
                payload.getOrDefault("city", ""),
                payload.getOrDefault("state", ""),
                payload.getOrDefault("country", "BR")
        );
    }

    @PostMapping("/convert-pfx")
    public ResponseEntity<?> converterParaPfx(
            @RequestParam("crtFile") MultipartFile crtFile,
            @RequestParam("keyFile") MultipartFile keyFile,
            @RequestParam(value = "password", defaultValue = "") String password) {
        try {
            byte[] pfxBytes = sslService.criarPfx(crtFile, keyFile, password);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"certificado.pfx\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(pfxBytes);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }

    @PostMapping("/extract-pfx")
    public ResponseEntity<?> extrairPfx(
            @RequestParam("pfxFile") MultipartFile pfxFile,
            @RequestParam(value = "password", defaultValue = "") String password) {
        Map<String, String> res = sslService.extrairPfx(pfxFile, password);
        if (res.containsKey("erro")) {
            return ResponseEntity.badRequest().body(res);
        }
        return ResponseEntity.ok(res);
    }
}
