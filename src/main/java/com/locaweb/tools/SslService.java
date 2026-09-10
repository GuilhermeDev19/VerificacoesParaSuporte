package com.locaweb.tools;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class SslService {

    public Map<String, String> gerarCsrEKey(String domain, String org, String ou, String city, String state, String country) {
        Map<String, String> result = new HashMap<>();
        String id = UUID.randomUUID().toString();
        File keyFile = new File("/tmp/key_" + id + ".key");
        File csrFile = new File("/tmp/csr_" + id + ".csr");

        try {
            String subj = String.format("/C=%s/ST=%s/L=%s/O=%s/OU=%s/CN=%s",
                    country == null || country.isEmpty() ? "BR" : country,
                    state == null || state.isEmpty() ? "SP" : state,
                    city == null || city.isEmpty() ? "Sao Paulo" : city,
                    org == null || org.isEmpty() ? "Empresa" : org,
                    ou == null || ou.isEmpty() ? "TI" : ou,
                    domain);

            ProcessBuilder pb = new ProcessBuilder(
                    "openssl", "req", "-new", "-newkey", "rsa:2048", "-nodes",
                    "-keyout", keyFile.getAbsolutePath(),
                    "-out", csrFile.getAbsolutePath(),
                    "-subj", subj
            );
            Process p = pb.start();
            p.waitFor();

            if (keyFile.exists() && csrFile.exists()) {
                result.put("key", Files.readString(keyFile.toPath()));
                result.put("csr", Files.readString(csrFile.toPath()));
            } else {
                result.put("erro", "Falha ao gerar CSR/Chave com OpenSSL.");
            }
        } catch (Exception e) {
            result.put("erro", "Erro na execução do OpenSSL: " + e.getMessage());
        } finally {
            if (keyFile.exists()) keyFile.delete();
            if (csrFile.exists()) csrFile.delete();
        }
        return result;
    }

    public byte[] criarPfx(MultipartFile crtFile, MultipartFile keyFile, String password) throws Exception {
        String id = UUID.randomUUID().toString();
        File crt = new File("/tmp/crt_" + id + ".crt");
        File key = new File("/tmp/key_" + id + ".key");
        File pfx = new File("/tmp/pfx_" + id + ".pfx");

        try {
            crtFile.transferTo(crt);
            keyFile.transferTo(key);

            ProcessBuilder pb = new ProcessBuilder(
                    "openssl", "pkcs12", "-export",
                    "-in", crt.getAbsolutePath(),
                    "-inkey", key.getAbsolutePath(),
                    "-out", pfx.getAbsolutePath(),
                    "-passout", "pass:" + (password != null ? password : "")
            );
            Process p = pb.start();
            p.waitFor();

            if (pfx.exists()) {
                return Files.readAllBytes(pfx.toPath());
            } else {
                throw new RuntimeException("Falha ao gerar PFX. Certifique-se de que a Chave Privada e o Certificado correspondem.");
            }
        } finally {
            if (crt.exists()) crt.delete();
            if (key.exists()) key.delete();
            if (pfx.exists()) pfx.delete();
        }
    }

    public Map<String, String> extrairPfx(MultipartFile pfxFile, String password) {
        Map<String, String> result = new HashMap<>();
        String id = UUID.randomUUID().toString();
        File pfx = new File("/tmp/in_" + id + ".pfx");
        File crt = new File("/tmp/out_" + id + ".crt");
        File key = new File("/tmp/out_" + id + ".key");

        try {
            pfxFile.transferTo(pfx);

            ProcessBuilder pbCrt = new ProcessBuilder(
                    "openssl", "pkcs12", "-in", pfx.getAbsolutePath(),
                    "-clcerts", "-nokeys", "-out", crt.getAbsolutePath(),
                    "-passin", "pass:" + (password != null ? password : "")
            );
            pbCrt.start().waitFor();

            ProcessBuilder pbKey = new ProcessBuilder(
                    "openssl", "pkcs12", "-in", pfx.getAbsolutePath(),
                    "-nocerts", "-nodes", "-out", key.getAbsolutePath(),
                    "-passin", "pass:" + (password != null ? password : "")
            );
            pbKey.start().waitFor();

            if (crt.exists() && key.exists()) {
                result.put("crt", Files.readString(crt.toPath()));
                result.put("key", Files.readString(key.toPath()));
            } else {
                result.put("erro", "Erro ao extrair PFX. Verifique se a senha do PFX está correta.");
            }
        } catch (Exception e) {
            result.put("erro", "Erro no processamento do PFX: " + e.getMessage());
        } finally {
            if (pfx.exists()) pfx.delete();
            if (crt.exists()) crt.delete();
            if (key.exists()) key.delete();
        }
        return result;
    }
}
