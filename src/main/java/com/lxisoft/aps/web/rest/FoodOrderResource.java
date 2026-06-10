package com.lxisoft.aps.web.rest;

import com.lxisoft.aps.repository.FoodOrderRepository;
import com.lxisoft.aps.service.FoodOrderService;
import com.lxisoft.aps.service.dto.FoodOrderDTO;
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
 * REST controller for managing {@link com.lxisoft.aps.domain.FoodOrder}.
 */
@RestController
@RequestMapping("/api/food-orders")
public class FoodOrderResource {

    private static final Logger LOG = LoggerFactory.getLogger(FoodOrderResource.class);

    private static final String ENTITY_NAME = "foodOrder";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final FoodOrderService foodOrderService;

    private final FoodOrderRepository foodOrderRepository;

    public FoodOrderResource(FoodOrderService foodOrderService, FoodOrderRepository foodOrderRepository) {
        this.foodOrderService = foodOrderService;
        this.foodOrderRepository = foodOrderRepository;
    }

    /**
     * {@code POST  /food-orders} : Create a new foodOrder.
     *
     * @param foodOrderDTO the foodOrderDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new foodOrderDTO, or with status {@code 400 (Bad Request)} if the foodOrder has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public Mono<ResponseEntity<FoodOrderDTO>> createFoodOrder(@Valid @RequestBody FoodOrderDTO foodOrderDTO) throws URISyntaxException {
        LOG.debug("REST request to save FoodOrder : {}", foodOrderDTO);
        if (foodOrderDTO.getId() != null) {
            throw new BadRequestAlertException("A new foodOrder cannot already have an ID", ENTITY_NAME, "idexists");
        }
        return foodOrderService
            .save(foodOrderDTO)
            .map(result -> {
                try {
                    return ResponseEntity.created(new URI("/api/food-orders/" + result.getId()))
                        .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, result.getId().toString()))
                        .body(result);
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }
            });
    }

    /**
     * {@code PUT  /food-orders/:id} : Updates an existing foodOrder.
     *
     * @param id the id of the foodOrderDTO to save.
     * @param foodOrderDTO the foodOrderDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated foodOrderDTO,
     * or with status {@code 400 (Bad Request)} if the foodOrderDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the foodOrderDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public Mono<ResponseEntity<FoodOrderDTO>> updateFoodOrder(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody FoodOrderDTO foodOrderDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update FoodOrder : {}, {}", id, foodOrderDTO);
        if (foodOrderDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, foodOrderDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        return foodOrderRepository
            .existsById(id)
            .flatMap(exists -> {
                if (!exists) {
                    return Mono.error(new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound"));
                }

                return foodOrderService
                    .update(foodOrderDTO)
                    .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)))
                    .map(result ->
                        ResponseEntity.ok()
                            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, result.getId().toString()))
                            .body(result)
                    );
            });
    }

    /**
     * {@code PATCH  /food-orders/:id} : Partial updates given fields of an existing foodOrder, field will ignore if it is null
     *
     * @param id the id of the foodOrderDTO to save.
     * @param foodOrderDTO the foodOrderDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated foodOrderDTO,
     * or with status {@code 400 (Bad Request)} if the foodOrderDTO is not valid,
     * or with status {@code 404 (Not Found)} if the foodOrderDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the foodOrderDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public Mono<ResponseEntity<FoodOrderDTO>> partialUpdateFoodOrder(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody FoodOrderDTO foodOrderDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update FoodOrder partially : {}, {}", id, foodOrderDTO);
        if (foodOrderDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, foodOrderDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        return foodOrderRepository
            .existsById(id)
            .flatMap(exists -> {
                if (!exists) {
                    return Mono.error(new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound"));
                }

                Mono<FoodOrderDTO> result = foodOrderService.partialUpdate(foodOrderDTO);

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
     * {@code GET  /food-orders} : get all the foodOrders.
     *
     * @param pageable the pagination information.
     * @param request a {@link ServerHttpRequest} request.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of foodOrders in body.
     */
    @GetMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<List<FoodOrderDTO>>> getAllFoodOrders(
        @org.springdoc.core.annotations.ParameterObject Pageable pageable,
        ServerHttpRequest request
    ) {
        LOG.debug("REST request to get a page of FoodOrders");
        return foodOrderService
            .countAll()
            .zipWith(foodOrderService.findAll(pageable).collectList())
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
     * {@code GET  /food-orders/:id} : get the "id" foodOrder.
     *
     * @param id the id of the foodOrderDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the foodOrderDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public Mono<ResponseEntity<FoodOrderDTO>> getFoodOrder(@PathVariable("id") Long id) {
        LOG.debug("REST request to get FoodOrder : {}", id);
        Mono<FoodOrderDTO> foodOrderDTO = foodOrderService.findOne(id);
        return ResponseUtil.wrapOrNotFound(foodOrderDTO);
    }

    /**
     * {@code DELETE  /food-orders/:id} : delete the "id" foodOrder.
     *
     * @param id the id of the foodOrderDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteFoodOrder(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete FoodOrder : {}", id);
        return foodOrderService
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
