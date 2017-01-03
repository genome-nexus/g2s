package org.cbioportal.pdb_annotation.web.controllers;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.cbioportal.pdb_annotation.scripts.PdbScriptsPipelineRunCommand;
import org.cbioportal.pdb_annotation.web.models.Alignment;
import org.cbioportal.pdb_annotation.web.models.Inputsequence;
import org.cbioportal.pdb_annotation.web.models.Residue;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

/**
 * 
 * Control the input sequence to blast
 * 
 * @author Juexin wang
 * 
 */
@Controller
public class InputsequenceController {

    @GetMapping("/input")
    public ModelAndView inputForm(Model model) {
        model.addAttribute("inputsequence", new Inputsequence());
        return new ModelAndView("input");
    }

    @PostMapping("/input")
    public ModelAndView resultBack(@ModelAttribute @Valid Inputsequence inputsequence, BindingResult bindingResult,
            HttpServletRequest request) {
        if (bindingResult.hasErrors()) {
            return new ModelAndView("input");
        }

        // is client behind something?
        String ipAddress = request.getHeader("X-FORWARDED-FOR");
        if (ipAddress == null) {
            ipAddress = request.getRemoteAddr();
        }

        inputsequence.setId(ipAddress);
        // inputsequence.setSequence(inputsequence.getSequence());

        PdbScriptsPipelineRunCommand pdbScriptsPipelineRunCommand = new PdbScriptsPipelineRunCommand();
        List<Alignment> alignments = pdbScriptsPipelineRunCommand.runCommand(inputsequence);

        // Instant instant = Instant.now ();
        // inputsequence.setTimenow(instant.toString());
        inputsequence.setTimenow(LocalDateTime.now().toString().replace("T", " "));

        List<Residue> residues = new ArrayList<Residue>();
        int inputAA = 0;
        if (!inputsequence.getResidueNum().equals("")) {
            inputAA = Integer.parseInt(inputsequence.getResidueNum());
        }

        for (Alignment ali : alignments) {
            // if getResidueNum is empty, then return alignments
            // else, return residues
            if (inputsequence.getResidueNum().equals("")
                    || (inputAA >= ali.getSeqFrom() && inputAA <= ali.getSeqTo())) {
                Residue re = new Residue();
                re.setAlignmentId(ali.getAlignmentId());
                re.setBitscore(ali.getBitscore());
                re.setChain(ali.getChain());
                re.setSeqAlign(ali.getSeqAlign());
                re.setSeqFrom(ali.getSeqFrom());
                re.setSeqId(ali.getSeqId());
                re.setSeqTo(ali.getSeqTo());
                re.setSegStart(ali.getSegStart());
                re.setEvalue(ali.getEvalue());
                re.setIdentity(ali.getIdentity());
                re.setIdentp(ali.getIdentp());
                re.setMidlineAlign(ali.getMidlineAlign());
                re.setPdbAlign(ali.getPdbAlign());
                re.setPdbFrom(ali.getPdbFrom());
                re.setPdbId(ali.getPdbId());
                re.setPdbNo(ali.getPdbNo());
                re.setPdbSeg(ali.getPdbSeg());
                re.setPdbTo(ali.getPdbTo());
                if (!(inputsequence.getResidueNum().equals(""))) {
                    re.setResidueName(
                            ali.getPdbAlign().substring(inputAA - ali.getSeqFrom(), inputAA - ali.getSeqFrom() + 1));
                    re.setResidueNum(new Integer(
                            Integer.parseInt(ali.getSegStart()) - 1 + ali.getPdbFrom() + (inputAA - ali.getSeqFrom()))
                                    .toString());
                }

                // Parameters for output TODO: not optimize
                re.setParaEvalue(inputsequence.getEvalue());
                re.setWord_size(inputsequence.getWord_size());
                re.setGapopen(inputsequence.getGapopen());
                re.setGapextend(inputsequence.getGapextend());
                re.setMatrix(inputsequence.getMatrix());
                re.setComp_based_stats(inputsequence.getComp_based_stats());
                re.setThreshold(inputsequence.getThreshold());
                re.setWindow_size(inputsequence.getWindow_size());

                re.setBlast_dblen(ali.getBlast_dblen());
                re.setBlast_dbnum(ali.getBlast_dbnum());
                re.setBlast_effspace(ali.getBlast_effspace());
                re.setBlast_entropy(ali.getBlast_entropy());
                re.setBlast_hsplen(ali.getBlast_hsplen());
                re.setBlast_kappa(ali.getBlast_kappa());
                re.setBlast_lambda(ali.getBlast_lambda());
                re.setBlast_reference(ali.getBlast_reference());
                re.setBlast_version(ali.getBlast_version());

                re.setTimenow(inputsequence.getTimenow());

                // input
                re.setSequence(inputsequence.getSequence());
                re.setInputResidueNum(inputsequence.getResidueNum());

                residues.add(re);
            }
        }
        return new ModelAndView("/result", "residues", residues);
    }

    @GetMapping("/api")
    public ModelAndView apiInfo() {
        return new ModelAndView("api");
    }

    @GetMapping("/clients")
    public ModelAndView clientsInfo() {
        return new ModelAndView("clients");
    }

    @GetMapping("/statistics")
    public ModelAndView statisticsInfo() {
        return new ModelAndView("statistics");
    }

    @GetMapping("/about")
    public ModelAndView aboutInfo() {
        return new ModelAndView("about");
    }

    @GetMapping("/contact")
    public ModelAndView contactInfo() {
        return new ModelAndView("contact");
    }

    @GetMapping("/")
    public ModelAndView homeInfo() {
        return new ModelAndView("api");
    }

}
