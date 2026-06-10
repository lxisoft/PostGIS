import React, { useEffect } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { Button, Col, Row } from 'reactstrap';
import { ValidatedField, ValidatedForm } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntities as getRestaurants } from 'app/entities/restaurant/restaurant.reducer';
import { getEntities as getDeliveryPartners } from 'app/entities/delivery-partner/delivery-partner.reducer';
import { OrderStatus } from 'app/shared/model/enumerations/order-status.model';
import { createEntity, getEntity, reset, updateEntity } from './food-order.reducer';

export const FoodOrderUpdate = () => {
  const dispatch = useAppDispatch();

  const navigate = useNavigate();

  const { id } = useParams<'id'>();
  const isNew = id === undefined;

  const restaurants = useAppSelector(state => state.restaurant.entities);
  const deliveryPartners = useAppSelector(state => state.deliveryPartner.entities);
  const foodOrderEntity = useAppSelector(state => state.foodOrder.entity);
  const loading = useAppSelector(state => state.foodOrder.loading);
  const updating = useAppSelector(state => state.foodOrder.updating);
  const updateSuccess = useAppSelector(state => state.foodOrder.updateSuccess);
  const orderStatusValues = Object.keys(OrderStatus);

  const handleClose = () => {
    navigate(`/food-order${location.search}`);
  };

  useEffect(() => {
    if (isNew) {
      dispatch(reset());
    } else {
      dispatch(getEntity(id));
    }

    dispatch(getRestaurants({}));
    dispatch(getDeliveryPartners({}));
  }, []);

  useEffect(() => {
    if (updateSuccess) {
      handleClose();
    }
  }, [updateSuccess]);

  const saveEntity = values => {
    if (values.id !== undefined && typeof values.id !== 'number') {
      values.id = Number(values.id);
    }

    const entity = {
      ...foodOrderEntity,
      ...values,
      restaurant: restaurants.find(it => it.id.toString() === values.restaurant?.toString()),
      deliveryPartner: deliveryPartners.find(it => it.id.toString() === values.deliveryPartner?.toString()),
    };

    if (isNew) {
      dispatch(createEntity(entity));
    } else {
      dispatch(updateEntity(entity));
    }
  };

  const defaultValues = () =>
    isNew
      ? {}
      : {
          status: 'PENDING',
          ...foodOrderEntity,
          restaurant: foodOrderEntity?.restaurant?.id,
          deliveryPartner: foodOrderEntity?.deliveryPartner?.id,
        };

  return (
    <div>
      <Row className="justify-content-center">
        <Col md="8">
          <h2 id="geoDeliveryApp.foodOrder.home.createOrEditLabel" data-cy="FoodOrderCreateUpdateHeading">
            Create or edit a Food Order
          </h2>
        </Col>
      </Row>
      <Row className="justify-content-center">
        <Col md="8">
          {loading ? (
            <p>Loading...</p>
          ) : (
            <ValidatedForm defaultValues={defaultValues()} onSubmit={saveEntity}>
              {!isNew ? <ValidatedField name="id" required readOnly id="food-order-id" label="ID" validate={{ required: true }} /> : null}
              <ValidatedField
                label="Customer Name"
                id="food-order-customerName"
                name="customerName"
                data-cy="customerName"
                type="text"
                validate={{
                  required: { value: true, message: 'This field is required.' },
                }}
              />
              <ValidatedField
                label="Delivery Address"
                id="food-order-deliveryAddress"
                name="deliveryAddress"
                data-cy="deliveryAddress"
                type="text"
                validate={{
                  required: { value: true, message: 'This field is required.' },
                }}
              />
              <ValidatedField
                label="Delivery Location"
                id="food-order-deliveryLocation"
                name="deliveryLocation"
                data-cy="deliveryLocation"
                type="text"
              />
              <ValidatedField label="Status" id="food-order-status" name="status" data-cy="status" type="select">
                {orderStatusValues.map(orderStatus => (
                  <option value={orderStatus} key={orderStatus}>
                    {orderStatus}
                  </option>
                ))}
              </ValidatedField>
              <ValidatedField id="food-order-restaurant" name="restaurant" data-cy="restaurant" label="Restaurant" type="select">
                <option value="" key="0" />
                {restaurants
                  ? restaurants.map(otherEntity => (
                      <option value={otherEntity.id} key={otherEntity.id}>
                        {otherEntity.id}
                      </option>
                    ))
                  : null}
              </ValidatedField>
              <ValidatedField
                id="food-order-deliveryPartner"
                name="deliveryPartner"
                data-cy="deliveryPartner"
                label="Delivery Partner"
                type="select"
              >
                <option value="" key="0" />
                {deliveryPartners
                  ? deliveryPartners.map(otherEntity => (
                      <option value={otherEntity.id} key={otherEntity.id}>
                        {otherEntity.id}
                      </option>
                    ))
                  : null}
              </ValidatedField>
              <Button tag={Link} id="cancel-save" data-cy="entityCreateCancelButton" to="/food-order" replace color="info">
                <FontAwesomeIcon icon="arrow-left" />
                &nbsp;
                <span className="d-none d-md-inline">Back</span>
              </Button>
              &nbsp;
              <Button color="primary" id="save-entity" data-cy="entityCreateSaveButton" type="submit" disabled={updating}>
                <FontAwesomeIcon icon="save" />
                &nbsp; Save
              </Button>
            </ValidatedForm>
          )}
        </Col>
      </Row>
    </div>
  );
};

export default FoodOrderUpdate;
