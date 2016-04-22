package org.cbioportal.pdb_annotation.service.internal;

import org.cbioportal.pdb_annotation.domain.*;
import org.cbioportal.pdb_annotation.service.PdbDataImportService;
import org.cbioportal.pdb_annotation.util.FileIO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.*;

import org.biojava.nbio.structure.*;
import org.biojava.nbio.structure.align.util.AtomCache;
import org.biojava.nbio.structure.io.FileParsingParameters;
import org.biojava.nbio.core.sequence.compound.*;
import org.biojava.nbio.core.sequence.loader.UniprotProxySequenceReader;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

/**
 *
 * @author jgao
 * @author Selcuk Onur Sumer
 */
@Component("residueMappingFromSifts")
@Service
public class PdbUniprotResidueMappingImportFromSiftsService implements PdbDataImportService
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

    private String pdbCacheDir;
    @Value("${pdb.cache_dir}")
    public void setPdbCacheDir(String pdbCacheDir)
    {
        this.pdbCacheDir = pdbCacheDir;
    }

    private String humanChainsURI;
    @Value("${pdb.human_chains_uri}")
    public void setHumanChainsURI(String humanChainsURI)
    {
        this.humanChainsURI = humanChainsURI;
    }

    private PdbUniprotResidueMappingRepository pdbUniprotResidueMappingRepository;
    private PdbUniprotAlignmentRepository pdbUniprotAlignmentRepository;
    private UniprotIdMappingRepository uniprotIdMappingRepository;

    @Autowired
    public PdbUniprotResidueMappingImportFromSiftsService(
        PdbUniprotResidueMappingRepository pdbUniprotResidueMappingRepository,
        PdbUniprotAlignmentRepository pdbUniprotAlignmentRepository,
        UniprotIdMappingRepository uniprotIdMappingRepository)
    {
        this.pdbUniprotResidueMappingRepository = pdbUniprotResidueMappingRepository;
        this.pdbUniprotAlignmentRepository = pdbUniprotAlignmentRepository;
        this.uniprotIdMappingRepository = uniprotIdMappingRepository;
    }

    public void importData()
    {
        try
        {
            importData(residueMappingURI,
                       readHumanChains(humanChainsURI),
                       pdbCacheDir,
                       identpThreshold);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }


    // TODO improve/optimize if necessary
    public void importData(String resourceURI, Set<String> humanChains,
        String pdbCacheDir, double identp_threhold)
        throws IOException
    {
        //MySQLbulkLoader.bulkLoadOn();
        BufferedReader buf = new BufferedReader(FileIO.getReader(resourceURI));
        //int alignId = DaoPdbUniprotResidueMapping.getLargestAlignmentId();
        Long alignId = pdbUniprotResidueMappingRepository.getMaxId();
        String line = buf.readLine();
        while (line.startsWith("#")) {
            line = buf.readLine();
        }


        AtomCache atomCache = getAtomCache(pdbCacheDir);

        buf.readLine(); // skip head

        for (; line != null; line = buf.readLine()) {
            //ProgressMonitor.incrementCurValue();
            //ConsoleUtil.showProgress();

            String[] parts = line.split("\t");
            String pdbId = parts[0];
            String chainId = parts[1];

            if (!humanChains.contains(pdbId+"."+chainId)) {
                continue;
            }

            System.out.println("processing "+line);

            String uniprotAcc = parts[2];

            int pdbSeqResBeg = Integer.parseInt(parts[3]);
            int pdbSeqResEnd = Integer.parseInt(parts[4]);
            int uniprotResBeg = Integer.parseInt(parts[7]);
            int uniprotResEnd = Integer.parseInt(parts[8]);

            if (pdbSeqResBeg-pdbSeqResEnd != uniprotResBeg-uniprotResEnd) {
                System.err.println("*** Lengths not equal");
                continue;
            }

//            String pdbAtomResBeg = parts[5]; // could have insertion code
//            String pdbAtomResEnd = parts[6]; // could have insertion code


            PdbUniprotAlignment pdbUniprotAlignment = new PdbUniprotAlignment(Integer.MIN_VALUE);
            List<PdbUniprotResidueMapping> pdbUniprotResidueMappings = new ArrayList<PdbUniprotResidueMapping>();

            if (processPdbUniprotAlignment(pdbUniprotAlignment, pdbUniprotResidueMappings,
                                           ++alignId, pdbId, chainId, uniprotAcc, uniprotResBeg,
                                           uniprotResEnd, pdbSeqResBeg, pdbSeqResEnd, identp_threhold, atomCache)) {
                pdbUniprotAlignmentRepository.save(pdbUniprotAlignment);
                for (PdbUniprotResidueMapping mapping : pdbUniprotResidueMappings) {
                    pdbUniprotResidueMappingRepository.save(mapping);
                }
            }
        }

        buf.close();
        //  Flush database
//        if (MySQLbulkLoader.isBulkLoad()) {
//            MySQLbulkLoader.flushAll();
//        }
    }

    private boolean processPdbUniprotAlignment(
        PdbUniprotAlignment pdbUniprotAlignment, List<PdbUniprotResidueMapping> pdbUniprotResidueMappings,
        Long alignId, String pdbId, String chainId, String uniprotAcc, int uniprotResBeg,
        int uniprotResEnd, int pdbSeqResBeg, int pdbSeqResEnd, double identp_threhold, AtomCache atomCache) {

        String uniprotSeq = getUniprotSequence(uniprotAcc, uniprotResBeg, uniprotResEnd);
        if (uniprotSeq==null) {
            System.err.println("Could not read UniProt Sequence");
            return false;
        }

        List<Group>pdbResidues = getPdbResidues(atomCache, pdbId, chainId, pdbSeqResBeg, pdbSeqResEnd);

        int len = uniprotResEnd-uniprotResBeg+1;

        if (pdbResidues.size()!=len) {
            System.err.println("*** Lengths not correct from structure");
            return false;
        }

        int start = 0;
        for (; start<len; start++) {
            if (pdbResidues.get(start).getResidueNumber()!=null) {
                break;
            }
        }

        if (start==len) {
            System.err.print("No atom residues");
            return false;
        }

        int end;
        for (end=len; end>start; end--) {
            if (pdbResidues.get(end-1).getResidueNumber()!=null) {
                break;
            }
        }

        int identity = 0;
        StringBuilder midline = new StringBuilder();
        StringBuilder pdbAlign = new StringBuilder();
        for (int i=start; i<end; i++) {
            Group pdbResidue = pdbResidues.get(i);
            if (!(pdbResidue instanceof AminoAcid)) {
                System.err.println("*** Non amino acid");
                return false;
            }

            ResidueNumber rn = pdbResidue.getResidueNumber();

            char pdbAA = ((AminoAcid)pdbResidue).getAminoType();
            char uniprotAA = uniprotSeq.charAt(i);
            char match = ' ';

            if (rn==null) { // if not a atom residue
                match = '-';
            } else if (pdbAA == uniprotAA) {
                identity++;
                match = pdbAA;
            }

            midline.append(match);
            pdbAlign.append(match);

            if (rn!=null) {
                PdbUniprotResidueMapping pdbUniprotResidueMapping =
                    new PdbUniprotResidueMapping(alignId,
                                                 rn.getSeqNum().longValue(),
                                                 rn.getInsCode()==null?null:rn.getInsCode().toString(),
                                                 uniprotResBeg+i, ""+match);
                pdbUniprotResidueMappings.add(pdbUniprotResidueMapping);
            }
        }

        double identp = identity*100.0/(end-start);

        if (identp < identp_threhold) {
            System.out.print("*** low identp: "+identp);
            return false;
        }

        pdbUniprotAlignment.setAlignmentId(alignId);

        pdbUniprotAlignment.setPdbId(pdbId);
        pdbUniprotAlignment.setChain(chainId);

        String uniprotId = this.mapFromUniprotAccessionToUniprotId(uniprotAcc);
        if (uniprotId==null) {
            System.out.println("could not mapping uniprotacc " + uniprotAcc);
            return false;
        }
        pdbUniprotAlignment.setUniprotId(uniprotId);

        ResidueNumber startRes = pdbResidues.get(start).getResidueNumber();
        ResidueNumber endRes = pdbResidues.get(end-1).getResidueNumber();
        pdbUniprotAlignment.setPdbFrom(Integer.toString(startRes.getSeqNum())
                                       +(startRes.getInsCode()==null?"":startRes.getInsCode()));
        pdbUniprotAlignment.setPdbTo(Integer.toString(endRes.getSeqNum())
                                     +(endRes.getInsCode()==null?"":endRes.getInsCode()));
        pdbUniprotAlignment.setUniprotFrom(uniprotResBeg+start);
        pdbUniprotAlignment.setUniprotTo(uniprotResBeg+end-1);
//        pdbUniprotAlignment.setEValue(null);
        pdbUniprotAlignment.setUniprotAlign(uniprotSeq.substring(start, end));

        pdbUniprotAlignment.setIdentity((float)identity);
        pdbUniprotAlignment.setIdentityP((float)(identp));
        pdbUniprotAlignment.setPdbAlign(pdbAlign.toString());
        pdbUniprotAlignment.setMidlineAlign(midline.toString());

        return true;
    }

    private String mapFromUniprotAccessionToUniprotId(String uniprotAcc)
    {
        List<UniprotIdMapping> list = this.uniprotIdMappingRepository.findByUniprotAcc(uniprotAcc);

        if (list.iterator().hasNext())
        {
            return list.iterator().next().getUniprotId();
        }

        return null;
    }

    private static AtomCache getAtomCache(String dirCache) {
        AtomCache atomCache = new AtomCache(dirCache, true);
        FileParsingParameters params = new FileParsingParameters();
        params.setAlignSeqRes(true);
        params.setParseSecStruc(false);
        params.setUpdateRemediatedFiles(false);
        atomCache.setFileParsingParams(params);
        atomCache.setAutoFetch(true);
        return atomCache;
    }

    private static List<Group> getPdbResidues(AtomCache atomCache, String pdbId, String chainId, int start, int end) {
        try {
            Structure struc = atomCache.getStructure(pdbId);

            if (struc!=null) {
                Chain chain = struc.getChainByPDB(chainId);
                return chain.getSeqResGroups().subList(start-1, end);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return Collections.emptyList();
    }

    private static String getUniprotSequence(String uniportAcc, int start, int end) {
        try {
            UniprotProxySequenceReader<AminoAcidCompound> uniprotSequence
                = new UniprotProxySequenceReader<AminoAcidCompound>(uniportAcc, AminoAcidCompoundSet.getAminoAcidCompoundSet());
            return uniprotSequence.getSequenceAsString().substring(start-1, end);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static Set<String> readHumanChains(String resourceURI) throws IOException {
        Set<String> humanChains = new HashSet<String>();
        BufferedReader buf = new BufferedReader(FileIO.getReader(resourceURI));
        for (String line = buf.readLine(); line != null; line = buf.readLine()) {
            String[] parts = line.split("\t");
            humanChains.add(parts[0]+"."+parts[1]);
        }
        buf.close();
        return humanChains;
    }
}
