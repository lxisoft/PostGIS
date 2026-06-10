package com.lxisoft.aps.web.rest;

import static com.lxisoft.aps.domain.DeliveryPartnerAsserts.*;
import static com.lxisoft.aps.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lxisoft.aps.IntegrationTest;
import com.lxisoft.aps.domain.DeliveryPartner;
import com.lxisoft.aps.domain.enumeration.PartnerStatus;
import com.lxisoft.aps.repository.DeliveryPartnerRepository;
import com.lxisoft.aps.repository.EntityManager;
import com.lxisoft.aps.service.dto.DeliveryPartnerDTO;
import com.lxisoft.aps.service.mapper.DeliveryPartnerMapper;
import java.time.Duration;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;

/**
 * Integration tests for the {@link DeliveryPartnerResource} REST controller.
 */
@IntegrationTest
@AutoConfigureWebTestClient(timeout = IntegrationTest.DEFAULT_ENTITY_TIMEOUT)
@WithMockUser
class DeliveryPartnerResourceIT {

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final PartnerStatus DEFAULT_STATUS = PartnerStatus.AVAILABLE;
    private static final PartnerStatus UPDATED_STATUS = PartnerStatus.BUSY;

    private static final String DEFAULT_LOCATION = "AAAAAAAAAA";
    private static final String UPDATED_LOCATION = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/delivery-partners";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private DeliveryPartnerRepository deliveryPartnerRepository;

    @Autowired
    private DeliveryPartnerMapper deliveryPartnerMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private WebTestClient webTestClient;

    private DeliveryPartner deliveryPartner;

    private DeliveryPartner insertedDeliveryPartner;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static DeliveryPartner createEntity() {
        return new DeliveryPartner().name(DEFAULT_NAME).status(DEFAULT_STATUS).location(DEFAULT_LOCATION);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static DeliveryPartner createUpdatedEntity() {
        return new DeliveryPartner().name(UPDATED_NAME).status(UPDATED_STATUS).location(UPDATED_LOCATION);
    }

    public static void deleteEntities(EntityManager em) {
        try {
            em.deleteAll(DeliveryPartner.class).block();
        } catch (Exception e) {
            // It can fail, if other entities are still referring this - it will be removed later.
        }
    }

    @BeforeEach
    public void initTest() {
        deliveryPartner = createEntity();
    }

    @AfterEach
    public void cleanup() {
        if (insertedDeliveryPartner != null) {
            deliveryPartnerRepository.delete(insertedDeliveryPartner).block();
            insertedDeliveryPartner = null;
        }
        deleteEntities(em);
    }

    @Test
    void createDeliveryPartner() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the DeliveryPartner
        DeliveryPartnerDTO deliveryPartnerDTO = deliveryPartnerMapper.toDto(deliveryPartner);
        var returnedDeliveryPartnerDTO = webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(deliveryPartnerDTO))
            .exchange()
            .expectStatus()
            .isCreated()
            .expectBody(DeliveryPartnerDTO.class)
            .returnResult()
            .getResponseBody();

        // Validate the DeliveryPartner in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedDeliveryPartner = deliveryPartnerMapper.toEntity(returnedDeliveryPartnerDTO);
        assertDeliveryPartnerUpdatableFieldsEquals(returnedDeliveryPartner, getPersistedDeliveryPartner(returnedDeliveryPartner));

        insertedDeliveryPartner = returnedDeliveryPartner;
    }

    @Test
    void createDeliveryPartnerWithExistingId() throws Exception {
        // Create the DeliveryPartner with an existing ID
        deliveryPartner.setId(1L);
        DeliveryPartnerDTO deliveryPartnerDTO = deliveryPartnerMapper.toDto(deliveryPartner);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(deliveryPartnerDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the DeliveryPartner in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    void checkNameIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        deliveryPartner.setName(null);

        // Create the DeliveryPartner, which fails.
        DeliveryPartnerDTO deliveryPartnerDTO = deliveryPartnerMapper.toDto(deliveryPartner);

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(deliveryPartnerDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void checkStatusIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        deliveryPartner.setStatus(null);

        // Create the DeliveryPartner, which fails.
        DeliveryPartnerDTO deliveryPartnerDTO = deliveryPartnerMapper.toDto(deliveryPartner);

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(deliveryPartnerDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void getAllDeliveryPartnersAsStream() {
        // Initialize the database
        deliveryPartnerRepository.save(deliveryPartner).block();

        List<DeliveryPartner> deliveryPartnerList = webTestClient
            .get()
            .uri(ENTITY_API_URL)
            .accept(MediaType.APPLICATION_NDJSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentTypeCompatibleWith(MediaType.APPLICATION_NDJSON)
            .returnResult(DeliveryPartnerDTO.class)
            .getResponseBody()
            .map(deliveryPartnerMapper::toEntity)
            .filter(deliveryPartner::equals)
            .collectList()
            .block(Duration.ofSeconds(5));

        assertThat(deliveryPartnerList).isNotNull();
        assertThat(deliveryPartnerList).hasSize(1);
        DeliveryPartner testDeliveryPartner = deliveryPartnerList.get(0);

        // Test fails because reactive api returns an empty object instead of null
        // assertDeliveryPartnerAllPropertiesEquals(deliveryPartner, testDeliveryPartner);
        assertDeliveryPartnerUpdatableFieldsEquals(deliveryPartner, testDeliveryPartner);
    }

    @Test
    void getAllDeliveryPartners() {
        // Initialize the database
        insertedDeliveryPartner = deliveryPartnerRepository.save(deliveryPartner).block();

        // Get all the deliveryPartnerList
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
            .value(hasItem(deliveryPartner.getId().intValue()))
            .jsonPath("$.[*].name")
            .value(hasItem(DEFAULT_NAME))
            .jsonPath("$.[*].status")
            .value(hasItem(DEFAULT_STATUS.toString()))
            .jsonPath("$.[*].location")
            .value(hasItem(DEFAULT_LOCATION));
    }

    @Test
    void getDeliveryPartner() {
        // Initialize the database
        insertedDeliveryPartner = deliveryPartnerRepository.save(deliveryPartner).block();

        // Get the deliveryPartner
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, deliveryPartner.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.id")
            .value(is(deliveryPartner.getId().intValue()))
            .jsonPath("$.name")
            .value(is(DEFAULT_NAME))
            .jsonPath("$.status")
            .value(is(DEFAULT_STATUS.toString()))
            .jsonPath("$.location")
            .value(is(DEFAULT_LOCATION));
    }

    @Test
    void getNonExistingDeliveryPartner() {
        // Get the deliveryPartner
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, Long.MAX_VALUE)
            .accept(MediaType.APPLICATION_PROBLEM_JSON)
            .exchange()
            .expectStatus()
            .isNotFound();
    }

    @Test
    void putExistingDeliveryPartner() throws Exception {
        // Initialize the database
        insertedDeliveryPartner = deliveryPartnerRepository.save(deliveryPartner).block();

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the deliveryPartner
        DeliveryPartner updatedDeliveryPartner = deliveryPartnerRepository.findById(deliveryPartner.getId()).block();
        updatedDeliveryPartner.name(UPDATED_NAME).status(UPDATED_STATUS).location(UPDATED_LOCATION);
        DeliveryPartnerDTO deliveryPartnerDTO = deliveryPartnerMapper.toDto(updatedDeliveryPartner);

        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, deliveryPartnerDTO.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(deliveryPartnerDTO))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the DeliveryPartner in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedDeliveryPartnerToMatchAllProperties(updatedDeliveryPartner);
    }

    @Test
    void putNonExistingDeliveryPartner() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        deliveryPartner.setId(longCount.incrementAndGet());

        // Create the DeliveryPartner
        DeliveryPartnerDTO deliveryPartnerDTO = deliveryPartnerMapper.toDto(deliveryPartner);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, deliveryPartnerDTO.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(deliveryPartnerDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the DeliveryPartner in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithIdMismatchDeliveryPartner() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        deliveryPartner.setId(longCount.incrementAndGet());

        // Create the DeliveryPartner
        DeliveryPartnerDTO deliveryPartnerDTO = deliveryPartnerMapper.toDto(deliveryPartner);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, longCount.incrementAndGet())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(deliveryPartnerDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the DeliveryPartner in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithMissingIdPathParamDeliveryPartner() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        deliveryPartner.setId(longCount.incrementAndGet());

        // Create the DeliveryPartner
        DeliveryPartnerDTO deliveryPartnerDTO = deliveryPartnerMapper.toDto(deliveryPartner);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(deliveryPartnerDTO))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the DeliveryPartner in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void partialUpdateDeliveryPartnerWithPatch() throws Exception {
        // Initialize the database
        insertedDeliveryPartner = deliveryPartnerRepository.save(deliveryPartner).block();

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the deliveryPartner using partial update
        DeliveryPartner partialUpdatedDeliveryPartner = new DeliveryPartner();
        partialUpdatedDeliveryPartner.setId(deliveryPartner.getId());

        partialUpdatedDeliveryPartner.name(UPDATED_NAME).status(UPDATED_STATUS).location(UPDATED_LOCATION);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedDeliveryPartner.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(partialUpdatedDeliveryPartner))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the DeliveryPartner in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertDeliveryPartnerUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedDeliveryPartner, deliveryPartner),
            getPersistedDeliveryPartner(deliveryPartner)
        );
    }

    @Test
    void fullUpdateDeliveryPartnerWithPatch() throws Exception {
        // Initialize the database
        insertedDeliveryPartner = deliveryPartnerRepository.save(deliveryPartner).block();

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the deliveryPartner using partial update
        DeliveryPartner partialUpdatedDeliveryPartner = new DeliveryPartner();
        partialUpdatedDeliveryPartner.setId(deliveryPartner.getId());

        partialUpdatedDeliveryPartner.name(UPDATED_NAME).status(UPDATED_STATUS).location(UPDATED_LOCATION);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedDeliveryPartner.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(partialUpdatedDeliveryPartner))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the DeliveryPartner in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertDeliveryPartnerUpdatableFieldsEquals(
            partialUpdatedDeliveryPartner,
            getPersistedDeliveryPartner(partialUpdatedDeliveryPartner)
        );
    }

    @Test
    void patchNonExistingDeliveryPartner() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        deliveryPartner.setId(longCount.incrementAndGet());

        // Create the DeliveryPartner
        DeliveryPartnerDTO deliveryPartnerDTO = deliveryPartnerMapper.toDto(deliveryPartner);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, deliveryPartnerDTO.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(deliveryPartnerDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the DeliveryPartner in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithIdMismatchDeliveryPartner() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        deliveryPartner.setId(longCount.incrementAndGet());

        // Create the DeliveryPartner
        DeliveryPartnerDTO deliveryPartnerDTO = deliveryPartnerMapper.toDto(deliveryPartner);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, longCount.incrementAndGet())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(deliveryPartnerDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the DeliveryPartner in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithMissingIdPathParamDeliveryPartner() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        deliveryPartner.setId(longCount.incrementAndGet());

        // Create the DeliveryPartner
        DeliveryPartnerDTO deliveryPartnerDTO = deliveryPartnerMapper.toDto(deliveryPartner);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(deliveryPartnerDTO))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the DeliveryPartner in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void deleteDeliveryPartner() {
        // Initialize the database
        insertedDeliveryPartner = deliveryPartnerRepository.save(deliveryPartner).block();

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the deliveryPartner
        webTestClient
            .delete()
            .uri(ENTITY_API_URL_ID, deliveryPartner.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNoContent();

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return deliveryPartnerRepository.count().block();
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

    protected DeliveryPartner getPersistedDeliveryPartner(DeliveryPartner deliveryPartner) {
        return deliveryPartnerRepository.findById(deliveryPartner.getId()).block();
    }

    protected void assertPersistedDeliveryPartnerToMatchAllProperties(DeliveryPartner expectedDeliveryPartner) {
        // Test fails because reactive api returns an empty object instead of null
        // assertDeliveryPartnerAllPropertiesEquals(expectedDeliveryPartner, getPersistedDeliveryPartner(expectedDeliveryPartner));
        assertDeliveryPartnerUpdatableFieldsEquals(expectedDeliveryPartner, getPersistedDeliveryPartner(expectedDeliveryPartner));
    }

    protected void assertPersistedDeliveryPartnerToMatchUpdatableProperties(DeliveryPartner expectedDeliveryPartner) {
        // Test fails because reactive api returns an empty object instead of null
        // assertDeliveryPartnerAllUpdatablePropertiesEquals(expectedDeliveryPartner, getPersistedDeliveryPartner(expectedDeliveryPartner));
        assertDeliveryPartnerUpdatableFieldsEquals(expectedDeliveryPartner, getPersistedDeliveryPartner(expectedDeliveryPartner));
    }
}
