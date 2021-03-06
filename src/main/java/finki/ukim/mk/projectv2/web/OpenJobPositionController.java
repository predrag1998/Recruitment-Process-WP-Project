package finki.ukim.mk.projectv2.web;

import finki.ukim.mk.projectv2.model.OpenJobPosition;
import finki.ukim.mk.projectv2.service.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.List;

@Controller
@RequestMapping("/jobs")
public class OpenJobPositionController {
    private final OpenJobPositionService openJobPositionService;
    private final PersonService personService;
    private final ApplicationService applicationService;
    private final PhaseService phaseService;
    private final EmailService emailService;

    public OpenJobPositionController(OpenJobPositionService openJobPositionService, PersonService personService, ApplicationService applicationService, PhaseService phaseService, EmailService emailService) {
        this.openJobPositionService = openJobPositionService;
        this.personService = personService;
        this.applicationService = applicationService;
        this.phaseService = phaseService;
        this.emailService = emailService;
    }


    @GetMapping("")
    public String getJobs(Model model, HttpSession session) {
        List<OpenJobPosition> jobs = this.openJobPositionService.findAll();
        model.addAttribute("jobs", jobs);
        session.setAttribute("test","Hello this is a message from session");
        model.addAttribute("bodyContent","openJobsPositions");
        return "master-template";
    }

    @GetMapping("/apply/{id}")
    public String getApplyFormJob(@PathVariable("id") Long jobId, Model model,HttpSession session) {
        String jobName = this.openJobPositionService.findById(jobId).get().getName();
        String jobDesc = this.openJobPositionService.findById(jobId).get().getDescription();
        model.addAttribute("jobId", jobId);
        model.addAttribute("jobName", jobName);
//        String s=session.getAttribute("test").toString();
        model.addAttribute("bodyContent","applyForm");
        model.addAttribute("jobDesc", jobDesc);
        return "master-template";
    }

    @PostMapping("/apply")
    public String saveApplication(@RequestParam String name,
                                  @RequestParam String surname,
                                  @RequestParam String mail,
                                  @RequestParam int age,
                                  @RequestParam Long jobId) {
        this.personService.saveWithPhase(name, surname, mail, age, phaseService.findById(1L).get()); //todo:find first phase
        if (personService.findByMail(mail).isPresent()) {
            OpenJobPosition job = this.openJobPositionService.findById(jobId).get(); //find JobPosition from hidden form id
            this.applicationService.save(personService.findByMail(mail).get(), job);
        }
        //Send mail after application with Application ID(ticket)
        Long personID = personService.findByMail(mail).get().getId();
        Long applicationID = applicationService.findByPersonId(personID).get().getApplicationID();

        this.emailService.sendSimpleMessage(mail, "Recruitment process(WP-project)", "Hello " + name +
                "\n\nThank you for your application" +
                "\n Your application ID(ticket) is " + applicationID +
                "\n\n Recruitment process team");
        return "redirect:/jobs";
    }

    @GetMapping("/add")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public String addJobForm(Model model) {
        model.addAttribute("bodyContent","addJobForm");
        return "master-template";
    }

    @PostMapping("/add")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public String addJob(@RequestParam String name,
                         @RequestParam String desc) {
        this.openJobPositionService.save(name, desc);
        return "redirect:/jobs";
    }

    @GetMapping("/mailTemplate/{id}")
    public String mailTemplate(@PathVariable Long id, Model model){
        OpenJobPosition job = this.openJobPositionService.findById(id).get();
        model.addAttribute("job", job);
        model.addAttribute("bodyContent","mailTemplate");
        return "master-template";
    }
}