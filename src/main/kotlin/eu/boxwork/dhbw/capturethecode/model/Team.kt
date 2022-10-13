package eu.boxwork.dhbw.capturethecode.model

import eu.boxwork.dhbw.capturethecode.dto.TeamDto
import java.util.*
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table
import javax.validation.constraints.Pattern

@Entity
@Table(name = "team")
data class Team (
    @Id @Column(name = "uuid") val uuid: UUID,
    @Column(name = "token") var teamToken: String,
    @Column(name = "name") var teamName: String
)
{
    fun dto() = TeamDto(uuid,null, teamName)
    fun dtoWithSecret() = TeamDto(uuid,teamToken, teamName)
}