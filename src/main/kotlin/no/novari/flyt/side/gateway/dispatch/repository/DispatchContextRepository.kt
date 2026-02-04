package no.novari.flyt.side.gateway.dispatch.repository

import no.novari.flyt.side.gateway.dispatch.model.DispatchContextEntity
import org.springframework.data.jpa.repository.JpaRepository

interface DispatchContextRepository : JpaRepository<DispatchContextEntity, String>
