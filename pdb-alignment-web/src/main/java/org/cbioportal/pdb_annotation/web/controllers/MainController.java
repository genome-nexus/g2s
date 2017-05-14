package org.cbioportal.pdb_annotation.web.controllers;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.apache.commons.lang3.StringUtils;
import org.cbioportal.pdb_annotation.scripts.PdbScriptsPipelineRunCommand;
import org.cbioportal.pdb_annotation.web.domain.StatisticsRepository;
import org.cbioportal.pdb_annotation.web.models.InputAlignment;
import org.cbioportal.pdb_annotation.web.models.InputSequence;
import org.cbioportal.pdb_annotation.web.models.Statistics;
import org.cbioportal.pdb_annotation.web.models.InputResidue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

/**
 * 
 * Main Controller of the whole website
 * Control the input sequence to blast Mainly use InputSequence in model
 * 
 * @author Juexin wang
 * 
 */
@Controller
public class MainController {

    @Autowired
    private StatisticsRepository statisticsRepository;

    @GetMapping("/sequence")
    public ModelAndView inputForm(Model model) {
        model.addAttribute("inputsequence", new InputSequence());
        return new ModelAndView("sequence");
    }

    @PostMapping("/sequence")
    public ModelAndView resultBack(@ModelAttribute @Valid InputSequence inputsequence, BindingResult bindingResult,
            HttpServletRequest request) {
        if (bindingResult.hasErrors()) {
            return new ModelAndView("sequence");
        }

        // is client behind something?
        String ipAddress = request.getHeader("X-FORWARDED-FOR");
        if (ipAddress == null) {
            ipAddress = request.getRemoteAddr();
        }

        inputsequence.setId(ipAddress);
        // inputsequence.setSequence(inputsequence.getSequence());

        PdbScriptsPipelineRunCommand pdbScriptsPipelineRunCommand = new PdbScriptsPipelineRunCommand();
        List<InputAlignment> alignments = pdbScriptsPipelineRunCommand.runCommand(inputsequence);

        // Instant instant = Instant.now ();
        // inputsequence.setTimenow(instant.toString());
        inputsequence.setTimenow(LocalDateTime.now().toString().replace("T", " "));

        List<InputResidue> residues = new ArrayList<InputResidue>();
        int inputAA = 0;
        if (inputsequence.getResidueNumList().size() != 0) {
            inputAA = Integer.parseInt(inputsequence.getResidueNumList().get(0));
        }

        for (InputAlignment ali : alignments) {
            // if getResidueNum is empty, then return alignments
            // else, return residues
            if (inputsequence.getResidueNumList().size() == 0
                    || (inputAA >= ali.getSeqFrom() && inputAA <= ali.getSeqTo())) {
                InputResidue re = new InputResidue();
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

                if (!(inputsequence.getResidueNumList().size() == 0)) {
                    re.setResidueName(
                            ali.getPdbAlign().substring(inputAA - ali.getSeqFrom(), inputAA - ali.getSeqFrom() + 1));
                    re.setResidueNum(new Integer(
                            Integer.parseInt(ali.getSegStart()) - 1 + ali.getPdbFrom() + (inputAA - ali.getSeqFrom()))
                                    .toString());
                }
                // For percentage
                int queryLength = ali.getSeqAlign().length();
                int targetLength = ali.getPdbAlign().length();
                int queryGapLength = StringUtils.countMatches(ali.getSeqAlign(), "-");
                int targetGapLength = StringUtils.countMatches(ali.getPdbAlign(), "-");
                int gapLength = Math.abs(queryGapLength - targetGapLength);

                // Test:
                if (queryLength != targetLength) {
                    System.out.println("Error! in " + ali.getPdbNo());
                }

                re.setIdentityPercentage(String.format("%.2f", ali.getIdentity() * 1.0f / queryLength));
                re.setPositivePercentage(String.format("%.2f", ali.getIdentp() * 1.0f / queryLength));
                re.setGapPercentage(String.format("%.2f", gapLength * 1.0f / queryLength));
                re.setGap(gapLength);
                re.setLength(queryLength);
                re.setIdentityPercentageStr("(" + ali.getIdentity() + "/" + queryLength + ")");
                re.setPositivePercentageStr("(" + ali.getIdentp() + "/" + queryLength + ")");
                re.setGapPercentageStr("(" + gapLength + "/" + queryLength + ")");

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
                if (inputsequence.getResidueNumList().size() != 0) {
                    re.setInputResidueNum(inputsequence.getResidueNumList().get(0));
                } else {
                    re.setInputResidueNum("");
                }

                residues.add(re);
            }
        }
        return new ModelAndView("/result", "residues", residues);
    }

    // Original Mapping
    @GetMapping("/pageapi")
    public ModelAndView apiInfo() {
        return new ModelAndView("api");
    }

    @GetMapping("/clients")
    public ModelAndView clientsInfo() {
        return new ModelAndView("clients");
    }

    @GetMapping("/statistics")
    public ModelAndView statisticsInfo(Model model) {
        List<Statistics> statistics = statisticsRepository.findTop2ByOrderByIdDesc();
        return new ModelAndView("/statistics", "statistics", statistics);
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
        return new ModelAndView("frontpage");
    }

}
