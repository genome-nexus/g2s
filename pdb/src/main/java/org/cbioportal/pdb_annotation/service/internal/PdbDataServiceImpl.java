package org.cbioportal.pdb_annotation.service.internal;

import org.cbioportal.pdb_annotation.domain.PdbHeader;
import org.cbioportal.pdb_annotation.service.PdbDataService;
import org.cbioportal.pdb_annotation.util.PdbFileParser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * @author Selcuk Onur Sumer
 */
@Service
public class PdbDataServiceImpl implements PdbDataService
{
    private String headerServiceURL;
    @Value("${pdb.header_service_url}")
    public void setHeaderServiceURL(String headerServiceURL)
    {
        this.headerServiceURL = headerServiceURL;
    }

    @Override
    public PdbHeader getPdbHeader(String pdbId)
    {
        PdbHeader info = null;

        // TODO implement a JSON cache! (mongo db)

        if (pdbId != null &&
            pdbId.length() > 0)
        {
            PdbFileParser pdbParser = new PdbFileParser();
            String rawData = this.getRawInfo(pdbId);

            if (rawData != null)
            {
                Map<String, String> content = pdbParser.parsePdbFile(rawData);

                info = new PdbHeader();

                info.setPdbId(pdbId);
                info.setTitle(pdbParser.parseTitle(content.get("title")));
                info.setCompound(pdbParser.parseCompound(content.get("compnd")));
                info.setSource(pdbParser.parseCompound(content.get("source")));
            }
        }

        return info;
    }

    public String getRawInfo(String pdbId)
    {
        //http://www.rcsb.org/pdb/files/PDB_ID.pdb?headerOnly=YES
        //http://files.rcsb.org/header/PDB_ID.pdb

        String uri = headerServiceURL.replace("PDB_ID", pdbId.toUpperCase());
        RestTemplate restTemplate = new RestTemplate();

        try {
            return restTemplate.getForObject(uri, String.class);
        } catch (Exception e) {
            //e.printStackTrace();
            return null;
        }
    }
}
