package com.locaweb.tools;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class DnsResponse {

    private String domain;

    @JsonProperty("registrosA")
    private List<String> aRecords;

    @JsonProperty("registrosAaaa")
    private List<String> aaaaRecords;

    @JsonProperty("registrosCname")
    private List<String> cnameRecords;

    @JsonProperty("registrosMx")
    private List<String> mxRecords;

    @JsonProperty("registrosTxt")
    private List<String> txtRecords;

    @JsonProperty("registrosNs")
    private List<String> nsRecords;

    private String ambienteDetectado;
    private String serverHeader;
    private String poweredByHeader;
    private String servidorWeb;
    private String versaoServidorWeb;
    private String versaoPhp;
    private String sistemaOperacional;

    public DnsResponse() {}

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public List<String> getaRecords() {
        return aRecords;
    }

    public void setaRecords(List<String> aRecords) {
        this.aRecords = aRecords;
    }

    public List<String> getAaaaRecords() {
        return aaaaRecords;
    }

    public void setAaaaRecords(List<String> aaaaRecords) {
        this.aaaaRecords = aaaaRecords;
    }

    public List<String> getCnameRecords() {
        return cnameRecords;
    }

    public void setCnameRecords(List<String> cnameRecords) {
        this.cnameRecords = cnameRecords;
    }

    public List<String> getMxRecords() {
        return mxRecords;
    }

    public void setMxRecords(List<String> mxRecords) {
        this.mxRecords = mxRecords;
    }

    public List<String> getTxtRecords() {
        return txtRecords;
    }

    public void setTxtRecords(List<String> txtRecords) {
        this.txtRecords = txtRecords;
    }

    public List<String> getNsRecords() {
        return nsRecords;
    }

    public void setNsRecords(List<String> nsRecords) {
        this.nsRecords = nsRecords;
    }

    public String getAmbienteDetectado() {
        return ambienteDetectado;
    }

    public void setAmbienteDetectado(String ambienteDetectado) {
        this.ambienteDetectado = ambienteDetectado;
    }

    public String getServerHeader() {
        return serverHeader;
    }

    public void setServerHeader(String serverHeader) {
        this.serverHeader = serverHeader;
    }

    public String getPoweredByHeader() {
        return poweredByHeader;
    }

    public void setPoweredByHeader(String poweredByHeader) {
        this.poweredByHeader = poweredByHeader;
    }

    public String getServidorWeb() {
        return servidorWeb;
    }

    public void setServidorWeb(String servidorWeb) {
        this.servidorWeb = servidorWeb;
    }

    public String getVersaoServidorWeb() {
        return versaoServidorWeb;
    }

    public void setVersaoServidorWeb(String versaoServidorWeb) {
        this.versaoServidorWeb = versaoServidorWeb;
    }

    public String getVersaoPhp() {
        return versaoPhp;
    }

    public void setVersaoPhp(String versaoPhp) {
        this.versaoPhp = versaoPhp;
    }

    public String getSistemaOperacional() {
        return sistemaOperacional;
    }

    public void setSistemaOperacional(String sistemaOperacional) {
        this.sistemaOperacional = sistemaOperacional;
    }
}
