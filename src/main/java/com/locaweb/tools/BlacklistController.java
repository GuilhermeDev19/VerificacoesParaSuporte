package com.locaweb.tools;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/blacklist")
@CrossOrigin(origins = "*")
public class BlacklistController {

    private final BlacklistService blacklistService;

    public BlacklistController(BlacklistService blacklistService) {
        this.blacklistService = blacklistService;
    }

    @GetMapping("/check")
    public ResponseEntity<?> checar(@RequestParam String target) {
        return ResponseEntity.ok(blacklistService.checarBlacklist(target));
    }
}
