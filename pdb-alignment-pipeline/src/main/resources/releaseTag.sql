--
-- database `pdb`
--
select MAX(CAST(UPDATE_DATE AS CHAR)) from pdb_seq_alignment;
select count(distinct PDB_ID) from pdb_entry;
select count(*) from pdb_entry;
select count(*) from pdb_seq_alignment;
