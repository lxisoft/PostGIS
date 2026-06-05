package com.lxisoft.aps.web.rest;

import com.lxisoft.aps.repository.DeliveryRequestRepository;
import com.lxisoft.aps.service.DeliveryRequestService;
import com.lxisoft.aps.service.dto.DeliveryRequestDTO;
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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.ForwardedHeaderUtils;
import reactor.core.publisher.Mono;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.reactive.ResponseUtil;

/**
 * REST controller for managing {@link com.lxisoft.aps.domain.DeliveryRequest}.
 */
@RestController
@RequestMapping("/api/delivery-requests")
public class DeliveryRequestResource {

    private static final Logger LOG = LoggerFactory.getLogger(DeliveryRequestResource.class);

    private static final String ENTITY_NAME = "deliveryRequest";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final DeliveryRequestService deliveryRequestService;

    private final DeliveryRequestRepository deliveryRequestRepository;

    public DeliveryRequestResource(DeliveryRequestService deliveryRequestService, DeliveryRequestRepository deliveryRequestRepository) {
        this.deliveryRequestService = deliveryRequestService;
        this.deliveryRequestRepository = deliveryRequestRepository;
    }

    /**
     * {@code POST  /delivery-requests} : Create a new deliveryRequest.
     *
     * @param deliveryRequestDTO the deliveryRequestDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new deliveryRequestDTO, or with status {@code 400 (Bad Request)} if the deliveryRequest has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public Mono<ResponseEntity<DeliveryRequestDTO>> createDeliveryRequest(@Valid @RequestBody DeliveryRequestDTO deliveryRequestDTO)
        throws URISyntaxException {
        LOG.debug("REST request to save DeliveryRequest : {}", deliveryRequestDTO);
        if (deliveryRequestDTO.getId() != null) {
            throw new BadRequestAlertException("A new deliveryRequest cannot already have an ID", ENTITY_NAME, "idexists");
        }
        return deliveryRequestService
            .save(deliveryRequestDTO)
            .map(result -> {
                try {
                    return ResponseEntity.created(new URI("/api/delivery-requests/" + result.getId()))
                        .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, result.getId().toString()))
                        .body(result);
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }
            });
    }

    /**
     * {@code PUT  /delivery-requests/:id} : Updates an existing deliveryRequest.
     *
     * @param id the id of the deliveryRequestDTO to save.
     * @param deliveryRequestDTO the deliveryRequestDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated deliveryRequestDTO,
     * or with status {@code 400 (Bad Request)} if the deliveryRequestDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the deliveryRequestDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public Mono<ResponseEntity<DeliveryRequestDTO>> updateDeliveryRequest(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody DeliveryRequestDTO deliveryRequestDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update DeliveryRequest : {}, {}", id, deliveryRequestDTO);
        if (deliveryRequestDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, deliveryRequestDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        return deliveryRequestRepository
            .existsById(id)
            .flatMap(exists -> {
                if (!exists) {
                    return Mono.error(new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound"));
                }

                return deliveryRequestService
                    .update(deliveryRequestDTO)
                    .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)))
                    .map(result ->
                        ResponseEntity.ok()
                            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, result.getId().toString()))
                            .body(result)
                    );
            });
    }

    /**
     * {@code PATCH  /delivery-requests/:id} : Partial updates given fields of an existing deliveryRequest, field will ignore if it is null
     *
     * @param id the id of the deliveryRequestDTO to save.
     * @param deliveryRequestDTO the deliveryRequestDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated deliveryRequestDTO,
     * or with status {@code 400 (Bad Request)} if the deliveryRequestDTO is not valid,
     * or with status {@code 404 (Not Found)} if the deliveryRequestDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the deliveryRequestDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public Mono<ResponseEntity<DeliveryRequestDTO>> partialUpdateDeliveryRequest(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody DeliveryRequestDTO deliveryRequestDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update DeliveryRequest partially : {}, {}", id, deliveryRequestDTO);
        if (deliveryRequestDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, deliveryRequestDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        return deliveryRequestRepository
            .existsById(id)
            .flatMap(exists -> {
                if (!exists) {
                    return Mono.error(new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound"));
                }

                Mono<DeliveryRequestDTO> result = deliveryRequestService.partialUpdate(deliveryRequestDTO);

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
     * {@code GET  /delivery-requests} : get all the deliveryRequests.
     *
     * @param pageable the pagination information.
     * @param request a {@link ServerHttpRequest} request.
     * @param eagerload flag to eager load entities from relationships (This is applicable for many-to-many).
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of deliveryRequests in body.
     */
    @GetMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<List<DeliveryRequestDTO>>> getAllDeliveryRequests(
        @org.springdoc.core.annotations.ParameterObject Pageable pageable,
        ServerHttpRequest request,
        @RequestParam(name = "eagerload", required = false, defaultValue = "true") boolean eagerload
    ) {
        LOG.debug("REST request to get a page of DeliveryRequests");
        return deliveryRequestService
            .countAll()
            .zipWith(deliveryRequestService.findAll(pageable).collectList())
            .map(countWithEntities ->
                ResponseEntity.ok()
                    .headers(
                        PaginationUtil.generatePaginationHttpHeaders(
                            ForwardedHeaderUtils.adaptFromForwardedHeaders(request.getURI(), request.getHeaders()),
                            new PageImpl<>(countWithEntities.getT2(), pageable, countWithEntities.getT1())
                        )
                    )
                    .body(countWithEntities.getT2())
            );
    }

    /**
     * {@code GET  /delivery-requests/:id} : get the "id" deliveryRequest.
     *
     * @param id the id of the deliveryRequestDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the deliveryRequestDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public Mono<ResponseEntity<DeliveryRequestDTO>> getDeliveryRequest(@PathVariable("id") Long id) {
        LOG.debug("REST request to get DeliveryRequest : {}", id);
        Mono<DeliveryRequestDTO> deliveryRequestDTO = deliveryRequestService.findOne(id);
        return ResponseUtil.wrapOrNotFound(deliveryRequestDTO);
    }

    /**
     * {@code DELETE  /delivery-requests/:id} : delete the "id" deliveryRequest.
     *
     * @param id the id of the deliveryRequestDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteDeliveryRequest(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete DeliveryRequest : {}", id);
        return deliveryRequestService
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
