package org.example.integrated_architectures.coach;

import org.example.integrated_architectures.sport.SportService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Public pages: coach catalog and coach profile. Open for guests (see SecurityConfig).
 * GET, not POST: the filter is in the URL, so a filtered page can be bookmarked or shared.
 */
@Controller
@RequestMapping("/coaches")
public class CoachCatalogController {

    private final CoachCatalogService coachCatalogService;
    private final SportService sportService;

    public CoachCatalogController(CoachCatalogService coachCatalogService, SportService sportService) {
        this.coachCatalogService = coachCatalogService;
        this.sportService = sportService;
    }

    // @ModelAttribute("filter"): Spring fills CoachSearchFilter from ?sportId=&name=&minExperience=
    // and puts it into the model, so the form shows the current values again.
    @GetMapping
    public String catalog(@ModelAttribute("filter") CoachSearchFilter filter,
                          @RequestParam(defaultValue = "0") int page,
                          Model model) {
        model.addAttribute("coaches", coachCatalogService.search(filter, page));
        model.addAttribute("sports", sportService.findAllSorted());
        return "coaches/list";
    }

    // Unknown or not ACTIVE coach -> CoachNotFoundException -> 404 (thanks to its @ResponseStatus).
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("coach", coachCatalogService.findActiveCoach(id));
        return "coaches/detail";
    }
}
