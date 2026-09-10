package com.locaweb.tools;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ports")
@CrossOrigin(origins = "*")
public class PortTesterController {

    private final PortTesterService portService;

    public PortTesterController(PortTesterService portService) {
        this.portService = portService;
    }

    @GetMapping("/scan")
    public ResponseEntity<?> scanPortas(@RequestParam String host) {
        return ResponseEntity.ok(portService.testarPortasServidor(host));
    }

    @GetMapping("/smtp-test")
    public ResponseEntity<?> testarSmtp(@RequestParam String host, @RequestParam(defaultValue = "587") int porta) {
        return ResponseEntity.ok(portService.testarSmtpHandshake(host, porta));
    }
}
