package eu.boxwork.dhbw.capturethecode.service

import org.hibernate.exception.ConstraintViolationException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.ModelAndView
import java.util.UUID


@RestController
@RequestMapping("/ui")
class SpectatorController(
    @Autowired val teamService: TeamService,
    @Autowired val trainingService: TrainingService,
    @Autowired val competitionService: CompetitionService,
    @Value("\${server.servlet.context-path}") private val baseURL: String,
) {


    @GetMapping(value = ["/","/index.html"])
    fun index(): ModelAndView {
        return ModelAndView("index") //view
    }

    @GetMapping(value = ["/training","/training.html"])
    fun trainings(): ModelAndView {
        val model = ModelAndView()
        model.viewName = "training"
        model.addObject("baseURL", baseURL)
        model.addObject("trainings", trainingService.listUI())
        return model
    }

    @GetMapping(value = ["/competition","/competition.html"])
    fun competitions(): ModelAndView {
        val model = ModelAndView()
        model.viewName = "competition"
        model.addObject("baseURL", baseURL)
        model.addObject("competitions", competitionService.listUI())
        return model
    }

    @GetMapping(value = ["/teams","/teams.html"])
    fun teams(): ModelAndView {
        val model = ModelAndView()
        model.viewName = "teams"
        model.addObject("baseURL", baseURL)
        model.addObject("teams", teamService.listUI())
        return model
    }

    @GetMapping(value = ["/team","/team.html"])
    fun team(
        @RequestParam(name = "id") id: UUID
    ): ModelAndView {
        val model = ModelAndView()
        model.viewName = "team"
        model.addObject("baseURL", baseURL)
        model.addObject("team", teamService.getTeamUI(id))
        return model
    }

    /*
    * ###########################
    * EXCEPTION HANDLER
    * */
    @ExceptionHandler(ConstraintViolationException::class)
    fun handleException(ex: ConstraintViolationException) : ResponseEntity<String> {
        return  ResponseEntity(ex.message, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(ServiceException::class)
    fun handleException(ex:ServiceException) : ResponseEntity<String> {
        return ResponseEntity.status(ex.code).body(ex.message)
    }
}