--
-- database `pdb`
--
drop table IF EXISTS pdb_ensembl_alignment;
drop table IF EXISTS ensembl_entry;
drop table IF EXISTS pdb_entry;

CREATE TABLE `ensembl_entry` (
    `ENSEMBL_ID` VARCHAR(20) NOT NULL,
    `ENSEMBL_GENE` VARCHAR(20),
    `ENSEMBL_TRANSCRIPT` VARCHAR(20),
    `ENTREZ_GENE_ID` int(255),
    PRIMARY KEY(`ENSEMBL_ID`)
    -- FOREIGN KEY(`ENTREZ_GENE_ID`) REFERENCES `gene` (`ENTREZ_GENE_ID`)
);
CREATE TABLE `pdb_entry` (
    `PDB_NO` VARCHAR(9) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
    `PDB_ID` VARCHAR(4) NOT NULL,
    `CHAIN` VARCHAR(4) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
    `DBREF` VARCHAR(255),
    PRIMARY KEY(`PDB_NO`)
);
CREATE TABLE `pdb_ensembl_alignment` (
  `ALIGNMENT_ID` int NOT NULL AUTO_INCREMENT,
  `PDB_NO` VARCHAR(9) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `PDB_ID` VARCHAR(4) NOT NULL,
  `CHAIN` VARCHAR(4) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `ENSEMBL_ID` VARCHAR(20) NOT NULL,
  `PDB_FROM` int NOT NULL,
  `PDB_TO` int NOT NULL,
  `ENSEMBL_FROM` int NOT NULL,
  `ENSEMBL_TO` int NOT NULL,
  `EVALUE` VARCHAR(10),
  `BITSCORE` float,
  `IDENTITY` float,
  `IDENTP` float,
  `ENSEMBL_ALIGN` text,
  `PDB_ALIGN` text,
  `MIDLINE_ALIGN` text,
  PRIMARY KEY (`ALIGNMENT_ID`),
  KEY(`ENSEMBL_ID`),
  KEY(`PDB_ID`, `CHAIN`),
  FOREIGN KEY(`PDB_NO`) REFERENCES `pdb_entry` (`PDB_NO`),
  FOREIGN KEY(`ENSEMBL_ID`) REFERENCES `ensembl_entry` (`ENSEMBL_ID`)
);
