package org.cbioportal.pdb_annotation.web.models.api;

import java.util.ArrayList;
import java.util.List;

import org.cbioportal.pdb_annotation.web.models.Ensembl;
import org.cbioportal.pdb_annotation.web.models.GenomeResidueInput;
import org.cbioportal.pdb_annotation.web.models.Residue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.web.client.RestTemplate;

@ConfigurationProperties
public class UtilAPI {
    @Value("${gn.api.url}")
    private String gnApiUrl;

    public String getGnApiUrl() {
        return gnApiUrl;
    }

    public void setGnApiUrl(String gnApiUrl) {
        this.gnApiUrl = gnApiUrl;
    }

    public List<GenomeResidueInput> callAPI(String chromosomeNum, long positionNum, String nucleotideType)
            throws Exception {
        ReadConfig rc = ReadConfig.getInstance();
        // System.out.println("ReadURL:\t"+rc.getGnApiUrl());
        List<GenomeResidueInput> outlist = new ArrayList<GenomeResidueInput>();

        String url = rc.getGnApiUrl();
        url = url.replace("CHROMSOME", chromosomeNum);
        url = url.replace("POSITION", Long.toString(positionNum));
        url = url.replace("ORIGINAL", nucleotideType);

        // Artificial mutation for API
        String mutation = "A";
        if (nucleotideType.equals("A")) {
            mutation = "T";
        }
        url = url.replace("MUTATION", mutation);
        //System.out.println("APIURL:\t"+url);

        RestTemplate restTemplate = new RestTemplate();

        Quote[] quotes = restTemplate.getForObject(url, Quote[].class);

        for (Quote quote : quotes) {
            List<Transcript_consequences> list = quote.getTranscript_consequences();
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).getBiotype().equals("protein_coding") && list.get(i).getProtein_start() != 0) {
                    GenomeResidueInput gr = new GenomeResidueInput();
                    Residue residue = new Residue();
                    residue.setResidueNum(list.get(i).getProtein_start());
                    gr.setResidue(residue);

                    Ensembl ensembl = new Ensembl();
                    ensembl.setEnsemblid(list.get(i).getProtein_id());// ENSP
                    ensembl.setEnsemblgene(list.get(i).getGene_id());// ENSG
                    ensembl.setEnsembltranscript(list.get(i).getTranscript_id());// ENST
                    gr.setEnsembl(ensembl);

                    outlist.add(gr);
                    // System.out.println(i+"th id/location:
                    // "+list.get(i).getProtein_id()+"\t"+list.get(i).getProtein_start());
                }
            }
        }

        return outlist;
    }

}
