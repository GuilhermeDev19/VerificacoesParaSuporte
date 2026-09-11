package com.locaweb.tools;

import org.springframework.stereotype.Service;

import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.InitialDirContext;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

        WebInfo webInfo = obterInformacoesWeb(domain);

        response.setServerHeader(webInfo.serverHeader);
        response.setPoweredByHeader(webInfo.poweredByHeader);
        response.setServidorWeb(webInfo.servidorWeb);
        response.setVersaoServidorWeb(webInfo.versaoServidorWeb);
        response.setVersaoPhp(webInfo.versaoPhp);
        response.setSistemaOperacional(webInfo.sistemaOperacional);

        response.setAmbienteDetectado(
                classificarAmbiente(
                        webInfo,
                        aRecords
                )
        );

        return response;
    }

    private List<String> buscarRegistros(String domain, String tipo, String dnsServer) {
        List<String> resultados = new ArrayList<>();

        try {
            Hashtable<String, String> env = new Hashtable<>();
            env.put(
                    "java.naming.factory.initial",
                    "com.sun.jndi.dns.DnsContextFactory"
            );

            if (dnsServer != null
                    && !dnsServer.trim().isEmpty()
                    && !dnsServer.equalsIgnoreCase("default")
                    && !dnsServer.equals("Padrão do Sistema")) {

                env.put(
                        "java.naming.provider.url",
                        "dns://" + dnsServer
                );
            }

            InitialDirContext dirContext = new InitialDirContext(env);

            try {
                Attributes attrs =
                        dirContext.getAttributes(
                                domain,
                                new String[]{tipo}
                        );

                Attribute attr = attrs.get(tipo);

                if (attr != null) {
                    for (int i = 0; i < attr.size(); i++) {

                        String entry = (String) attr.get(i);

                        if (domain.startsWith("www.")
                                && tipo.equals("CNAME")) {

                            entry = "[www] " + entry;

                        } else if (domain.startsWith("email-locaweb.")) {

                            entry = "[email-locaweb] " + entry;

                        } else if (domain.startsWith("_dmarc.")) {

                            entry = "[_dmarc] " + entry;
                        }

                        resultados.add(entry);
                    }
                }

            } finally {
                dirContext.close();
            }

        } catch (Exception ignored) {
        }

        return resultados;
    }

    private WebInfo obterInformacoesWeb(String domain) {
        WebInfo info = consultarCabecalhos("https://" + domain);

        if (!info.temInformacaoUtil()) {
            info = consultarCabecalhos("http://" + domain);
        }

        return info;
    }

    private WebInfo consultarCabecalhos(String urlString) {
        WebInfo info = new WebInfo();

        try {
            URL url = new URL(urlString);

            HttpURLConnection conn =
                    (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("HEAD");
            conn.setConnectTimeout(3000);
            conn.setReadTimeout(3000);
            conn.setInstanceFollowRedirects(true);

            conn.setRequestProperty(
                    "User-Agent",
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64)"
            );

            info.serverHeader = conn.getHeaderField("Server");
            info.poweredByHeader = conn.getHeaderField("X-Powered-By");

            conn.disconnect();

            interpretarCabecalhos(info);

        } catch (Exception ignored) {
        }

        return info;
    }

    private void interpretarCabecalhos(WebInfo info) {
        String server = valorSeguro(info.serverHeader);
        String poweredBy = valorSeguro(info.poweredByHeader);

        String combined =
                (server + " " + poweredBy).toLowerCase(Locale.ROOT);

        // Windows / IIS
        if (combined.contains("microsoft-iis")
                || combined.contains("iis/")
                || combined.contains("asp.net")
                || combined.contains("win64")
                || combined.contains("win32")) {

            info.sistemaOperacional = "Windows";
        }

        // Apache
        Matcher apacheMatcher = Pattern
                .compile("(?i)\\bapache(?:/|\\s+)([0-9][0-9a-zA-Z.\\-]*)")
                .matcher(server);

        if (apacheMatcher.find()) {
            info.servidorWeb = "Apache";
            info.versaoServidorWeb = apacheMatcher.group(1);
        }

        // Nginx
        Matcher nginxMatcher = Pattern
                .compile("(?i)\\bnginx(?:/|\\s+)([0-9][0-9a-zA-Z.\\-]*)")
                .matcher(server);

        if (nginxMatcher.find()) {
            info.servidorWeb = "Nginx";
            info.versaoServidorWeb = nginxMatcher.group(1);
        }

        // Microsoft IIS
        Matcher iisMatcher = Pattern
                .compile("(?i)\\bmicrosoft-iis(?:/|\\s+)?([0-9][0-9a-zA-Z.\\-]*)?")
                .matcher(server);

        if (iisMatcher.find()) {
            info.servidorWeb = "Microsoft IIS";

            if (iisMatcher.group(1) != null
                    && !iisMatcher.group(1).isBlank()) {

                info.versaoServidorWeb = iisMatcher.group(1);
            }
        }

        // PHP exposto em X-Powered-By
        Matcher phpMatcher = Pattern
                .compile("(?i)\\bphp(?:/|\\s+)([0-9]+(?:\\.[0-9]+){0,2})")
                .matcher(poweredBy);

        if (!phpMatcher.find()) {
            phpMatcher = Pattern
                    .compile("(?i)\\bphp(?:/|\\s+)([0-9]+(?:\\.[0-9]+){0,2})")
                    .matcher(server);
        }

        if (phpMatcher.find()) {
            info.versaoPhp = phpMatcher.group(1);
        }

        // Se não conseguiu detectar o sistema, usa algumas assinaturas seguras
        if (info.sistemaOperacional == null
                || info.sistemaOperacional.isBlank()) {

            if (combined.contains("rocky")
                    || combined.contains("centos")
                    || combined.contains("ubuntu")
                    || combined.contains("debian")
                    || combined.contains("linux")) {

                info.sistemaOperacional = "Linux";
            }
        }

        // Fallback do servidor web
        if (info.servidorWeb == null
                && server.contains("apache")) {

            info.servidorWeb = "Apache";
        }

        if (info.servidorWeb == null
                && server.contains("nginx")) {

            info.servidorWeb = "Nginx";
        }

        if (info.servidorWeb == null
                && server.contains("iis")) {

            info.servidorWeb = "Microsoft IIS";
        }
    }

    private String classificarAmbiente(
            WebInfo info,
            List<String> aRecords) {

        String serverHeader =
                valorSeguro(info.serverHeader).toLowerCase(Locale.ROOT);

        String poweredByHeader =
                valorSeguro(info.poweredByHeader).toLowerCase(Locale.ROOT);

        // 1. Windows / IIS tem prioridade
        if ("Windows".equalsIgnoreCase(info.sistemaOperacional)
                || "Microsoft IIS".equalsIgnoreCase(info.servidorWeb)
                || serverHeader.contains("microsoft-iis")) {

            String detalhes = montarDetalhes(info);

            return "Windows / IIS" + detalhes;
        }

        // 2. PHP acima de 8.0 = Dinesh
        if (versaoPhpMaiorQue(info.versaoPhp, 8, 0)) {
            return "Dinesh" + montarDetalhes(info);
        }

        // 3. Assinaturas conhecidas da Dinesh
        if (serverHeader.contains("apache/2.4.37")
                || serverHeader.contains("nginx/1.24")
                || serverHeader.contains("rocky")) {

            return "Dinesh" + montarDetalhes(info);
        }

        // 4. PHP 8.0 ou inferior + assinaturas conhecidas de HM
        if (temAssinaturaHm(serverHeader, poweredByHeader)) {

            return "HM" + montarDetalhes(info);
        }

        // 5. Mapeamento por IP como fallback
        if (aRecords != null && !aRecords.isEmpty()) {

            for (String record : aRecords) {

                String ip =
                        record
                                .replace("[www] ", "")
                                .trim();

                if (ip.equals("179.188.55.116")
                        || ip.startsWith("179.188.16.")
                        || ip.startsWith("179.188.15.")) {

                    return "Dinesh" + montarDetalhes(info);
                }

                if (ip.startsWith("179.188.54.")) {

                    return "HM" + montarDetalhes(info);
                }

                if (ip.startsWith("179.188.")
                        || ip.startsWith("186.202.")) {

                    return "Hospedagem Locaweb"
                            + montarDetalhes(info);
                }
            }
        }

        // 6. Se temos servidor, mas não conseguimos mapear hospedagem
        if (info.servidorWeb != null) {
            return "Servidor não identificado"
                    + montarDetalhes(info);
        }

        return "Indeterminado / Desconhecido";
    }

    private boolean temAssinaturaHm(
            String serverHeader,
            String poweredByHeader) {

        String combined =
                serverHeader + " " + poweredByHeader;

        return combined.contains("apache/2.2")
                || combined.contains("apache/2.4.6")
                || combined.contains("centos");
    }

    private boolean versaoPhpMaiorQue(
            String versao,
            int majorReferencia,
            int minorReferencia) {

        if (versao == null || versao.isBlank()) {
            return false;
        }

        try {
            String[] partes = versao.split("\\.");

            int major = Integer.parseInt(partes[0]);

            int minor =
                    partes.length > 1
                            ? Integer.parseInt(partes[1])
                            : 0;

            if (major != majorReferencia) {
                return major > majorReferencia;
            }

            return minor > minorReferencia;

        } catch (NumberFormatException e) {
            return false;
        }
    }

    private String montarDetalhes(WebInfo info) {
        List<String> detalhes = new ArrayList<>();

        if (info.servidorWeb != null) {
            if (info.versaoServidorWeb != null) {
                detalhes.add(
                        info.servidorWeb
                                + " "
                                + info.versaoServidorWeb
                );
            } else {
                detalhes.add(info.servidorWeb);
            }
        }

        if (info.versaoPhp != null) {
            detalhes.add("PHP " + info.versaoPhp);
        }

        if (info.sistemaOperacional != null) {
            detalhes.add(info.sistemaOperacional);
        }

        if (detalhes.isEmpty()) {
            return "";
        }

        return " (" + String.join(" - ", detalhes) + ")";
    }

    private String valorSeguro(String valor) {
        return valor != null ? valor.trim() : "";
    }

    private static class WebInfo {

        private String serverHeader;
        private String poweredByHeader;
        private String servidorWeb;
        private String versaoServidorWeb;
        private String versaoPhp;
        private String sistemaOperacional;

        private boolean temInformacaoUtil() {
            return (serverHeader != null
                    && !serverHeader.isBlank())
                    || (poweredByHeader != null
                    && !poweredByHeader.isBlank());
        }
    }
}
