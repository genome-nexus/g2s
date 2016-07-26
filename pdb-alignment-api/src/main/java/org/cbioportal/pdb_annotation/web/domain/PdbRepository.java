package org.cbioportal.pdb_annotation.web.domain;

import javax.transaction.Transactional;

import org.cbioportal.pdb_annotation.web.models.Alignment;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 
 * @author Juexin Wang
 *
 */
@Transactional
public interface PdbRepository extends JpaRepository<Alignment, Long>{

}
