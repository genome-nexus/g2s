--
-- database `pdb`
--
drop table IF EXISTS pdb_ensembl_alignment;
drop table IF EXISTS ensembl_entry;
drop table IF EXISTS pdb_entry;

CREATE TABLE `ensembl_entry` (
    `ensemblid` VARCHAR(20) NOT NULL,
    `ensemblgene` VARCHAR(20),
    `ensembltranscript` VARCHAR(20),
    `entrezid` int(255),
    PRIMARY KEY(`ensemblid`)
    -- FOREIGN KEY(`ENTREZID`) REFERENCES `gene` (`ENTREZ_GENE_ID`)
);
CREATE TABLE `pdb_entry` (
    `pdbno` VARCHAR(9) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
    `pdbid` VARCHAR(4) NOT NULL,
    `chain` VARCHAR(4) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
    `dbref` VARCHAR(255),
    PRIMARY KEY(`pdbno`)
);
CREATE TABLE `pdb_ensembl_alignment` (
  `alignmentid` int NOT NULL AUTO_INCREMENT,
  `pdbno` VARCHAR(9) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `pdbid` VARCHAR(4) NOT NULL,
  `chain` VARCHAR(4) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `ensemblid` VARCHAR(20) NOT NULL,
  `pdbfrom` int NOT NULL,
  `pdbto` int NOT NULL,
  `ensemblfrom` int NOT NULL,
  `ensemblto` int NOT NULL,
  `evalue` VARCHAR(10),
  `bitscore` float,
  `identity` float,
  `identp` float,
  `ensemblalign` text,
  `pdbalign` text,
  `midlinealign` text,
  PRIMARY KEY (`alignmentid`),
  KEY(`ensemblid`),
  KEY(`pdbno`),
  FOREIGN KEY(`pdbno`) REFERENCES `pdb_entry` (`pdbno`),
  FOREIGN KEY(`ensemblid`) REFERENCES `ensembl_entry` (`ensemblid`)
);

