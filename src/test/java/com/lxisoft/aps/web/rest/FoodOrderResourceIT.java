package com.lxisoft.aps.web.rest;

import static com.lxisoft.aps.domain.FoodOrderAsserts.*;
import static com.lxisoft.aps.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lxisoft.aps.IntegrationTest;
import com.lxisoft.aps.domain.FoodOrder;
import com.lxisoft.aps.domain.enumeration.OrderStatus;
import com.lxisoft.aps.repository.EntityManager;
import com.lxisoft.aps.repository.FoodOrderRepository;
import com.lxisoft.aps.service.dto.FoodOrderDTO;
import com.lxisoft.aps.service.mapper.FoodOrderMapper;
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
 * Integration tests for the {@link FoodOrderResource} REST controller.
 */
@IntegrationTest
@AutoConfigureWebTestClient(timeout = IntegrationTest.DEFAULT_ENTITY_TIMEOUT)
@WithMockUser
class FoodOrderResourceIT {

    private static final String DEFAULT_CUSTOMER_NAME = "AAAAAAAAAA";
    private static final String UPDATED_CUSTOMER_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_DELIVERY_ADDRESS = "AAAAAAAAAA";
    private static final String UPDATED_DELIVERY_ADDRESS = "BBBBBBBBBB";

    private static final String DEFAULT_DELIVERY_LOCATION = "AAAAAAAAAA";
    private static final String UPDATED_DELIVERY_LOCATION = "BBBBBBBBBB";

    private static final OrderStatus DEFAULT_STATUS = OrderStatus.PENDING;
    private static final OrderStatus UPDATED_STATUS = OrderStatus.ASSIGNED;

    private static final String ENTITY_API_URL = "/api/food-orders";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private FoodOrderRepository foodOrderRepository;

    @Autowired
    private FoodOrderMapper foodOrderMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private WebTestClient webTestClient;

    private FoodOrder foodOrder;

    private FoodOrder insertedFoodOrder;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static FoodOrder createEntity() {
        return new FoodOrder()
            .customerName(DEFAULT_CUSTOMER_NAME)
            .deliveryAddress(DEFAULT_DELIVERY_ADDRESS)
            .deliveryLocation(DEFAULT_DELIVERY_LOCATION)
            .status(DEFAULT_STATUS);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static FoodOrder createUpdatedEntity() {
        return new FoodOrder()
            .customerName(UPDATED_CUSTOMER_NAME)
            .deliveryAddress(UPDATED_DELIVERY_ADDRESS)
            .deliveryLocation(UPDATED_DELIVERY_LOCATION)
            .status(UPDATED_STATUS);
    }

    public static void deleteEntities(EntityManager em) {
        try {
            em.deleteAll(FoodOrder.class).block();
        } catch (Exception e) {
            // It can fail, if other entities are still referring this - it will be removed later.
        }
    }

    @BeforeEach
    public void initTest() {
        foodOrder = createEntity();
    }

    @AfterEach
    public void cleanup() {
        if (insertedFoodOrder != null) {
            foodOrderRepository.delete(insertedFoodOrder).block();
            insertedFoodOrder = null;
        }
        deleteEntities(em);
    }

    @Test
    void createFoodOrder() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the FoodOrder
        FoodOrderDTO foodOrderDTO = foodOrderMapper.toDto(foodOrder);
        var returnedFoodOrderDTO = webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(foodOrderDTO))
            .exchange()
            .expectStatus()
            .isCreated()
            .expectBody(FoodOrderDTO.class)
            .returnResult()
            .getResponseBody();

        // Validate the FoodOrder in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedFoodOrder = foodOrderMapper.toEntity(returnedFoodOrderDTO);
        assertFoodOrderUpdatableFieldsEquals(returnedFoodOrder, getPersistedFoodOrder(returnedFoodOrder));

        insertedFoodOrder = returnedFoodOrder;
    }

    @Test
    void createFoodOrderWithExistingId() throws Exception {
        // Create the FoodOrder with an existing ID
        foodOrder.setId(1L);
        FoodOrderDTO foodOrderDTO = foodOrderMapper.toDto(foodOrder);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(foodOrderDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the FoodOrder in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    void checkCustomerNameIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        foodOrder.setCustomerName(null);

        // Create the FoodOrder, which fails.
        FoodOrderDTO foodOrderDTO = foodOrderMapper.toDto(foodOrder);

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(foodOrderDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void checkDeliveryAddressIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        foodOrder.setDeliveryAddress(null);

        // Create the FoodOrder, which fails.
        FoodOrderDTO foodOrderDTO = foodOrderMapper.toDto(foodOrder);

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(foodOrderDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void checkStatusIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        foodOrder.setStatus(null);

        // Create the FoodOrder, which fails.
        FoodOrderDTO foodOrderDTO = foodOrderMapper.toDto(foodOrder);

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(foodOrderDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void getAllFoodOrders() {
        // Initialize the database
        insertedFoodOrder = foodOrderRepository.save(foodOrder).block();

        // Get all the foodOrderList
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
            .value(hasItem(foodOrder.getId().intValue()))
            .jsonPath("$.[*].customerName")
            .value(hasItem(DEFAULT_CUSTOMER_NAME))
            .jsonPath("$.[*].deliveryAddress")
            .value(hasItem(DEFAULT_DELIVERY_ADDRESS))
            .jsonPath("$.[*].deliveryLocation")
            .value(hasItem(DEFAULT_DELIVERY_LOCATION))
            .jsonPath("$.[*].status")
            .value(hasItem(DEFAULT_STATUS.toString()));
    }

    @Test
    void getFoodOrder() {
        // Initialize the database
        insertedFoodOrder = foodOrderRepository.save(foodOrder).block();

        // Get the foodOrder
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, foodOrder.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.id")
            .value(is(foodOrder.getId().intValue()))
            .jsonPath("$.customerName")
            .value(is(DEFAULT_CUSTOMER_NAME))
            .jsonPath("$.deliveryAddress")
            .value(is(DEFAULT_DELIVERY_ADDRESS))
            .jsonPath("$.deliveryLocation")
            .value(is(DEFAULT_DELIVERY_LOCATION))
            .jsonPath("$.status")
            .value(is(DEFAULT_STATUS.toString()));
    }

    @Test
    void getNonExistingFoodOrder() {
        // Get the foodOrder
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, Long.MAX_VALUE)
            .accept(MediaType.APPLICATION_PROBLEM_JSON)
            .exchange()
            .expectStatus()
            .isNotFound();
    }

    @Test
    void putExistingFoodOrder() throws Exception {
        // Initialize the database
        insertedFoodOrder = foodOrderRepository.save(foodOrder).block();

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the foodOrder
        FoodOrder updatedFoodOrder = foodOrderRepository.findById(foodOrder.getId()).block();
        updatedFoodOrder
            .customerName(UPDATED_CUSTOMER_NAME)
            .deliveryAddress(UPDATED_DELIVERY_ADDRESS)
            .deliveryLocation(UPDATED_DELIVERY_LOCATION)
            .status(UPDATED_STATUS);
        FoodOrderDTO foodOrderDTO = foodOrderMapper.toDto(updatedFoodOrder);

        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, foodOrderDTO.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(foodOrderDTO))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the FoodOrder in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedFoodOrderToMatchAllProperties(updatedFoodOrder);
    }

    @Test
    void putNonExistingFoodOrder() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        foodOrder.setId(longCount.incrementAndGet());

        // Create the FoodOrder
        FoodOrderDTO foodOrderDTO = foodOrderMapper.toDto(foodOrder);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, foodOrderDTO.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(foodOrderDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the FoodOrder in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithIdMismatchFoodOrder() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        foodOrder.setId(longCount.incrementAndGet());

        // Create the FoodOrder
        FoodOrderDTO foodOrderDTO = foodOrderMapper.toDto(foodOrder);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, longCount.incrementAndGet())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(foodOrderDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the FoodOrder in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithMissingIdPathParamFoodOrder() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        foodOrder.setId(longCount.incrementAndGet());

        // Create the FoodOrder
        FoodOrderDTO foodOrderDTO = foodOrderMapper.toDto(foodOrder);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(foodOrderDTO))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the FoodOrder in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void partialUpdateFoodOrderWithPatch() throws Exception {
        // Initialize the database
        insertedFoodOrder = foodOrderRepository.save(foodOrder).block();

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the foodOrder using partial update
        FoodOrder partialUpdatedFoodOrder = new FoodOrder();
        partialUpdatedFoodOrder.setId(foodOrder.getId());

        partialUpdatedFoodOrder.customerName(UPDATED_CUSTOMER_NAME);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedFoodOrder.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(partialUpdatedFoodOrder))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the FoodOrder in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertFoodOrderUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedFoodOrder, foodOrder),
            getPersistedFoodOrder(foodOrder)
        );
    }

    @Test
    void fullUpdateFoodOrderWithPatch() throws Exception {
        // Initialize the database
        insertedFoodOrder = foodOrderRepository.save(foodOrder).block();

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the foodOrder using partial update
        FoodOrder partialUpdatedFoodOrder = new FoodOrder();
        partialUpdatedFoodOrder.setId(foodOrder.getId());

        partialUpdatedFoodOrder
            .customerName(UPDATED_CUSTOMER_NAME)
            .deliveryAddress(UPDATED_DELIVERY_ADDRESS)
            .deliveryLocation(UPDATED_DELIVERY_LOCATION)
            .status(UPDATED_STATUS);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedFoodOrder.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(partialUpdatedFoodOrder))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the FoodOrder in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertFoodOrderUpdatableFieldsEquals(partialUpdatedFoodOrder, getPersistedFoodOrder(partialUpdatedFoodOrder));
    }

    @Test
    void patchNonExistingFoodOrder() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        foodOrder.setId(longCount.incrementAndGet());

        // Create the FoodOrder
        FoodOrderDTO foodOrderDTO = foodOrderMapper.toDto(foodOrder);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, foodOrderDTO.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(foodOrderDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the FoodOrder in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithIdMismatchFoodOrder() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        foodOrder.setId(longCount.incrementAndGet());

        // Create the FoodOrder
        FoodOrderDTO foodOrderDTO = foodOrderMapper.toDto(foodOrder);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, longCount.incrementAndGet())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(foodOrderDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the FoodOrder in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithMissingIdPathParamFoodOrder() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        foodOrder.setId(longCount.incrementAndGet());

        // Create the FoodOrder
        FoodOrderDTO foodOrderDTO = foodOrderMapper.toDto(foodOrder);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(foodOrderDTO))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the FoodOrder in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void deleteFoodOrder() {
        // Initialize the database
        insertedFoodOrder = foodOrderRepository.save(foodOrder).block();

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the foodOrder
        webTestClient
            .delete()
            .uri(ENTITY_API_URL_ID, foodOrder.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNoContent();

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return foodOrderRepository.count().block();
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

    protected FoodOrder getPersistedFoodOrder(FoodOrder foodOrder) {
        return foodOrderRepository.findById(foodOrder.getId()).block();
    }

    protected void assertPersistedFoodOrderToMatchAllProperties(FoodOrder expectedFoodOrder) {
        // Test fails because reactive api returns an empty object instead of null
        // assertFoodOrderAllPropertiesEquals(expectedFoodOrder, getPersistedFoodOrder(expectedFoodOrder));
        assertFoodOrderUpdatableFieldsEquals(expectedFoodOrder, getPersistedFoodOrder(expectedFoodOrder));
    }

    protected void assertPersistedFoodOrderToMatchUpdatableProperties(FoodOrder expectedFoodOrder) {
        // Test fails because reactive api returns an empty object instead of null
        // assertFoodOrderAllUpdatablePropertiesEquals(expectedFoodOrder, getPersistedFoodOrder(expectedFoodOrder));
        assertFoodOrderUpdatableFieldsEquals(expectedFoodOrder, getPersistedFoodOrder(expectedFoodOrder));
    }
}
