package org.cbioportal.pdb_annotation.web.domain;

import java.util.List;

import javax.transaction.Transactional;

import org.cbioportal.pdb_annotation.web.models.Ensembl;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 
 * @author Juexin Wang
 *
 */
@Transactional
public interface EnsemblRepository extends JpaRepository<Ensembl, Long> {
    // exact
    public List<Ensembl> findByEnsemblId(String ensemblId);

    // like
    public List<Ensembl> findByEnsemblIdStartingWith(String ensemblId);

    public List<Ensembl> findByEnsemblGene(String ensemblGene);

    public List<Ensembl> findByEnsemblGeneStartingWith(String ensemblGene);

    public List<Ensembl> findByEnsemblTranscript(String EnsemblTranscript);

    public List<Ensembl> findByEnsemblTranscriptStartingWith(String EnsemblTranscript);

}
