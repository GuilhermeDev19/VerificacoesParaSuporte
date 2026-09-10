package com.locaweb.tools;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth-eval")
@CrossOrigin(origins = "*")
public class EmailAuthController {

    private final EmailAuthEvaluatorService evalService;

    public EmailAuthController(EmailAuthEvaluatorService evalService) {
        this.evalService = evalService;
    }

    @GetMapping("/check")
    public ResponseEntity<?> avaliarDomínio(@RequestParam String dominio) {
        return ResponseEntity.ok(evalService.avaliarAutenticacaoEmail(dominio));
    }
}
