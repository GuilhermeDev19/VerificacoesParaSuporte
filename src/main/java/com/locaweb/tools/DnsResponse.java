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

    public DnsResponse() {}

    public String getDomain() { return domain; }
    public void setDomain(String domain) { this.domain = domain; }

    public List<String> getaRecords() { return aRecords; }
    public void setaRecords(List<String> aRecords) { this.aRecords = aRecords; }

    public List<String> getAaaaRecords() { return aaaaRecords; }
    public void setAaaaRecords(List<String> aaaaRecords) { this.aaaaRecords = aaaaRecords; }

    public List<String> getCnameRecords() { return cnameRecords; }
    public void setCnameRecords(List<String> cnameRecords) { this.cnameRecords = cnameRecords; }

    public List<String> getMxRecords() { return mxRecords; }
    public void setMxRecords(List<String> mxRecords) { this.mxRecords = mxRecords; }

    public List<String> getTxtRecords() { return txtRecords; }
    public void setTxtRecords(List<String> txtRecords) { this.txtRecords = txtRecords; }

    public List<String> getNsRecords() { return nsRecords; }
    public void setNsRecords(List<String> nsRecords) { this.nsRecords = nsRecords; }

    public String getAmbienteDetectado() { return ambienteDetectado; }
    public void setAmbienteDetectado(String ambienteDetectado) { this.ambienteDetectado = ambienteDetectado; }

    public String getServerHeader() { return serverHeader; }
    public void setServerHeader(String serverHeader) { this.serverHeader = serverHeader; }
}
