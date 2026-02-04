package no.novari.flyt.side.gateway.dispatch.repository

import no.novari.flyt.side.gateway.dispatch.model.DispatchReceiptEntity
import org.springframework.data.jpa.repository.JpaRepository

interface DispatchReceiptRepository : JpaRepository<DispatchReceiptEntity, String>
