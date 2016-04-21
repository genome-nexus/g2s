package org.cbioportal.pdb_annotation.service.internal;

import org.cbioportal.pdb_annotation.domain.PdbUniprotAlignment;
import org.cbioportal.pdb_annotation.domain.PdbUniprotAlignmentRepository;
import org.cbioportal.pdb_annotation.domain.PdbUniprotResidueMapping;
import org.cbioportal.pdb_annotation.domain.PdbUniprotResidueMappingRepository;
import org.cbioportal.pdb_annotation.service.PdbDataImportService;
import org.cbioportal.pdb_annotation.util.FileIO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.*;

/**
 *
 * @author jgao
 * @author Selcuk Onur Sumer
 */
@Service
public class PdbUniprotResidueMappingImportFromMAService implements PdbDataImportService
{
    private String residueMappingURI;
    @Value("${pdb.uniprot_residue_mapping_uri}")
    public void setResidueMappingURI(String residueMappingURI)
    {
        this.residueMappingURI = residueMappingURI;
    }

    private Double identpThreshold;
    @Value("${pdb.identp_threshold}")
    public void setIdentpThreshold(Double identpThreshold)
    {
        this.identpThreshold = identpThreshold;
    }

    private PdbUniprotResidueMappingRepository pdbUniprotResidueMappingRepository;
    private PdbUniprotAlignmentRepository pdbUniprotAlignmentRepository;

    @Autowired
    public PdbUniprotResidueMappingImportFromMAService(
        PdbUniprotResidueMappingRepository pdbUniprotResidueMappingRepository,
        PdbUniprotAlignmentRepository pdbUniprotAlignmentRepository)
    {
        this.pdbUniprotResidueMappingRepository = pdbUniprotResidueMappingRepository;
        this.pdbUniprotAlignmentRepository = pdbUniprotAlignmentRepository;
    }

    @Override
    public void importData()
    {
        try
        {
            importData(residueMappingURI, identpThreshold);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    // TODO improve/optimize if necessary
    private void importData(String resourceURI, double identpThreshold) throws IOException
    {
        BufferedReader buf = new BufferedReader(FileIO.getReader(resourceURI));
        String line = buf.readLine();
        Long alignId = pdbUniprotResidueMappingRepository.getMaxId();

        if (alignId == null) {
            alignId = 0L;
        }

        PdbUniprotAlignment pdbUniprotAlignment = new PdbUniprotAlignment(Integer.MIN_VALUE);
        List<PdbUniprotResidueMapping> pdbUniprotResidueMappings = Collections.emptyList();
        Map<Integer, Integer> mappingUniPdbProtein = Collections.emptyMap();
        Map<Integer, Integer> mappingUniPdbAlignment = Collections.emptyMap();
        Map<Integer, Integer> mappingPdbUniProtein = Collections.emptyMap();
        Map<Integer, Integer> mappingPdbUniAlignment = Collections.emptyMap();

        while (line != null) {
            if (!line.startsWith("#")) {
                String parts[] = line.split("\t",-1);
                if (line.startsWith(">")) {
                    // alignment line, eg. >1a37   A       1433B_HUMAN     1       32      3       34      0.000000        29.000000       90.625000       MDKSELVQKAKLAEQAERYDDMAAAMKAVTEQ        MDKNELVQKAKLAEQAERYDDMAACMKSVTEQ        MDK+ELVQKAKLAEQAERYDDMAA MK+VTEQ

                    if (!pdbUniprotResidueMappings.isEmpty()) {
                        if (pdbUniprotAlignment.getIdentityP() >= identpThreshold) {
                            pdbUniprotAlignmentRepository.save(pdbUniprotAlignment);
                            for (PdbUniprotResidueMapping mapping : pdbUniprotResidueMappings) {
                                pdbUniprotResidueMappingRepository.save(mapping);
                            }
                            mappingUniPdbProtein.putAll(mappingUniPdbAlignment);
                            mappingPdbUniProtein.putAll(mappingPdbUniAlignment);
                        }
                    }

                    String pdbId = parts[0].substring(1);
                    if (!pdbId.equals(pdbUniprotAlignment.getPdbId())
                        || !parts[1].equals(pdbUniprotAlignment.getChain())
                        || !parts[2].equals(pdbUniprotAlignment.getUniprotId())) {
                        mappingUniPdbProtein = new HashMap<Integer, Integer>();
                        mappingPdbUniProtein = new HashMap<Integer, Integer>();
                    }

                    pdbUniprotAlignment.setAlignmentId(++alignId);

                    pdbUniprotAlignment.setPdbId(pdbId);
                    pdbUniprotAlignment.setChain(parts[1]);
                    pdbUniprotAlignment.setUniprotId(parts[2]);

                    pdbUniprotAlignment.setPdbFrom(parts[3]);
                    pdbUniprotAlignment.setPdbTo(parts[4]);
                    pdbUniprotAlignment.setUniprotFrom(Integer.parseInt(parts[5]));
                    pdbUniprotAlignment.setUniprotTo(Integer.parseInt(parts[6]));
                    pdbUniprotAlignment.seteValue(Float.parseFloat(parts[7]));
                    pdbUniprotAlignment.setIdentity(Float.parseFloat(parts[8]));
                    pdbUniprotAlignment.setIdentityP(Float.parseFloat(parts[9]));
                    pdbUniprotAlignment.setUniprotAlign(parts[10]);
                    pdbUniprotAlignment.setPdbAlign(parts[11]);
                    pdbUniprotAlignment.setMidlineAlign(parts[12]);

                    pdbUniprotResidueMappings = new ArrayList<PdbUniprotResidueMapping>();
                    mappingUniPdbAlignment = new HashMap<Integer, Integer>();
                    mappingPdbUniAlignment = new HashMap<Integer, Integer>();

                } else {
                    // residue mapping line, e.g. 1a37    A       M1      1433B_HUMAN     M3      M
                    int pdbPos = Integer.parseInt(parts[2].substring(1));
                    int uniprotPos = Integer.parseInt(parts[4].substring(1));
                    Integer prePdb = mappingUniPdbProtein.get(uniprotPos);
                    Integer preUni = mappingPdbUniProtein.get(pdbPos);
                    if ((prePdb!=null && prePdb!=pdbPos) || (preUni!=null && preUni!=uniprotPos)) {
                        // mismatch
                        pdbUniprotResidueMappings.clear();
                        while (line !=null && !line.startsWith(">")) {
                            line = buf.readLine();
                            //ProgressMonitor.incrementCurValue();
                            //ConsoleUtil.showProgress();
                        }
                        continue;
                    }

                    mappingUniPdbAlignment.put(uniprotPos, pdbPos);
                    mappingPdbUniAlignment.put(pdbPos, uniprotPos);

                    String match = parts[5].length()==0 ? " " : parts[5];
                    PdbUniprotResidueMapping pdbUniprotResidueMapping =
                        new PdbUniprotResidueMapping(alignId, (long)pdbPos, null, uniprotPos, match);
                    pdbUniprotResidueMappings.add(pdbUniprotResidueMapping);
                }

            }

            line = buf.readLine();

            //ProgressMonitor.incrementCurValue();
            //ConsoleUtil.showProgress();
        }

        // last one
        if (!pdbUniprotResidueMappings.isEmpty()) {
            if (pdbUniprotAlignment.getIdentityP() >= identpThreshold) {
                pdbUniprotAlignmentRepository.save(pdbUniprotAlignment);
                for (PdbUniprotResidueMapping mapping : pdbUniprotResidueMappings) {
                    pdbUniprotResidueMappingRepository.save(mapping);
                }
            }
        }

        buf.close();
    }
}
