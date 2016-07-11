package org.cbioportal.pdb_annotation.scripts;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class PdbScriptsPipelineUpdate implements CommandLineRunner{
	
	@Override
	public void run(String... args) throws Exception{
		
		System.out.println("update");
		
	}
}
