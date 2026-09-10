package com.locaweb.tools;

import org.springframework.stereotype.Service;
import javax.naming.directory.Attributes;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.Attribute;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

@Service
public class DnsService {

    public DnsResponse consultarDns(String domain, String dnsServer) {
        DnsResponse response = new DnsResponse();
        response.setDomain(domain);

        List<String> aRecords = buscarRegistros(domain, "A", dnsServer);
        List<String> wwwARecords = buscarRegistros("www." + domain, "A", dnsServer);
        for (String ip : wwwARecords) {
            if (!aRecords.contains(ip)) {
                aRecords.add("[www] " + ip);
            }
        }
        response.setaRecords(aRecords);

        response.setAaaaRecords(buscarRegistros(domain, "AAAA", dnsServer));

        List<String> cnameRecords = buscarRegistros(domain, "CNAME", dnsServer);
        cnameRecords.addAll(buscarRegistros("www." + domain, "CNAME", dnsServer));
        response.setCnameRecords(cnameRecords);

        response.setMxRecords(buscarRegistros(domain, "MX", dnsServer));

        List<String> txtRecords = buscarRegistros(domain, "TXT", dnsServer);
        txtRecords.addAll(buscarRegistros("email-locaweb." + domain, "TXT", dnsServer));
        txtRecords.addAll(buscarRegistros("_dmarc." + domain, "TXT", dnsServer));
        response.setTxtRecords(txtRecords);

        response.setNsRecords(buscarRegistros(domain, "NS", dnsServer));

        String serverHeader = obterServerHeader(domain);
        response.setServerHeader(serverHeader);
        response.setAmbienteDetectado(classificarAmbiente(serverHeader, aRecords));

        return response;
    }

    private List<String> buscarRegistros(String domain, String tipo, String dnsServer) {
        List<String> resultados = new ArrayList<>();
        try {
            Hashtable<String, String> env = new Hashtable<>();
            env.put("java.naming.factory.initial", "com.sun.jndi.dns.DnsContextFactory");

            if (dnsServer != null && !dnsServer.trim().isEmpty() && !dnsServer.equalsIgnoreCase("default") && !dnsServer.equals("Padrão do Sistema")) {
                env.put("java.naming.provider.url", "dns://" + dnsServer);
            }

            InitialDirContext dirContext = new InitialDirContext(env);
            Attributes attrs = dirContext.getAttributes(domain, new String[]{tipo});
            Attribute attr = attrs.get(tipo);

            if (attr != null) {
                for (int i = 0; i < attr.size(); i++) {
                    String entry = (String) attr.get(i);
                    if (domain.startsWith("www.") && tipo.equals("CNAME")) {
                        entry = "[www] " + entry;
                    } else if (domain.startsWith("email-locaweb.")) {
                        entry = "[email-locaweb] " + entry;
                    } else if (domain.startsWith("_dmarc.")) {
                        entry = "[_dmarc] " + entry;
                    }
                    resultados.add(entry);
                }
            }
        } catch (Exception ignored) {}
        return resultados;
    }

    private String obterServerHeader(String domain) {
        String header = fazerRequisicaoHead("http://" + domain);
        if (header == null || header.isEmpty()) {
            header = fazerRequisicaoHead("https://" + domain);
        }
        return header != null ? header : "Indisponível / Offline";
    }

    private String fazerRequisicaoHead(String urlString) {
        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("HEAD");
            conn.setConnectTimeout(3000);
            conn.setReadTimeout(3000);
            conn.setInstanceFollowRedirects(true);
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)");

            String server = conn.getHeaderField("Server");
            conn.disconnect();
            return server;
        } catch (Exception e) {
            return null;
        }
    }

    private String classificarAmbiente(String serverHeader, List<String> aRecords) {
        String headerLower = (serverHeader != null) ? serverHeader.toLowerCase() : "";

        // 1. Detecção por Assinatura de Servidor (Qualquer IP)
        if (headerLower.contains("cloudflare")) {
            return "Cloudflare (Proxy / IP Oculto)";
        } else if (headerLower.contains("apache/2.4.37") || headerLower.contains("apache/2.4.5") || headerLower.contains("nginx/1.24") || headerLower.contains("rocky")) {
            return "Dinesh (Rocky Linux 8 - Apache/Nginx)";
        } else if (headerLower.contains("apache/2.2") || headerLower.contains("apache/2.4.6") || headerLower.contains("centos")) {
            return "HM (CentOS - Apache 2.2/2.4.6)";
        }

        // 2. Detecção por Mapeamento de IP (Fallback)
        if (aRecords != null && !aRecords.isEmpty()) {
            for (String record : aRecords) {
                String ip = record.replace("[www] ", "").trim();

                if (ip.equals("179.188.55.116") || ip.startsWith("179.188.16.") || ip.startsWith("179.188.15.")) {
                    return "Dinesh (Hospedagem Rocky Linux 8)";
                } else if (ip.startsWith("179.188.54.")) {
                    return "HM (Hospedagem CentOS / Revenda)";
                } else if (ip.startsWith("179.188.") || ip.startsWith("186.202.")) {
                    return "Hospedagem Locaweb (" + (serverHeader != null && !serverHeader.equals("Indisponível / Offline") ? serverHeader : "IP " + ip) + ")";
                }
            }
        }

        if (serverHeader != null && !serverHeader.equals("Indisponível / Offline")) {
            return "Servidor Personalizado (" + serverHeader + ")";
        }

        return "Indeterminado / Desconhecido";
    }
}
