package eu.boxwork.dhbw.capturethecode.model

import com.fasterxml.jackson.annotation.JsonIgnore
import eu.boxwork.dhbw.capturethecode.dto.PlayerDto
import eu.boxwork.dhbw.capturethecode.enums.PlayerState
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import java.util.UUID
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.Table
import javax.validation.Valid
import javax.validation.constraints.Pattern

@Entity
@Table(name = "player")
data class Player(
    @Id @Column(name = "uuid") val uuid: UUID,
    @Pattern(regexp = "\\S{1,20}") @Column(name = "name") var name: String,
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "teamid", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JsonIgnore
    val team: Team
) {
    fun dto() = PlayerDto(
        uuid,
        name,
        team.teamName
    )
}
