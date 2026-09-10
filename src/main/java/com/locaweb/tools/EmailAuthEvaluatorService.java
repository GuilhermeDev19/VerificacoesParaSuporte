package com.locaweb.tools;

import org.springframework.stereotype.Service;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Record;
import org.xbill.DNS.TXTRecord;
import org.xbill.DNS.Type;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class EmailAuthEvaluatorService {

    public Map<String, Object> avaliarAutenticacaoEmail(String dominio) {
        Map<String, Object> res = new HashMap<>();
        res.put("dominio", dominio);

        // --- SPF ---
        Map<String, Object> spfInfo = avaliarSpf(dominio);
        res.put("spf", spfInfo);

        // --- DMARC ---
        Map<String, Object> dmarcInfo = avaliarDmarc(dominio);
        res.put("dmarc", dmarcInfo);

        return res;
    }

    private Map<String, Object> avaliarSpf(String dominio) {
        Map<String, Object> res = new HashMap<>();
        String spfRecord = buscarRecordTxt(dominio, "v=spf1");

        res.put("encontrado", spfRecord != null);
        res.put("raw", spfRecord != null ? spfRecord : "Nenhum registro SPF (v=spf1) encontrado.");

        if (spfRecord != null) {
            int lookups = contarLookupsSpf(spfRecord);
            res.put("dnsLookups", lookups);
            res.put("lookupValido", lookups <= 10);
            
            List<String> avisos = new ArrayList<>();
            if (lookups > 10) {
                avisos.add("O registro SPF excede o limite de 10 DNS Lookups (RFC 7208). Isso fará com que o SPF falhe em servidores rigorosos.");
            }
            if (spfRecord.contains("+all")) {
                avisos.add("PERIGO: O uso de '+all' permite que QUALQUER IP envie e-mails em nome do seu domínio.");
            }
            if (!spfRecord.contains("~all") && !spfRecord.contains("-all")) {
                avisos.add("Recomendado usar '-all' (Fail) ou '~all' (SoftFail) no final do registro SPF.");
            }
            res.put("avisos", avisos);
        }
        return res;
    }

    private int contarLookupsSpf(String spf) {
        int count = 0;
        String[] tokens = spf.split("\\s+");
        for (String t : tokens) {
            String lower = t.toLowerCase();
            if (lower.startsWith("include:") || lower.startsWith("a") || lower.startsWith("mx") || lower.startsWith("exists:") || lower.startsWith("redirect=")) {
                count++;
            }
        }
        return count;
    }

    private Map<String, Object> avaliarDmarc(String dominio) {
        Map<String, Object> res = new HashMap<>();
        String dmarcHost = "_dmarc." + dominio;
        String dmarcRecord = buscarRecordTxt(dmarcHost, "v=DMARC1");

        res.put("encontrado", dmarcRecord != null);
        res.put("raw", dmarcRecord != null ? dmarcRecord : "Nenhum registro DMARC encontrado em " + dmarcHost);

        if (dmarcRecord != null) {
            String politica = "none";
            if (dmarcRecord.contains("p=quarantine")) politica = "quarantine";
            else if (dmarcRecord.contains("p=reject")) politica = "reject";

            res.put("politica", politica);

            List<String> avisos = new ArrayList<>();
            if ("none".equals(politica)) {
                avisos.add("A política está como 'p=none' (Apenas monitoramento). O DMARC não bloqueará e-mails forjados.");
            }
            if (!dmarcRecord.contains("rua=")) {
                avisos.add("Recomendado incluir a tag 'rua=mailto:...' para receber relatórios de agregação DMARC.");
            }
            res.put("avisos", avisos);
        }
        return res;
    }

    private String buscarRecordTxt(String target, String prefixo) {
        try {
            Lookup lookup = new Lookup(target, Type.TXT);
            lookup.run();
            if (lookup.getResult() == Lookup.SUCCESSFUL) {
                Record[] records = lookup.getAnswers();
                if (records != null) {
                    for (Record r : records) {
                        TXTRecord txt = (TXTRecord) r;
                        String txtVal = String.join("", txt.getStrings());
                        if (txtVal.toLowerCase().contains(prefixo.toLowerCase())) {
                            return txtVal;
                        }
                    }
                }
            }
        } catch (Exception ignored) {}
        return null;
    }
}
