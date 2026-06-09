package com.lxisoft.aps.web.rest;

import com.lxisoft.aps.repository.DeliveryPartnerRepository;
import com.lxisoft.aps.service.DeliveryPartnerService;
import com.lxisoft.aps.service.dto.DeliveryPartnerDTO;
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
 * REST controller for managing {@link com.lxisoft.aps.domain.DeliveryPartner}.
 */
@RestController
@RequestMapping("/api/delivery-partners")
public class DeliveryPartnerResource {

    private static final Logger LOG = LoggerFactory.getLogger(DeliveryPartnerResource.class);

    private static final String ENTITY_NAME = "deliveryPartner";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final DeliveryPartnerService deliveryPartnerService;

    private final DeliveryPartnerRepository deliveryPartnerRepository;

    public DeliveryPartnerResource(DeliveryPartnerService deliveryPartnerService, DeliveryPartnerRepository deliveryPartnerRepository) {
        this.deliveryPartnerService = deliveryPartnerService;
        this.deliveryPartnerRepository = deliveryPartnerRepository;
    }

    /**
     * {@code POST  /delivery-partners} : Create a new deliveryPartner.
     *
     * @param deliveryPartnerDTO the deliveryPartnerDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new deliveryPartnerDTO, or with status {@code 400 (Bad Request)} if the deliveryPartner has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public Mono<ResponseEntity<DeliveryPartnerDTO>> createDeliveryPartner(@Valid @RequestBody DeliveryPartnerDTO deliveryPartnerDTO)
        throws URISyntaxException {
        LOG.debug("REST request to save DeliveryPartner : {}", deliveryPartnerDTO);
        if (deliveryPartnerDTO.getId() != null) {
            throw new BadRequestAlertException("A new deliveryPartner cannot already have an ID", ENTITY_NAME, "idexists");
        }
        return deliveryPartnerService
            .save(deliveryPartnerDTO)
            .map(result -> {
                try {
                    return ResponseEntity.created(new URI("/api/delivery-partners/" + result.getId()))
                        .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, result.getId().toString()))
                        .body(result);
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }
            });
    }

    /**
     * {@code PUT  /delivery-partners/:id} : Updates an existing deliveryPartner.
     *
     * @param id the id of the deliveryPartnerDTO to save.
     * @param deliveryPartnerDTO the deliveryPartnerDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated deliveryPartnerDTO,
     * or with status {@code 400 (Bad Request)} if the deliveryPartnerDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the deliveryPartnerDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public Mono<ResponseEntity<DeliveryPartnerDTO>> updateDeliveryPartner(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody DeliveryPartnerDTO deliveryPartnerDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update DeliveryPartner : {}, {}", id, deliveryPartnerDTO);
        if (deliveryPartnerDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, deliveryPartnerDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        return deliveryPartnerRepository
            .existsById(id)
            .flatMap(exists -> {
                if (!exists) {
                    return Mono.error(new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound"));
                }

                return deliveryPartnerService
                    .update(deliveryPartnerDTO)
                    .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)))
                    .map(result ->
                        ResponseEntity.ok()
                            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, result.getId().toString()))
                            .body(result)
                    );
            });
    }

    /**
     * {@code PATCH  /delivery-partners/:id} : Partial updates given fields of an existing deliveryPartner, field will ignore if it is null
     *
     * @param id the id of the deliveryPartnerDTO to save.
     * @param deliveryPartnerDTO the deliveryPartnerDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated deliveryPartnerDTO,
     * or with status {@code 400 (Bad Request)} if the deliveryPartnerDTO is not valid,
     * or with status {@code 404 (Not Found)} if the deliveryPartnerDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the deliveryPartnerDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public Mono<ResponseEntity<DeliveryPartnerDTO>> partialUpdateDeliveryPartner(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody DeliveryPartnerDTO deliveryPartnerDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update DeliveryPartner partially : {}, {}", id, deliveryPartnerDTO);
        if (deliveryPartnerDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, deliveryPartnerDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        return deliveryPartnerRepository
            .existsById(id)
            .flatMap(exists -> {
                if (!exists) {
                    return Mono.error(new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound"));
                }

                Mono<DeliveryPartnerDTO> result = deliveryPartnerService.partialUpdate(deliveryPartnerDTO);

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
     * {@code GET  /delivery-partners} : get all the deliveryPartners.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of deliveryPartners in body.
     */
    @GetMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<List<DeliveryPartnerDTO>> getAllDeliveryPartners() {
        LOG.debug("REST request to get all DeliveryPartners");
        return deliveryPartnerService.findAll().collectList();
    }

    /**
     * {@code GET  /delivery-partners} : get all the deliveryPartners as a stream.
     * @return the {@link Flux} of deliveryPartners.
     */
    @GetMapping(value = "", produces = MediaType.APPLICATION_NDJSON_VALUE)
    public Flux<DeliveryPartnerDTO> getAllDeliveryPartnersAsStream() {
        LOG.debug("REST request to get all DeliveryPartners as a stream");
        return deliveryPartnerService.findAll();
    }

    /**
     * {@code GET  /delivery-partners/:id} : get the "id" deliveryPartner.
     *
     * @param id the id of the deliveryPartnerDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the deliveryPartnerDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public Mono<ResponseEntity<DeliveryPartnerDTO>> getDeliveryPartner(@PathVariable("id") Long id) {
        LOG.debug("REST request to get DeliveryPartner : {}", id);
        Mono<DeliveryPartnerDTO> deliveryPartnerDTO = deliveryPartnerService.findOne(id);
        return ResponseUtil.wrapOrNotFound(deliveryPartnerDTO);
    }

    /**
     * {@code DELETE  /delivery-partners/:id} : delete the "id" deliveryPartner.
     *
     * @param id the id of the deliveryPartnerDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteDeliveryPartner(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete DeliveryPartner : {}", id);
        return deliveryPartnerService
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
