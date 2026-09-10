package com.locaweb.tools;

import org.springframework.stereotype.Service;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Record;
import org.xbill.DNS.Type;

import java.net.InetAddress;
import java.util.*;
import java.util.concurrent.*;

@Service
public class BlacklistService {

    private static final List<String> RBL_LIST = Arrays.asList(
            "zen.spamhaus.org",
            "bl.spamcop.net",
            "b.barracudacentral.org",
            "dnsbl.sorbs.net",
            "spam.dnsbl.sorbs.net",
            "cbl.abuseat.org",
            "psbl.surriel.com",
            "ubl.unsubscore.com"
    );

    public Map<String, Object> checarBlacklist(String input) {
        Map<String, Object> resultado = new HashMap<>();
        String ipTarget = resolverParaIp(input.trim());

        if (ipTarget == null) {
            resultado.put("erro", "Não foi possível resolver o IP para o host/domínio informado.");
            return resultado;
        }

        resultado.put("target", input);
        resultado.put("ipTarget", ipTarget);

        String[] octetos = ipTarget.split("\\.");
        if (octetos.length != 4) {
            resultado.put("erro", "O endereço resolvido não é um IPv4 válido.");
            return resultado;
        }

        String ipReverso = octetos[3] + "." + octetos[2] + "." + octetos[1] + "." + octetos[0];

        ExecutorService executor = Executors.newFixedThreadPool(RBL_LIST.size());
        List<Future<Map<String, Object>>> futures = new ArrayList<>();

        for (String rbl : RBL_LIST) {
            futures.add(executor.submit(() -> consultarRbl(ipReverso, rbl)));
        }

        List<Map<String, Object>> rblResultados = new ArrayList<>();
        int listadoCount = 0;

        for (Future<Map<String, Object>> future : futures) {
            try {
                Map<String, Object> res = future.get(3, TimeUnit.SECONDS);
                rblResultados.add(res);
                if ((Boolean) res.get("listado")) {
                    listadoCount++;
                }
            } catch (Exception e) {
                // Timeout ou erro de busca
            }
        }

        executor.shutdown();

        resultado.put("totalConsultados", rblResultados.size());
        resultado.put("totalListado", listadoCount);
        resultado.put("limpo", listadoCount == 0);
        resultado.put("detalhes", rblResultados);

        return resultado;
    }

    private String resolverParaIp(String input) {
        try {
            if (input.matches("^[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}$")) {
                return input;
            }
            InetAddress address = InetAddress.getByName(input);
            return address.getHostAddress();
        } catch (Exception e) {
            return null;
        }
    }

    private Map<String, Object> consultarRbl(String ipReverso, String rblHost) {
        Map<String, Object> res = new HashMap<>();
        res.put("rbl", rblHost);

        String query = ipReverso + "." + rblHost;
        try {
            Lookup lookup = new Lookup(query, Type.A);
            lookup.run();

            if (lookup.getResult() == Lookup.SUCCESSFUL) {
                Record[] records = lookup.getAnswers();
                res.put("listado", true);
                res.put("resposta", records != null && records.length > 0 ? records[0].rdataToString() : "Listado");
            } else {
                res.put("listado", false);
                res.put("resposta", "Limpo / Não Listado");
            }
        } catch (Exception e) {
            res.put("listado", false);
            res.put("resposta", "Falha na consulta");
        }
        return res;
    }
}
