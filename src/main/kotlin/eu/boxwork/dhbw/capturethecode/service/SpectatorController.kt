package eu.boxwork.dhbw.capturethecode.service

import org.hibernate.exception.ConstraintViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/spectate")
class SpectatorController {

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