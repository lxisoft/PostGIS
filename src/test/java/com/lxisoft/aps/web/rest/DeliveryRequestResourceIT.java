package com.lxisoft.aps.web.rest;

import static com.lxisoft.aps.domain.DeliveryRequestAsserts.*;
import static com.lxisoft.aps.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lxisoft.aps.IntegrationTest;
import com.lxisoft.aps.domain.DeliveryRequest;
import com.lxisoft.aps.repository.DeliveryRequestRepository;
import com.lxisoft.aps.repository.EntityManager;
import com.lxisoft.aps.service.DeliveryRequestService;
import com.lxisoft.aps.service.dto.DeliveryRequestDTO;
import com.lxisoft.aps.service.mapper.DeliveryRequestMapper;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;

/**
 * Integration tests for the {@link DeliveryRequestResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureWebTestClient(timeout = IntegrationTest.DEFAULT_ENTITY_TIMEOUT)
@WithMockUser
class DeliveryRequestResourceIT {

    private static final Instant DEFAULT_REQUEST_DATE = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_REQUEST_DATE = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final String DEFAULT_STATUS = "AAAAAAAAAA";
    private static final String UPDATED_STATUS = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/delivery-requests";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private DeliveryRequestRepository deliveryRequestRepository;

    @Mock
    private DeliveryRequestRepository deliveryRequestRepositoryMock;

    @Autowired
    private DeliveryRequestMapper deliveryRequestMapper;

    @Mock
    private DeliveryRequestService deliveryRequestServiceMock;

    @Autowired
    private EntityManager em;

    @Autowired
    private WebTestClient webTestClient;

    private DeliveryRequest deliveryRequest;

    private DeliveryRequest insertedDeliveryRequest;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static DeliveryRequest createEntity() {
        return new DeliveryRequest().requestDate(DEFAULT_REQUEST_DATE).status(DEFAULT_STATUS);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static DeliveryRequest createUpdatedEntity() {
        return new DeliveryRequest().requestDate(UPDATED_REQUEST_DATE).status(UPDATED_STATUS);
    }

    public static void deleteEntities(EntityManager em) {
        try {
            em.deleteAll(DeliveryRequest.class).block();
        } catch (Exception e) {
            // It can fail, if other entities are still referring this - it will be removed later.
        }
    }

    @BeforeEach
    public void initTest() {
        deliveryRequest = createEntity();
    }

    @AfterEach
    public void cleanup() {
        if (insertedDeliveryRequest != null) {
            deliveryRequestRepository.delete(insertedDeliveryRequest).block();
            insertedDeliveryRequest = null;
        }
        deleteEntities(em);
    }

    @Test
    void createDeliveryRequest() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the DeliveryRequest
        DeliveryRequestDTO deliveryRequestDTO = deliveryRequestMapper.toDto(deliveryRequest);
        var returnedDeliveryRequestDTO = webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(deliveryRequestDTO))
            .exchange()
            .expectStatus()
            .isCreated()
            .expectBody(DeliveryRequestDTO.class)
            .returnResult()
            .getResponseBody();

        // Validate the DeliveryRequest in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedDeliveryRequest = deliveryRequestMapper.toEntity(returnedDeliveryRequestDTO);
        assertDeliveryRequestUpdatableFieldsEquals(returnedDeliveryRequest, getPersistedDeliveryRequest(returnedDeliveryRequest));

        insertedDeliveryRequest = returnedDeliveryRequest;
    }

    @Test
    void createDeliveryRequestWithExistingId() throws Exception {
        // Create the DeliveryRequest with an existing ID
        deliveryRequest.setId(1L);
        DeliveryRequestDTO deliveryRequestDTO = deliveryRequestMapper.toDto(deliveryRequest);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(deliveryRequestDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the DeliveryRequest in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    void checkRequestDateIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        deliveryRequest.setRequestDate(null);

        // Create the DeliveryRequest, which fails.
        DeliveryRequestDTO deliveryRequestDTO = deliveryRequestMapper.toDto(deliveryRequest);

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(deliveryRequestDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void checkStatusIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        deliveryRequest.setStatus(null);

        // Create the DeliveryRequest, which fails.
        DeliveryRequestDTO deliveryRequestDTO = deliveryRequestMapper.toDto(deliveryRequest);

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(deliveryRequestDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void getAllDeliveryRequests() {
        // Initialize the database
        insertedDeliveryRequest = deliveryRequestRepository.save(deliveryRequest).block();

        // Get all the deliveryRequestList
        webTestClient
            .get()
            .uri(ENTITY_API_URL + "?sort=id,desc")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.[*].id")
            .value(hasItem(deliveryRequest.getId().intValue()))
            .jsonPath("$.[*].requestDate")
            .value(hasItem(DEFAULT_REQUEST_DATE.toString()))
            .jsonPath("$.[*].status")
            .value(hasItem(DEFAULT_STATUS));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllDeliveryRequestsWithEagerRelationshipsIsEnabled() {
        when(deliveryRequestServiceMock.findAllWithEagerRelationships(any())).thenReturn(Flux.empty());

        webTestClient.get().uri(ENTITY_API_URL + "?eagerload=true").exchange().expectStatus().isOk();

        verify(deliveryRequestServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllDeliveryRequestsWithEagerRelationshipsIsNotEnabled() {
        when(deliveryRequestServiceMock.findAllWithEagerRelationships(any())).thenReturn(Flux.empty());

        webTestClient.get().uri(ENTITY_API_URL + "?eagerload=false").exchange().expectStatus().isOk();
        verify(deliveryRequestRepositoryMock, times(1)).findAllWithEagerRelationships(any());
    }

    @Test
    void getDeliveryRequest() {
        // Initialize the database
        insertedDeliveryRequest = deliveryRequestRepository.save(deliveryRequest).block();

        // Get the deliveryRequest
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, deliveryRequest.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.id")
            .value(is(deliveryRequest.getId().intValue()))
            .jsonPath("$.requestDate")
            .value(is(DEFAULT_REQUEST_DATE.toString()))
            .jsonPath("$.status")
            .value(is(DEFAULT_STATUS));
    }

    @Test
    void getNonExistingDeliveryRequest() {
        // Get the deliveryRequest
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, Long.MAX_VALUE)
            .accept(MediaType.APPLICATION_PROBLEM_JSON)
            .exchange()
            .expectStatus()
            .isNotFound();
    }

    @Test
    void putExistingDeliveryRequest() throws Exception {
        // Initialize the database
        insertedDeliveryRequest = deliveryRequestRepository.save(deliveryRequest).block();

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the deliveryRequest
        DeliveryRequest updatedDeliveryRequest = deliveryRequestRepository.findById(deliveryRequest.getId()).block();
        updatedDeliveryRequest.requestDate(UPDATED_REQUEST_DATE).status(UPDATED_STATUS);
        DeliveryRequestDTO deliveryRequestDTO = deliveryRequestMapper.toDto(updatedDeliveryRequest);

        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, deliveryRequestDTO.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(deliveryRequestDTO))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the DeliveryRequest in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedDeliveryRequestToMatchAllProperties(updatedDeliveryRequest);
    }

    @Test
    void putNonExistingDeliveryRequest() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        deliveryRequest.setId(longCount.incrementAndGet());

        // Create the DeliveryRequest
        DeliveryRequestDTO deliveryRequestDTO = deliveryRequestMapper.toDto(deliveryRequest);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, deliveryRequestDTO.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(deliveryRequestDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the DeliveryRequest in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithIdMismatchDeliveryRequest() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        deliveryRequest.setId(longCount.incrementAndGet());

        // Create the DeliveryRequest
        DeliveryRequestDTO deliveryRequestDTO = deliveryRequestMapper.toDto(deliveryRequest);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, longCount.incrementAndGet())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(deliveryRequestDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the DeliveryRequest in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithMissingIdPathParamDeliveryRequest() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        deliveryRequest.setId(longCount.incrementAndGet());

        // Create the DeliveryRequest
        DeliveryRequestDTO deliveryRequestDTO = deliveryRequestMapper.toDto(deliveryRequest);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(deliveryRequestDTO))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the DeliveryRequest in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void partialUpdateDeliveryRequestWithPatch() throws Exception {
        // Initialize the database
        insertedDeliveryRequest = deliveryRequestRepository.save(deliveryRequest).block();

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the deliveryRequest using partial update
        DeliveryRequest partialUpdatedDeliveryRequest = new DeliveryRequest();
        partialUpdatedDeliveryRequest.setId(deliveryRequest.getId());

        partialUpdatedDeliveryRequest.requestDate(UPDATED_REQUEST_DATE).status(UPDATED_STATUS);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedDeliveryRequest.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(partialUpdatedDeliveryRequest))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the DeliveryRequest in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertDeliveryRequestUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedDeliveryRequest, deliveryRequest),
            getPersistedDeliveryRequest(deliveryRequest)
        );
    }

    @Test
    void fullUpdateDeliveryRequestWithPatch() throws Exception {
        // Initialize the database
        insertedDeliveryRequest = deliveryRequestRepository.save(deliveryRequest).block();

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the deliveryRequest using partial update
        DeliveryRequest partialUpdatedDeliveryRequest = new DeliveryRequest();
        partialUpdatedDeliveryRequest.setId(deliveryRequest.getId());

        partialUpdatedDeliveryRequest.requestDate(UPDATED_REQUEST_DATE).status(UPDATED_STATUS);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedDeliveryRequest.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(partialUpdatedDeliveryRequest))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the DeliveryRequest in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertDeliveryRequestUpdatableFieldsEquals(
            partialUpdatedDeliveryRequest,
            getPersistedDeliveryRequest(partialUpdatedDeliveryRequest)
        );
    }

    @Test
    void patchNonExistingDeliveryRequest() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        deliveryRequest.setId(longCount.incrementAndGet());

        // Create the DeliveryRequest
        DeliveryRequestDTO deliveryRequestDTO = deliveryRequestMapper.toDto(deliveryRequest);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, deliveryRequestDTO.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(deliveryRequestDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the DeliveryRequest in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithIdMismatchDeliveryRequest() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        deliveryRequest.setId(longCount.incrementAndGet());

        // Create the DeliveryRequest
        DeliveryRequestDTO deliveryRequestDTO = deliveryRequestMapper.toDto(deliveryRequest);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, longCount.incrementAndGet())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(deliveryRequestDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the DeliveryRequest in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithMissingIdPathParamDeliveryRequest() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        deliveryRequest.setId(longCount.incrementAndGet());

        // Create the DeliveryRequest
        DeliveryRequestDTO deliveryRequestDTO = deliveryRequestMapper.toDto(deliveryRequest);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(deliveryRequestDTO))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the DeliveryRequest in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void deleteDeliveryRequest() {
        // Initialize the database
        insertedDeliveryRequest = deliveryRequestRepository.save(deliveryRequest).block();

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the deliveryRequest
        webTestClient
            .delete()
            .uri(ENTITY_API_URL_ID, deliveryRequest.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNoContent();

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return deliveryRequestRepository.count().block();
    }

    protected void assertIncrementedRepositoryCount(long countBefore) {
        assertThat(countBefore + 1).isEqualTo(getRepositoryCount());
    }

    protected void assertDecrementedRepositoryCount(long countBefore) {
        assertThat(countBefore - 1).isEqualTo(getRepositoryCount());
    }

    protected void assertSameRepositoryCount(long countBefore) {
        assertThat(countBefore).isEqualTo(getRepositoryCount());
    }

    protected DeliveryRequest getPersistedDeliveryRequest(DeliveryRequest deliveryRequest) {
        return deliveryRequestRepository.findById(deliveryRequest.getId()).block();
    }

    protected void assertPersistedDeliveryRequestToMatchAllProperties(DeliveryRequest expectedDeliveryRequest) {
        // Test fails because reactive api returns an empty object instead of null
        // assertDeliveryRequestAllPropertiesEquals(expectedDeliveryRequest, getPersistedDeliveryRequest(expectedDeliveryRequest));
        assertDeliveryRequestUpdatableFieldsEquals(expectedDeliveryRequest, getPersistedDeliveryRequest(expectedDeliveryRequest));
    }

    protected void assertPersistedDeliveryRequestToMatchUpdatableProperties(DeliveryRequest expectedDeliveryRequest) {
        // Test fails because reactive api returns an empty object instead of null
        // assertDeliveryRequestAllUpdatablePropertiesEquals(expectedDeliveryRequest, getPersistedDeliveryRequest(expectedDeliveryRequest));
        assertDeliveryRequestUpdatableFieldsEquals(expectedDeliveryRequest, getPersistedDeliveryRequest(expectedDeliveryRequest));
    }
}
