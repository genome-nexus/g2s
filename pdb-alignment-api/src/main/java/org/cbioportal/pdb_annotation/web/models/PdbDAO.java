package org.cbioportal.pdb_annotation.web.models;

import javax.transaction.Transactional;

import org.springframework.data.repository.CrudRepository;

@Transactional
public interface PdbDAO extends CrudRepository<Alignment, Long>{

}
