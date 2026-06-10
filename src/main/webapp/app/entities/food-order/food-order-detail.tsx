import React, { useEffect } from 'react';
import { Link, useParams } from 'react-router-dom';
import { Button, Col, Row } from 'reactstrap';
import {} from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntity } from './food-order.reducer';

export const FoodOrderDetail = () => {
  const dispatch = useAppDispatch();

  const { id } = useParams<'id'>();

  useEffect(() => {
    dispatch(getEntity(id));
  }, []);

  const foodOrderEntity = useAppSelector(state => state.foodOrder.entity);
  return (
    <Row>
      <Col md="8">
        <h2 data-cy="foodOrderDetailsHeading">Food Order</h2>
        <dl className="jh-entity-details">
          <dt>
            <span id="id">ID</span>
          </dt>
          <dd>{foodOrderEntity.id}</dd>
          <dt>
            <span id="customerName">Customer Name</span>
          </dt>
          <dd>{foodOrderEntity.customerName}</dd>
          <dt>
            <span id="deliveryAddress">Delivery Address</span>
          </dt>
          <dd>{foodOrderEntity.deliveryAddress}</dd>
          <dt>
            <span id="deliveryLocation">Delivery Location</span>
          </dt>
          <dd>{foodOrderEntity.deliveryLocation}</dd>
          <dt>
            <span id="status">Status</span>
          </dt>
          <dd>{foodOrderEntity.status}</dd>
          <dt>Restaurant</dt>
          <dd>{foodOrderEntity.restaurant ? foodOrderEntity.restaurant.id : ''}</dd>
          <dt>Delivery Partner</dt>
          <dd>{foodOrderEntity.deliveryPartner ? foodOrderEntity.deliveryPartner.id : ''}</dd>
        </dl>
        <Button tag={Link} to="/food-order" replace color="info" data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" /> <span className="d-none d-md-inline">Back</span>
        </Button>
        &nbsp;
        <Button tag={Link} to={`/food-order/${foodOrderEntity.id}/edit`} replace color="primary">
          <FontAwesomeIcon icon="pencil-alt" /> <span className="d-none d-md-inline">Edit</span>
        </Button>
      </Col>
    </Row>
  );
};

export default FoodOrderDetail;
