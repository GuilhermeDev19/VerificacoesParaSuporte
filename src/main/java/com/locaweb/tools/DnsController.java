package com.locaweb.tools;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dns")
@CrossOrigin(origins = "*")
public class DnsController {

    private final DnsService dnsService;

    public DnsController(DnsService dnsService) {
        this.dnsService = dnsService;
    }

    @GetMapping
    public ResponseEntity<?> consultarDns(
            @RequestParam String dominio,
            @RequestParam(required = false) String server) {
        try {
            DnsResponse resultado = dnsService.consultarDns(dominio, server);
            return ResponseEntity.ok(resultado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Erro ao realizar consulta DNS.");
        }
    }
}
