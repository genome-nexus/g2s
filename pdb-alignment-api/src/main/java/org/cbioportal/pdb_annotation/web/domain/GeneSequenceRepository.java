package org.cbioportal.pdb_annotation.web.domain;

import java.util.List;
import javax.transaction.Transactional;
import org.cbioportal.pdb_annotation.web.models.GeneSequence;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 
 * @author Juexin Wang
 *
 */
@Transactional
public interface GeneSequenceRepository extends JpaRepository<GeneSequence, Long> {
    public List<GeneSequence> findBySeqId(String seqId);
}
