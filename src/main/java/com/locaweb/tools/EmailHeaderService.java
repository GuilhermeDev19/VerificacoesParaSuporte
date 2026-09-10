package com.locaweb.tools;

import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class EmailHeaderService {

    public Map<String, Object> analisarCabecalho(String header) {
        Map<String, Object> result = new HashMap<>();

        if (header == null || header.trim().isEmpty()) {
            result.put("erro", "Cabeçalho vazio");
            return result;
        }

        String cleanHeader = header.replace("\r\n", "\n").replace("\r", "\n");

        String rawFrom = extrairValorHeader(cleanHeader, "From");
        result.put("fromRaw", rawFrom);
        result.put("fromEmail", extrairEmailAddress(rawFrom));

        String rawTo = extrairValorHeader(cleanHeader, "To");
        result.put("toRaw", rawTo);
        result.put("toEmail", extrairEmailAddress(rawTo));

        result.put("dateSent", extrairValorHeader(cleanHeader, "Date"));

        String msgId = extrairValorHeader(cleanHeader, "Message-ID");
        if (msgId.equals("Não identificado")) {
            msgId = extrairValorHeader(cleanHeader, "Message-Id");
        }
        if (msgId.equals("Não identificado")) {
            msgId = extrairValorHeader(cleanHeader, "Message-id");
        }
        result.put("messageId", msgId);

        Map<String, String> bounceInfo = new HashMap<>();
        String diag = extrairValorHeader(cleanHeader, "Diagnostic-Code");
        if (diag.equals("Não identificado")) {
            diag = extrairValorHeader(cleanHeader, "Status");
        }

        if (!diag.equals("Não identificado")) {
            bounceInfo.put("diagnosticCode", diag);
        } else {
            bounceInfo.put("diagnosticCode", "Nenhum código de diagnóstico explícito encontrado");
        }

        result.put("bounce", bounceInfo);

        return result;
    }

    private String extrairValorHeader(String headerText, String headerName) {
        try {
            Pattern pattern = Pattern.compile("(?im)(?:^|\\n)\\s*" + Pattern.quote(headerName) + ":\\s*([^\\r\\n]+)");
            Matcher matcher = pattern.matcher(headerText);
            if (matcher.find()) {
                return matcher.group(1).trim();
            }
        } catch (Exception ignored) {}
        return "Não identificado";
    }

    private String extrairEmailAddress(String raw) {
        if (raw == null || raw.equals("Não identificado")) return "Não identificado";

        Pattern anglePattern = Pattern.compile("<([^>]+@[^>]+)>");
        Matcher angleMatcher = anglePattern.matcher(raw);
        if (angleMatcher.find()) {
            return angleMatcher.group(1).trim();
        }

        Pattern plainPattern = Pattern.compile("([a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,})");
        Matcher plainMatcher = plainPattern.matcher(raw);
        if (plainMatcher.find()) {
            return plainMatcher.group(1).trim();
        }

        return raw;
    }
}
