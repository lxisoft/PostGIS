package com.lxisoft.aps.web.rest;

import static com.lxisoft.aps.domain.DeliveryZoneAsserts.*;
import static com.lxisoft.aps.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lxisoft.aps.IntegrationTest;
import com.lxisoft.aps.domain.DeliveryZone;
import com.lxisoft.aps.repository.DeliveryZoneRepository;
import com.lxisoft.aps.repository.EntityManager;
import com.lxisoft.aps.service.dto.DeliveryZoneDTO;
import com.lxisoft.aps.service.mapper.DeliveryZoneMapper;
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
 * Integration tests for the {@link DeliveryZoneResource} REST controller.
 */
@IntegrationTest
@AutoConfigureWebTestClient(timeout = IntegrationTest.DEFAULT_ENTITY_TIMEOUT)
@WithMockUser
class DeliveryZoneResourceIT {

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_DESCRIPTION = "AAAAAAAAAA";
    private static final String UPDATED_DESCRIPTION = "BBBBBBBBBB";

    private static final Boolean DEFAULT_ACTIVE = false;
    private static final Boolean UPDATED_ACTIVE = true;

    private static final String ENTITY_API_URL = "/api/delivery-zones";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private DeliveryZoneRepository deliveryZoneRepository;

    @Autowired
    private DeliveryZoneMapper deliveryZoneMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private WebTestClient webTestClient;

    private DeliveryZone deliveryZone;

    private DeliveryZone insertedDeliveryZone;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static DeliveryZone createEntity() {
        return new DeliveryZone().name(DEFAULT_NAME).description(DEFAULT_DESCRIPTION).active(DEFAULT_ACTIVE);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static DeliveryZone createUpdatedEntity() {
        return new DeliveryZone().name(UPDATED_NAME).description(UPDATED_DESCRIPTION).active(UPDATED_ACTIVE);
    }

    public static void deleteEntities(EntityManager em) {
        try {
            em.deleteAll(DeliveryZone.class).block();
        } catch (Exception e) {
            // It can fail, if other entities are still referring this - it will be removed later.
        }
    }

    @BeforeEach
    public void initTest() {
        deliveryZone = createEntity();
    }

    @AfterEach
    public void cleanup() {
        if (insertedDeliveryZone != null) {
            deliveryZoneRepository.delete(insertedDeliveryZone).block();
            insertedDeliveryZone = null;
        }
        deleteEntities(em);
    }

    @Test
    void createDeliveryZone() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the DeliveryZone
        DeliveryZoneDTO deliveryZoneDTO = deliveryZoneMapper.toDto(deliveryZone);
        var returnedDeliveryZoneDTO = webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(deliveryZoneDTO))
            .exchange()
            .expectStatus()
            .isCreated()
            .expectBody(DeliveryZoneDTO.class)
            .returnResult()
            .getResponseBody();

        // Validate the DeliveryZone in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedDeliveryZone = deliveryZoneMapper.toEntity(returnedDeliveryZoneDTO);
        assertDeliveryZoneUpdatableFieldsEquals(returnedDeliveryZone, getPersistedDeliveryZone(returnedDeliveryZone));

        insertedDeliveryZone = returnedDeliveryZone;
    }

    @Test
    void createDeliveryZoneWithExistingId() throws Exception {
        // Create the DeliveryZone with an existing ID
        deliveryZone.setId(1L);
        DeliveryZoneDTO deliveryZoneDTO = deliveryZoneMapper.toDto(deliveryZone);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(deliveryZoneDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the DeliveryZone in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    void checkNameIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        deliveryZone.setName(null);

        // Create the DeliveryZone, which fails.
        DeliveryZoneDTO deliveryZoneDTO = deliveryZoneMapper.toDto(deliveryZone);

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(deliveryZoneDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void checkActiveIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        deliveryZone.setActive(null);

        // Create the DeliveryZone, which fails.
        DeliveryZoneDTO deliveryZoneDTO = deliveryZoneMapper.toDto(deliveryZone);

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(deliveryZoneDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void getAllDeliveryZones() {
        // Initialize the database
        insertedDeliveryZone = deliveryZoneRepository.save(deliveryZone).block();

        // Get all the deliveryZoneList
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
            .value(hasItem(deliveryZone.getId().intValue()))
            .jsonPath("$.[*].name")
            .value(hasItem(DEFAULT_NAME))
            .jsonPath("$.[*].description")
            .value(hasItem(DEFAULT_DESCRIPTION))
            .jsonPath("$.[*].active")
            .value(hasItem(DEFAULT_ACTIVE));
    }

    @Test
    void getDeliveryZone() {
        // Initialize the database
        insertedDeliveryZone = deliveryZoneRepository.save(deliveryZone).block();

        // Get the deliveryZone
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, deliveryZone.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.id")
            .value(is(deliveryZone.getId().intValue()))
            .jsonPath("$.name")
            .value(is(DEFAULT_NAME))
            .jsonPath("$.description")
            .value(is(DEFAULT_DESCRIPTION))
            .jsonPath("$.active")
            .value(is(DEFAULT_ACTIVE));
    }

    @Test
    void getNonExistingDeliveryZone() {
        // Get the deliveryZone
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, Long.MAX_VALUE)
            .accept(MediaType.APPLICATION_PROBLEM_JSON)
            .exchange()
            .expectStatus()
            .isNotFound();
    }

    @Test
    void putExistingDeliveryZone() throws Exception {
        // Initialize the database
        insertedDeliveryZone = deliveryZoneRepository.save(deliveryZone).block();

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the deliveryZone
        DeliveryZone updatedDeliveryZone = deliveryZoneRepository.findById(deliveryZone.getId()).block();
        updatedDeliveryZone.name(UPDATED_NAME).description(UPDATED_DESCRIPTION).active(UPDATED_ACTIVE);
        DeliveryZoneDTO deliveryZoneDTO = deliveryZoneMapper.toDto(updatedDeliveryZone);

        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, deliveryZoneDTO.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(deliveryZoneDTO))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the DeliveryZone in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedDeliveryZoneToMatchAllProperties(updatedDeliveryZone);
    }

    @Test
    void putNonExistingDeliveryZone() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        deliveryZone.setId(longCount.incrementAndGet());

        // Create the DeliveryZone
        DeliveryZoneDTO deliveryZoneDTO = deliveryZoneMapper.toDto(deliveryZone);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, deliveryZoneDTO.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(deliveryZoneDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the DeliveryZone in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithIdMismatchDeliveryZone() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        deliveryZone.setId(longCount.incrementAndGet());

        // Create the DeliveryZone
        DeliveryZoneDTO deliveryZoneDTO = deliveryZoneMapper.toDto(deliveryZone);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, longCount.incrementAndGet())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(deliveryZoneDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the DeliveryZone in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithMissingIdPathParamDeliveryZone() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        deliveryZone.setId(longCount.incrementAndGet());

        // Create the DeliveryZone
        DeliveryZoneDTO deliveryZoneDTO = deliveryZoneMapper.toDto(deliveryZone);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(deliveryZoneDTO))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the DeliveryZone in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void partialUpdateDeliveryZoneWithPatch() throws Exception {
        // Initialize the database
        insertedDeliveryZone = deliveryZoneRepository.save(deliveryZone).block();

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the deliveryZone using partial update
        DeliveryZone partialUpdatedDeliveryZone = new DeliveryZone();
        partialUpdatedDeliveryZone.setId(deliveryZone.getId());

        partialUpdatedDeliveryZone.name(UPDATED_NAME).description(UPDATED_DESCRIPTION);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedDeliveryZone.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(partialUpdatedDeliveryZone))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the DeliveryZone in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertDeliveryZoneUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedDeliveryZone, deliveryZone),
            getPersistedDeliveryZone(deliveryZone)
        );
    }

    @Test
    void fullUpdateDeliveryZoneWithPatch() throws Exception {
        // Initialize the database
        insertedDeliveryZone = deliveryZoneRepository.save(deliveryZone).block();

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the deliveryZone using partial update
        DeliveryZone partialUpdatedDeliveryZone = new DeliveryZone();
        partialUpdatedDeliveryZone.setId(deliveryZone.getId());

        partialUpdatedDeliveryZone.name(UPDATED_NAME).description(UPDATED_DESCRIPTION).active(UPDATED_ACTIVE);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedDeliveryZone.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(partialUpdatedDeliveryZone))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the DeliveryZone in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertDeliveryZoneUpdatableFieldsEquals(partialUpdatedDeliveryZone, getPersistedDeliveryZone(partialUpdatedDeliveryZone));
    }

    @Test
    void patchNonExistingDeliveryZone() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        deliveryZone.setId(longCount.incrementAndGet());

        // Create the DeliveryZone
        DeliveryZoneDTO deliveryZoneDTO = deliveryZoneMapper.toDto(deliveryZone);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, deliveryZoneDTO.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(deliveryZoneDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the DeliveryZone in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithIdMismatchDeliveryZone() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        deliveryZone.setId(longCount.incrementAndGet());

        // Create the DeliveryZone
        DeliveryZoneDTO deliveryZoneDTO = deliveryZoneMapper.toDto(deliveryZone);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, longCount.incrementAndGet())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(deliveryZoneDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the DeliveryZone in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithMissingIdPathParamDeliveryZone() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        deliveryZone.setId(longCount.incrementAndGet());

        // Create the DeliveryZone
        DeliveryZoneDTO deliveryZoneDTO = deliveryZoneMapper.toDto(deliveryZone);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(deliveryZoneDTO))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the DeliveryZone in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void deleteDeliveryZone() {
        // Initialize the database
        insertedDeliveryZone = deliveryZoneRepository.save(deliveryZone).block();

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the deliveryZone
        webTestClient
            .delete()
            .uri(ENTITY_API_URL_ID, deliveryZone.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNoContent();

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return deliveryZoneRepository.count().block();
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

    protected DeliveryZone getPersistedDeliveryZone(DeliveryZone deliveryZone) {
        return deliveryZoneRepository.findById(deliveryZone.getId()).block();
    }

    protected void assertPersistedDeliveryZoneToMatchAllProperties(DeliveryZone expectedDeliveryZone) {
        // Test fails because reactive api returns an empty object instead of null
        // assertDeliveryZoneAllPropertiesEquals(expectedDeliveryZone, getPersistedDeliveryZone(expectedDeliveryZone));
        assertDeliveryZoneUpdatableFieldsEquals(expectedDeliveryZone, getPersistedDeliveryZone(expectedDeliveryZone));
    }

    protected void assertPersistedDeliveryZoneToMatchUpdatableProperties(DeliveryZone expectedDeliveryZone) {
        // Test fails because reactive api returns an empty object instead of null
        // assertDeliveryZoneAllUpdatablePropertiesEquals(expectedDeliveryZone, getPersistedDeliveryZone(expectedDeliveryZone));
        assertDeliveryZoneUpdatableFieldsEquals(expectedDeliveryZone, getPersistedDeliveryZone(expectedDeliveryZone));
    }
}
