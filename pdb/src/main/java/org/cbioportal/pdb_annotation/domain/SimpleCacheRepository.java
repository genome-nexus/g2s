package org.cbioportal.pdb_annotation.domain;

import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * @author Selcuk Onur Sumer
 */
public interface SimpleCacheRepository extends MongoRepository<SimpleCacheEntity, String> {}
