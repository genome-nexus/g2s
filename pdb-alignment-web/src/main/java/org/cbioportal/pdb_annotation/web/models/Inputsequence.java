package org.cbioportal.pdb_annotation.web.models;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

public class Inputsequence {
    
    private String id;
    
    @NotNull
    @Size(min=2, max=100)
    //@Pattern(regexp="\\D")
    private String sequence;
    
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getSequence() {
        return sequence;
    }
    public void setSequence(String sequence) {
        this.sequence = sequence;
    }
    
    

}
