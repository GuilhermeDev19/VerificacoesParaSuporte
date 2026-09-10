package com.locaweb.tools;

import org.springframework.stereotype.Service;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.*;

@Service
public class PortTesterService {

    private static final Map<Integer, String> PORTAS_PADRAO = new LinkedHashMap<>();

    static {
        PORTAS_PADRAO.put(25, "SMTP (Relay/Padrão)");
        PORTAS_PADRAO.put(587, "SMTP Submission (STARTTLS)");
        PORTAS_PADRAO.put(465, "SMTPS (SSL/TLS Direto - Locaweb)");
        PORTAS_PADRAO.put(110, "POP3");
        PORTAS_PADRAO.put(995, "POP3S (SSL)");
        PORTAS_PADRAO.put(143, "IMAP");
        PORTAS_PADRAO.put(993, "IMAPS (SSL)");
        PORTAS_PADRAO.put(80, "HTTP");
        PORTAS_PADRAO.put(443, "HTTPS");
    }

    public List<Map<String, Object>> testarPortasServidor(String host) {
        List<Map<String, Object>> resultados = new ArrayList<>();
        String targetHost = host.trim().replace("https://", "").replace("http://", "").split("/")[0];

        for (Map.Entry<Integer, String> entry : PORTAS_PADRAO.entrySet()) {
            int porta = entry.getKey();
            String descricao = entry.getValue();

            Map<String, Object> res = new HashMap<>();
            res.put("porta", porta);
            res.put("servico", descricao);

            long inicio = System.currentTimeMillis();
            try {
                if (porta == 465 || porta == 995 || porta == 993 || porta == 443) {
                    SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
                    try (SSLSocket socket = (SSLSocket) factory.createSocket()) {
                        socket.connect(new InetSocketAddress(targetHost, porta), 3000);
                        socket.startHandshake();
                        long fim = System.currentTimeMillis();
                        res.put("aberta", true);
                        res.put("latenciaMs", (fim - inicio));
                        res.put("sslValido", true);
                    }
                } else {
                    try (Socket socket = new Socket()) {
                        socket.connect(new InetSocketAddress(targetHost, porta), 3000);
                        long fim = System.currentTimeMillis();
                        res.put("aberta", true);
                        res.put("latenciaMs", (fim - inicio));
                    }
                }
            } catch (Exception e) {
                res.put("aberta", false);
                res.put("erro", e.getMessage());
            }
            resultados.add(res);
        }
        return resultados;
    }

    public Map<String, Object> testarSmtpHandshake(String host, int porta) {
        Map<String, Object> res = new HashMap<>();
        String targetHost = host.trim().replace("https://", "").replace("http://", "").split("/")[0];

        try {
            Socket socket;
            if (porta == 465) {
                SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
                SSLSocket sslSocket = (SSLSocket) factory.createSocket();
                sslSocket.connect(new InetSocketAddress(targetHost, porta), 5000);
                sslSocket.startHandshake();
                socket = sslSocket;
                res.put("modoConexao", "SSL/TLS Direto (Porta 465)");
            } else {
                socket = new Socket();
                socket.connect(new InetSocketAddress(targetHost, porta), 5000);
                res.put("modoConexao", "Plain / STARTTLS (Porta " + porta + ")");
            }

            socket.setSoTimeout(5000);
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);

            String banner = reader.readLine();
            res.put("banner", banner);

            writer.println("EHLO techtools.darkcreative.com.br");
            List<String> ehloRespostas = new ArrayList<>();
            String linha;
            while ((linha = reader.readLine()) != null) {
                ehloRespostas.add(linha);
                if (linha.startsWith("250 ")) break;
            }
            res.put("ehloRespostas", ehloRespostas);
            boolean suportaStartTls = ehloRespostas.stream().anyMatch(l -> l.toUpperCase().contains("STARTTLS"));
            res.put("suportaStartTls", suportaStartTls);
            res.put("sucesso", true);

            socket.close();
        } catch (Exception e) {
            res.put("sucesso", false);
            res.put("erro", "Falha na conexão SMTP/SSL: " + e.getMessage());
        }
        return res;
    }
}
