package com.lxisoft.aps.web.rest;

import com.lxisoft.aps.repository.DeliveryZoneRepository;
import com.lxisoft.aps.service.DeliveryZoneService;
import com.lxisoft.aps.service.dto.DeliveryZoneDTO;
import com.lxisoft.aps.web.rest.errors.BadRequestAlertException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.reactive.ResponseUtil;

/**
 * REST controller for managing {@link com.lxisoft.aps.domain.DeliveryZone}.
 */
@RestController
@RequestMapping("/api/delivery-zones")
public class DeliveryZoneResource {

    private static final Logger LOG = LoggerFactory.getLogger(DeliveryZoneResource.class);

    private static final String ENTITY_NAME = "deliveryZone";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final DeliveryZoneService deliveryZoneService;

    private final DeliveryZoneRepository deliveryZoneRepository;

    public DeliveryZoneResource(DeliveryZoneService deliveryZoneService, DeliveryZoneRepository deliveryZoneRepository) {
        this.deliveryZoneService = deliveryZoneService;
        this.deliveryZoneRepository = deliveryZoneRepository;
    }

    /**
     * {@code POST  /delivery-zones} : Create a new deliveryZone.
     *
     * @param deliveryZoneDTO the deliveryZoneDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new deliveryZoneDTO, or with status {@code 400 (Bad Request)} if the deliveryZone has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public Mono<ResponseEntity<DeliveryZoneDTO>> createDeliveryZone(@Valid @RequestBody DeliveryZoneDTO deliveryZoneDTO)
        throws URISyntaxException {
        LOG.debug("REST request to save DeliveryZone : {}", deliveryZoneDTO);
        if (deliveryZoneDTO.getId() != null) {
            throw new BadRequestAlertException("A new deliveryZone cannot already have an ID", ENTITY_NAME, "idexists");
        }
        return deliveryZoneService
            .save(deliveryZoneDTO)
            .map(result -> {
                try {
                    return ResponseEntity.created(new URI("/api/delivery-zones/" + result.getId()))
                        .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, result.getId().toString()))
                        .body(result);
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }
            });
    }

    /**
     * {@code PUT  /delivery-zones/:id} : Updates an existing deliveryZone.
     *
     * @param id the id of the deliveryZoneDTO to save.
     * @param deliveryZoneDTO the deliveryZoneDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated deliveryZoneDTO,
     * or with status {@code 400 (Bad Request)} if the deliveryZoneDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the deliveryZoneDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public Mono<ResponseEntity<DeliveryZoneDTO>> updateDeliveryZone(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody DeliveryZoneDTO deliveryZoneDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update DeliveryZone : {}, {}", id, deliveryZoneDTO);
        if (deliveryZoneDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, deliveryZoneDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        return deliveryZoneRepository
            .existsById(id)
            .flatMap(exists -> {
                if (!exists) {
                    return Mono.error(new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound"));
                }

                return deliveryZoneService
                    .update(deliveryZoneDTO)
                    .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)))
                    .map(result ->
                        ResponseEntity.ok()
                            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, result.getId().toString()))
                            .body(result)
                    );
            });
    }

    /**
     * {@code PATCH  /delivery-zones/:id} : Partial updates given fields of an existing deliveryZone, field will ignore if it is null
     *
     * @param id the id of the deliveryZoneDTO to save.
     * @param deliveryZoneDTO the deliveryZoneDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated deliveryZoneDTO,
     * or with status {@code 400 (Bad Request)} if the deliveryZoneDTO is not valid,
     * or with status {@code 404 (Not Found)} if the deliveryZoneDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the deliveryZoneDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public Mono<ResponseEntity<DeliveryZoneDTO>> partialUpdateDeliveryZone(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody DeliveryZoneDTO deliveryZoneDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update DeliveryZone partially : {}, {}", id, deliveryZoneDTO);
        if (deliveryZoneDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, deliveryZoneDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        return deliveryZoneRepository
            .existsById(id)
            .flatMap(exists -> {
                if (!exists) {
                    return Mono.error(new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound"));
                }

                Mono<DeliveryZoneDTO> result = deliveryZoneService.partialUpdate(deliveryZoneDTO);

                return result
                    .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)))
                    .map(res ->
                        ResponseEntity.ok()
                            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, res.getId().toString()))
                            .body(res)
                    );
            });
    }

    /**
     * {@code GET  /delivery-zones} : get all the deliveryZones.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of deliveryZones in body.
     */
    @GetMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<List<DeliveryZoneDTO>> getAllDeliveryZones() {
        LOG.debug("REST request to get all DeliveryZones");
        return deliveryZoneService.findAll().collectList();
    }

    /**
     * {@code GET  /delivery-zones} : get all the deliveryZones as a stream.
     * @return the {@link Flux} of deliveryZones.
     */
    @GetMapping(value = "", produces = MediaType.APPLICATION_NDJSON_VALUE)
    public Flux<DeliveryZoneDTO> getAllDeliveryZonesAsStream() {
        LOG.debug("REST request to get all DeliveryZones as a stream");
        return deliveryZoneService.findAll();
    }

    /**
     * {@code GET  /delivery-zones/:id} : get the "id" deliveryZone.
     *
     * @param id the id of the deliveryZoneDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the deliveryZoneDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public Mono<ResponseEntity<DeliveryZoneDTO>> getDeliveryZone(@PathVariable("id") Long id) {
        LOG.debug("REST request to get DeliveryZone : {}", id);
        Mono<DeliveryZoneDTO> deliveryZoneDTO = deliveryZoneService.findOne(id);
        return ResponseUtil.wrapOrNotFound(deliveryZoneDTO);
    }

    /**
     * {@code DELETE  /delivery-zones/:id} : delete the "id" deliveryZone.
     *
     * @param id the id of the deliveryZoneDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteDeliveryZone(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete DeliveryZone : {}", id);
        return deliveryZoneService
            .delete(id)
            .then(
                Mono.just(
                    ResponseEntity.noContent()
                        .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
                        .build()
                )
            );
    }
}
