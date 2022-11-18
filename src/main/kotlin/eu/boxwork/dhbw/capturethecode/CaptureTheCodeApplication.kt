package eu.boxwork.dhbw.capturethecode

import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType
import io.swagger.v3.oas.annotations.info.Info
import io.swagger.v3.oas.annotations.security.SecurityScheme
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication

@EntityScan("eu.boxwork.dhbw.capturethecode.model")
@ComponentScan(
	"eu.boxwork.dhbw.capturethecode.service",
	"eu.boxwork.dhbw.capturethecode.security",
	"eu.boxwork.dhbw.capturethecode.components")
@OpenAPIDefinition(info = Info(title = "Capture Code API", version = "0.2.0 (see endpoint /base/info)",
	description = "Api for training, administration and competition in the CTC."))
@SecurityScheme(name = "Token", scheme = "token", type = SecuritySchemeType.APIKEY, `in` = SecuritySchemeIn.HEADER)
@EnableScheduling
class CaptureTheCodeApplication

fun main(args: Array<String>) {
	runApplication<CaptureTheCodeApplication>(*args)
}
